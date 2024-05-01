package smlauncher;

import com.formdev.flatlaf.FlatDarkLaf;
import smlauncher.community.LauncherCommunityPanel;
import smlauncher.content.LauncherContentPanel;
import smlauncher.downloader.JavaDownloader;
import smlauncher.downloader.JavaVersion;
import smlauncher.fileio.ImageFileUtil;
import smlauncher.fileio.TextFileUtil;
import smlauncher.forums.LauncherForumsPanel;
import smlauncher.mainui.*;
import smlauncher.mainui.scrolldisplay.ScrollDisplayPanel;
import smlauncher.mainui.scrolldisplay.ScrollablePanel;
import smlauncher.mainui.scrolldisplay.ScrollDisplayControlPanel;
import smlauncher.mainui.windowcontrols.WindowDragPanel;
import smlauncher.news.LauncherNewsPanel;
import smlauncher.starmade.*;
import smlauncher.util.OperatingSystem;
import smlauncher.util.Palette;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Main class for the StarMade Launcher.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class StarMadeLauncher extends JFrame {

	public static final String BUG_REPORT_URL = "https://github.com/garretreichenbach/New-StarMade-Launcher/issues";
	public static final String LAUNCHER_VERSION = "3.0.15";
	private static final String[] J18ARGS = {"--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED", "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED", "--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED", "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED", "--add-opens=jdk.compiler/com.sun.tools.javac=ALL-UNNAMED", "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED", "--add-opens=java.base/java.io=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED"};
	private static IndexFileEntry gameVersion;
	private static GameBranch lastUsedBranch = GameBranch.RELEASE;
	private static boolean debugMode;
	private static boolean useSteam;
	private static String selectedVersion;
	private static boolean serverMode;
	private static int port;
	private final OperatingSystem currentOS;
	private final VersionRegistry versionRegistry;
	private final DownloadStatus dlStatus = new DownloadStatus();
	private GameUpdaterThread updaterThread;
	private JButton updateButton;
	private JTextField portField;
	private JPanel versionPanel;
	private JPanel playPanel;
	private JPanel serverPanel;
	private JPanel playPanelButtons;

	public StarMadeLauncher() {
		// Set window properties
		super("StarMade Launcher [" + LAUNCHER_VERSION + "]");
		setBounds(100, 100, 800, 550);
		setMinimumSize(new Dimension(800, 550));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Set window icon
		try {
			URL resource = StarMadeLauncher.class.getResource("/sprites/icon.png");
			if(resource != null) setIconImage(Toolkit.getDefaultToolkit().getImage(resource));
		} catch(Exception exception) {
			System.out.println("Could not set window icon");
		}

		// Fetch game versions
		versionRegistry = new VersionRegistry();
		try {
			versionRegistry.createRegistry();
		} catch(Exception exception) {
			System.out.println("Could not load versions list, switching to offline");
			//Todo: Offline Mode
		}

		// Read launch settings
		LaunchSettings.readSettings();

		// Read game version and branch
		gameVersion = getLastUsedVersion();
		setGameVersion(gameVersion);
		setBranch(gameVersion.branch);

		LaunchSettings.saveSettings();

		// Delete updater jar (in case launcher was updated)
		File updaterJar = new File("Updater.jar");
		if(updaterJar.exists()) updaterJar.delete();

		// Get the current OS
		currentOS = OperatingSystem.getCurrent();

		// Download JREs
		try {
			downloadJRE(JavaVersion.JAVA_8);
			downloadJRE(JavaVersion.JAVA_18);
		} catch(Exception exception) {
			System.out.println("Could not download JREs");
			JOptionPane.showMessageDialog(this, "Failed to download Java Runtimes for first time setup. Please make sure you have a stable internet connection and try again.", "Error", JOptionPane.ERROR_MESSAGE);
		}

		// Create launcher UI
		createLauncherUI();
		dispose();
		setUndecorated(true);
		setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
		setResizable(false);
		getRootPane().setDoubleBuffered(true);
		setVisible(true);
	}

	// Main Method

	public static void main(String[] args) {
		boolean headless = false;
		int backupMode = GameUpdater.BACK_DB;
		boolean selectVersion = false;

		if(args == null || args.length == 0) startup();
		else {
			GameBranch buildBranch = GameBranch.RELEASE;
			for(String arg : args) {
				arg = arg.toLowerCase();
				if(arg.equals("-debug_mode")) debugMode = true;
				if(arg.contains("-version")) {
					selectVersion = true;
					if(arg.contains("-dev")) buildBranch = GameBranch.DEV;
					else if(arg.contains("-pre")) buildBranch = GameBranch.PRE;
					else buildBranch = GameBranch.RELEASE;
				} else if("-no_gui".equals(arg) || "-nogui".equals(arg)) {
					if(GraphicsEnvironment.isHeadless()) {
						displayHelp();
						System.out.println("Please use the '-nogui' parameter to run the launcher in text mode!");
						return;
					} else headless = true;
				}
				if(headless) {
					switch (arg) {
						case "-h":
						case "-help":
							displayHelp();
							return;
						case "-backup":
						case "-backup_all":
							backupMode = GameUpdater.BACK_ALL;
							break;
						case "-no_backup":
							backupMode = GameUpdater.BACK_NONE;
							break;
						case "-server":
							serverMode = true;
							break;
					}
					if(arg.startsWith("-port:")) {
						try {
							port = Integer.parseInt(arg.substring(6));
						} catch(NumberFormatException ignored) {
						}
					}
					GameUpdater.withoutGUI((args.length > 1 && "-force".equals(args[1])), LaunchSettings.getInstallDir(), buildBranch, backupMode, selectVersion);
				} else startup();
				startup();
			}
		}
	}

	// Run StarMade methods

	private static void startup() {
		EventQueue.invokeLater(() -> {
			try {
				FlatDarkLaf.setup();
				if(LauncherUpdaterHelper.checkForUpdate()) {
					System.err.println("Launcher version doesn't match latest version, so an update must be available.");
					showLauncherUpdateDialog();
				} else startLauncherWindow();
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println("Error occurred while running launcher");
			}
		});
	}

	private static void showLauncherUpdateDialog() {
		JDialog updateDialog = new LauncherUpdateDialog(
				"Launcher Update Available", 500, 350,
				e -> LauncherUpdaterHelper.updateLauncher(),
				e -> startLauncherWindow()
		);
		updateDialog.setVisible(true);
	}

	private static void startLauncherWindow() {
		JFrame frame = new StarMadeLauncher();
		(new Thread(() -> {
			//For steam: keep it repainting so the damn overlays go away
			try {
				Thread.sleep(1200);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			while (frame.isVisible()) {
				try {
					Thread.sleep(500);
				} catch(InterruptedException exception) {
					exception.printStackTrace();
				}
				EventQueue.invokeLater(frame::repaint);
			}
		})).start();
	}

	private static void displayHelp() {
		System.out.println("StarMade Launcher " + LAUNCHER_VERSION + " Help:");
		System.out.println("-version : Version selection prompt");
		System.out.println("-no_gui : Don't start gui (needed for linux dedicated servers)");
		System.out.println("-no_backup : Don't create backup (default backup is server database only)");
		System.out.println("-backup_all : Create backup of everything (default backup is server database only)");
		System.out.println("-pre : Use pre branch (default is release)");
		System.out.println("-dev : Use dev branch (default is release)");
		System.out.println("-server -port:<port> : Start in server mode");
	}

	private void runStarMade(boolean server) {
		ArrayList<String> commandComponents = getCommandComponents(server);
		//Run game
		ProcessBuilder process = new ProcessBuilder(commandComponents);
		process.directory(new File(LaunchSettings.getInstallDir()));
		process.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		process.redirectError(ProcessBuilder.Redirect.INHERIT);
		try {
			System.out.println("Command: " + String.join(" ", commandComponents));
			process.start();
			System.exit(0);
		} catch(Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	public ArrayList<String> getCommandComponents(boolean server) {
		boolean useJava8 = gameVersion.version.startsWith("0.2") || gameVersion.version.startsWith("0.1");
		String bundledJavaPath = new File(useJava8 ? getJavaPath(JavaVersion.JAVA_8) : getJavaPath(JavaVersion.JAVA_18)).getAbsolutePath();

		ArrayList<String> commandComponents = new ArrayList<>();
		commandComponents.add(bundledJavaPath);
		if(!useJava8) commandComponents.addAll(Arrays.asList(J18ARGS));

		if(currentOS == OperatingSystem.MAC) {
			// Run OpenGL on main thread on macOS
			// Needs to be added before "-jar"
			commandComponents.add("-XstartOnFirstThread");
		}
//		commandComponents.add("-Dfml.earlyprogresswindow=false");

		if(currentOS == OperatingSystem.LINUX) {
			// Override (meaningless?) default library path
			commandComponents.add("-Djava.library.path=lib:native/linux");
		}

		commandComponents.add("-jar");
		commandComponents.add("StarMade.jar");

		// Memory Arguments
		if(!LaunchSettings.getJvmArgs().isEmpty()) {
			String[] launchArgs = LaunchSettings.getLaunchArgs().split(" ");
			for(String arg : launchArgs) {
				if(arg.startsWith("-Xms") || arg.startsWith("-Xmx")) continue;
				commandComponents.add(arg.trim());
			}
		}
		commandComponents.add("-Xms1024m");
		commandComponents.add("-Xmx" + LaunchSettings.getMemory() + "m");

		// Game arguments
		commandComponents.add("-force");
		if(portField != null) port = Integer.parseInt(portField.getText());
		if(server) {
			commandComponents.add("-server");
			commandComponents.add("-port:" + port);
		}
		return commandComponents;
	}

	private void updateGame(IndexFileEntry version) {
		String[] options = {"Backup Database", "Backup Everything", "Don't Backup"};
		int choice = JOptionPane.showOptionDialog(this, "Would you like to backup your database, everything, or nothing?", "Backup", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		int backupMode = GameUpdaterThread.BACKUP_MODE_NONE;
		if(choice == 0) backupMode = GameUpdaterThread.BACKUP_MODE_DATABASE;
		else if(choice == 1) backupMode = GameUpdaterThread.BACKUP_MODE_EVERYTHING;
		ImageIcon updateButtonEmpty = ImageFileUtil.getIcon("sprites/update_load_empty.png");
		ImageIcon updateButtonFilled = ImageFileUtil.getIcon("sprites/update_load_full.png");
		updateButton.setIcon(updateButtonEmpty);
		//Start update process and update progress bar
		(updaterThread = new GameUpdaterThread(version, backupMode, new File(LaunchSettings.getInstallDir())) {
			@Override
			public void onProgress(float progress, String file, long mbDownloaded, long mbTotal, long mbSpeed) {
				dlStatus.setInstallProgress(progress);
				dlStatus.setDownloadedMb(mbDownloaded);
				dlStatus.setTotalMb(mbTotal);
				dlStatus.setSpeedMb(mbSpeed);
				if(file != null && !file.equals("null")) dlStatus.setFilename(file);
				int width = updateButtonEmpty.getIconWidth();
				int height = updateButtonEmpty.getIconHeight();
				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = image.createGraphics();
				g.drawImage(updateButtonEmpty.getImage(), 0, 0, null);
				int filledWidth = (int) (width * progress);
				g.drawImage(updateButtonFilled.getImage(), 0, 0, filledWidth, updateButtonFilled.getIconHeight(), 0, 0, filledWidth, updateButtonFilled.getIconHeight(), null);
				g.dispose();
				updateButton.setIcon(new ImageIcon(image));
				updateButton.setToolTipText(dlStatus.toString());
				updateButton.repaint();
			}

			@Override
			public void onFinished() {
				gameVersion = getLastUsedVersion();
				assert gameVersion != null;
				LaunchSettings.setLastUsedVersion(gameVersion.version);
				selectedVersion = gameVersion.version;
				setBranch(gameVersion.branch);
				LaunchSettings.saveSettings();
				SwingUtilities.invokeLater(() -> {
					try {
						sleep(1);
						updatePlayPanelButtons(playPanel, false);
					} catch(InterruptedException e) {
						throw new RuntimeException(e);
					}
				});
			}

			@Override
			public void onError(Exception exception) {
				exception.printStackTrace();
				updateButton.setIcon(ImageFileUtil.getIcon("sprites/update_btn.png"));
			}
		}).start();
	}

	// Launcher Getters and Setters

	private static String getCurrentUser() {
		try {
			return StarMadeCredentials.read().getUser();
		} catch(Exception ignored) {
			return null;
		}
	}

	private static void removeCurrentUser() {
		try {
			StarMadeCredentials.removeFile();
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	private static void setGameVersion(IndexFileEntry gameVersion) {
		if(gameVersion != null) {
			LaunchSettings.setLastUsedVersion(gameVersion.version);
			if(usingOldVersion()) LaunchSettings.setJvmArgs("--illegal-access=permit");
			else LaunchSettings.setJvmArgs("");
		} else {
			LaunchSettings.setLastUsedVersion("NONE");
			LaunchSettings.setJvmArgs("");
		}
	}

	private static void setBranch(GameBranch branch) {
		lastUsedBranch = branch;
		LaunchSettings.setLastUsedBranch(lastUsedBranch.index);
	}

	private static boolean usingOldVersion() {
		return gameVersion.version.startsWith("0.2") || gameVersion.version.startsWith("0.1");
	}

	private void downloadJRE(JavaVersion version) throws Exception {
		if(new File(getJavaPath(version)).exists()) {
			System.out.println("JRE " + version + " already downloaded");
			return;
		}
		System.out.println("Downloading JRE " + version);
		(new JavaDownloader(version)).downloadAndUnzip();
	}

	private IndexFileEntry getLastUsedVersion() {
		try {
			String version;
			File versionFile = new File(LaunchSettings.getInstallDir(), "version.txt");
			if(versionFile.exists()) {
				version = TextFileUtil.readText(versionFile);
			} else {
				version = LaunchSettings.getLastUsedVersion();
			}
			String shortVersion = version.substring(0, version.indexOf('#'));

			IndexFileEntry entry = versionRegistry.searchForVersion(e -> shortVersion.equals(e.version));
			if(entry != null) return entry;
		} catch(Exception e) {
			System.out.println("Could not read game version from file");
		}
		// Return latest release if nothing found
		return versionRegistry.getLatestVersion(GameBranch.RELEASE);
	}

	private String getJavaPath(JavaVersion version) {
		return LaunchSettings.getInstallDir() + "/" + String.format(currentOS.javaPath, version.number);
	}

	private IndexFileEntry getLatestVersion(GameBranch branch) {
		IndexFileEntry currentVersion = getLastUsedVersion();
		if(debugMode || (currentVersion != null && !currentVersion.version.startsWith("0.2") && !currentVersion.version.startsWith("0.1"))) {
			return getLastUsedVersion();
		}
		return versionRegistry.getLatestVersion(branch);
	}

	private boolean gameJarExists(String installDir) {
		return (new File(installDir + "/StarMade.jar")).exists();
	}

	// UI Methods

	private void createLauncherUI() {
		JPanel mainPanel = new JPanel();
		mainPanel.setDoubleBuffered(true);
		mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		mainPanel.setLayout(new BorderLayout(0, 0));
		setContentPane(mainPanel);

		//Drag and control the window with the top panel
		JPanel topPanel = new WindowDragPanel(ImageFileUtil.getIcon("sprites/header_top.png"), this);
		mainPanel.add(topPanel, BorderLayout.NORTH);

		//Display scrollable content in the center
		ScrollDisplayPanel centerPanel = new ScrollDisplayPanel(
				new ScrollablePanel[] {
					new LauncherNewsPanel(), new LauncherForumsPanel(),
					new LauncherContentPanel(), new LauncherCommunityPanel()
			}
		);
		mainPanel.add(centerPanel, BorderLayout.CENTER);

		//Control the center panel view with the left panel
		JPanel leftPanel = new ScrollDisplayControlPanel(centerPanel);
		mainPanel.add(leftPanel, BorderLayout.WEST);

		//Update and play the game with the footer panel
		JPanel footerPanel = createFooterPanel();
		mainPanel.add(footerPanel, BorderLayout.SOUTH);

		if(serverPanel != null) serverPanel.setVisible(false);
		versionPanel.setVisible(true);
	}

	// Footer Panel Methods

	private JPanel createFooterPanel() {
		JPanel footerPanel = new JPanel();
		footerPanel.setDoubleBuffered(true);
		footerPanel.setOpaque(false);
		footerPanel.setLayout(new StackLayout());

		JLabel footerLabel = new JLabel();
		footerLabel.setDoubleBuffered(true);
		footerLabel.setIcon(ImageFileUtil.getIcon("sprites/footer_normalplay_bg.jpg"));
		footerPanel.add(footerLabel);

		JPanel footerPanelButtons = new JPanel();
		footerPanelButtons.setDoubleBuffered(true);
		footerPanelButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
		footerPanelButtons.setOpaque(false);
		footerPanelButtons.add(Box.createRigidArea(new Dimension(10, 0)));
		footerPanelButtons.add(Box.createRigidArea(new Dimension(30, 0)));
		footerPanelButtons.setBounds(0, 0, 800, 30);
		footerLabel.add(footerPanelButtons);

		JButton clientPlayButton = new SetPlayModeButton("Play", e -> switchPlayMode(footerPanel, footerLabel, false));
		footerPanelButtons.add(clientPlayButton);

		JButton serverPlayButton = new SetPlayModeButton("Dedicated Server", e -> switchPlayMode(footerPanel, footerLabel, true));
		footerPanelButtons.add(serverPlayButton);

		if(getLastUsedVersion() == null) selectedVersion = null;
		else selectedVersion = gameVersion.version;
		createPlayPanel(footerPanel);
		createServerPanel(footerPanel);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setDoubleBuffered(true);
		bottomPanel.setOpaque(false);
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		footerPanel.add(bottomPanel, BorderLayout.SOUTH);

		JButton launchSettings = createLaunchSettingsDialogButton();
		bottomPanel.add(launchSettings);

		JButton installSettings = createInstallSettingsDialogButton();
		bottomPanel.add(installSettings);

		switchPlayMode(footerPanel, footerLabel, false); // make sure right components are visible
		return footerPanel;
	}

	private static JButton createLaunchSettingsDialogButton() {
		return new SettingsDialogButton(
				"Launch Settings",
				ImageFileUtil.getIcon("sprites/memory_options_gear.png"),
				e -> {
					JDialog settingsDialog = new LaunchSettingsDialog("Launch Settings", 500, 350);
					settingsDialog.setVisible(true);
				}
		);
	}

	private JButton createInstallSettingsDialogButton() {
		return new SettingsDialogButton(
				"Installation Settings",
				ImageFileUtil.getIcon("sprites/launch_options_gear.png"),
				e -> {
					JDialog installDialog = new InstallSettingsDialog(
							"Installation Settings",
							450, 150,
							e1 -> {
								IndexFileEntry version = getLatestVersion(lastUsedBranch);
								if(version != null) {
									if(updaterThread == null || !updaterThread.updating) {
										this.dispose();
										updatePlayPanelButtons(playPanel, true);
										updateGame(version);
									}
								} else
									JOptionPane.showMessageDialog(this, "The Launcher needs to be online to do this!", "Error", JOptionPane.ERROR_MESSAGE);
							}
					);
					installDialog.setVisible(true);
				}
		);
	}

	private void switchPlayMode(JPanel footerPanel, JLabel footerLabel, boolean serverMode) {
		if(updaterThread != null && updaterThread.updating) { //Don't allow this while the game is updating
			return;
		}
		if(serverMode) switchToServerMode(footerPanel, footerLabel);
		else switchToClientMode(footerPanel, footerLabel);
	}

	private void switchToClientMode(JPanel footerPanel, JLabel footerLabel) {
		footerLabel.setIcon(ImageFileUtil.getIcon("sprites/footer_normalplay_bg.jpg"));
		serverPanel.setVisible(false);
		versionPanel.setVisible(true);
		createPlayPanel(footerPanel);
		// TODO don't reset version dropdown
	}

	private void switchToServerMode(JPanel footerPanel, JLabel footerLabel) {
		footerLabel.setIcon(ImageFileUtil.getIcon("sprites/footer_dedicated_bg.jpg"));
		serverPanel.setVisible(true);
		versionPanel.setVisible(false);
		playPanelButtons.removeAll();
		versionPanel.removeAll();
		createServerPanel(footerPanel);
	}

	private JPanel createPlayButtonsPanel(boolean repair) {
		JPanel playPanelButtons = new JPanel();
		playPanelButtons.setDoubleBuffered(true);
		playPanelButtons.setOpaque(false);
		playPanelButtons.setLayout(new BorderLayout());

		JPanel playPanelButtonsSub = new JPanel();
		playPanelButtonsSub.setDoubleBuffered(true);
		playPanelButtonsSub.setOpaque(false);
		playPanelButtonsSub.setLayout(new FlowLayout(FlowLayout.RIGHT));
		playPanelButtons.add(playPanelButtonsSub, BorderLayout.SOUTH);

		if((repair || !gameJarExists(LaunchSettings.getInstallDir()) || gameVersion == null || !Objects.equals(gameVersion.version, selectedVersion)) && !debugMode) {
			updateButton = createUpdateButton();
			playPanelButtonsSub.add(updateButton);
		} else {
			JButton playButton = createPlayButton();
			playPanelButtonsSub.add(playButton);
		}
		return playPanelButtons;
	}

	private JButton createPlayButton() {
		JButton playButton = new JButton(ImageFileUtil.getIcon("sprites/launch_btn.png")); //Todo: Reduce button glow so this doesn't look weird
		playButton.setDoubleBuffered(true);
		playButton.setOpaque(false);
		playButton.setContentAreaFilled(false);
		playButton.setBorderPainted(false);
		playButton.addActionListener(e -> {
			dispose();
			LaunchSettings.setLastUsedVersion(gameVersion.version);
			LaunchSettings.saveSettings();
			try {
				if(usingOldVersion()) downloadJRE(JavaVersion.JAVA_8);
				else downloadJRE(JavaVersion.JAVA_18);
			} catch(Exception exception) {
				exception.printStackTrace();
				(new ErrorDialog("Error", "Failed to unzip java, manual installation required", exception)).setVisible(true);
				return;
			}
			runStarMade(serverMode);
			System.exit(0);
		});
		playButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				playButton.setIcon(ImageFileUtil.getIcon("sprites/launch_roll.png"));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				playButton.setIcon(ImageFileUtil.getIcon("sprites/launch_btn.png"));
			}
		});
		return playButton;
	}

	private JButton createUpdateButton() {
		JButton updateButton = new JButton(ImageFileUtil.getIcon("sprites/update_btn.png"));
		updateButton.setDoubleBuffered(true);
		updateButton.setOpaque(false);
		updateButton.setContentAreaFilled(false);
		updateButton.setBorderPainted(false);
		updateButton.addActionListener(e -> {
			IndexFileEntry version = versionRegistry.searchForVersion(lastUsedBranch, v -> v.version.equals(selectedVersion));
			System.out.println("selected version " + version);
			if(version != null) {
				if(updaterThread == null || !updaterThread.updating) updateGame(version);
			} else {
				JOptionPane.showMessageDialog(null, "The Launcher needs to be online to do this!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		updateButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if(updaterThread == null || !updaterThread.updating)
					updateButton.setIcon(ImageFileUtil.getIcon("sprites/update_roll.png"));
				else updateButton.setToolTipText(dlStatus.toString());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if(updaterThread == null || !updaterThread.updating)
					updateButton.setIcon(ImageFileUtil.getIcon("sprites/update_btn.png"));
				else updateButton.setToolTipText(dlStatus.toString());
			}
		});
		return updateButton;
	}

	private void updatePlayPanelButtons(JPanel panel, boolean repair) {
		// Called when switching client/server, and selecting branch/version
		if(playPanelButtons != null) {
			panel.remove(playPanelButtons);
		}

		playPanelButtons = createPlayButtonsPanel(repair);
		panel.add(playPanelButtons, BorderLayout.EAST);
		panel.revalidate();
		panel.repaint();
	}

	// Panel Methods

	private void createPlayPanel(JPanel footerPanel) {
		clearPanel(playPanel);
		serverMode = false;
		playPanel = createPlayVersionPanel(footerPanel, false);
		footerPanel.add(playPanel);
		footerPanel.revalidate();
		footerPanel.repaint();
	}

	private void createServerPanel(JPanel footerPanel) {
		clearPanel(serverPanel);
		serverMode = true;
		serverPanel = createPlayVersionPanel(footerPanel, true);
		footerPanel.add(serverPanel);
		footerPanel.revalidate();
		footerPanel.repaint();
	}

	private static void clearPanel(JPanel panel) {
		if(panel != null) {
			panel.removeAll();
			panel.revalidate();
			panel.repaint();
		}
	}

	private JPanel createPlayVersionPanel(JPanel footerPanel, boolean serverMode) {
		JPanel panel = new JPanel();
		panel.setDoubleBuffered(true);
		panel.setOpaque(false);
		panel.setLayout(new BorderLayout());

		versionPanel = createVersionSelectPanel(serverMode);
		footerPanel.add(versionPanel, BorderLayout.WEST);

		playPanelButtons = createPlayButtonsPanel(false);
		panel.add(playPanelButtons, BorderLayout.EAST);
		return panel;
	}

	private JPanel createVersionSelectPanel(boolean serverMode) {
		JPanel versionPanel = new JPanel();
		versionPanel.setDoubleBuffered(true);
		versionPanel.setOpaque(false);
		versionPanel.setLayout(new BorderLayout());

		JPanel versionSubPanel = new JPanel();
		versionSubPanel.setDoubleBuffered(true);
		versionSubPanel.setOpaque(false);
		versionSubPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		versionPanel.add(versionSubPanel, BorderLayout.SOUTH);

		//Change color of arrow
		UIDefaults defaults = new UIDefaults();
		defaults.put("ComboBox:\"ComboBox.arrowButton\"[Enabled].backgroundPainter", Palette.buttonColor);

		//Branch dropdown
		JComboBox<String> branchDropdown = createBranchDropdown(lastUsedBranch.index, defaults);
		versionSubPanel.add(branchDropdown);

		//Version dropdown
		JComboBox<String> versionDropdown = new DropdownMenu(defaults);
		versionSubPanel.add(versionDropdown);

		branchDropdown.addItemListener(e -> onSelectBranch(branchDropdown, versionDropdown));

		versionDropdown.addItemListener(e -> onSelectVersion(versionDropdown));
		setDropdownVersionsList(versionDropdown, lastUsedBranch, versionRegistry);
		selectLastUsedVersion(versionDropdown);

		//Port field
		if(serverMode) {
			if(portField == null) portField = new PortField("4242");
			else portField.setVisible(true);
			versionSubPanel.add(portField);
		} else {
			if(portField != null) {
				portField.setVisible(false);
				versionSubPanel.remove(portField);
			}
		}
		return versionPanel;
	}

	private JComboBox<String> createBranchDropdown(int startIndex, UIDefaults defaults) {
		JComboBox<String> branchDropdown = new DropdownMenu(defaults);
		branchDropdown.addItem("Release");
		branchDropdown.addItem("Dev");
		branchDropdown.addItem("Pre-Release");
		branchDropdown.setSelectedIndex(startIndex);
		return branchDropdown;
	}

	private void onSelectBranch(JComboBox<String> branchDropdown, JComboBox<String> versionDropdown) {
		//Change settings
		int branchIndex = branchDropdown.getSelectedIndex();
		setBranch(GameBranch.getForIndex(branchIndex));
		LaunchSettings.saveSettings();

		//Update UI components
		GameBranch branch = GameBranch.getForIndex(branchDropdown.getSelectedIndex());
		setDropdownVersionsList(versionDropdown, branch, versionRegistry);
		updatePlayPanelButtons(playPanel, false);
	}

	private void onSelectVersion(JComboBox<String> versionDropdown) {
		if(versionDropdown.getSelectedIndex() == -1) return;
		selectedVersion = versionDropdown.getItemAt(versionDropdown.getSelectedIndex()).split(" ")[0];
		LaunchSettings.setLastUsedVersion(selectedVersion);
		LaunchSettings.saveSettings();
		if(playPanel != null) updatePlayPanelButtons(playPanel, false);
	}

	// TODO maybe save and don't re-add every time
	private static void setDropdownVersionsList(JComboBox<String> versionDropdown, GameBranch branch, VersionRegistry versionRegistry) {
		List<IndexFileEntry> versions = versionRegistry.getVersions(branch);
		if(versions == null) return;

		// Add versions to dropdown
		versionDropdown.removeAllItems();
		for(IndexFileEntry version : versions) {
			if(version.equals(versions.get(0))) versionDropdown.addItem(version.version + " (Latest)");
			else versionDropdown.addItem(version.version);
		}
	}

	private static void selectLastUsedVersion(JComboBox<String> versionDropdown) {
		String lastUsedVersion = LaunchSettings.getLastUsedVersion();
		if(lastUsedVersion.isEmpty()) lastUsedVersion = "NONE";
		//Select last used version in dropdown if it exists
		for(int i = 0; i < versionDropdown.getItemCount(); i++) {
			if(versionDropdown.getItemAt(i).equals(lastUsedVersion)) {
				versionDropdown.setSelectedIndex(i);
				break;
			}
		}
	}

}