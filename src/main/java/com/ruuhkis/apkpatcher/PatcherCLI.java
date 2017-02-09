package com.ruuhkis.apkpatcher;

import com.ruuhkis.apkpatcher.filequerier.APKFileQuorier;
import com.ruuhkis.apkpatcher.filequerier.BasicFileQuerier;
import com.ruuhkis.apkpatcher.filequerier.FileQuerier;
import com.ruuhkis.apkpatcher.filequerier.PatchesFileQuerier;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by PasiMatalamaki on 5.9.2016.
 */
public class PatcherCLI {

    public static final String DETAILS_TXT = "details.txt";

    public static final String ANDROID_HOME_KEY = "ANDROID_HOME";
    public static final String JAVA_HOME_KEY = "JAVA_HOME";
    public static final String APK_TOOL_KEY = "APK_TOOL";
    public static final String KEYSTORE_KEY = "KEYSTORE";
    public static final String KEYSTORE_PASS_KEY = "KEYSTORE_PASS";
    public static final String KEYSTORE_ALIAS_KEY = "KEYSTORE_ALIAS";
    public static final String KEYSTORE_KEY_PASS_KEY = "KEYSTORE_KEY_PASS";
    public static final String BUILD_TOOLS_DIR_NAME = "build-tools";

    public static final String PATCHER_CFG = "patcher.cfg";

    public static final String TEMP_FOLDER_NAME = "temp";

    public static final String DECOMPILE_OUTPUT_DIR_NAME = "decompile_output";
    public static final String BUILD_OUTPUT_UNSIGNED_NAME = "build_output_us.apk";
    public static final String BUILD_OUTPUT_UNALIGNED_NAME = "build_output_ua.apk";

    private static final String KEYSTORE_OUTPUT_NAME = "keystore";
    private static final String KEYSTORE_ALIAS = "APK_PATCHER";
    private static final String KEYSTORE_PASS = "APK_PATCHER";
    private static final String KEYSTORE_KEY_PASS = "APK_PATCHER";

    public static final int KEYSTORE_VALIDITY = 10000;
    public static final int KEYSTORE_SIZE = 2048;
    public static final String KEYSTORE_ALGORITHM = "RSA";
    private static final String KEYSTORE_DETAILS = "CN=APK PatcherCLI, OU=APK PatcherCLI, O=APK PatcherCLI, L=APK PatcherCLI, S=APK PatcherCLI, C=FI";

    private static Pattern installFailurePattern = Pattern.compile("Failure \\[([A-Z_]*)\\]");
    private static Pattern packagePattern = Pattern.compile("package: name='([\\S.]*)' versionCode='([\\S.]*)' versionName='([\\S.]*)' platformBuildVersionName='([\\S.]*)'");


    private PatcherConfig patcherConfig;

    private static class InstallCommunicationHandler implements CommunicationHandler {

        private boolean success;
        private String failureMessage;

        @Override
        public void onInput(String input, OutputStream outputStream) {
            Matcher installFailureMatcher = installFailurePattern.matcher(input);

            if(installFailureMatcher.find()) {
                failureMessage = installFailureMatcher.group(1);
            }

            if(input.contains("Success")) {
                success = true;
            }

            System.out.println(input);
        }

        @Override
        public void onError(String error, OutputStream outputStream) {
            System.err.println(error);
        }

        public String getFailureMessage() {
            return failureMessage;
        }

        public void setFailureMessage(String failureMessage) {
            this.failureMessage = failureMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }

    private static class DumpBadgingCommunicationHandler implements CommunicationHandler {

        private String packageName;

        @Override
        public void onInput(String input, OutputStream outputStream) {
            Matcher packageMatcher = packagePattern.matcher(input);

            if(packageMatcher.find()) {
                packageName = packageMatcher.group(1);
            }

            System.out.println(input);
        }

        @Override
        public void onError(String error, OutputStream outputStream) {

            System.err.println(error);
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }
    }

    public static void main(String[] args) {

        new PatcherCLI().run();


    }

    private static void install(Scanner scanner, PatcherConfig patcherConfig, File apk) {
        try {
            DumpBadgingCommunicationHandler dumpBadingCommunicationHandler = new DumpBadgingCommunicationHandler();

            executeCommand(dumpBadingCommunicationHandler, patcherConfig.getAaptTool().getAbsolutePath(), "dump", "badging", apk.getAbsolutePath());

            String packageName = dumpBadingCommunicationHandler.getPackageName();

            InstallCommunicationHandler installCommunicationHandler = new InstallCommunicationHandler();

            executeCommand(installCommunicationHandler, patcherConfig.getAdbTool().getAbsolutePath(), "install", "-r", apk.getAbsolutePath());

            if(!installCommunicationHandler.isSuccess()) {
                System.out.println("Install failed with error " + installCommunicationHandler.getFailureMessage() + ". Would you like to uninstall the package and try again? [Y/N]");

                if(scanner.nextLine().equalsIgnoreCase("y")) {
                    executeCommand(patcherConfig.getAdbTool().getAbsolutePath(), "shell", "pm", "uninstall", packageName);
                    install(scanner, patcherConfig, apk);
                }
            } else {
                System.out.println("Install succesful! Would you like to start the application? [Y/N]");

                if(scanner.nextLine().equalsIgnoreCase("y")) {
                    executeCommand(patcherConfig.getAdbTool().getAbsolutePath(), "shell", "monkey", "-p", packageName, "-c", "android.intent.category.LAUNCHER", "1");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void run() {
        Scanner scanner = new Scanner(System.in);

        PatcherConfig patcherConfig = queryConfig(scanner);

        File selectedApk = queryApk(scanner, patcherConfig, patcherConfig.getWorkingDir());

        List<File> patches = queryPatches(scanner, patcherConfig, patcherConfig.getPatchesDir());

        applyPatches(selectedApk, patches, patcherConfig);

        install(scanner, patcherConfig, patcherConfig.getReleasetOuput());
    }

    private File queryApk(Scanner scanner, PatcherConfig patcherConfig, File directory) {
        System.out.println("Choose APK to be used");

        if (!hasApks(directory)) {
            System.out.println(directory.getAbsolutePath() + " doesn't contain any APK files, please choose another directory:");

            File selectedFile = new File(scanner.nextLine());

            if (selectedFile.exists() && selectedFile.getName().endsWith(".apk")) {
                return selectedFile;
            }

            return queryApk(scanner, patcherConfig, selectedFile);
        } else {

            File[] files = directory.listFiles();

            return queryFile(scanner, files, new APKFileQuorier(patcherConfig));
        }
    }

    private boolean hasApks(File directory) {
        File[] files = directory.listFiles();

        if (files != null) {

            for (File file : files) {
                if (file.getName().endsWith(".apk")) {
                    return true;
                }
            }
        }
        return false;
    }

    private PatcherConfig queryConfig(Scanner scanner) {
        Properties configuration = loadConfiguration(getConfigurationFile());

        File sdkDirectory = getConfiguredPathOrInput(scanner, configuration, ANDROID_HOME_KEY);

        File apkTool = getConfiguredPathOrInput(scanner, configuration, APK_TOOL_KEY);

        File javaDirectory = getConfiguredPathOrInput(scanner, configuration, JAVA_HOME_KEY);

        File keystore = getConfiguredPathOrInput(scanner, configuration, KEYSTORE_KEY, new File(KEYSTORE_OUTPUT_NAME));

        String keystoreAlias = getConfiguredKeyOrInput(scanner, configuration, KEYSTORE_ALIAS_KEY, KEYSTORE_ALIAS);

        String keystorePass = getConfiguredKeyOrInput(scanner, configuration, KEYSTORE_PASS_KEY, KEYSTORE_PASS);

        String keystoreKeyPass = getConfiguredKeyOrInput(scanner, configuration, KEYSTORE_KEY_PASS_KEY, KEYSTORE_KEY_PASS);

        saveConfiguration(configuration, getConfigurationFile());

        File buildToolsDir = fetchBuildToolsDir(scanner, sdkDirectory);

        if (buildToolsDir == null) {
            throw new RuntimeException("You have no build tools installed or your SDK path is wrong");
        }

        return new PatcherConfig(sdkDirectory, buildToolsDir, apkTool, javaDirectory, keystore, keystorePass, keystoreAlias, keystoreKeyPass);
    }

    private List<File> queryPatches(Scanner scanner, PatcherConfig patcherConfig, File directory) {
        if (!directory.exists() || !directory.isDirectory() || directory.listFiles().length <= 0) {
            System.out.println(directory.getAbsolutePath() + " isn't valid patches directory, please choose another directory that contains patches");

            File selectedDirectory = new File(scanner.nextLine());

            if (new File(selectedDirectory, DETAILS_TXT).exists()) {
                ArrayList<File> files = new ArrayList<File>();

                files.add(selectedDirectory);

                return files;
            } else {
                return queryPatches(scanner, patcherConfig, selectedDirectory);
            }
        }

        PatchesFileQuerier patchesFileQuerier = new PatchesFileQuerier();

        while (true) {
            System.out.println("Toggle selected patches, or press enter to proceed patching..");

            File selectedPatch = queryFile(scanner, patcherConfig.getPatchesDir().listFiles(), patchesFileQuerier);

            if (selectedPatch != null) {
                patchesFileQuerier.togglePatch(selectedPatch);
            } else {
                break;
            }
        }

        return patchesFileQuerier.getSelectedPatches();
    }

    private File getConfigurationFile() {
        return new File(PATCHER_CFG);
    }

    private Properties loadConfiguration(File configurationFile) {
        Properties configuration = new Properties();


        if (configurationFile.exists()) {
            System.out.println("Reading config from " + PATCHER_CFG + "..");

            FileInputStream is = null;
            try {
                is = new FileInputStream(configurationFile);

                configuration.load(is);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return configuration;
    }

    private void saveConfiguration(Properties configuration, File configurationFile) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(configurationFile);

            configuration.store(os, "");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File fetchBuildToolsDir(Scanner scanner, File sdkDirectory) {
        File buildToolsParentDir = new File(sdkDirectory, BUILD_TOOLS_DIR_NAME);

        File[] buildToolsDirs = buildToolsParentDir.listFiles();

        File buildToolsDir = null;

        if (buildToolsDirs != null && buildToolsDirs.length > 0) {
            if (buildToolsDirs.length > 1) {
                System.out.println("Choose your buildToolsDir");
                buildToolsDir = queryFile(scanner, buildToolsDirs, new BasicFileQuerier());
            } else {
                buildToolsDir = buildToolsDirs[0];
            }
        }

        return buildToolsDir;
    }

    public void applyPatches(File apkFile, List<File> selectedPatches, PatcherConfig patcherConfig) {
        if (selectedPatches.size() > 0) {

            try {
                File tempDir = ensureTempDirExists(patcherConfig);

                File decompileOutput = new File(tempDir, DECOMPILE_OUTPUT_DIR_NAME);

                if (!decompileOutput.exists()) {
                    decompileOutput.mkdirs();
                }


                executeCommand(patcherConfig.getJava().getAbsolutePath(), "-jar", patcherConfig.getApkTool().getAbsolutePath(), "d", apkFile.getAbsolutePath(), "-o", decompileOutput.getAbsolutePath(), "-f");

                for (File patch : selectedPatches) {
                    System.out.println("Applying patch " + patch.getName() + "..");

                    copyStructureTo(patch, decompileOutput);
                }

                File buildOutputUnsigned = new File(tempDir, BUILD_OUTPUT_UNSIGNED_NAME);

                executeCommand(patcherConfig.getJava().getAbsolutePath(), "-jar", patcherConfig.getApkTool().getName(), "b", decompileOutput.getAbsolutePath(), "-o", buildOutputUnsigned.getAbsolutePath(), "-f");

                File keystore = patcherConfig.getKeystore();

                if (!keystore.exists()) {

                    executeCommand(patcherConfig.getKeyTool().getAbsolutePath(), "-genkey", "-v", "-keystore", keystore.getAbsolutePath(), "-alias", patcherConfig.getKeystoreAlias(), "-keyalg", KEYSTORE_ALGORITHM, "-keysize", Integer.toString(KEYSTORE_SIZE), "-validity", Integer.toString(KEYSTORE_VALIDITY), "-keypass", patcherConfig.getKeystoreKeyPass(), "-storepass", patcherConfig.getKeystorePass(), "-dname", KEYSTORE_DETAILS);
                }

                File signedOutput = new File(tempDir, BUILD_OUTPUT_UNALIGNED_NAME);


                executeCommand(patcherConfig.getJarSigner().getAbsolutePath(), "-verbose", "-keystore", keystore.getAbsolutePath(), "-signedjar", signedOutput.getAbsolutePath(), buildOutputUnsigned.getAbsolutePath(), patcherConfig.getKeystoreAlias(), "-storepass", patcherConfig.getKeystorePass());

                File releaseOutput = patcherConfig.getReleasetOuput();

                executeCommand(patcherConfig.getZipAlign().getAbsolutePath(), "-f", "4", signedOutput.getAbsolutePath(), releaseOutput.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No patches selected.. Nothing to do..");
        }
    }

    private File ensureTempDirExists(PatcherConfig patcherConfig) {
        File tempDir = new File(patcherConfig.getWorkingDir(), TEMP_FOLDER_NAME);

        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        return tempDir;
    }

    private static void copyStructureTo(File srcRootFolder, File targetFolder) throws IOException {
        copyStructureTo(srcRootFolder, targetFolder, srcRootFolder);
    }

    private static void copyStructureTo(File srcRootFolder, File targetFolder, File srcFile) throws IOException {

        String relativePath = srcRootFolder.toURI().relativize(srcFile.toURI()).getPath();

        System.out.println(relativePath);

        File targetFile = new File(targetFolder, relativePath);

        if (srcFile.isDirectory()) {
            if (!targetFile.exists() && !targetFile.mkdirs()) {
                throw new IOException("Unable to make directory at " + targetFile.getAbsolutePath());
            }

            File[] files = srcFile.listFiles();

            if (files != null) {
                for (File subFile : files) {
                    copyStructureTo(srcRootFolder, targetFolder, subFile);
                }
            }
        } else if (!srcFile.getName().equals(DETAILS_TXT)) {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile));

            byte[] buffer = new byte[1024];

            int read;

            while ((read = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }

            bis.close();
            bos.close();
        }
    }


    private static int executeCommand(String... command) throws IOException, InterruptedException {
        System.out.println(Arrays.toString(command));

        return executeCommand(new CommunicationHandler() {
            @Override
            public void onInput(String input, OutputStream outputStream) {
                System.out.println(input);
            }

            @Override
            public void onError(String error, OutputStream outputStream) {
                System.err.println(error);
            }
        }, command);
    }

    private static int executeCommand(CommunicationHandler communicationHandler, String... command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder()
                .command(command)
                .redirectErrorStream(true)
                .start();

        InputStream inputStream = process.getInputStream();
        InputStream errorStream = process.getErrorStream();
        OutputStream outputStream = process.getOutputStream();

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        boolean wasAlive = process.isAlive();

        while(wasAlive) {

            String inputString = readAll(inputStream);

            if(inputString.length() > 0) {
                communicationHandler.onInput(inputString, outputStream);
            }

            String errorString = readAll(errorStream);

            if(errorString.length() > 0) {
                communicationHandler.onError(errorString, outputStream);
            }

            //might seem stupid? but imagine scenario where the process has been shutdown, but theres still input to be read.
            wasAlive = process.isAlive();
        }

        executor.shutdownNow();

        return process.exitValue();


    }

    private static String readAll(final InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        while((len = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }

        return new String(baos.toByteArray());
    }


    private static File getConfiguredPathOrInput(Scanner scanner, Properties properties, String key) {
        return getConfiguredPathOrInput(scanner, properties, key, null);
    }

    private static File getConfiguredPathOrInput(Scanner scanner, Properties properties, String key, File defaultValue) {
        String path = null;

        if (properties.getProperty(key) != null && new File(properties.getProperty(key)).exists()) {
            path = properties.getProperty(key);
        }

        if (path == null && System.getenv(key) != null && new File(System.getenv(key)).exists()) {
            path = System.getenv(key);
        }

        if (path == null && System.getProperty(key) != null && new File(System.getProperty(key)).exists()) {
            path = System.getProperty(key);
        }

        if (path == null) {
            System.out.println(key + " environment variable not set!");
            System.out.println("Please set environment variable path, input it manually or exit.");
            if(defaultValue != null) {
                System.out.println("Default(" + defaultValue.getAbsolutePath() + ")");
            }

            String readedLine = scanner.nextLine();


            if (new File(readedLine).exists()) {
                path = readedLine;
            }
        }

        File file = null;

        if (path != null && path.length() > 0) {
            file = new File(path);
        } else {
            file = defaultValue;
        }

        if(file != null) {
            properties.setProperty(key, file.getAbsolutePath());
        }

        return file;
    }

    private static String getConfiguredKeyOrInput(Scanner scanner, Properties properties, String key, String defaultValue) {
        String value = null;

        if (properties.getProperty(key) != null) {
            value = properties.getProperty(key);
        }

        if (value == null && System.getenv(key) != null) {
            value = System.getenv(key);
        }

        if (value == null && System.getProperty(key) != null) {
            value = System.getProperty(key);
        }

        if (value == null) {
            System.out.println(key + " environment variable not set!");
            System.out.println("Please set environment variable path, input it manually or exit.");
            System.out.println("default(" + defaultValue + ")");

            value = scanner.nextLine();

            if(value.length() == 0) {
                value = defaultValue;
            }
        }

        if (value != null) {
            properties.setProperty(key, value);
        }

        return value;
    }

    private static File queryFile(Scanner scanner, File[] files, FileQuerier fileQuerier) {
        List<File> indexedFiles = new ArrayList<File>();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            if (fileQuerier.queryFile(indexedFiles.size(), file)) {
                indexedFiles.add(file);
            }
        }

        String line = scanner.nextLine();
        if (line.length() > 0) {
            int index = Integer.parseInt(line);

            if (index >= 0 && index < indexedFiles.size()) {
                return indexedFiles.get(index);
            } else {
                return queryFile(scanner, files, fileQuerier);
            }
        } else {
            return null;
        }
    }

}
