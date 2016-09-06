package com.ruuhkis.apkpatcher;

import java.io.File;

/**
 * Created by PasiMatalamaki on 5.9.2016.
 */
public class PatcherConfig {

    private File sdkDirectory;
    private File buildToolsDir;

    public PatcherConfig(File sdkDirectory, File buildToolsDir) {
        this.sdkDirectory = sdkDirectory;
        this.buildToolsDir = buildToolsDir;
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
}
