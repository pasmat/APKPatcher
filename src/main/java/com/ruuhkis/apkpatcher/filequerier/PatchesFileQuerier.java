package com.ruuhkis.apkpatcher.filequerier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by PasiMatalamaki on 5.9.2016.
 */
public class PatchesFileQuerier implements FileQuerier {

    public static final String DETAILS_TXT = "details.txt";

    private List<File> selectedPatches;

    public PatchesFileQuerier() {
        this(new ArrayList<File>());
    }

    public PatchesFileQuerier(List<File> selectedPatches) {
        this.selectedPatches = selectedPatches;
    }

    public boolean queryFile(int index, File file) {
        boolean directory = file.isDirectory();

        if(directory) {
            File detailsFile = new File(file, DETAILS_TXT);

            System.out.println(index + ": [" + (selectedPatches.contains(file) ? "x" : " ") + "] " + file.getName());

            if(detailsFile.exists()) {
                try {
                    System.out.println(readFile(detailsFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Warning: no details file inside patch " + file.getName());
            }
        }

        return directory;
    }

    private String readFile(File file) throws IOException {
        boolean firstLine = true;

        String line;

        StringBuilder sb = new StringBuilder();

        BufferedReader br = new BufferedReader(new FileReader(file));
        while((line = br.readLine()) != null) {
            if(firstLine) {
                firstLine = false;
            } else {
                sb.append("\n");
            }

            sb.append(line);
        }
        br.close();

        return sb.toString();
    }

    public void togglePatch(File selectedPatch) {
        if(selectedPatches.contains(selectedPatch)) {
            selectedPatches.remove(selectedPatch);
        } else {
            selectedPatches.add(selectedPatch);
        }
    }

    public List<File> getSelectedPatches() {
        return selectedPatches;
    }

    public void setSelectedPatches(List<File> selectedPatches) {
        this.selectedPatches = selectedPatches;
    }
}
