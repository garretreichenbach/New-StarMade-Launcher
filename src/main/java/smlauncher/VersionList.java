package smlauncher;

import smlauncher.contents.LaunchPanel;
import smlauncher.contents.MainPanel;
import smlauncher.util.IndexFileEntry;
import smlauncher.util.Updater;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public class VersionList {
    public static String installationDir = "./sm/";

    public static final ArrayList<IndexFileEntry> releaseVersions = new ArrayList<>();
    public static final ArrayList<IndexFileEntry> devVersions = new ArrayList<>();
    public static final ArrayList<IndexFileEntry> preReleaseVersions = new ArrayList<>();
    public static final ArrayList<IndexFileEntry> archiveVersions = new ArrayList<>();
    public static final int LAUNCHER_VERSION = 3;

    public static void updateGame() {
        String[] options = {"Backup Database", "Backup Everything", "Don't Backup"};
        int choice = JOptionPane.showOptionDialog(null, "Would you like to backup your database, everything, or nothing?", "Backup", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        int backupMode = UpdaterThread.BACKUP_MODE_NONE;
        if (choice == 0) backupMode = UpdaterThread.BACKUP_MODE_DATABASE;
        else if (choice == 1) backupMode = UpdaterThread.BACKUP_MODE_EVERYTHING;

        //Start update process and update progress bar
        UpdaterThread updaterThread = new UpdaterThread((IndexFileEntry) LaunchPanel.versionDropdown.getSelectedItem(), backupMode, new File(installationDir));
        updaterThread.start();
        MainPanel.inst.launchPanel.launchButton.setEnabled(false);
        MainPanel.inst.launchPanel.playButton.setEnabled(false);
    }

    public static void loadVersionList() throws IOException {
        URL url;
        releaseVersions.clear();
        devVersions.clear();
        preReleaseVersions.clear();
        archiveVersions.clear();
        for(Updater.VersionFile branch : Updater.VersionFile.values()) {
            url = new URL(branch.location);
            URLConnection openConnection = url.openConnection();
            openConnection.setConnectTimeout(10000);
            openConnection.setReadTimeout(10000);
            openConnection.setRequestProperty("User-Agent", "StarMade-Updater_" + LAUNCHER_VERSION);

            BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(openConnection.getInputStream()), StandardCharsets.UTF_8));
            String str;
            while((str = in.readLine()) != null) {
                String[] vPath = str.split(" ", 2);
                String[] vBuild = vPath[0].split("#", 2);
                String version = vBuild[0];
                String build = vBuild[1];
                String path = vPath[1];
                switch(branch) {
                    case RELEASE:
                        releaseVersions.add(new IndexFileEntry(build, path, version, branch));
                        releaseVersions.sort(Collections.reverseOrder());
                        System.err.println("loaded files (sorted) " + releaseVersions);
                        break;
                    case DEV:
                        devVersions.add(new IndexFileEntry(build, path, version, branch));
                        devVersions.sort(Collections.reverseOrder());
                        devVersions.removeIf(next -> next.toString().startsWith("2017")); // 2017 broke
                        System.err.println("loaded files (sorted) " + devVersions);
                        break;
                    case PRE:
                        preReleaseVersions.add(new IndexFileEntry(build, path, version, branch));
                        preReleaseVersions.sort(Collections.reverseOrder());
                        System.err.println("loaded files (sorted) " + preReleaseVersions);
                        break;
                    case ARCHIVE:
                        archiveVersions.add(new IndexFileEntry(build, path, version, branch));
                        archiveVersions.sort(Collections.reverseOrder());
                        System.err.println("loaded files (sorted) " + archiveVersions);
                        break;
                }
            }
            in.close();
            openConnection.getInputStream().close();
        }
    }
}
