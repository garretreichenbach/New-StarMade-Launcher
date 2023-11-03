package smlauncher;

import smlauncher.util.IndexFileEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class InstalledUtils {
    public static IndexFileEntry getInstalledVersion(){
        try {
            Scanner fileReader = new Scanner(new File(VersionList.installationDir + "/version.txt"));
            String v = fileReader.nextLine();
            fileReader.close();
            ArrayList<IndexFileEntry> allEntries = new ArrayList<>();
            allEntries.addAll(VersionList.releaseVersions);
            allEntries.addAll(VersionList.devVersions);
            allEntries.addAll(VersionList.archiveVersions);
            allEntries.addAll(VersionList.preReleaseVersions);

            for (IndexFileEntry en : allEntries) {
                if(v.endsWith(en.path)){
                    System.out.println("Installed version: " + en.toStringFull());
                    return en;
                }
            }


        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}
