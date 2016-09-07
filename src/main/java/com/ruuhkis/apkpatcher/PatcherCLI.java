package com.ruuhkis.apkpatcher;

import com.ruuhkis.apkpatcher.filequerier.APKFileQuorier;
import com.ruuhkis.apkpatcher.filequerier.BasicFileQuerier;
import com.ruuhkis.apkpatcher.filequerier.FileQuerier;
import com.ruuhkis.apkpatcher.filequerier.PatchesFileQuerier;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by PasiMatalamaki on 5.9.2016.
 */
public class PatcherCLI {

    public static final String DETAILS_TXT = "details.txt";

    public static final String ANDROID_HOME_KEY = "ANDROID_HOME";
    public static final String JAVA_HOME_KEY = "JAVA_HOME";
    public static final String APK_TOOL_KEY = "APK_TOOL";
    public static final String BUILD_TOOLS_DIR_NAME = "build-tools";

    public static final String PATCHER_CFG = "patcher.cfg";

    public static final String TEMP_FOLDER_NAME = "temp";

    public static final String DECOMPILE_OUTPUT_DIR_NAME = "decompile_output";
    public static final String BUILD_OUTPUT_UNSIGNED_NAME = "build_output_us.apk";
    public static final String BUILD_OUTPUT_UNALIGNED_NAME = "build_output_ua.apk";
    public static final String BUILD_RELEASE_OUTPUT_NAME = "build_output_release.apk";

    private static final String KEYSTORE_OUTPUT_NAME = "keystore";
    public static final int KEYSTORE_VALIDITY = 10000;
    public static final int KEYSTORE_SIZE = 2048;
    public static final String KEYSTORE_ALGORITHM = "RSA";
    private static final String KEYSTORE_ALIAS = "APK_PATCHER";
    private static final String KEYSTORE_PASS = "APK_PATCHER";
    private static final String KEYSTORE_DETAILS = "CN=APK PatcherCLI, OU=APK PatcherCLI, O=APK PatcherCLI, L=APK PatcherCLI, S=APK PatcherCLI, C=FI";

    public static final String DECOMPILE_COMMAND = "\"%s\" -jar \"%s\" d \"%s\" -o \"%s\" -f";
    public static final String COMPILE_COMMAND = "\"%s\" -jar \"%s\" b \"%s\" -o \"%s\" -f";
    public static final String GENERATE_KEY = "\"%s\" -genkey -v -keystore \"%s\" -alias \"%s\" -keyalg %s -keysize %d -validity %d -storepass \"%s\" -keypass \"%s\" -dname \"%s\"";
    public static final String SIGN_JAR = "\"%s\" -verbose -keystore \"%s\" -signedjar \"%s\" \"%s\" %s -storepass \"%s\"";
    public static final String ZIP_ALIGN = "\"%s\" -f 4 \"%s\" \"%s\"";

    private PatcherConfig patcherConfig;

    public static void main(String[] args) {
        new PatcherCLI().run();

    }

    private void run() {
        Scanner scanner = new Scanner(System.in);

        PatcherConfig patcherConfig = queryConfig(scanner);

        File selectedApk = queryApk(scanner, patcherConfig, patcherConfig.getWorkingDir());

        List<File> patches = queryPatches(scanner, patcherConfig, patcherConfig.getPatchesDir());

        applyPatches(selectedApk, patches, patcherConfig);
    }

    private File queryApk(Scanner scanner, PatcherConfig patcherConfig, File directory) {
        System.out.println("Choose APK to be used");

        if(!hasApks(directory)) {
            System.out.println(directory.getAbsolutePath() + " doesn't contain any APK files, please choose another directory:");

            File selectedFile = new File(scanner.nextLine());

            if(selectedFile.exists() && selectedFile.getName().endsWith(".apk")){
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

        if(files != null) {

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

        saveConfiguration(configuration, getConfigurationFile());

        File buildToolsDir = fetchBuildToolsDir(scanner, sdkDirectory);

        if (buildToolsDir == null) {
            throw new RuntimeException("You have no build tools installed or your SDK path is wrong");
        }

        return new PatcherConfig(sdkDirectory, buildToolsDir, apkTool, javaDirectory);
    }

    private List<File> queryPatches(Scanner scanner, PatcherConfig patcherConfig, File directory) {
        if(!directory.exists() || !directory.isDirectory() || directory.listFiles().length <= 0) {
            System.out.println(directory.getAbsolutePath() + " isn't valid patches directory, please choose another directory that contains patches");

            File selectedDirectory = new File(scanner.nextLine());

            if(new File(selectedDirectory, DETAILS_TXT).exists()) {
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

                if(!decompileOutput.exists()) {
                    decompileOutput.mkdirs();
                }

                String decompileCommand = String.format(DECOMPILE_COMMAND, patcherConfig.getJava().getAbsolutePath(), patcherConfig.getApkTool().getAbsolutePath(), apkFile.getAbsolutePath(), decompileOutput.getAbsolutePath());

                executeCommand(decompileCommand);

                for (File patch : selectedPatches) {
                    System.out.println("Applying patch " + patch.getName() + "..");

                    copyStructureTo(patch, decompileOutput);
                }

                File buildOutputUnsigned = new File(tempDir, BUILD_OUTPUT_UNSIGNED_NAME);

                String compileCommand = String.format(COMPILE_COMMAND, patcherConfig.getJava().getAbsolutePath(), patcherConfig.getApkTool().getName(), decompileOutput.getAbsolutePath(), buildOutputUnsigned.getAbsolutePath());

                executeCommand(compileCommand);

                File keystore = new File(tempDir, KEYSTORE_OUTPUT_NAME);

                if(!keystore.exists()) {
                    String generateKeyCommand = String.format(GENERATE_KEY, patcherConfig.getKeyTool().getAbsolutePath(), keystore.getAbsolutePath(), KEYSTORE_ALIAS, KEYSTORE_ALGORITHM, KEYSTORE_SIZE, KEYSTORE_VALIDITY, KEYSTORE_PASS, KEYSTORE_PASS, KEYSTORE_DETAILS);

                    executeCommand(generateKeyCommand);
                }

                File signedOutput = new File(tempDir, BUILD_OUTPUT_UNALIGNED_NAME);

                String jarSignCommand = String.format(SIGN_JAR, patcherConfig.getJarSigner().getAbsolutePath(), keystore.getAbsolutePath(), signedOutput.getAbsolutePath(), buildOutputUnsigned.getAbsolutePath(), KEYSTORE_ALIAS, KEYSTORE_PASS);

                executeCommand(jarSignCommand);

                File releaseOutput = new File(patcherConfig.getWorkingDir(), BUILD_RELEASE_OUTPUT_NAME);

                String zipAlignCommand = String.format(ZIP_ALIGN, patcherConfig.getZipAlign().getAbsolutePath(), signedOutput.getAbsolutePath(), releaseOutput.getAbsolutePath());

                executeCommand(zipAlignCommand);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No patches selected.. Nothing to do..");
        }
    }

    private File ensureTempDirExists(PatcherConfig patcherConfig) {
        File tempDir = new File(patcherConfig.getWorkingDir(), TEMP_FOLDER_NAME);

        if(!tempDir.exists()) {
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

            if(files != null) {
                for (File subFile : files) {
                    copyStructureTo(srcRootFolder, targetFolder, subFile);
                }
            }
        } else if(!srcFile.getName().equals(DETAILS_TXT)) {
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

    private static void executeCommand(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);

        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line = null;

        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }

        br.close();

        process.destroy();
    }

    private static File getConfiguredPathOrInput(Scanner scanner, Properties properties, String key) {
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

            String readedLine = scanner.nextLine();

            if (new File(readedLine).exists()) {
                path = readedLine;
            }
        }

        File file = null;

        if (path != null) {
            file = new File(path);

            properties.setProperty(key, path);
        }

        return file;
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

            if(index >= 0 && index < indexedFiles.size()) {
                return indexedFiles.get(index);
            } else {
                return queryFile(scanner, files, fileQuerier);
            }
        } else {
            return null;
        }
    }

}
