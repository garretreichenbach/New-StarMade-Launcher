package smlauncher;

import com.formdev.flatlaf.FlatDarkLaf;
import smlauncher.community.LauncherCommunityPanel;
import smlauncher.downloader.JavaDownloader;
import smlauncher.downloader.JavaVersion;
import smlauncher.fileio.TextFileUtil;
import smlauncher.news.LauncherNewsPanel;
import smlauncher.starmade.*;
import smlauncher.util.OperatingSystem;
import smlauncher.util.Palette;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Main class for the StarMade Launcher.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class StarMadeLauncher extends JFrame {

	public static final String BUG_REPORT_URL = "https://github.com/garretreichenbach/New-StarMade-Launcher/issues";
	public static final String LAUNCHER_VERSION = "3.0.11";
	private static final String[] J18ARGS = {
			"--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED",
			"--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
			"--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED",
			"--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
			"--add-opens=jdk.compiler/com.sun.tools.javac=ALL-UNNAMED",
			"--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
			"--add-opens=java.base/java.lang=ALL-UNNAMED",
			"--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
			"--add-opens=java.base/java.io=ALL-UNNAMED",
			"--add-opens=java.base/java.util=ALL-UNNAMED"
	};

	private final OperatingSystem currentOS;
	private static IndexFileEntry gameVersion;
	private static GameBranch lastUsedBranch = GameBranch.RELEASE;
	private static boolean debugMode;
	private static boolean useSteam;
	private static String selectedVersion;
	private final VersionRegistry versionRegistry;
	private final DownloadStatus dlStatus = new DownloadStatus();
	private UpdaterThread updaterThread;
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
	private static boolean serverMode;
	private static int port;

	public StarMadeLauncher() {
		// Set window properties
		super("StarMade Launcher [" + LAUNCHER_VERSION + "]");
		setBounds(100, 100, 800, 550);
		setMinimumSize(new Dimension(800, 550));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		// Set window icon
		try {
			URL resource = StarMadeLauncher.class.getResource("/sprites/icon.png");
			if (resource != null) setIconImage(Toolkit.getDefaultToolkit().getImage(resource));
		} catch (Exception exception) {
			System.out.println("Could not set window icon");
		}

		// Fetch game versions
		versionRegistry = new VersionRegistry();
		try {
			versionRegistry.createRegistry();
		} catch (Exception exception) {
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
//		deleteUpdaterJar();

		// Get the current OS
		currentOS = OperatingSystem.getCurrent();

		// Download JREs
		try {
			downloadJRE(JavaVersion.JAVA_8);
			downloadJRE(JavaVersion.JAVA_18);
		} catch (Exception exception) {
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

	private void downloadJRE(JavaVersion version) throws Exception {
		if (new File(getJavaPath(version)).exists()) return;
		new JavaDownloader(version).downloadAndUnzip();
	}

	private static void deleteUpdaterJar() {
		File updaterJar = new File("Updater.jar");
		if (updaterJar.exists()) updaterJar.delete();
	}

	public static void main(String[] args) {
		boolean headless = false;
		int backupMode = Updater.BACK_DB;
		boolean selectVersion = false;

		if (args == null || args.length == 0) startup();
		else {
			GameBranch buildBranch = GameBranch.RELEASE;
			for (String arg : args) {
				arg = arg.toLowerCase();
				if (arg.equals("-debug_mode")) debugMode = true;
				if (arg.contains("-version")) {
					selectVersion = true;
					if (arg.contains("-dev")) buildBranch = GameBranch.DEV;
					else if (arg.contains("-pre")) buildBranch = GameBranch.PRE;
					else buildBranch = GameBranch.RELEASE;
				} else if ("-no_gui".equals(arg) || "-nogui".equals(arg)) {
					if (GraphicsEnvironment.isHeadless()) {
						displayHelp();
						System.out.println("Please use the '-nogui' parameter to run the launcher in text mode!");
						return;
					} else headless = true;
				}
				if (headless) {
					switch (arg) {
						case "-h":
						case "-help":
							displayHelp();
							return;
						case "-backup":
						case "-backup_all":
							backupMode = Updater.BACK_ALL;
							break;
						case "-no_backup":
							backupMode = Updater.BACK_NONE;
							break;
						case "-server":
							serverMode = true;
							break;
					}
					if (arg.startsWith("-port:")) {
						try {
							port = Integer.parseInt(arg.substring(6));
						} catch (NumberFormatException ignored) {
						}
					}
					Updater.withoutGUI((args.length > 1 && "-force".equals(args[1])),
							LaunchSettings.getInstallDir(), buildBranch, backupMode, selectVersion);
				} else startup();
				startup();
			}
		}
	}

	private static void startup() {
		EventQueue.invokeLater(() -> {
			try {
				FlatDarkLaf.setup();
				if (LauncherUpdater.checkForUpdate()) {
					System.err.println("Launcher version doesn't match latest version, so an update must be available.");
					JDialog updateDialog = createLauncherUpdateDialog();
					updateDialog.setVisible(true);
				} else startLauncherFrame();
			} catch (Exception e) {
				System.out.println("Error occurred while running launcher");
			}
		});
	}

	private static JDialog createLauncherUpdateDialog() {
		JDialog dialog = new JDialog();
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setModal(true);
		dialog.setResizable(false);
		dialog.setTitle("Launcher Update Available");
		dialog.setSize(500, 350);
		dialog.setLocationRelativeTo(null);
		dialog.setLayout(new BorderLayout());
		dialog.setAlwaysOnTop(true);
		dialog.setLayout(new BorderLayout());

		JPanel descPanel = new JPanel();
		descPanel.setDoubleBuffered(true);
		descPanel.setOpaque(true);
		descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.Y_AXIS));
		dialog.add(descPanel);

		JLabel descLabel = new JLabel("A new launcher update is available, please update to continue.");
		descLabel.setDoubleBuffered(true);
		descLabel.setOpaque(true);
		descLabel.setFont(new Font("Roboto", Font.BOLD, 16));
		descPanel.add(descLabel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setDoubleBuffered(true);
		buttonPanel.setOpaque(true);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		JButton updateButton = new JButton("Update");
		updateButton.setDoubleBuffered(true);
		updateButton.setOpaque(true);
		updateButton.setFont(new Font("Roboto", Font.BOLD, 12));
		updateButton.addActionListener(e -> {
			dialog.dispose();
			LauncherUpdater.updateLauncher();
		});
		buttonPanel.add(updateButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setDoubleBuffered(true);
		cancelButton.setOpaque(true);
		cancelButton.setFont(new Font("Roboto", Font.BOLD, 12));
		cancelButton.addActionListener(e -> {
			dialog.dispose();
			startLauncherFrame();
		});
		buttonPanel.add(cancelButton);
		return dialog;
	}

	private static void startLauncherFrame() {
		JFrame frame = new StarMadeLauncher();
		(new Thread(() -> {
			//For steam: keep it repainting so the damn overlays go away
			try {
				Thread.sleep(1200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			while (frame.isVisible()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException exception) {
					exception.printStackTrace();
				}
				EventQueue.invokeLater(frame::repaint);
			}
		})).start();
	}

	public static ImageIcon getIcon(String s) {
		try {
			return new ImageIcon(ImageIO.read(Objects.requireNonNull(StarMadeLauncher.class.getResource("/" + s))));
		} catch (IOException exception) {
			return new ImageIcon();
		}
	}

	public static ImageIcon getIcon(String s, int width, int height) {
		try {
			return new ImageIcon(ImageIO.read(Objects.requireNonNull(StarMadeLauncher.class.getResource("/" + s))).getScaledInstance(width, height, Image.SCALE_SMOOTH));
		} catch (IOException exception) {
			return new ImageIcon();
		}
	}

	public static void displayHelp() {
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
		} catch (Exception ignored) {
			return null;
		}
	}

	private static void removeCurrentUser() {
		try {
			StarMadeCredentials.removeFile();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	private IndexFileEntry getLastUsedVersion() {
		try {
			String version;
			File versionFile = new File(LaunchSettings.getInstallDir(), "version.txt");
			if (versionFile.exists()) {
				version = TextFileUtil.readText(versionFile);
			} else {
				version = LaunchSettings.getLastUsedVersion();
			}
			String shortVersion = version.substring(0, version.indexOf('#'));

			IndexFileEntry entry = versionRegistry.searchForVersion(
					e -> shortVersion.equals(e.version)
			);
			if (entry != null) return entry;
		} catch (Exception e) {
			System.out.println("Could not read game version from file");
		}
		// Return latest release if nothing found
		return versionRegistry.getLatestVersion(GameBranch.RELEASE);
	}

	private void createMainPanel() {
		mainPanel = new JPanel();
		mainPanel.setDoubleBuffered(true);
		mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		setContentPane(mainPanel);
		mainPanel.setLayout(new BorderLayout(0, 0));
		JPanel topPanel = new JPanel();
		topPanel.setDoubleBuffered(true);
		topPanel.setOpaque(false);
		topPanel.setLayout(new StackLayout());
		mainPanel.add(topPanel, BorderLayout.NORTH);
		JLabel topLabel = new JLabel();
		topLabel.setDoubleBuffered(true);
		topLabel.setIcon(getIcon("sprites/header_top.png"));
		topPanel.add(topLabel);
		JPanel topPanelButtons = new JPanel();
		topPanelButtons.setDoubleBuffered(true);
		topPanelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		topPanelButtons.setOpaque(false);
		JButton closeButton = new JButton(null, getIcon("sprites/close_icon.png")); //Todo: Replace these cus they look like shit
		closeButton.setDoubleBuffered(true);
		closeButton.setOpaque(false);
		closeButton.setContentAreaFilled(false);
		closeButton.setBorderPainted(false);
		closeButton.addActionListener(e -> {
			dispose();
			System.exit(0);
		});
		JButton minimizeButton = new JButton(null, getIcon("sprites/minimize_icon.png"));
		minimizeButton.setDoubleBuffered(true);
		minimizeButton.setOpaque(false);
		minimizeButton.setContentAreaFilled(false);
		minimizeButton.setBorderPainted(false);
		minimizeButton.addActionListener(e -> setState(Frame.ICONIFIED));
		topPanelButtons.add(minimizeButton);
		topPanelButtons.add(closeButton);
		topLabel.add(topPanelButtons);
		topPanelButtons.setBounds(0, 0, 800, 30);
		//Use top panel to drag the window
		topPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mouseX = e.getX();
				mouseY = e.getY();
				//If the mouse is on the top panel buttons, don't drag the window
				if (mouseX > 770 || mouseY > 30) {
					mouseX = 0;
					mouseY = 0;
				}
				super.mousePressed(e);
			}
		});
		topPanel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (mouseX != 0 && mouseY != 0)
					setLocation(getLocation().x + e.getX() - mouseX, getLocation().y + e.getY() - mouseY);
				super.mouseDragged(e);
			}
		});
		JPanel leftPanel = new JPanel();
		leftPanel.setDoubleBuffered(true);
		leftPanel.setOpaque(false);
		leftPanel.setLayout(new StackLayout());
		mainPanel.add(leftPanel, BorderLayout.WEST);
		JLabel leftLabel = new JLabel();
		leftLabel.setDoubleBuffered(true);
		try {
			Image image = ImageIO.read(Objects.requireNonNull(StarMadeLauncher.class.getResource("/sprites/left_panel.png")));
			//Resize the image to the left panel
			image = image.getScaledInstance(150, 500, Image.SCALE_SMOOTH);
			leftLabel.setIcon(new ImageIcon(image));
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		//Stretch the image to the left panel
		leftPanel.add(leftLabel, StackLayout.BOTTOM);
		JPanel topLeftPanel = new JPanel();
		topLeftPanel.setDoubleBuffered(true);
		topLeftPanel.setOpaque(false);
		topLeftPanel.setLayout(new BorderLayout());
		leftPanel.add(topLeftPanel, StackLayout.TOP);
		//Add list
		JList<JLabel> list = new JList<>();
		list.setDoubleBuffered(true);
		list.setOpaque(false);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer((list1, value, index, isSelected, cellHasFocus) -> {
			if (isSelected) {
				value.setForeground(Palette.selectedColor);
				value.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Palette.selectedColor));
			} else {
				value.setForeground(Palette.deselectedColor);
				value.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Palette.deselectedColor));
			}
			return value;
		});
		//Highlight on mouse hover
		list.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int index = list.locationToIndex(e.getPoint());
				if (index != -1) list.setSelectedIndex(index);
				else list.clearSelection();
			}
		});
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					int index = list.locationToIndex(e.getPoint());
					if (index != -1) {
						switch (index) {
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
		list.setFixedCellHeight(48);
		DefaultListModel<JLabel> listModel = new DefaultListModel<>();
		listModel.addElement(new JLabel("NEWS"));
		listModel.addElement(new JLabel("FORUMS"));
		listModel.addElement(new JLabel("CONTENT"));
		listModel.addElement(new JLabel("COMMUNITY"));
		for (int i = 0; i < listModel.size(); i++) {
			JLabel label = listModel.get(i);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setFont(new Font("Roboto", Font.BOLD, 18));
			label.setForeground(Palette.selectedColor);
			label.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Palette.selectedColor));
			label.setDoubleBuffered(true);
			label.setOpaque(false);
		}
		list.setModel(listModel);
		topLeftPanel.add(list);
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
		logo.setIcon(getIcon("sprites/logo.png"));
		leftInset.add(logo);
		footerPanel = new JPanel();
		footerPanel.setDoubleBuffered(true);
		footerPanel.setOpaque(false);
		footerPanel.setLayout(new StackLayout());
		mainPanel.add(footerPanel, BorderLayout.SOUTH);
		JLabel footerLabel = new JLabel();
		footerLabel.setDoubleBuffered(true);
		footerLabel.setIcon(getIcon("sprites/footer_normalplay_bg.jpg"));
		footerPanel.add(footerLabel);
		JPanel topRightPanel = new JPanel();
		topRightPanel.setDoubleBuffered(true);
		topRightPanel.setOpaque(false);
		topRightPanel.setLayout(new BorderLayout());
		topPanel.add(topRightPanel, BorderLayout.EAST);
		JLabel logoLabel = new JLabel();
		logoLabel.setDoubleBuffered(true);
		logoLabel.setOpaque(false);
		logoLabel.setIcon(getIcon("sprites/launcher_schine_logo.png"));
		topRightPanel.add(logoLabel, BorderLayout.EAST);
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
		if (getLastUsedVersion() == null) selectedVersion = null;
		else selectedVersion = gameVersion.version;
		createPlayPanel(footerPanel);
		createServerPanel(footerPanel);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setDoubleBuffered(true);
		bottomPanel.setOpaque(false);
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		footerPanel.add(bottomPanel, BorderLayout.SOUTH);
		JButton launchSettings = new JButton("Launch Settings");
		launchSettings.setIcon(getIcon("sprites/memory_options_gear.png"));
		launchSettings.setFont(new Font("Roboto", Font.BOLD, 12));
		launchSettings.setDoubleBuffered(true);
		launchSettings.setOpaque(false);
		launchSettings.setContentAreaFilled(false);
		launchSettings.setForeground(Palette.textColor);
		bottomPanel.add(launchSettings);
		launchSettings.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				launchSettings.setForeground(Palette.selectedColor);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				launchSettings.setForeground(Palette.textColor);
			}
		});
		launchSettings.addActionListener(e -> {
			JDialog dialog = new JDialog();
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setModal(true);
			dialog.setResizable(false);
			dialog.setTitle("Launch Settings");
			dialog.setSize(500, 350);
			dialog.setLocationRelativeTo(null);
			dialog.setLayout(new BorderLayout());
			dialog.setAlwaysOnTop(true);
//			dialog.setBackground(Palette.paneColor);
//			dialog.setForeground(Palette.foregroundColor);
			JPanel dialogPanel = new JPanel();
			dialogPanel.setDoubleBuffered(true);
			dialogPanel.setOpaque(true);
//			dialogPanel.setBackground(Palette.paneColor);
//			dialogPanel.setForeground(Palette.foregroundColor);
			dialogPanel.setLayout(new BorderLayout());
			dialog.add(dialogPanel);
			JPanel northPanel = new JPanel();
			northPanel.setDoubleBuffered(true);
			northPanel.setOpaque(true);
			northPanel.setLayout(new BorderLayout());
//			northPanel.setBackground(Palette.paneColor);
//			northPanel.setForeground(Palette.foregroundColor);
			dialogPanel.add(northPanel, BorderLayout.NORTH);
			JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 2048, getSystemMemory(), LaunchSettings.getMemory());
//			slider.setBackground(Palette.paneColor);
			JLabel sliderLabel = new JLabel("Memory: " + slider.getValue() + " MB");
//			sliderLabel.setBackground(Palette.paneColor);
			sliderLabel.setDoubleBuffered(true);
			sliderLabel.setOpaque(true);
			sliderLabel.setFont(new Font("Roboto", Font.BOLD, 12));
			sliderLabel.setHorizontalAlignment(SwingConstants.CENTER);
			northPanel.add(sliderLabel, BorderLayout.NORTH);
			slider.setDoubleBuffered(true);
			slider.setOpaque(true);
			if (getSystemMemory() > 16384) { //Make sure the slider is not too squished for people that have a really epic gamer pc
				slider.setMajorTickSpacing(2048);
				slider.setMajorTickSpacing(1024);
				slider.setLabelTable(slider.createStandardLabels(4096));
			} else if (getSystemMemory() > 8192) {
				slider.setMajorTickSpacing(1024);
				slider.setMinorTickSpacing(512);
				slider.setLabelTable(slider.createStandardLabels(2048));
			} else {
				slider.setMajorTickSpacing(1024);
				slider.setMinorTickSpacing(256);
				slider.setLabelTable(slider.createStandardLabels(1024));
			}
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			slider.setSnapToTicks(true);
			northPanel.add(slider, BorderLayout.CENTER);
			slider.addChangeListener(e1 -> sliderLabel.setText("Memory: " + slider.getValue() + " MB"));
			JPanel centerPanel = new JPanel();
			centerPanel.setDoubleBuffered(true);
			centerPanel.setOpaque(false);
			centerPanel.setLayout(new BorderLayout());
//			centerPanel.setBackground(Palette.backgroundColor);
//			centerPanel.setForeground(Palette.foregroundColor);
			dialogPanel.add(centerPanel, BorderLayout.CENTER);
			JTextArea launchArgs = new JTextArea();
//			launchArgs.setBackground(Palette.paneColor);
			launchArgs.setDoubleBuffered(true);
			launchArgs.setOpaque(true);
			launchArgs.setText(LaunchSettings.getLaunchArgs());
			launchArgs.setLineWrap(true);
			launchArgs.setWrapStyleWord(true);
			launchArgs.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			centerPanel.add(launchArgs, BorderLayout.CENTER);
			JLabel launchArgsLabel = new JLabel("Launch Arguments");
//			launchArgsLabel.setBackground(Palette.paneColor);
			launchArgsLabel.setDoubleBuffered(true);
			launchArgsLabel.setOpaque(true);
			launchArgsLabel.setFont(new Font("Roboto", Font.BOLD, 12));
			launchArgsLabel.setHorizontalAlignment(SwingConstants.CENTER);
			centerPanel.add(launchArgsLabel, BorderLayout.NORTH);
			JPanel buttonPanel = new JPanel();
			buttonPanel.setDoubleBuffered(true);
			buttonPanel.setOpaque(true);
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
			JButton saveButton = new JButton("Save");
			saveButton.setFont(new Font("Roboto", Font.BOLD, 12));
			saveButton.setDoubleBuffered(true);
			buttonPanel.add(saveButton);
			JButton cancelButton = new JButton("Cancel");
			cancelButton.setFont(new Font("Roboto", Font.BOLD, 12));
			cancelButton.setDoubleBuffered(true);
			buttonPanel.add(cancelButton);
			saveButton.addActionListener(e1 -> {
				LaunchSettings.setMemory(slider.getValue());
				LaunchSettings.setLaunchArgs(launchArgs.getText());
				LaunchSettings.saveSettings();
				dialog.dispose();
			});
			cancelButton.addActionListener(e1 -> dialog.dispose());
			dialog.setVisible(true);
		});
		JButton installSettings = new JButton("Installation Settings");
		installSettings.setIcon(getIcon("sprites/launch_options_gear.png"));
		installSettings.setFont(new Font("Roboto", Font.BOLD, 12));
		installSettings.setDoubleBuffered(true);
		installSettings.setOpaque(false);
		installSettings.setContentAreaFilled(false);
		installSettings.setForeground(Palette.textColor);
		bottomPanel.add(installSettings);
		installSettings.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				installSettings.setForeground(Palette.selectedColor);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				installSettings.setForeground(Palette.textColor);
			}
		});
		installSettings.addActionListener(e -> {
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
				if (path == null || path.isEmpty()) return;
				File file = new File(path);
				if (!file.exists()) return;
				if (!file.isDirectory()) file = file.getParentFile();
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
				if (result == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if (!file.isDirectory()) file = file.getParentFile();
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
				if (version != null) {
					if (updaterThread == null || !updaterThread.updating) {
						dialog[0].dispose();
						recreateButtons(playPanel, true);
						updateGame(version);
					}
				} else
					JOptionPane.showMessageDialog(dialog[0], "The Launcher needs to be online to do this!", "Error", JOptionPane.ERROR_MESSAGE);
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
				if (installDir != null) {
					LaunchSettings.setInstallDir(installDir);
					LaunchSettings.saveSettings();
				}
				dialog[0].dispose();
			});
			cancelButton.addActionListener(e1 -> dialog[0].dispose());
			dialog[0].setVisible(true);
		});
		if (serverPanel != null) serverPanel.setVisible(false);
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
			if (updaterThread == null || !updaterThread.updating) { //Don't allow this while the game is updating
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
		try {
			Image image = ImageIO.read(Objects.requireNonNull(StarMadeLauncher.class.getResource("/sprites/left_panel.png")));
			//Resize the image to the left panel
			image = image.getScaledInstance(800, 500, Image.SCALE_SMOOTH);
			background.setIcon(new ImageIcon(image));
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		centerPanel.add(background, BorderLayout.CENTER);

		switchToClientMode(footerLabel); // make sure right components are visible
	}

	private void switchToClientMode(JLabel footerLabel) {
		System.out.println("switch to client");
		footerLabel.setIcon(getIcon("sprites/footer_normalplay_bg.jpg"));
		serverPanel.setVisible(false);
		versionPanel.setVisible(true);
		createPlayPanel(footerPanel);
	}

	private void switchToServerMode(JLabel footerLabel) {
		System.out.println("switch to server");
		footerLabel.setIcon(getIcon("sprites/footer_dedicated_bg.jpg"));
		versionPanel.setVisible(false);
		playPanelButtons.removeAll();
		versionPanel.removeAll();
		createServerPanel(footerPanel);
		serverPanel.setVisible(true);
	}

	private static void setGameVersion(IndexFileEntry gameVersion) {
		if (gameVersion != null) {
			LaunchSettings.setLastUsedVersion(gameVersion.version);
			if (usingOldVersion()) LaunchSettings.setJvmArgs("--illegal-access=permit");
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

	private int getSystemMemory() {
		com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		return (int) (os.getTotalPhysicalMemorySize() / (1024 * 1024));
	}

	private void recreateButtons(JPanel playPanel, boolean repair) {
		if (playPanelButtons != null) {
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

		if ((repair || !gameJarExists(LaunchSettings.getInstallDir())
				|| gameVersion == null
				|| !Objects.equals(gameVersion.version, selectedVersion)) && !debugMode) {
			updateButton = new JButton(getIcon("sprites/update_btn.png"));
			updateButton.setDoubleBuffered(true);
			updateButton.setOpaque(false);
			updateButton.setContentAreaFilled(false);
			updateButton.setBorderPainted(false);
			updateButton.addActionListener(e -> {
				IndexFileEntry version = versionRegistry.searchForVersion(lastUsedBranch, v -> v.version.equals(selectedVersion));
				System.out.println("selected version " + version);
				if (version != null) {
					if (updaterThread == null || !updaterThread.updating) updateGame(version);
				} else
					JOptionPane.showMessageDialog(null, "The Launcher needs to be online to do this!", "Error", JOptionPane.ERROR_MESSAGE);
			});
			updateButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					if (updaterThread == null || !updaterThread.updating)
						updateButton.setIcon(getIcon("sprites/update_roll.png"));
					else
						updateButton.setToolTipText(dlStatus.toString());
				}

				@Override
				public void mouseExited(MouseEvent e) {
					if (updaterThread == null || !updaterThread.updating)
						updateButton.setIcon(getIcon("sprites/update_btn.png"));
					else updateButton.setToolTipText("");
				}
			});
			playPanelButtonsSub.add(updateButton);
		} else {
			JButton playButton = new JButton(getIcon("sprites/launch_btn.png")); //Todo: Reduce button glow so this doesn't look weird
			playButton.setDoubleBuffered(true);
			playButton.setOpaque(false);
			playButton.setContentAreaFilled(false);
			playButton.setBorderPainted(false);
			playButton.addActionListener(e -> {
				dispose();
				LaunchSettings.setLastUsedVersion(gameVersion.version);
				LaunchSettings.saveSettings();
				try {
					if (usingOldVersion()) downloadJRE(JavaVersion.JAVA_8);
					else downloadJRE(JavaVersion.JAVA_18);
				} catch (Exception exception) {
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
					playButton.setIcon(getIcon("sprites/launch_roll.png"));
				}

				@Override
				public void mouseExited(MouseEvent e) {
					playButton.setIcon(getIcon("sprites/launch_btn.png"));
				}
			});
			playPanelButtonsSub.add(playButton);
		}
		playPanel.revalidate();
		playPanel.repaint();
	}

	private void runStarMade(boolean server) {
		ArrayList<String> commandComponents = getCommandComponents(server);

		// Run game
		ProcessBuilder process = new ProcessBuilder(commandComponents);
		process.directory(new File(LaunchSettings.getInstallDir()));
		process.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		process.redirectError(ProcessBuilder.Redirect.INHERIT);
		try {
			System.out.println("Command: " + String.join(" ", commandComponents));
			process.start();
			System.exit(0);
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private ArrayList<String> getCommandComponents(boolean server) {
		ArrayList<String> commandComponents = new ArrayList<>();

		// Java command and arguments
		if (usingOldVersion()) {
			// Java 8
			String bundledJavaPath = new File(getJavaPath(JavaVersion.JAVA_8)).getAbsolutePath();
			commandComponents.add(bundledJavaPath);
		} else {
			// Java 18
			String bundledJavaPath = new File(getJavaPath(JavaVersion.JAVA_18)).getAbsolutePath();
			commandComponents.add(bundledJavaPath);
			commandComponents.addAll(Arrays.asList(J18ARGS));
		}

		if (currentOS == OperatingSystem.MAC) {
			// Run OpenGL on main thread on macOS
			// Needs to be added before "-jar"
			commandComponents.add("-XstartOnFirstThread");
		}
//		commandComponents.add("-Dfml.earlyprogresswindow=false");

		commandComponents.add("-jar");
		commandComponents.add("StarMade.jar");

		// Memory Arguments
		if (!LaunchSettings.getJvmArgs().isEmpty()) {
			String[] launchArgs = LaunchSettings.getLaunchArgs().split(" ");
			for (String arg : launchArgs) {
				if (arg.startsWith("-Xms") || arg.startsWith("-Xmx")) continue;
				commandComponents.add(arg.trim());
			}
		}
		commandComponents.add("-Xms1024m");
		commandComponents.add("-Xmx" + LaunchSettings.getMemory() + "m");

		// Game arguments
		commandComponents.add("-force");
		if (portField != null) port = Integer.parseInt(portField.getText());
		if (server) {
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

		//Version dropdown
		JComboBox<String> versionDropdown = createDropdown();
		setClientProperties(versionDropdown, defaults);

		//Branch dropdown
		JComboBox<String> branchDropdown = createBranchDropdown(versionDropdown, lastUsedBranch.index);
		setClientProperties(branchDropdown, defaults);

		versionDropdown.removeAllItems();
		updateVersionDropdown(versionDropdown, branchDropdown, versionRegistry);
		versionDropdown.addItemListener(e -> onSelectVersion(versionDropdown));
		setInitialVersion(versionDropdown);

		versionSubPanel.add(branchDropdown);
		versionSubPanel.add(versionDropdown);

		//Port field
		if (serverMode) {
			portField = createPortField();
			versionSubPanel.add(portField);
		} else {
			if (portField != null) {
				portField.setVisible(false);
				versionSubPanel.remove(portField);
			}
		}
		return versionPanel;
	}

	private static JTextField createPortField() {
		JTextField portField = new JTextField("4242");
		portField.setDoubleBuffered(true);
		portField.setOpaque(true);
		portField.setBackground(Palette.paneColor);
		portField.setForeground(Palette.textColor);
		portField.setFont(new Font("Roboto", Font.PLAIN, 12));
		portField.setMinimumSize(new Dimension(50, 20));
		portField.setPreferredSize(new Dimension(50, 20));
		portField.setMaximumSize(new Dimension(50, 20));
		portField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				portField.setToolTipText("Port");
			}

			@Override
			public void mouseExited(MouseEvent e) {
				portField.setToolTipText("");
			}
		});
		portField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				try {
					int port = Integer.parseInt(portField.getText() + c);
					if (port > 65535 || port < 1 || !Character.isDigit(c)) e.consume();
				} catch (Exception ignored) {
					e.consume();
				}
			}
		});
		return portField;
	}

	// Dropdown Methods

	private static JComboBox<String> createDropdown() {
		JComboBox<String> dropDown = new JComboBox<>();
		dropDown.setDoubleBuffered(true);
		dropDown.setOpaque(true);
		dropDown.setBackground(Palette.paneColor);
		dropDown.setForeground(Palette.textColor);
		dropDown.setUI(new BasicComboBoxUI() {
			@Override
			protected JButton createArrowButton() {
				JButton button = super.createArrowButton();
				button.setDoubleBuffered(true);
				button.setOpaque(false);
				button.setBackground(Palette.paneColor);
				button.setForeground(Palette.textColor);
				button.setContentAreaFilled(false);
				button.setRolloverEnabled(false);
				button.setBorder(BorderFactory.createEmptyBorder());
				button.setFocusable(false);
				return button;
			}
		});
		dropDown.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (isSelected) setBackground(Palette.selectedColor);
				else setBackground(Palette.deselectedColor);
				return this;
			}
		});
		return dropDown;
	}

	private JComboBox<String> createBranchDropdown(JComboBox<String> versionDropdown, int startIndex) {
		JComboBox<String> branchDropdown = createDropdown();
		branchDropdown.addItem("Release");
		branchDropdown.addItem("Dev");
		branchDropdown.addItem("Pre-Release");
		branchDropdown.setSelectedIndex(startIndex);
		branchDropdown.addItemListener(e -> onSelectBranch(branchDropdown, versionDropdown));
		return branchDropdown;
	}

	private void onSelectBranch(JComboBox<String> branchDropdown, JComboBox<String> versionDropdown) {
		int branchIndex = branchDropdown.getSelectedIndex();
		setBranch(GameBranch.getForIndex(branchIndex));
		LaunchSettings.saveSettings();
		versionDropdown.removeAllItems();
		updateVersionDropdown(versionDropdown, branchDropdown, versionRegistry);
		recreateButtons(playPanel, false);
	}

	private void onSelectVersion(JComboBox<String> versionDropdown) {
		if (versionDropdown.getSelectedIndex() == -1) return;
		selectedVersion = versionDropdown.getItemAt(versionDropdown.getSelectedIndex()).split(" ")[0];
		LaunchSettings.setLastUsedVersion(selectedVersion);
		LaunchSettings.saveSettings();
		recreateButtons(playPanel, false);
	}

	private static void setInitialVersion(JComboBox<String> versionDropdown) {
		String lastUsedVersion = LaunchSettings.getLastUsedVersion();
		if (lastUsedVersion.isEmpty()) lastUsedVersion = "NONE";
		for (int i = 0; i < versionDropdown.getItemCount(); i++) {
			if (versionDropdown.getItemAt(i).equals(lastUsedVersion)) {
				versionDropdown.setSelectedIndex(i);
				break;
			}
		}
	}

	private static void setClientProperties(JComboBox<String> dropdown, UIDefaults defaults) {
		dropdown.putClientProperty("Nimbus.Overrides", defaults);
		dropdown.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
	}

	// TODO maybe save and don't re-add every time
	private static void updateVersionDropdown(JComboBox<String> versionDropdown, JComboBox<String> branchDropdown, VersionRegistry versionRegistry) {
		GameBranch branch = GameBranch.getForIndex(branchDropdown.getSelectedIndex());
		List<IndexFileEntry> versions = versionRegistry.getVersions(branch);
		if (versions == null) return;

		// Add versions to dropdown
		for (IndexFileEntry version : versions) {
			if (version.equals(versions.get(0))) versionDropdown.addItem(version.version + " (Latest)");
			else versionDropdown.addItem(version.version);
		}
	}

	private void updateGame(IndexFileEntry version) {
		String[] options = {"Backup Database", "Backup Everything", "Don't Backup"};
		int choice = JOptionPane.showOptionDialog(this, "Would you like to backup your database, everything, or nothing?", "Backup", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		int backupMode = UpdaterThread.BACKUP_MODE_NONE;
		if (choice == 0) backupMode = UpdaterThread.BACKUP_MODE_DATABASE;
		else if (choice == 1) backupMode = UpdaterThread.BACKUP_MODE_EVERYTHING;
		ImageIcon updateButtonEmpty = getIcon("sprites/update_load_empty.png");
		ImageIcon updateButtonFilled = getIcon("sprites/update_load_full.png");
		updateButton.setIcon(updateButtonEmpty);
		//Start update process and update progress bar
		(updaterThread = new UpdaterThread(version, backupMode, new File(LaunchSettings.getInstallDir())) {
			@Override
			public void onProgress(float progress, String file, long mbDownloaded, long mbTotal, long mbSpeed) {
				dlStatus.setInstallProgress(progress);
				dlStatus.setDownloadedMb(mbDownloaded);
				dlStatus.setTotalMb(mbTotal);
				dlStatus.setSpeedMb(mbSpeed);
				if (file != null && !file.equals("null")) dlStatus.setFilename(file);
				int width = updateButtonEmpty.getIconWidth();
				int height = updateButtonEmpty.getIconHeight();
				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = image.createGraphics();
				g.drawImage(updateButtonEmpty.getImage(), 0, 0, null);
				int filledWidth = (int) (width * progress);
				g.drawImage(updateButtonFilled.getImage(), 0, 0, filledWidth, updateButtonFilled.getIconHeight(), 0, 0, filledWidth, updateButtonFilled.getIconHeight(), null);
				g.dispose();
				updateButton.setIcon(new ImageIcon(image));
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
						Thread.sleep(1);
						recreateButtons(playPanel, false);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				});
			}

			@Override
			public void onError(Exception exception) {
				exception.printStackTrace();
				updateButton.setIcon(getIcon("sprites/update_btn.png"));
			}
		}).start();
	}

	private void createScroller(JPanel currentPanel) {
		if (centerScrollPane == null) {
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
		if (newsPanel == null) newsPanel = new LauncherNewsPanel();
		createScroller(newsPanel);
		newsPanel.updatePanel();
		SwingUtilities.invokeLater(() -> {
			JScrollBar vertical = centerScrollPane.getVerticalScrollBar();
			vertical.setValue(vertical.getMinimum());
		});
	}

	private void createForumsPanel() {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			String ccURL = "https://starmadedock.net/forums/";
			try {
				Desktop.getDesktop().browse(new URI(ccURL));
			} catch (IOException | URISyntaxException exception) {
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
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			String ccURL = "https://starmadedock.net/content/";
			try {
				Desktop.getDesktop().browse(new URI(ccURL));
			} catch (IOException | URISyntaxException exception) {
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
		if (debugMode || (currentVersion != null && !currentVersion.version.startsWith("0.2") && !currentVersion.version.startsWith("0.1"))) {
			return getLastUsedVersion();
		}
		return versionRegistry.getLatestVersion(branch);
	}

	private boolean gameJarExists(String installDir) {
		return (new File(installDir + "/StarMade.jar")).exists();
	}

}