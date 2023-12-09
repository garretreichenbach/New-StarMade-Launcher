package smlauncher;


import smlauncher.starmade.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static smlauncher.starmade.Updater.FILES_URL;

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
	public boolean updating;

	public UpdaterThread(IndexFileEntry entry, int backupMode, File installDir) {
		this.entry = entry;
		this.backupMode = backupMode;
		this.installDir = installDir;
	}

	@Override
	public void run() {
		try {
			updating = true;
			boolean dbOnly = backupMode == BACKUP_MODE_DATABASE;
			if(backupMode != BACKUP_MODE_NONE && installDir.exists()) (new StarMadeBackupTool()).backUp(installDir.getPath(), "server-database", String.valueOf(System.currentTimeMillis()), ".zip", false, dbOnly, null);

			String branch = "";
			if(StarMadeLauncher.lastUsedBranch == 1) branch = "dev/";
			else if(StarMadeLauncher.lastUsedBranch == 2) branch = "pre/";
			String buildDir = FILES_URL + "build/" + branch + "starmade-build_" + entry.path;
			ChecksumFile checksums = Updater.getChecksums(buildDir);
			if(!installDir.exists()) installDir.mkdirs();
			float finalSize = checksums.checksums.size();
			if(finalSize == 0) {
				onFinished();
				return;
			}
			checksums.download(false, buildDir, installDir, installDir.getPath(), new FileDowloadCallback() {
				@Override
				public void update(FileDownloadUpdate u) {
					if(u.total == 0) {
						onFinished();
						return;
					}
					onProgress((float) u.index / u.total - 1);
					if(u.index >= u.total - 1) {
						updating = false;
						onFinished();
					}
				}

				@Override
				public void update(String u) {
					if(u.contains("Nothing to download")) onFinished();
				}
			});
			//onFinished();
		} catch(IOException exception) {
			exception.printStackTrace();
			onError(exception);
		} catch(NoSuchAlgorithmException exception) {
			throw new RuntimeException(exception);
		}
	}

	public void onProgress(float progress) {}

	public void onFinished() {}

	public void onError(Exception exception) {}
}
