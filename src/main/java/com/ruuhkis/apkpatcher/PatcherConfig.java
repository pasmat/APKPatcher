package com.ruuhkis.apkpatcher;

import java.io.File;

/**
 * Created by PasiMatalamaki on 5.9.2016.
 */
public class PatcherConfig {

    public static final String PATCHES_DIR_NAME = "patches";
    public static final String BIN_DIR_NAME = "bin";
    public static final String KEYTOOL_EXE = "keytool.exe";
    public static final String JARSIGNER_EXE = "jarsigner.exe";
    public static final String JAVA_EXE = "java.exe";
    public static final String ZIPALIGN_EXE = "zipalign.exe";

    private File sdkDirectory;
    private File buildToolsDir;
    private File apkTool;
    private File javaDirectory;

    public PatcherConfig(File sdkDirectory, File buildToolsDir, File apkTool, File javaDirectory) {
        this.sdkDirectory = sdkDirectory;
        this.buildToolsDir = buildToolsDir;
        this.apkTool = apkTool;
        this.javaDirectory = javaDirectory;
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

}
