package com.ruuhkis.apkpatcher;

import java.io.File;

/**
 * Created by PasiMatalamaki on 5.9.2016.
 */
public class PatcherConfig {

    public static final String PATCHES_DIR_NAME = "patches";
    public static final String BIN_DIR_NAME = "bin";
    public static final String KEYTOOL_EXE = "keytool";
    public static final String JARSIGNER_EXE = "jarsigner";
    public static final String JAVA_EXE = "java";
    public static final String ZIPALIGN_EXE = "zipalign";
    public static final String AAPT_EXE = "aapt";
    private static final String ADB_EXE = "adb";
    private static final String PLATFORM_TOOLS = "platform-tools";
    public static final String BUILD_RELEASE_OUTPUT_NAME = "build_output_release.apk";

    private File sdkDirectory;
    private File buildToolsDir;
    private File apkTool;
    private File javaDirectory;
    private File keystore;

    private String keystorePass;
    private String keystoreAlias;
    private String keystoreKeyPass;

    public PatcherConfig(File sdkDirectory, File buildToolsDir, File apkTool, File javaDirectory, File keystore, String keystorePass, String keystoreAlias, String keystoreKeyPass) {
        this.sdkDirectory = sdkDirectory;
        this.buildToolsDir = buildToolsDir;
        this.apkTool = apkTool;
        this.javaDirectory = javaDirectory;
        this.keystore = keystore;
        this.keystorePass = keystorePass;
        this.keystoreAlias = keystoreAlias;
        this.keystoreKeyPass = keystoreKeyPass;

    }

    public File getSdkDirectory() {
        return sdkDirectory;
    }

    public void setSdkDirectory(File sdkDirectory) {
        this.sdkDirectory = sdkDirectory;
    }

    public File getBuildToolsDir() {
        return buildToolsDir;
    }

    public void setBuildToolsDir(File buildToolsDir) {
        this.buildToolsDir = buildToolsDir;
    }

    public File getApkTool() {
        return apkTool;
    }

    public void setApkTool(File apkTool) {
        this.apkTool = apkTool;
    }

    public File getJavaDirectory() {
        return javaDirectory;
    }

    public void setJavaDirectory(File javaDirectory) {
        this.javaDirectory = javaDirectory;
    }

    public File getJavaBinDirectory() {
        return new File(javaDirectory, BIN_DIR_NAME);
    }

    public File getJava() {
        return new File(getJavaBinDirectory(), JAVA_EXE);
    }

    public File getKeyTool() {
        return new File(getJavaBinDirectory(), KEYTOOL_EXE);
    }

    public File getJarSigner() {
        return new File(getJavaBinDirectory(), JARSIGNER_EXE);
    }

    public File getZipAlign() {
        return new File(buildToolsDir, ZIPALIGN_EXE);
    }

    public File getWorkingDir() {
        return new File(".");
    }

    public File getPatchesDir() {
        return new File(PATCHES_DIR_NAME);
    }

    public File getKeystore() {
        return keystore;
    }

    public void setKeystore(File keystore) {
        this.keystore = keystore;
    }

    public String getKeystoreAlias() {
        return keystoreAlias;
    }

    public void setKeystoreAlias(String keystoreAlias) {
        this.keystoreAlias = keystoreAlias;
    }

    public String getKeystorePass() {
        return keystorePass;
    }

    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }

    public String getKeystoreKeyPass() {
        return keystoreKeyPass;
    }

    public File getAaptTool() {
        return new File(buildToolsDir, AAPT_EXE);
    }

    public File getAdbTool() {
        return new File(getPlatformToolsDir(), ADB_EXE);
    }

    private File getPlatformToolsDir() {
        return new File(sdkDirectory, PLATFORM_TOOLS);
    }

    public File getReleasetOuput() {
        return new File(getWorkingDir(), BUILD_RELEASE_OUTPUT_NAME);

    }
}
