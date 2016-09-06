package com.ruuhkis.apkpatcher.filequerier;

import java.io.File;

/**
 * Created by PasiMatalamaki on 5.9.2016.
 */
public class BasicFileQuerier implements FileQuerier {
    public boolean queryFile(int index, File file) {
        System.out.println(index + ": " + file.getName());
        return true;
    }
}
