package smlauncher.starmade;

import smlauncher.LogManager;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * FolderZiper provide a static method to zip a folder.
 */
public class FolderZipper {

	/**
	 * add the srcFolder to the zip stream.
	 *
	 * @param path   String, the relatif path with the root archive.
	 * @param zip    ZipOutputStram, the stream to use to write the given file.
	 * @param buf
	 * @param cb
	 * @param filter
	 */
	private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip, String startsWithFilter, byte[] buf, ZipCallback cb, FileFilter filter) {

		path.replace('\\', '/');
		srcFolder.replace('\\', '/');

		File folder = new File(srcFolder);
		if(filter != null && !filter.accept(folder)) {
			return;
		}
		if(cb != null) {
			cb.update(folder);
		}
		String[] fileListe = folder.list();
		try {
			for(int i = 0; i < fileListe.length; i++) {
				if(startsWithFilter == null || !fileListe[i].startsWith(startsWithFilter)) {
					addToZip(path + "/" + folder.getName(), srcFolder + "/" + fileListe[i], zip, startsWithFilter, buf, cb, filter);
				}
			}
		} catch(Exception ex) {
		}
	}

	/**
	 * Write the content of srcFile in a new ZipEntry, named path+srcFile, of
	 * the zip stream. The result is that the srcFile will be in the path folder
	 * in the generated archive.
	 *
	 * @param path    String, the relatif path with the root archive.
	 * @param srcFile String, the absolute path of the file to add
	 * @param zip     ZipOutputStram, the stream to use to write the given file.
	 * @param buf
	 * @param cb
	 * @param filter
	 */
	private static void addToZip(String path, String srcFile, ZipOutputStream zip, String startsWithFilter, byte[] buf, ZipCallback cb, FileFilter filter) {

		path.replace('\\', '/');
		srcFile.replace('\\', '/');

		File folder = new File(srcFile);

		if(filter != null && !filter.accept(folder)) {
			return;
		}
		if(folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip, startsWithFilter, buf, cb, filter);
		} else {

			if(cb != null) {
				cb.update(new File(srcFile));
			}

			int len;
			try {
				FileInputStream in = new FileInputStream(srcFile);

				String entryName;
				// if(path.length() > 0){
				entryName = path + "/" + folder.getName();
				if(entryName.startsWith("/")) {
					entryName = entryName.substring(1);
				}
				//				System.err.println("[ZIP] PUTTING: "+entryName);
				// }else{
				// entryName = folder.getName();
				// }
				ZipEntry zipEntry = new ZipEntry(entryName);
				zip.putNextEntry(zipEntry);

				while((len = in.read(buf)) > 0) {
					zip.write(buf, 0, len);
				}
				in.close();
			} catch(Exception ex) {
				LogManager.logWarning("Error while adding file to zip: " + srcFile, ex);
			}
		}
	}

	/**
	 * Zip the srcFolder into the destFileZipFile. All the folder subtree of the
	 * src folder is added to the destZipFile archive.
	 * <p/>
	 * TODO handle the usecase of srcFolder being en file.
	 *
	 * @param srcFolder   String, the path of the srcFolder
	 * @param destZipFile String, the path of the destination zipFile. This file will be
	 *                    created or erased.
	 * @throws IOException
	 */
	public static void zipFolder(String srcFolder, String destZipFile, String startsWithFilter, FileFilter filter) throws IOException {
		zipFolder(srcFolder, destZipFile, startsWithFilter, null, filter);
	}

	public static void zipFolder(String srcFolder, String destZipFile, String startsWithFilter, ZipCallback cb, String pathPrefix, FileFilter filter) throws IOException {
		zipFolder(srcFolder, destZipFile, startsWithFilter, cb, pathPrefix, filter, false);
	}

	public static void zipFolder(String srcFolder, String destZipFile, String startsWithFilter, ZipCallback cb, String pathPrefix, FileFilter filter, boolean skipSelfFolder) throws IOException {
		ZipOutputStream zip = null;
		BufferedOutputStream fileWriter = null;

		System.out.println("[ZIP] Zipping folder: " + srcFolder + " to " + destZipFile + " (Filter: " + startsWithFilter + ")");
		destZipFile.replace('\\', '/');
		srcFolder.replace('\\', '/');
		File destFile = new File(destZipFile);
		File src = new File(srcFolder);
		System.out.println("[ZIP] Writing to " + destFile.getAbsolutePath());
		fileWriter = new BufferedOutputStream(new FileOutputStream(destFile), 4096);
		zip = new ZipOutputStream(fileWriter);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024 * 4];
		if(!skipSelfFolder) {
			addFolderToZip(pathPrefix, srcFolder, zip, startsWithFilter, buf, cb, filter);
		} else {
			String[] fileListe = src.list();
			try {
				for(int i = 0; i < fileListe.length; i++) {
					if(startsWithFilter == null || !fileListe[i].startsWith(startsWithFilter)) {
						addToZip("", srcFolder + "/" + fileListe[i], zip, startsWithFilter, buf, cb, filter);
					}
				}
			} catch(Exception ex) {
			}
		}

		zip.flush();
		zip.close();
		fileWriter.close();

	}

	public static void zipFolder(String srcFolder, String destZipFile, String startsWithFilter, ZipCallback cb, FileFilter filter) throws IOException {
		zipFolder(srcFolder, destZipFile, startsWithFilter, cb, "", filter);

	}

	public interface ZipCallback {
		void update(File f);
	}
}
