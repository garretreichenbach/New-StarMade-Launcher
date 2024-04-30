package smlauncher;

import com.formdev.flatlaf.FlatDarkLaf;
import smlauncher.community.LauncherCommunityPanel;
import smlauncher.downloader.JavaDownloader;
import smlauncher.downloader.JavaVersion;
import smlauncher.fileio.ImageFileUtil;
import smlauncher.fileio.TextFileUtil;
import smlauncher.mainui.*;
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
import java.net.URI;
import java.net.URISyntaxException;
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
	private int mouseX;
	private int mouseY;
	private JButton updateButton;
	private JTextField portField;
	private JPanel mainPanel;
	private JPanel centerPanel;
	private JPanel footerPanel;
	private JPanel versionPanel;
	private JPanel playPanel;
	private JPanel serverPanel;
	private JPanel playPanelButtons;
	private JScrollPane centerScrollPane;
	private LauncherNewsPanel newsPanel;
//	private LauncherForumsPanel forumsPanel;
//	private LauncherContentPanel contentPanel;
	private LauncherCommunityPanel communityPanel;

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
		createMainPanel();
		createNewsPanel();
		dispose();
		setUndecorated(true);
		setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
		setResizable(false);
		getRootPane().setDoubleBuffered(true);
		setVisible(true);
	}

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
					switch(arg) {
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
			while(frame.isVisible()) {
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

	// Main Panel Methods
	private void createMainPanel() {
		mainPanel = new JPanel();
		mainPanel.setDoubleBuffered(true);
		mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		setContentPane(mainPanel);
		mainPanel.setLayout(new BorderLayout(0, 0));

		//Drag and control the window with the top panel
		JPanel topPanel = createTopPanel();
		mainPanel.add(topPanel, BorderLayout.NORTH);

		//Select the center panel with the left panel
		JPanel leftPanel = createLeftPanel();
		mainPanel.add(leftPanel, BorderLayout.WEST);

		//Update and play the game with the footer panel
		footerPanel = new JPanel();
		footerPanel.setDoubleBuffered(true);
		footerPanel.setOpaque(false);
		footerPanel.setLayout(new StackLayout());
		mainPanel.add(footerPanel, BorderLayout.SOUTH);

		JLabel footerLabel = new JLabel();
		footerLabel.setDoubleBuffered(true);
		footerLabel.setIcon(ImageFileUtil.getIcon("sprites/footer_normalplay_bg.jpg"));
		footerPanel.add(footerLabel);

		JButton normalPlayButton = new JButton("Play");
		normalPlayButton.setFont(new Font("Roboto", Font.BOLD, 12));
		normalPlayButton.setDoubleBuffered(true);
		normalPlayButton.setOpaque(false);
		normalPlayButton.setContentAreaFilled(false);
		normalPlayButton.setBorderPainted(false);
		normalPlayButton.setForeground(Palette.textColor);

		JButton dedicatedServerButton = new JButton("Dedicated Server");
		dedicatedServerButton.setFont(new Font("Roboto", Font.BOLD, 12));
		dedicatedServerButton.setDoubleBuffered(true);
		dedicatedServerButton.setOpaque(false);
		dedicatedServerButton.setContentAreaFilled(false);
		dedicatedServerButton.setBorderPainted(false);
		dedicatedServerButton.setForeground(Palette.textColor);

		JPanel footerPanelButtons = new JPanel();
		footerPanelButtons.setDoubleBuffered(true);
		footerPanelButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
		footerPanelButtons.setOpaque(false);
		footerPanelButtons.add(Box.createRigidArea(new Dimension(10, 0)));
		footerPanelButtons.add(normalPlayButton);
		footerPanelButtons.add(Box.createRigidArea(new Dimension(30, 0)));
		footerPanelButtons.add(dedicatedServerButton);
		footerLabel.add(footerPanelButtons);
		footerPanelButtons.setBounds(0, 0, 800, 30);

		if(getLastUsedVersion() == null) selectedVersion = null;
		else selectedVersion = gameVersion.version;
		createPlayPanel(footerPanel);
		createServerPanel(footerPanel);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setDoubleBuffered(true);
		bottomPanel.setOpaque(false);
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		footerPanel.add(bottomPanel, BorderLayout.SOUTH);

		JButton launchSettings = new SettingsDialogButton(
				"Launch Settings",
				ImageFileUtil.getIcon("sprites/memory_options_gear.png"),
				e -> {
					JDialog settingsDialog = new LaunchSettingsDialog("Launch Settings", 500, 350);
					settingsDialog.setVisible(true);
				}
		);
		bottomPanel.add(launchSettings);

		JButton installSettings = new SettingsDialogButton(
				"Installation Settings",
				ImageFileUtil.getIcon("sprites/launch_options_gear.png"),
				e -> {
					createInstallSettingsDialog();
				}
		bottomPanel.add(installSettings);

		if(serverPanel != null) serverPanel.setVisible(false);
		versionPanel.setVisible(true);

		normalPlayButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				normalPlayButton.setForeground(Palette.selectedColor);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				normalPlayButton.setForeground(Palette.textColor);
			}
		});
		normalPlayButton.addActionListener(e -> switchToClientMode(footerLabel));

		dedicatedServerButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				dedicatedServerButton.setForeground(Palette.selectedColor);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				dedicatedServerButton.setForeground(Palette.textColor);
			}
		});
		dedicatedServerButton.addActionListener(e -> {
			if(updaterThread == null || !updaterThread.updating) { //Don't allow this while the game is updating
				switchToServerMode(footerLabel);
			}
		});

		centerPanel = new JPanel();
		centerPanel.setDoubleBuffered(true);
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BorderLayout());
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		JLabel background = new JLabel();
		background.setDoubleBuffered(true);
		background.setIcon(ImageFileUtil.getIcon("sprites/left_panel.png", 800, 500));
		centerPanel.add(background, BorderLayout.CENTER);

		switchToClientMode(footerLabel); // make sure right components are visible
	}

	private void createInstallSettingsDialog() {
		final String[] tempInstallDir = {null};

		final JDialog[] dialog = {new JDialog()};
		dialog[0].setModal(true);
		dialog[0].setResizable(false);
		dialog[0].setTitle("Installation Settings");
		dialog[0].setSize(450, 150);
		dialog[0].setLocationRelativeTo(null);
		dialog[0].setLayout(new BorderLayout());
		dialog[0].setAlwaysOnTop(true);
		dialog[0].setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dialogPanel = new JPanel();
		dialogPanel.setDoubleBuffered(true);
		dialogPanel.setOpaque(false);
		dialog[0].add(dialogPanel, BorderLayout.CENTER);

		JPanel installLabelPanel = new JPanel();
		installLabelPanel.setDoubleBuffered(true);
		installLabelPanel.setOpaque(false);
		installLabelPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		dialogPanel.add(installLabelPanel);
		JLabel installLabel = new JLabel("Install Directory: ");
		installLabel.setDoubleBuffered(true);
		installLabel.setOpaque(false);
		installLabel.setFont(new Font("Roboto", Font.BOLD, 12));
		installLabelPanel.add(installLabel);

		JTextField installLabelPath = new JTextField(LaunchSettings.getInstallDir());
		installLabelPath.setDoubleBuffered(true);
		installLabelPath.setOpaque(false);
		installLabelPath.setFont(new Font("Roboto", Font.PLAIN, 12));
		installLabelPath.setMinimumSize(new Dimension(200, 20));
		installLabelPath.setPreferredSize(new Dimension(200, 20));
		installLabelPath.setMaximumSize(new Dimension(200, 20));
		installLabelPanel.add(installLabelPath);
		installLabelPath.addActionListener(e1 -> {
			String path = installLabelPath.getText();
			if(path == null || path.isEmpty()) return;
			File file = new File(path);
			if(!file.exists()) return;
			if(!file.isDirectory()) file = file.getParentFile();
			tempInstallDir[0] = file.getAbsolutePath();
			installLabelPath.setText(tempInstallDir[0]);
		});

		JButton installButton = new JButton("Change");
		installButton.setIcon(UIManager.getIcon("FileView.directoryIcon"));
		installButton.setDoubleBuffered(true);
		installButton.setOpaque(false);
		installButton.setContentAreaFilled(false);
		installButton.setBorderPainted(false);
		installButton.setFont(new Font("Roboto", Font.BOLD, 12));
		dialogPanel.add(installButton);
		installButton.addActionListener(e1 -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fileChooser.showOpenDialog(dialog[0]);
			if(result == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				if(!file.isDirectory()) file = file.getParentFile();
				tempInstallDir[0] = file.getAbsolutePath();
				installLabelPath.setText(tempInstallDir[0]);
			}
		});

		JButton repairButton = new JButton("Repair");
		repairButton.setIcon(UIManager.getIcon("FileView.checkIcon"));
		repairButton.setDoubleBuffered(true);
		repairButton.setOpaque(false);
		repairButton.setFont(new Font("Roboto", Font.BOLD, 12));
		dialogPanel.add(repairButton);
		repairButton.addActionListener(e1 -> {
			IndexFileEntry version = getLatestVersion(lastUsedBranch);
			if(version != null) {
				if(updaterThread == null || !updaterThread.updating) {
					dialog[0].dispose();
					recreateButtons(playPanel, true);
					updateGame(version);
				}
			} else JOptionPane.showMessageDialog(dialog[0], "The Launcher needs to be online to do this!", "Error", JOptionPane.ERROR_MESSAGE);
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setDoubleBuffered(true);
		buttonPanel.setOpaque(false);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		dialog[0].add(buttonPanel, BorderLayout.SOUTH);

		JButton saveButton = new JButton("Save");
		saveButton.setFont(new Font("Roboto", Font.BOLD, 12));
		saveButton.setDoubleBuffered(true);
		buttonPanel.add(saveButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setFont(new Font("Roboto", Font.BOLD, 12));
		cancelButton.setDoubleBuffered(true);
		buttonPanel.add(cancelButton);
		saveButton.addActionListener(e1 -> {
			String installDir = tempInstallDir[0];
			if(installDir != null) {
				LaunchSettings.setInstallDir(installDir);
				LaunchSettings.saveSettings();
			}
			dialog[0].dispose();
		});
		cancelButton.addActionListener(e1 -> dialog[0].dispose());
		dialog[0].setVisible(true);
	}

	private JPanel createTopPanel() {
		JPanel topPanel = new WindowDragPanel(
				ImageFileUtil.getIcon("sprites/header_top.png"),
				new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						mouseX = e.getX();
						mouseY = e.getY();
						//If the mouse is on the top panel buttons, don't drag the window
						if(mouseX > 770 || mouseY > 30) {
							mouseX = 0;
							mouseY = 0;
						}
						super.mousePressed(e);
					}
				},
				new MouseMotionAdapter() {
					@Override
					public void mouseDragged(MouseEvent e) {
						if(mouseX != 0 && mouseY != 0) {
							setLocation(getLocation().x + e.getX() - mouseX, getLocation().y + e.getY() - mouseY);
						}
						super.mouseDragged(e);
					}
				}
		);

		//Buttons to minimize/close window
		JPanel windowControlsPanel = new WindowControlsPanel(
				ImageFileUtil.getIcon("sprites/minimize_icon.png"),
				e -> setState(Frame.ICONIFIED),
				ImageFileUtil.getIcon("sprites/close_icon.png"),
				e -> {
					dispose();
					System.exit(0);
				}
		);

		//Was previously topLabel.add(windowControlsPanel)
		//Changing it doesn't seem to make a difference
		topPanel.add(windowControlsPanel);

		// Display Schine logo
		JPanel topRightPanel = new JPanel();
		topRightPanel.setDoubleBuffered(true);
		topRightPanel.setOpaque(false);
		topRightPanel.setLayout(new BorderLayout());
		topPanel.add(topRightPanel, BorderLayout.EAST);

		JLabel logoLabel = new JLabel();
		logoLabel.setDoubleBuffered(true);
		logoLabel.setOpaque(false);
		logoLabel.setIcon(ImageFileUtil.getIcon("sprites/launcher_schine_logo.png"));
		topRightPanel.add(logoLabel, BorderLayout.EAST);
		return topPanel;
	}

	private JPanel createLeftPanel() {
		JPanel leftPanel = new JPanel();
		leftPanel.setDoubleBuffered(true);
		leftPanel.setOpaque(false);
		leftPanel.setLayout(new StackLayout());

		JLabel leftLabel = new JLabel();
		leftLabel.setDoubleBuffered(true);
		//Resize the image to the left panel
		leftLabel.setIcon(ImageFileUtil.getIcon("sprites/left_panel.png", 150, 500));
		//Stretch the image to the left panel
		leftPanel.add(leftLabel, StackLayout.BOTTOM);

		JPanel topLeftPanel = new JPanel();
		topLeftPanel.setDoubleBuffered(true);
		topLeftPanel.setOpaque(false);
		topLeftPanel.setLayout(new BorderLayout());
		leftPanel.add(topLeftPanel, StackLayout.TOP);

		//Add list to display links to game website
		JList<JLabel> list = createPanelSelectList();
		topLeftPanel.add(list);

		//Display game logo
		JPanel topLeftLogoPanel = new JPanel();
		topLeftLogoPanel.setDoubleBuffered(true);
		topLeftLogoPanel.setOpaque(false);
		topLeftLogoPanel.setLayout(new BorderLayout());
		topLeftPanel.add(topLeftLogoPanel, BorderLayout.NORTH);

		//Add a left inset
		JPanel leftInset = new JPanel();
		leftInset.setDoubleBuffered(true);
		leftInset.setOpaque(false);
		topLeftLogoPanel.add(leftInset, BorderLayout.CENTER);

		//Add logo at top left
		JLabel logo = new JLabel();
		logo.setDoubleBuffered(true);
		logo.setOpaque(false);
		logo.setIcon(ImageFileUtil.getIcon("sprites/logo.png"));
		leftInset.add(logo);
		return leftPanel;
	}

	private JList<JLabel> createPanelSelectList() {
		JList<JLabel> list = new PanelSelectList(
				new String[] {"NEWS", "FORUMS", "CONTENT", "COMMUNITY"}
		);
		//Select panels on click
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 1) {
					int index = list.locationToIndex(e.getPoint());
					if(index != -1) {
						switch(index) {
							case 0:
								createNewsPanel();
								break;
							case 1:
								createForumsPanel();
								break;
							case 2:
								createContentPanel();
								break;
							case 3:
								createCommunityPanel();
								break;
						}
					}
				}
			}
		});
		return list;
	}

	private void switchToClientMode(JLabel footerLabel) {
		footerLabel.setIcon(ImageFileUtil.getIcon("sprites/footer_normalplay_bg.jpg"));
		serverPanel.setVisible(false);
		versionPanel.setVisible(true);
		createPlayPanel(footerPanel);
	}

	// Panel Methods

	private void switchToServerMode(JLabel footerLabel) {
		footerLabel.setIcon(ImageFileUtil.getIcon("sprites/footer_dedicated_bg.jpg"));
		versionPanel.setVisible(false);
		playPanelButtons.removeAll();
		versionPanel.removeAll();
		createServerPanel(footerPanel);
		serverPanel.setVisible(true);
	}

	private void recreateButtons(JPanel playPanel, boolean repair) {
		if(playPanelButtons != null) {
			playPanelButtons.removeAll();
			playPanel.remove(playPanelButtons);
		}
		playPanelButtons = new JPanel();
		playPanelButtons.setDoubleBuffered(true);
		playPanelButtons.setOpaque(false);
		playPanelButtons.setLayout(new BorderLayout());
		playPanel.remove(playPanelButtons);
		playPanel.add(playPanelButtons, BorderLayout.EAST);
		JPanel playPanelButtonsSub = new JPanel();
		playPanelButtonsSub.setDoubleBuffered(true);
		playPanelButtonsSub.setOpaque(false);
		playPanelButtonsSub.setLayout(new FlowLayout(FlowLayout.RIGHT));
		playPanelButtons.add(playPanelButtonsSub, BorderLayout.SOUTH);

		if((repair || !gameJarExists(LaunchSettings.getInstallDir()) || gameVersion == null || !Objects.equals(gameVersion.version, selectedVersion)) && !debugMode) {
			updateButton = new JButton(ImageFileUtil.getIcon("sprites/update_btn.png"));
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
					if(updaterThread == null || !updaterThread.updating) updateButton.setIcon(ImageFileUtil.getIcon("sprites/update_roll.png"));
					else updateButton.setToolTipText(dlStatus.toString());
				}

				@Override
				public void mouseExited(MouseEvent e) {
					if(updaterThread == null || !updaterThread.updating) updateButton.setIcon(ImageFileUtil.getIcon("sprites/update_btn.png"));
					else updateButton.setToolTipText(dlStatus.toString());
				}
			});
			playPanelButtonsSub.add(updateButton);
		} else {
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
			playPanelButtonsSub.add(playButton);
		}
		playPanel.revalidate();
		playPanel.repaint();
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

	private String getJavaPath(JavaVersion version) {
		return LaunchSettings.getInstallDir() + "/" + String.format(currentOS.javaPath, version.number);
	}

	// Panel Methods

	private void createPlayPanel(JPanel footerPanel) {
		clearPanel(playPanel);
		serverMode = false;
		playPanel = createPanel(footerPanel, false);
	}

	private void createServerPanel(JPanel footerPanel) {
		clearPanel(serverPanel);
		serverMode = true;
		serverPanel = createPanel(footerPanel, true);
	}

	private JPanel createPanel(JPanel footerPanel, boolean serverMode) {
		JPanel panel = new JPanel();
		panel.setDoubleBuffered(true);
		panel.setOpaque(false);
		panel.setLayout(new BorderLayout());
		footerPanel.add(panel);

		versionPanel = createVersionPanel(serverMode);
		footerPanel.add(versionPanel, BorderLayout.WEST);

		recreateButtons(panel, false);

		footerPanel.revalidate();
		footerPanel.repaint();
		return panel;
	}

	private static void clearPanel(JPanel panel) {
		if (panel != null) {
			panel.removeAll();
			panel.revalidate();
			panel.repaint();
		}
	}

	private JPanel createVersionPanel(boolean serverMode) {
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
			if (portField == null) portField = new PortField("4242");
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
		recreateButtons(playPanel, false);
	}

	private void onSelectVersion(JComboBox<String> versionDropdown) {
		if(versionDropdown.getSelectedIndex() == -1) return;
		selectedVersion = versionDropdown.getItemAt(versionDropdown.getSelectedIndex()).split(" ")[0];
		LaunchSettings.setLastUsedVersion(selectedVersion);
		LaunchSettings.saveSettings();
		if(playPanel != null) recreateButtons(playPanel, false);
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
						recreateButtons(playPanel, false);
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

	private void createScroller(JPanel currentPanel) {
		//Display selected content in the scroll pane
		if(centerScrollPane == null) {
			centerScrollPane = new JScrollPane(currentPanel);
			centerScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			centerScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
			centerScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			centerScrollPane.getVerticalScrollBar().setUnitIncrement(16);
			centerPanel.add(centerScrollPane, BorderLayout.CENTER);
		}
		centerScrollPane.setViewportView(currentPanel);
	}

	private void createNewsPanel() {
		if(newsPanel == null) newsPanel = new LauncherNewsPanel();
		createScroller(newsPanel);
		newsPanel.updatePanel();
		SwingUtilities.invokeLater(() -> {
			JScrollBar vertical = centerScrollPane.getVerticalScrollBar();
			vertical.setValue(vertical.getMinimum());
		});
	}

	private void createForumsPanel() {
		if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			String ccURL = "https://starmadedock.net/forums/";
			try {
				Desktop.getDesktop().browse(new URI(ccURL));
			} catch(IOException | URISyntaxException exception) {
				exception.printStackTrace();
			}
		}
		/* Todo: Create forums panel
		forumsPanel = new LauncherForumsPanel();
		createScroller(forumsPanel);
		forumsPanel.updatePanel();
		SwingUtilities.invokeLater(() -> {
			JScrollBar vertical = centerScrollPane.getVerticalScrollBar();
			vertical.setValue(vertical.getMinimum());
		});
		 */
	}

	private void createContentPanel() {
		if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			String ccURL = "https://starmadedock.net/content/";
			try {
				Desktop.getDesktop().browse(new URI(ccURL));
			} catch(IOException | URISyntaxException exception) {
				exception.printStackTrace();
			}
		}
		/* Todo: Create content panel
		contentPanel = new LauncherContentPanel();
		createScroller(contentPanel);
		contentPanel.updatePanel();
		SwingUtilities.invokeLater(() -> {
			JScrollBar vertical = centerScrollPane.getVerticalScrollBar();
			vertical.setValue(vertical.getMinimum());
		});
		 */
	}

	private void createCommunityPanel() {
		communityPanel = new LauncherCommunityPanel();
		createScroller(communityPanel);
		communityPanel.updatePanel();
		SwingUtilities.invokeLater(() -> {
			JScrollBar vertical = centerScrollPane.getVerticalScrollBar();
			vertical.setValue(vertical.getMinimum());
		});
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

}