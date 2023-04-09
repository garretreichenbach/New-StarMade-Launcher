package smlauncher;


import smlauncher.contents.LaunchPanel;
import smlauncher.util.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static smlauncher.util.Updater.FILES_URL;

/**
 * Thread for updating the game.
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class UpdaterThread extends Thread {

	public static final int BACKUP_MODE_NONE = 0;
	public static final int BACKUP_MODE_DATABASE = 1;
	public static final int BACKUP_MODE_EVERYTHING = 2;

	private final IndexFileEntry entry;
	private final int backupMode;
	private final File installDir;

	public UpdaterThread(IndexFileEntry entry, int backupMode, File installDir) {
		this.entry = entry;
		this.backupMode = backupMode;
		this.installDir = installDir;
	}

	@Override
	public void run() {
		try {
			boolean dbOnly = backupMode == BACKUP_MODE_DATABASE;
			if(backupMode != BACKUP_MODE_NONE && installDir.exists()) (new StarMadeBackupTool()).backUp(installDir.getPath(), "server-database", String.valueOf(System.currentTimeMillis()), ".zip", false, dbOnly, null);

			String buildDir = FILES_URL + "build/starmade-build_" + entry.path;
			ChecksumFile checksums = Updater.getChecksums(buildDir);
			if(!installDir.exists()) installDir.mkdirs();
			checksums.download(false, buildDir, installDir, installDir.getPath(), new FileDowloadCallback() {
				@Override
				public void update(FileDownloadUpdate u) {
					float totalPct = u.index/ ((float) u.total);
					LaunchPanel.totalBar.setValue((int) (1000*totalPct));
					LaunchPanel.totalBar.setString((int)(u.downloadSpeed/1024) + " Kbps");

					System.out.println(u);
					float indPct = u.downloaded/ ((float) u.size);
					LaunchPanel.indBar.setValue((int) (1000*indPct));
					LaunchPanel.indBar.setString(u.fileName);
				}

				@Override
				public void update(String u) {

				}
			});
			System.out.println("Done lol");
		} catch(IOException exception) {
			exception.printStackTrace();
		} catch(NoSuchAlgorithmException exception) {
			throw new RuntimeException(exception);
		}
	}
}
