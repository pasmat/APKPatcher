package com.ruuhkis.apkpatcher.filequerier;

import com.ruuhkis.apkpatcher.PatcherConfig;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by PasiMatalamaki on 5.9.2016.
 */
public class APKFileQuorier implements FileQuerier {

    public static final String APK_FILE_SUFFIX = ".apk";

    private PatcherConfig patcherConfig;

    public APKFileQuorier(PatcherConfig patcherConfig) {
        this.patcherConfig = patcherConfig;
    }

    public boolean acceptFile(File file) {
        return file.getName().endsWith(APK_FILE_SUFFIX);
    }

    public boolean queryFile(int index, File file) {
        if (file.getName().toLowerCase().endsWith(APK_FILE_SUFFIX)) {
            File aapt = new File(patcherConfig.getBuildToolsDir(), "aapt");

            try {
                Process aaptProcess = Runtime.getRuntime().exec(aapt.getAbsolutePath() + " d badging " + file.getName());

                Scanner aaptScanner = new Scanner(aaptProcess.getInputStream());

                String line;

                while (aaptScanner.hasNextLine() && (line = aaptScanner.nextLine()) != null) {
                    if (line.startsWith("package:")) {
                        String packageName = queryPart("name", line);
                        String versionName = queryPart("versionName", line);

                        System.out.println(index + ": " + packageName + "(version: " + versionName + ", name: " + file.getName() + ")");

                    }
                }

                aaptScanner.close();

                aaptProcess.destroy();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static String queryPart(String partName, String line) {
        String value = null;

        Matcher partMatcher = Pattern.compile(partName + "='([^']*)'").matcher(line);

        if (partMatcher.find()) {
            value = partMatcher.group(1);

        }

        return value;
    }

}
