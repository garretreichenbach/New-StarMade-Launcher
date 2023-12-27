package smlauncher.starmade;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Observable;

/**
 * Backs up the game directory.
 *
 * @author TheDerpGamer
 */
// TODO replace deprecated API
public class StarMadeBackupTool extends Observable {

	int file;
	int maxFile;

	public void backUp(String installDir, String databasePath, String backupName, String Fileension, boolean removeOldVersion, boolean databaseOnly, FileFilter filter) throws IOException {
		File dir = new File(installDir);
		if(dir.exists() && dir.list().length > 0) {
			setChanged();
			notifyObservers("Backing Up");
			String backup = ("backup-StarMade-" + VersionContainer.VERSION + "-" + VersionContainer.build + "_" + backupName + (!Fileension.startsWith(".") ? ("." + Fileension) : Fileension));
			System.out.println("Backing Up (archiving files)");

			file = 0;
			if(databaseOnly) maxFile = FileUtil.countFilesRecusrively((new File(installDir)).getAbsolutePath() + File.separator + databasePath);
			else maxFile = FileUtil.countFilesRecusrively((new File(installDir)).getAbsolutePath());
			ZipGUICallback guiCallBack = new ZipGUICallback();
			FolderZipper.ZipCallback zipCallback = f -> {
				guiCallBack.f = f;
				guiCallBack.fileMax = maxFile;
				guiCallBack.fileIndex = file;
				setChanged();
				notifyObservers(guiCallBack);
				file++;
			};

			if(databaseOnly) {

				// zip everything except backups themselves
				File f = new File(installDir);
				for(File fg : f.listFiles()) {
					if(fg.isDirectory() && fg.getName().equals(databasePath)) FolderZipper.zipFolder(fg.getAbsolutePath(), backup + ".tmp", "backup-StarMade-", zipCallback, "", filter);
				}
			} else {
				// zip everything except backups themselves
				FolderZipper.zipFolder(installDir, backup + ".tmp", "backup-StarMade-", zipCallback, filter);
			}
			setChanged();
			notifyObservers("resetbars");
			System.out.println("Copying Backup mFile to install dir...");
			File backUpFile = new File(backup + ".tmp");
			if(backUpFile.exists()) {
				File file = new File(new File(installDir).getAbsolutePath() + File.separator + backup);
				System.err.println("Copy to: " + file.getAbsolutePath());
				DataUtil.copy(backUpFile, file);
				backUpFile.delete();
			}

			if(removeOldVersion) {
				setChanged();
				notifyObservers("Deleting old installation");
				System.out.println("Cleaning up current installation");

				//			File oldCatalog = new File(INSTALL_DIR+"/blueprints/Catalog.txt");
				//			if(oldCatalog.exists()){
				//				File backupCatalog = new File(INSTALL_DIR+"/blueprints/Catalog-old-"+System.currentTimeMillis()+".txt");
				//				oldCatalog.renameTo(backupCatalog);
				//			}
				File[] list = dir.listFiles();
				for(File f : list) {
					if("data".equals(f.getName()) || "native".equals(f.getName()) || f.getName().startsWith("StarMade") ||
							//f.getName().startsWith("server-database") ||
							//f.getName().startsWith("client-database") ||
							"MANIFEST.MF".equals(f.getName()) || "version.txt".equals(f.getName())) {
						FileUtil.deleteDir(f);
						f.delete();
					}
				}
			}
			System.out.println("[BACKUP] DONE");
		}
	}
}
