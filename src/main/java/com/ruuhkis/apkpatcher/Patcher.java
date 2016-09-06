package com.ruuhkis.apkpatcher;

import com.ruuhkis.apkpatcher.filequerier.APKFileQuorier;
import com.ruuhkis.apkpatcher.filequerier.BasicFileQuerier;
import com.ruuhkis.apkpatcher.filequerier.FileQuerier;
import com.ruuhkis.apkpatcher.filequerier.PatchesFileQuerier;

import javax.security.auth.login.Configuration;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by PasiMatalamaki on 5.9.2016.
 */
public class Patcher {

    public static final String ANDROID_HOME_KEY = "ANDROID_HOME";
    public static final String JAVA_HOME_KEY = "JAVA_HOME";
    public static final String APK_TOOL_KEY = "APK_TOOL";
    public static final String BUILD_TOOLS_DIR_NAME = "build-tools";
    public static final String PATCHES_DIR_NAME = "patches";
    public static final String BIN_DIR_NAME = "bin";
    public static final String KEYTOOL_EXE = "keytool.exe";
    public static final String JARSIGNER_EXE = "jarsigner.exe";
    public static final String PATCHER_CFG = "patcher.cfg";
    public static final String JAVA_EXE = "java.exe";
    public static final String TEMP_FOLDER_NAME = "temp";
    public static final String DECOMPILE_OUTPUT_DIR_NAME = "decompile_output";
    public static final String BUILD_OUTPUT_UNSIGNED_NAME = "build_output_us.apk";
    public static final String BUILD_OUTPUT_UNALIGNED_NAME = "build_output_ua.apk";
    public static final String BUILD_ALIGNED_OUTPUT_NAME = "build_output.apk";
    public static final String BUILD_RELEASE_OUTPUT_NAME = "build_output_release.apk";
    private static final String KEYSTORE_OUTPUT_NAME = "keystore";
    public static final int KEYSTORE_VALIDITY = 10000;
    public static final int KEYSTORE_SIZE = 2048;
    public static final String KEYSTORE_ALGORITHM = "RSA";
    private static final String KEYSTORE_ALIAS = "APK_PATCHER";
    private static final String KEYSTORE_PASS = "APK_PATCHER";
    public static final String ZIPALIGN_EXE = "zipalign.exe";
    private static final String KEYSTORE_DETAILS = "CN=APK Patcher, OU=APK Patcher, O=APK Patcher, L=APK Patcher, S=APK Patcher, C=FI";
    public static final String DETAILS_TXT = "details.txt";

    private final PatcherConfig patcherConfig;


    public Patcher(PatcherConfig patcherConfig) {
        this.patcherConfig = patcherConfig;
    }

    public static void main(String[] args) {
        new Patcher();

        File configurationFile = new File(PATCHER_CFG);

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

        Scanner scanner = new Scanner(System.in);

        File sdkDirectory = fetchPath(scanner, configuration, ANDROID_HOME_KEY);

        File apkTool = fetchPath(scanner, configuration, APK_TOOL_KEY);

        File javaDirectory = fetchPath(scanner, configuration, JAVA_HOME_KEY);


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


        File javaBinDirectory = new File(javaDirectory, BIN_DIR_NAME);

        File java = new File(javaBinDirectory, JAVA_EXE);

        File keyTool = new File(javaBinDirectory, KEYTOOL_EXE);

        File jarSigner = new File(javaBinDirectory, JARSIGNER_EXE);

        System.out.println("Choose your buildToolsDir");
        File buildToolsParentDir = new File(sdkDirectory, BUILD_TOOLS_DIR_NAME);

        File[] buildToolsDirs = buildToolsParentDir.listFiles();

        File buildToolsDir = null;

        if (buildToolsDirs != null && buildToolsDirs.length > 0) {
            if (buildToolsDirs.length > 1) {
                buildToolsDir = queryFile(scanner, buildToolsDirs, new BasicFileQuerier());
            } else {
                buildToolsDir = buildToolsDirs[0];
            }
        }

        if (buildToolsDir == null) {
            throw new RuntimeException("You have no build tools installed or your SDK path is wrong");
        }


        File zipAlign = new File(buildToolsDir, ZIPALIGN_EXE);

        PatcherConfig patcherConfig = new PatcherConfig(sdkDirectory, buildToolsDir);

        File workingDir = new File(".");

        System.out.println("Choose APK to be used");
        File selectedApk = queryFile(scanner, workingDir.listFiles(), new APKFileQuorier(patcherConfig));

        File patchesDir = new File(workingDir, PATCHES_DIR_NAME);

        PatchesFileQuerier patchesFileQuerier = new PatchesFileQuerier();

        while (true) {
            System.out.println("Toggle selected patches, or press enter to proceed patching..");

            File selectedPatch = queryFile(scanner, patchesDir.listFiles(), patchesFileQuerier);

            if (selectedPatch != null) {
                patchesFileQuerier.togglePatch(selectedPatch);
            } else {
                break;
            }
        }

        List<File> selectedPatches = patchesFileQuerier.getSelectedPatches();

        applyPatches();
    }

    public void applyPatches() {
        if (selectedPatches.size() > 0) {

            try {
                File tempDir = new File(workingDir, TEMP_FOLDER_NAME);

                if(!tempDir.exists()) {
                    tempDir.mkdirs();
                }

                File decompileOutput = new File(tempDir, DECOMPILE_OUTPUT_DIR_NAME);

                if(!decompileOutput.exists()) {
                    decompileOutput.mkdirs();
                }

                String decompileCommand = "\"" + java.getAbsolutePath() + "\"" + " -jar \"" + apkTool.getName() + "\" d \"" + selectedApk.getAbsolutePath() + "\" -o \"" + decompileOutput.getAbsolutePath() + "\" -f";

                executeCommand(decompileCommand);

                for (File patch : selectedPatches) {
                    System.out.println("Applying patch " + patch.getName() + "..");

                    copyStructureTo(patch, decompileOutput);
                }

                File buildOutputUnsigned = new File(tempDir, BUILD_OUTPUT_UNSIGNED_NAME);

                String compileCommand = "\"" + java.getAbsolutePath() + "\"" + " -jar \"" + apkTool.getName() + "\" b \"" + decompileOutput.getAbsolutePath() + "\" -o \"" + buildOutputUnsigned.getAbsolutePath() + "\" -f";

                executeCommand(compileCommand);

                File keystore = new File(tempDir, KEYSTORE_OUTPUT_NAME);

                if(!keystore.exists()) {
                    String generateKeyCommand = "\"" + keyTool.getAbsolutePath() + "\" -genkey -v -keystore \"" + keystore.getAbsolutePath() + "\" -alias \"" + KEYSTORE_ALIAS + "\" -keyalg " + KEYSTORE_ALGORITHM + " -keysize " + KEYSTORE_SIZE + " -validity " + KEYSTORE_VALIDITY + " -storepass \"" + KEYSTORE_PASS + "\" -keypass \"" + KEYSTORE_PASS + "\" -dname \"" + KEYSTORE_DETAILS + "\"";

                    executeCommand(generateKeyCommand);
                }

                File signedOutput = new File(tempDir, BUILD_OUTPUT_UNALIGNED_NAME);

                String jarSignCommand = "\"" + jarSigner.getAbsolutePath() + "\" -verbose -keystore \"" + keystore.getAbsolutePath() + "\" -signedjar \"" + signedOutput.getAbsolutePath() + "\" \"" + buildOutputUnsigned.getAbsolutePath() + "\" " + KEYSTORE_ALIAS + " -storepass \"" + KEYSTORE_PASS + "\"";

                executeCommand(jarSignCommand);

                File releaseOutput = new File(workingDir, BUILD_RELEASE_OUTPUT_NAME);

                String zipAlignCommand = "\"" +zipAlign.getAbsolutePath() + "\" -f 4 \"" + signedOutput.getAbsolutePath() + "\" \"" + releaseOutput.getAbsolutePath() + "\"";

                executeCommand(zipAlignCommand);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No patches selected.. Nothing to do..");
        }
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

    private static File fetchPath(Scanner scanner, Properties properties, String key) {
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
            return indexedFiles.get(Integer.parseInt(line));
        } else {
            return null;
        }
    }

}
