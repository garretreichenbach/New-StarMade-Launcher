package smlauncher.starmade;

import smlauncher.StarMadeLauncher;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Updater extends Observable {
	public static final int BACK_NONE = 0;
	public static final int BACK_DB = 1;
	public static final int BACK_ALL = 2;
	public static String FILES_URL = "http://files.star-made.org/";
	public static String LAUNCHER_VERSION_SITE = "http://files.star-made.org/version";
	public static String MIRROR_SITE = "http://files.star-made.org/mirrors";
	public final ArrayList<IndexFileEntry> versions = new ArrayList<>();
	private final ArrayList<String> mirrorURLs = new ArrayList<>();
	boolean loading;
	boolean versionsLoaded;
	private final StarMadeBackupTool backup = new StarMadeBackupTool();
	private boolean updating;

	public Updater(String installDir) {
		reloadVersion(installDir);
	}

	public static void withoutGUI(boolean force, String installDir, GameBranch branch, int backUp, boolean selectVersion) {
		Updater u = new Updater(installDir);
		try {
			u.startLoadVersionList(branch);
			while(u.loading) {
				Thread.sleep(100);
			}

			if(selectVersion) selectVersion(true, u, force, installDir, branch, backUp, selectVersion);
			else {
				if(u.isNewerVersionAvailable()) {
					System.err.println("A New Version Is Available!");
					u.startUpdateNew(installDir, u.versions.get(u.versions.size() - 1), false, backUp);
				} else System.err.println("You Are Already on the Newest Version: use -force to force an update");
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void selectVersion(boolean display, Updater u, boolean force, String installDir, GameBranch f, int backUp, boolean selectVersion) {
		if(display) {
			for(int i = 0; i < u.versions.size(); i++) {
				System.out.println("[" + i + "] v" + u.versions.get(i).version + "; " + u.versions.get(i).build);
			}
		}

		int k;
		try {
			System.out.println("Select the build you want to install (type in number in brackets and press Enter)");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
			k = Integer.parseInt(br.readLine());
		} catch(NumberFormatException e) {
			System.out.println("Error: Input must be number");
			selectVersion(false, u, force, installDir, f, backUp, selectVersion);
			return;
		} catch(IOException e) {
			e.printStackTrace();
			selectVersion(false, u, force, installDir, f, backUp, selectVersion);
			return;
		}
		if(k < 0 || k >= u.versions.size() - 1) {
			System.out.println("Error: Version does not exist");
			selectVersion(false, u, force, installDir, f, backUp, selectVersion);
			return;
		}
		u.startUpdateNew(installDir, u.versions.get(k), false, backUp);
	}

	public static String getRemoteLauncherVersion() throws IOException {
		URL urlVersion = new URL(LAUNCHER_VERSION_SITE);
		URLConnection openConnection = urlVersion.openConnection();
		openConnection.setRequestProperty("User-Agent", "StarMade-Updater_" + StarMadeLauncher.LAUNCHER_VERSION);
		openConnection.setConnectTimeout(10000);
		openConnection.setReadTimeout(10000);
		// Read all the text returned by the server
		BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(openConnection.getInputStream()), StandardCharsets.UTF_8));
		String version = in.readLine();
		in.close();
		return version;
	}

	public static int askBackup(JFrame f) {

		String[] options = {"Yes (Only Database)", "Yes (Everything)", "No"};
		int n = JOptionPane.showOptionDialog(f, "Create Backup of current game data? (recommended)", "Backup?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		switch(n) {
			case 0:
				return BACK_DB;
			case 1:
				return BACK_ALL;
			case 2:
				return BACK_NONE;
		}
		return BACK_NONE;
	}

	private static String getJavaExec() {
		if("Mac OS X".equals(System.getProperty("os.name")) || System.getProperty("os.name").contains("Linux")) {
			return "java";
		} else {
			return "javaw";
		}
	}

	@Override
	public synchronized void addObserver(Observer o) {
		super.addObserver(o);
		backup.addObserver(o);
	}

	public boolean isNewerVersionAvailable() {
		if(!versionsLoaded) {
			return false;
		}
		if(versions.isEmpty()) {
			System.err.println("versions empty");
			return false;
		}
		if(VersionContainer.build == null || "undefined".equals(VersionContainer.build)) {
			System.err.println("Version build null or undefined");
			return true;
		}
		if("latest".equals(VersionContainer.build)) {
			System.err.println("newer version always available for develop version!");
			return true;
		}
		System.out.println("checking your version " + VersionContainer.build + " against latest " + versions.get(versions.size() - 1).build + " = " + VersionContainer.build.compareTo(versions.get(versions.size() - 1).build));
		return !versions.isEmpty() && VersionContainer.build.compareTo(versions.get(versions.size() - 1).build) < 0;
	}

	private void loadVersionList(GameBranch branch) throws IOException {
		setChanged();
		notifyObservers("Retrieving Launcher Version");

		loading = true;
		try {
			versions.clear();
			String version = getRemoteLauncherVersion();
			if(!Objects.equals(version, StarMadeLauncher.LAUNCHER_VERSION)) throw new OldVersionException("You have an old Launcher Version.\n" + "Please download the latest Launcher Version at http://www.star-made.org/\n('retry' will let you ignore this message [not recommended!])");
		} catch(MalformedURLException e) {
			e.printStackTrace();
			(new ErrorDialog("Error", "Malformed URL", e)).setVisible(true);
		} catch(IOException e) {
			e.printStackTrace();
			(new ErrorDialog("Error", "IO Error", e)).setVisible(true);
		} catch(OldVersionException e) {
			e.printStackTrace();
		} finally {
			loading = false;
		}

		setChanged();
		notifyObservers("Retrieving Mirrors");
		loading = true;
		URL urlMirrors;
		try {
			versions.clear();
			urlMirrors = new URL(MIRROR_SITE);

			URLConnection openConnection = urlMirrors.openConnection();
			openConnection.setConnectTimeout(10000);
			openConnection.setRequestProperty("User-Agent", "StarMade-Updater_" + StarMadeLauncher.LAUNCHER_VERSION);
			int version = 0;
			// Read all the text returned by the server
			BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(openConnection.getInputStream()), StandardCharsets.UTF_8));
			String str;
			while((str = in.readLine()) != null) mirrorURLs.add(str);
			in.close();
		} catch(MalformedURLException e) {
			e.printStackTrace();
			(new ErrorDialog("Error", "Malformed URL", e)).setVisible(true);
		} catch(IOException e) {
			e.printStackTrace();
			(new ErrorDialog("Error", "IO Error", e)).setVisible(true);
		} finally {
			loading = false;
		}

		setChanged();
		notifyObservers("Retrieving Versions");
		loading = true;
		URL url;
		try {
			versions.clear();
			url = new URL(branch.url);

			URLConnection openConnection = url.openConnection();
			openConnection.setConnectTimeout(10000);
			openConnection.setReadTimeout(10000);
			openConnection.setRequestProperty("User-Agent", "StarMade-Updater_" + StarMadeLauncher.LAUNCHER_VERSION);
			// Read all the text returned by the server
			BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(openConnection.getInputStream()), StandardCharsets.UTF_8));
			String str;
			while((str = in.readLine()) != null) {
				versions.add(IndexFileEntry.create(str, branch));
			}

			Collections.sort(versions);
			System.err.println("loaded files (sorted) " + versions);
			in.close();
			versionsLoaded = true;
			setChanged();
			notifyObservers("versions loaded");
			openConnection.getInputStream().close();
		} catch(MalformedURLException e) {
			e.printStackTrace();
			(new ErrorDialog("Error", "Malformed URL", e)).setVisible(true);
		} catch(IOException e) {
			e.printStackTrace();
			(new ErrorDialog("Error", "IO Error", e)).setVisible(true);
		} finally {
			loading = false;
		}
	}

	public void reloadVersion(String installDir) {
		try {
			VersionContainer.loadVersion(installDir);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void startLoadVersionList(GameBranch branch) {
		loading = true;
		new Thread(() -> {
			try {
				loadVersionList(branch);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void startUpdateNew(String installDirStr, IndexFileEntry newest, boolean forced, int backupFromMain) {
		if(updating) return;

		try {
			Eula eula = getEula();
			if(eula != null) {
				//eula not accepted
				//create new dialog
				JDialog dialog = new JDialog();
				dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				dialog.setModal(true);
				dialog.setTitle("StarMade EULA");
				dialog.setSize(500, 500);
				dialog.setLocationRelativeTo(null);
				dialog.setResizable(false);
				dialog.setLayout(new BorderLayout());

				JTextArea textArea = new JTextArea(eula.text);
				JScrollPane scrollPane = new JScrollPane(textArea);
				textArea.setLineWrap(true);
				textArea.setWrapStyleWord(true);
				scrollPane.setPreferredSize(new Dimension(500, 500));
				dialog.add(scrollPane, BorderLayout.CENTER);

				JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new FlowLayout());
				JButton acceptButton = new JButton("I have read the EULA and accept");
				acceptButton.addActionListener(e -> {
					File file;
					try {
						file = new File(OperatingSystem.getAppDir(), "eula.properties");
					} catch(IOException ex) {
						throw new RuntimeException(ex);
					}
					Properties p = new Properties();

					p.put("eula", "true");
					try {
						p.store(new FileOutputStream(file), "StarMade EULA");
					} catch(IOException e1) {
						e1.printStackTrace();
					}
					dialog.dispose();
					startUpdateNew(installDirStr, newest, forced, backupFromMain);
				});

				JButton declineButton = new JButton("I don't accept");
				declineButton.addActionListener(e -> {
					dialog.dispose();
				});
				buttonPanel.add(acceptButton);
				buttonPanel.add(declineButton);
				dialog.add(buttonPanel, BorderLayout.SOUTH);
				dialog.setVisible(true);
			}
		} catch(IOException e) {
			e.printStackTrace();
			(new ErrorDialog("Error", "IO Error", e)).setVisible(true);
		}

		File installDir = new File(installDirStr);
		if(!installDir.exists()) installDir.mkdirs();
		if(!installDir.isDirectory()) {
			try {
				throw new IOException("Installation dir is not a directory");
			} catch(IOException e1) {
				(new ErrorDialog("Error", "IO Error", e1)).setVisible(true);
			}
		}

		if(!installDir.canWrite()) {
			try {
				throw new IOException("Your operating System denies access \n" + "to where you are trying to install StarMade (for good reasons)\n" + (new File(installDirStr).getAbsolutePath()) + "\n\n" + "To solve this Problem,\n" + "Please change the install destination to another directory,\n" + "Or Force the install by executing this file as administrator");
			} catch(IOException e1) {
				(new ErrorDialog("Error", "IO Error", e1)).setVisible(true);
			}
		}

		setChanged();
		notifyObservers("updating");

		File instalDir = new File(installDirStr);
		downloadDiff(instalDir, installDirStr, newest, backupFromMain, forced);
	}

	private void downloadDiff(File installDir, String installDirStr, IndexFileEntry version, int backup, boolean forced) {
		updating = true;

		new Thread(() -> {
			try {
				if(backup != BACK_NONE) {
					setChanged();
					notifyObservers("Creating backup!");
					boolean removeOld = false;
					boolean dbOnly = (backup & BACK_DB) == BACK_DB;
					System.err.println("BACKING UP: " + installDirStr);
					this.backup.backUp(installDirStr, "server-database", String.valueOf(System.currentTimeMillis()), ".zip", removeOld, dbOnly, null);
				}

				setChanged();
				notifyObservers("Retrieving checksums for v" + version.version + "(build " + version.build + ")");
				ChecksumFile checksums = getChecksums(version.path);
				System.err.println("Downloaded checksums: \n" + checksums);

				String buildDir = FILES_URL + version.path + "/";

				checksums.download(forced, buildDir, installDir, installDirStr, new FileDowloadCallback() {
					@Override
					public void update(FileDownloadUpdate u) {
						setChanged();
						notifyObservers(u);
					}

					@Override
					public void update(String u) {
						setChanged();
						notifyObservers(u);
					}

					@Override
					public void done(FileDownloadUpdate u) {

					}
				});

				setChanged();
				notifyObservers("Update Successfull!");

				try {
					Thread.sleep(500);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				setChanged();
				notifyObservers("reset");
			} catch(IOException e1) {
				e1.printStackTrace();
				setChanged();
				notifyObservers("failed IO");
				(new ErrorDialog("Error", "IO Error", e1)).setVisible(true);
			} catch(NoSuchAlgorithmException e1) {
				e1.printStackTrace();
				setChanged();
				notifyObservers("failed Sha");
				(new ErrorDialog("Error", "Sha Error", e1)).setVisible(true);
			} finally {
				updating = false;
				setChanged();
				notifyObservers("finished");
			}
		}).start();
	}

	public Eula getEula() throws IOException {
		System.err.println("URL::: " + FILES_URL + "/smeula.txt");
		URL urlVersion = new URL(FILES_URL + "/smeula.txt");
		URLConnection openConnection = urlVersion.openConnection();
		openConnection.setRequestProperty("User-Agent", "StarMade-Updater_" + StarMadeLauncher.LAUNCHER_VERSION);
		openConnection.setConnectTimeout(10000);
		openConnection.setReadTimeout(10000);
		Eula e = new Eula();
		// Read all the text returned by the server
		BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(openConnection.getInputStream()), StandardCharsets.UTF_8));
		StringBuilder b = new StringBuilder();
		String line;
		while((line = in.readLine()) != null) {
			if(e.title == null) {
				e.title = line;
				File file = new File(OperatingSystem.getAppDir(), "eula.properties");
				Properties p = new Properties();
				if(file.exists()) {
					FileInputStream fs = new FileInputStream(file);
					p.load(fs);
					fs.close();
					if(p.getProperty("EULA") != null && p.getProperty("EULA").equals(e.title)) return null;
				}
			}
			b.append(line + "\n");
		}
		in.close();
		e.text = b.toString();
		return e;
	}

	public static ChecksumFile getChecksums(String relPath) throws IOException {
		URL urlVersion = new URL(relPath + "/checksums");

		URLConnection openConnection = urlVersion.openConnection();
		openConnection.setRequestProperty("User-Agent", "StarMade-Updater_" + StarMadeLauncher.LAUNCHER_VERSION);
		openConnection.setConnectTimeout(10000);
		openConnection.setReadTimeout(10000);
		// TODO URL is using wrong build for dev/release
		BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(openConnection.getInputStream()), StandardCharsets.UTF_8));
		ChecksumFile f = new ChecksumFile();
		f.parse(in);
		in.close();
		return f;
	}

}
