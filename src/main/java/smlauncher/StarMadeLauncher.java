package smlauncher;

import com.formdev.flatlaf.FlatDarkLaf;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import smlauncher.community.LauncherCommunityPanel;
import smlauncher.content.LauncherContentPanel;
import smlauncher.downloader.JavaDownloader;
import smlauncher.forums.LauncherForumsPanel;
import smlauncher.news.LauncherNewsPanel;
import smlauncher.starmade.*;
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
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.*;

/**
 * Main class for the StarMade Launcher.
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 * @author SlavSquatSuperstar
 */
public class StarMadeLauncher extends JFrame {

	public static final String BUG_REPORT_URL = "https://github.com/garretreichenbach/New-StarMade-Launcher/issues";
	public static final String LAUNCHER_VERSION = "3.0.9";
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

	public String installDir = "StarMade";
	public IndexFileEntry gameVersion;
	private final OperatingSystem currentOS;
	public static boolean debugMode;
	public static boolean useSteam;

	public static Updater.VersionFile buildBranch = Updater.VersionFile.RELEASE;
	public static int lastUsedBranch;
	private static String selectedVersion;
	private static boolean selectVersion;
	private static int backup = Updater.BACK_DB;
	public final ArrayList<IndexFileEntry> releaseVersions = new ArrayList<>();
	public final ArrayList<IndexFileEntry> devVersions = new ArrayList<>();
	public final ArrayList<IndexFileEntry> preReleaseVersions = new ArrayList<>();
	private final float[] installProgress = new float[1];
	private final String[] fileName = {"None"};
	private final long[] mb = new long[3];
	private final JSONObject launchSettings;
	private UpdaterThread updaterThread;
	private int mouseX;
	private int mouseY;
	private JButton updateButton;
	private JPanel mainPanel;
	private JPanel centerPanel;
	private JPanel footerPanel;
	private JPanel versionPanel;
	private JPanel playPanel;
	private JPanel serverPanel;
	private JPanel playPanelButtons;
	private JScrollPane centerScrollPane;
	private LauncherNewsPanel newsPanel;
	private LauncherForumsPanel forumsPanel;
	private LauncherContentPanel contentPanel;
	private LauncherCommunityPanel communityPanel;

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
			if(resource != null) setIconImage(Toolkit.getDefaultToolkit().getImage(resource));
		} catch(Exception exception) {
			System.out.println("Could not set window icon");
		}

		// Fetch game versions
		try {
			loadVersionList();
		} catch(Exception exception) {
			System.out.println("Could not load versions list, switching to offline");
			//Todo: Offline Mode
		}

		// Read launch settings and game directory
		launchSettings = readLaunchSettings();
		installDir = launchSettings.getString("installDir");

		// Read game version
		gameVersion = getCurrentVersion();
		setGameVersion(gameVersion);

		// Read game branch
		lastUsedBranch = getBranchForVersion(gameVersion);
		launchSettings.put("lastUsedBranch", lastUsedBranch);

		saveLaunchSettings();
		deleteUpdaterJar();

		// TODO download jre into install dir

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

	private void downloadJRE(JavaVersion version) throws Exception {
		if (new File(getJavaPath(version)).exists()) return;
		new JavaDownloader(version).setInstallDir(installDir).downloadAndUnzip();
	}

	private static void deleteUpdaterJar() {
		File updaterJar = new File("Updater.jar");
		if(updaterJar.exists()) updaterJar.delete();
	}

	public static void main(String[] args) {
		boolean headless = false;
		if(args == null || args.length == 0) startup();
		else {
			for(String arg : args) {
				arg = arg.toLowerCase();
				if(arg.equals("-debug_mode")) debugMode = true;
				if(arg.contains("-version")) {
					selectVersion = true;
					if(arg.contains("-dev")) buildBranch = Updater.VersionFile.DEV;
					else if(arg.contains("-pre")) buildBranch = Updater.VersionFile.PRE;
					else buildBranch = Updater.VersionFile.RELEASE;
				} else if("-no_gui".equals(arg) || "-nogui".equals(arg)) {
					if(GraphicsEnvironment.isHeadless()) {
						displayHelp();
						System.out.println("Please use the '-nogui' parameter to run the launcher in text mode!");
						return;
					} else headless = true;
				}
//				if(headless) {
//					switch(arg) {
//						case "-h", "-help" -> {
//							displayHelp();
//							return;
//						}
//						case "-steam" -> useSteam = true;
//						case "-backup" -> backup = Updater.BACK_ALL;
//						case "-backup_all" -> backup = Updater.BACK_ALL;
//						case "-no_backup" -> backup = Updater.BACK_NONE;
//					}
//					Updater.withoutGUI((args.length > 1 && "-force".equals(args[1])), installDir, buildBranch, backup, selectVersion);
//				} else startup();
				startup();
			}
		}
	}

	private static void startup() {
		EventQueue.invokeLater(() -> {
			try {
				FlatDarkLaf.setup();
				if(LauncherUpdater.checkForUpdate()) {
					System.err.println("Launcher version doesn't match latest version, so an update must be available.");
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
						startFrame();
					});
					buttonPanel.add(cancelButton);
					dialog.setVisible(true);
				} else startFrame();
			} catch(Exception e) {
				e.printStackTrace();
			}
		});
	}

	private static void startFrame() {
		StarMadeLauncher frame = new StarMadeLauncher();
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

	public static ImageIcon getIcon(String s) {
		try {
			return new ImageIcon(ImageIO.read(Objects.requireNonNull(StarMadeLauncher.class.getResource("/" + s))));
		} catch(IOException exception) {
			return new ImageIcon();
		}
	}

	public static ImageIcon getIcon(String s, int width, int height) {
		try {
			return new ImageIcon(ImageIO.read(Objects.requireNonNull(StarMadeLauncher.class.getResource("/" + s))).getScaledInstance(width, height, Image.SCALE_SMOOTH));
		} catch(IOException exception) {
			return new ImageIcon();
		}
	}

	public static void displayHelp() {
		System.out.println("StarMade Launcher " + LAUNCHER_VERSION + " Help:");
		System.out.println("-version version selection prompt");
		System.out.println("-no_gui dont start gui (needed for linux dedicated servers)");
		System.out.println("-no_backup dont create backup (default backup is server database only)");
		System.out.println("-backup_all create backup of everything (default backup is server database only)");
		System.out.println("-pre use pre branch (default is release)");
		System.out.println("-dev use dev branch (default is release)");
	}

	public static String getVersionShort(IndexFileEntry version) {
		return version.version.split("#")[0];
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

	private IndexFileEntry getCurrentVersion() {
		try {
			File versionFile = new File(installDir, "version.txt");
			if(!versionFile.exists()) return null;

			String version = Files.readString(versionFile.toPath());
			version = version.substring(0, version.indexOf('#'));
			for(IndexFileEntry entry : releaseVersions) if(version.equals(entry.build)) return entry;
			for(IndexFileEntry entry : devVersions) if(version.equals(entry.build)) return entry;
			for(IndexFileEntry entry : preReleaseVersions) if(version.equals(entry.build)) return entry;
		} catch(IOException exception) {
			System.out.println("Could not read game version from file");
		}
		return null;
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
				if(mouseX > 770 || mouseY > 30) {
					mouseX = 0;
					mouseY = 0;
				}
				super.mousePressed(e);
			}
		});
		topPanel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if(mouseX != 0 && mouseY != 0) setLocation(getLocation().x + e.getX() - mouseX, getLocation().y + e.getY() - mouseY);
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
		} catch(IOException exception) {
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
			if(isSelected) {
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
				if(index != -1) list.setSelectedIndex(index);
				else list.clearSelection();
			}
		});
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
		list.setFixedCellHeight(48);
		DefaultListModel<JLabel> listModel = new DefaultListModel<>();
		listModel.addElement(new JLabel("NEWS"));
		listModel.addElement(new JLabel("FORUMS"));
		listModel.addElement(new JLabel("CONTENT"));
		listModel.addElement(new JLabel("COMMUNITY"));
		for(int i = 0; i < listModel.size(); i++) {
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
		if(getCurrentVersion() == null) selectedVersion = null;
		else selectedVersion = getCurrentVersion().build;
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
			JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 2048, getSystemMemory(), Objects.requireNonNull(readLaunchSettings()).getInt("memory"));
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
			if(getSystemMemory() > 16384) { //Make sure the slider is not too squished for people that have a really epic gamer pc
				slider.setMajorTickSpacing(2048);
				slider.setMajorTickSpacing(1024);
				slider.setLabelTable(slider.createStandardLabels(4096));
			} else if(getSystemMemory() > 8192) {
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
			launchArgs.setText(readLaunchSettings().getString("launchArgs"));
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
				this.launchSettings.put("memory", slider.getValue());
				this.launchSettings.put("launchArgs", launchArgs.getText());
				saveLaunchSettings();
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
			JDialog dialog = new JDialog();
			dialog.setModal(true);
			dialog.setResizable(false);
			dialog.setTitle("Installation Settings");
			dialog.setSize(450, 150);
			dialog.setLocationRelativeTo(null);
			dialog.setLayout(new BorderLayout());
			dialog.setAlwaysOnTop(true);
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			JPanel dialogPanel = new JPanel();
			dialogPanel.setDoubleBuffered(true);
			dialogPanel.setOpaque(false);
			dialog.add(dialogPanel, BorderLayout.CENTER);
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
			JTextField installLabelPath = new JTextField(installDir);
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
				installDir = file.getAbsolutePath();
				installLabelPath.setText(installDir);
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
				int result = fileChooser.showOpenDialog(dialog);
				if(result == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if(!file.isDirectory()) file = file.getParentFile();
					installDir = file.getAbsolutePath();
					installLabelPath.setText(installDir);
				}
			});
			JButton repairButton = new JButton("Repair");
			repairButton.setIcon(UIManager.getIcon("FileView.checkIcon"));
			repairButton.setDoubleBuffered(true);
			repairButton.setOpaque(false);
			repairButton.setFont(new Font("Roboto", Font.BOLD, 12));
			dialogPanel.add(repairButton);
			repairButton.addActionListener(e1 -> {
				Updater.VersionFile branch = getLastUsedBranch();
				if(branch == null) branch = Updater.VersionFile.RELEASE;
				IndexFileEntry version = getLatestVersion(branch);
				if(version != null) {
					if(updaterThread == null || !updaterThread.updating) {
						dialog.dispose();
						recreateButtons(playPanel, true);
						updateGame(updateButton, version);
					}
				} else JOptionPane.showMessageDialog(dialog, "The Launcher needs to be online to do this!", "Error", JOptionPane.ERROR_MESSAGE);
			});

			JPanel buttonPanel = new JPanel();
			buttonPanel.setDoubleBuffered(true);
			buttonPanel.setOpaque(false);
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			dialog.add(buttonPanel, BorderLayout.SOUTH);
			JButton saveButton = new JButton("Save");
			saveButton.setFont(new Font("Roboto", Font.BOLD, 12));
			saveButton.setDoubleBuffered(true);
			buttonPanel.add(saveButton);
			JButton cancelButton = new JButton("Cancel");
			cancelButton.setFont(new Font("Roboto", Font.BOLD, 12));
			cancelButton.setDoubleBuffered(true);
			buttonPanel.add(cancelButton);
			saveButton.addActionListener(e1 -> {
				this.launchSettings.put("installDir", installDir);
				saveLaunchSettings();
				dialog.dispose();
			});
			cancelButton.addActionListener(e1 -> dialog.dispose());
			dialog.setVisible(true);
		});
		serverPanel.setVisible(false);
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
		normalPlayButton.addActionListener(e -> {
			footerLabel.setIcon(getIcon("sprites/footer_normalplay_bg.jpg"));
			serverPanel.setVisible(false);
			versionPanel.setVisible(true);
			createPlayPanel(footerPanel);
		});
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
				footerLabel.setIcon(getIcon("sprites/footer_dedicated_bg.jpg"));
				versionPanel.setVisible(false);
				playPanelButtons.removeAll();
				versionPanel.removeAll();
				serverPanel.setVisible(true);
				createServerPanel(footerPanel);
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
		} catch(IOException exception) {
			exception.printStackTrace();
		}
		centerPanel.add(background, BorderLayout.CENTER);
	}

	private JSONObject readLaunchSettings() {
		File jsonFile = new File("./launch-settings.json");
		JSONObject defaultSettings = getDefaultLaunchSettings();

		// Return default if not present
		if (!jsonFile.exists()) {
			try (FileWriter writer = new FileWriter(jsonFile, StandardCharsets.UTF_8)) {
				writer.write(defaultSettings.toString());
				writer.flush();
			} catch (IOException e){
				System.out.println("Could not create launch settings file");
			}
			return defaultSettings;
		}

		// Read the settings file
		try (FileReader reader = new FileReader(jsonFile, StandardCharsets.UTF_8)){
			String text = IOUtils.toString(reader);
			return new JSONObject(text);
		} catch(IOException e) {
			System.out.println("Could not read launch settings from file");
		}
		return defaultSettings;
	}

	private JSONObject getDefaultLaunchSettings() {
		JSONObject settings = new JSONObject();
		settings.put("memory", 4096);
		settings.put("launchArgs", "");
		settings.put("installDir", "StarMade");
		settings.put("lastUsedBranch", 0); // Release
		settings.put("lastUsedVersion", "NONE");
		settings.put("jvm_args", "");
		return settings;
	}

	private void setGameVersion(IndexFileEntry gameVersion) {
		if(gameVersion != null) {
			launchSettings.put("lastUsedVersion", gameVersion.build);

			if(usingOldVersion()) launchSettings.put("jvm_args", "--illegal-access=permit");
			else launchSettings.put("jvm_args", "");
		} else {
			launchSettings.put("lastUsedVersion", "NONE");
			launchSettings.put("jvm_args", "");
		}
	}

	private void saveLaunchSettings() {
		try (FileWriter writer = new FileWriter("launch-settings.json", StandardCharsets.UTF_8)) {
			writer.write(launchSettings.toString());
			writer.flush();
		} catch(IOException exception) {
			System.out.println("Could not save launch settings to file");
		}
	}

	private boolean usingOldVersion() {
		return gameVersion.build.startsWith("0.2") || gameVersion.build.startsWith("0.1");
	}

	private int getSystemMemory() {
		com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		return (int) (os.getTotalPhysicalMemorySize() / (1024 * 1024));
	}

	private void createPlayPanel(JPanel footerPanel) {
		if(playPanel != null) {
			playPanel.removeAll();
			playPanel.revalidate();
			playPanel.repaint();
		}
		playPanel = new JPanel();
		playPanel.setDoubleBuffered(true);
		playPanel.setOpaque(false);
		playPanel.setLayout(new BorderLayout());
		footerPanel.add(playPanel);
		versionPanel = new JPanel();
		versionPanel.setDoubleBuffered(true);
		versionPanel.setOpaque(false);
		versionPanel.setLayout(new BorderLayout());
		footerPanel.add(versionPanel, BorderLayout.WEST);
		JPanel versionSubPanel = new JPanel();
		versionSubPanel.setDoubleBuffered(true);
		versionSubPanel.setOpaque(false);
		versionSubPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		versionPanel.add(versionSubPanel, BorderLayout.SOUTH);

		//Change color of arrow
		UIDefaults defaults = new UIDefaults();
		defaults.put("ComboBox:\"ComboBox.arrowButton\"[Enabled].backgroundPainter", Palette.buttonColor);

		//Version dropdown
		JComboBox<String> versionDropdown = new JComboBox<>();
		versionDropdown.setDoubleBuffered(true);
		versionDropdown.setOpaque(true);
		versionDropdown.setBackground(Palette.paneColor);
		versionDropdown.setForeground(Palette.textColor);
		versionDropdown.setUI(new BasicComboBoxUI() {
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
		versionDropdown.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if(isSelected) setBackground(Palette.selectedColor);
				else setBackground(Palette.deselectedColor);
				return this;
			}
		});
		versionDropdown.putClientProperty("Nimbus.Overrides", defaults);
		versionDropdown.putClientProperty("Nimbus.Overrides.InheritDefaults", true);

		//Branch dropdown
		JComboBox<String> branchDropdown = new JComboBox<>();
		branchDropdown.setDoubleBuffered(true);
		branchDropdown.setOpaque(true);
		branchDropdown.setBackground(Palette.paneColor);
		branchDropdown.setForeground(Palette.textColor);
		branchDropdown.setUI(new BasicComboBoxUI() {
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
		branchDropdown.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if(isSelected) setBackground(Palette.selectedColor);
				else setBackground(Palette.deselectedColor);
				return this;
			}
		});
		branchDropdown.addItem("Release");
		branchDropdown.addItem("Dev");
		branchDropdown.addItem("Pre-Release");
		lastUsedBranch = Objects.requireNonNull(readLaunchSettings()).getInt("lastUsedBranch");
		branchDropdown.setSelectedIndex(lastUsedBranch);
		branchDropdown.addItemListener(e -> {
			lastUsedBranch = branchDropdown.getSelectedIndex();
			launchSettings.put("lastUsedBranch", lastUsedBranch);
			saveLaunchSettings();
			versionDropdown.removeAllItems();
			updateVersions(versionDropdown, branchDropdown);
			recreateButtons(playPanel, false);
		});
		branchDropdown.putClientProperty("Nimbus.Overrides", defaults);
		branchDropdown.putClientProperty("Nimbus.Overrides.InheritDefaults", true);

		versionDropdown.removeAllItems();
		updateVersions(versionDropdown, branchDropdown);
		versionDropdown.addItemListener(e -> {
			if(versionDropdown.getSelectedIndex() == -1) return;
			selectedVersion = versionDropdown.getItemAt(versionDropdown.getSelectedIndex()).split(" ")[0];
			recreateButtons(playPanel, false);
		});
		String lastUsedVersion = Objects.requireNonNull(readLaunchSettings()).getString("lastUsedVersion");
		if(lastUsedVersion == null || lastUsedVersion.isEmpty()) lastUsedVersion = "NONE";
		for(int i = 0; i < versionDropdown.getItemCount(); i++) {
			if(versionDropdown.getItemAt(i).equals(lastUsedVersion)) {
				versionDropdown.setSelectedIndex(i);
				break;
			}
		}
		versionSubPanel.add(branchDropdown);
		versionSubPanel.add(versionDropdown);
		recreateButtons(playPanel, false);
		footerPanel.revalidate();
		footerPanel.repaint();
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
		if((repair || !lookForGame(installDir) || gameVersion == null || (!Objects.equals(gameVersion.build, selectedVersion))) && !debugMode) {
			updateButton = new JButton(getIcon("sprites/update_btn.png"));
			updateButton.setDoubleBuffered(true);
			updateButton.setOpaque(false);
			updateButton.setContentAreaFilled(false);
			updateButton.setBorderPainted(false);
			updateButton.addActionListener(e -> {
				Updater.VersionFile branch = getLastUsedBranch();
				if(branch == null) branch = Updater.VersionFile.RELEASE;
				IndexFileEntry version = getLatestVersion(branch);
				if(version != null) {
					if(updaterThread == null || !updaterThread.updating) updateGame(updateButton, version);
				} else JOptionPane.showMessageDialog(null, "The Launcher needs to be online to do this!", "Error", JOptionPane.ERROR_MESSAGE);
			});
			updateButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					if(updaterThread == null || !updaterThread.updating) updateButton.setIcon(getIcon("sprites/update_roll.png"));
					else updateButton.setToolTipText("Updating... [" + (int) (installProgress[0] * 100) + ".0%]\nDownloading " + fileName[0] + " [" + formatBytes(mb[0]) + " / " + formatBytes(mb[1]) + " at " + formatBytes(mb[2]) + "/s]");
				}

				@Override
				public void mouseExited(MouseEvent e) {
					if(updaterThread == null || !updaterThread.updating) updateButton.setIcon(getIcon("sprites/update_btn.png"));
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
				launchSettings.put("lastUsedVersion", gameVersion.build);
				saveLaunchSettings();
				try {
					if(usingOldVersion()) downloadJRE(JavaVersion.JAVA_8);
					else downloadJRE(JavaVersion.JAVA_18);
				} catch(Exception exception) {
					exception.printStackTrace();
					(new ErrorDialog("Error", "Failed to unzip java, manual installation required", exception)).setVisible(true);
					return;
				}
				runStarMade(false);
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

	private String formatBytes(long bytes) {
		if(bytes < 1024) return bytes + " B";
		else if(bytes < 1024 * 1024) return bytes / 1024 + " KB";
		else if(bytes < 1024 * 1024 * 1024) return bytes / (1024 * 1024) + " MB";
		else return bytes / (1024 * 1024 * 1024) + " GB";
	}

	private boolean checkJavaVersion() {
		File jre8 = new File("./jre8/bin/java.exe");
		File jre18 = new File("./jre18/bin/java.exe");
		if(usingOldVersion()) return jre8.exists();
		else return jre18.exists();
	}

	public void runStarMade(boolean server) {
		boolean useJava8 = usingOldVersion();
		String bundledJavaPath = new File(useJava8 ? getJavaPath(JavaVersion.JAVA_8) : getJavaPath(JavaVersion.JAVA_18)).getPath();

		ArrayList<String> commandComponents = new ArrayList<>();
		commandComponents.add(bundledJavaPath);
		if(!useJava8) commandComponents.addAll(List.of(J18ARGS));

		if(currentOS == OperatingSystem.MAC) {
			// Run OpenGL on main thread on macOS
			// Needs to be added before "-jar"
			commandComponents.add("-XstartOnFirstThread");
		}

		commandComponents.add("-jar");
		commandComponents.add("StarMade.jar");

		if(!Objects.requireNonNull(readLaunchSettings()).getString("jvm_args").isEmpty()) {
			String[] launchArgs = Objects.requireNonNull(readLaunchSettings()).getString("launchArgs").split(" ");
			for(String arg : launchArgs) {
				if(arg.startsWith("-Xms") || arg.startsWith("-Xmx")) continue;
				commandComponents.add(arg.trim());
			}
		}
		commandComponents.add("-Xms1024m");
		commandComponents.add("-Xmx" + readLaunchSettings().getInt("memory") + "m");

		if(server) commandComponents.add("-server");
		else commandComponents.add("-force");

		ProcessBuilder process = new ProcessBuilder(commandComponents);
		process.directory(new File(installDir));
		System.out.println("installed in " + new File(installDir).getAbsolutePath());
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

	// TODO only looks for jre inside starmade game folder
	// TODO move extracted jar to starmade folder, read from launch-settings.json
	private String getJavaPath(JavaVersion version) {
		return installDir + "/" + String.format(currentOS.javaPath, version.number);
	}

	private void createServerPanel(JPanel footerPanel) {
		serverPanel = new JPanel();
		//Todo: Create server panel
	}

	private void updateGame(JButton updateButton, IndexFileEntry version) {
		String[] options = {"Backup Database", "Backup Everything", "Don't Backup"};
		int choice = JOptionPane.showOptionDialog(this, "Would you like to backup your database, everything, or nothing?", "Backup", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		int backupMode = UpdaterThread.BACKUP_MODE_NONE;
		if(choice == 0) backupMode = UpdaterThread.BACKUP_MODE_DATABASE;
		else if(choice == 1) backupMode = UpdaterThread.BACKUP_MODE_EVERYTHING;
		ImageIcon updateButtonEmpty = getIcon("sprites/update_load_empty.png");
		ImageIcon updateButtonFilled = getIcon("sprites/update_load_full.png");
		updateButton.setIcon(updateButtonEmpty);
		//Start update process and update progress bar
		(updaterThread = new UpdaterThread(version, backupMode, new File(installDir)) {
			@Override
			public void onProgress(float progress, String file, long mbDownloaded, long mbTotal, long mbSpeed) {
				installProgress[0] = progress;
				mb[0] = mbDownloaded;
				mb[1] = mbTotal;
				mb[2] = mbSpeed;
				if(file != null && !file.equals("null")) fileName[0] = file;
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
				gameVersion = getCurrentVersion();
				assert gameVersion != null;
				launchSettings.put("lastUsedVersion", gameVersion.build);
				selectedVersion = gameVersion.build;
				saveLaunchSettings();
				SwingUtilities.invokeLater(() -> {
					try {
						Thread.sleep(1);
						recreateButtons(playPanel, false);
					} catch(InterruptedException e) {
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

	private Updater.VersionFile getLastUsedBranch() {
		switch(lastUsedBranch) {
			case 1:
				return Updater.VersionFile.DEV;
			case 2:
				return Updater.VersionFile.PRE;
			default:
				return Updater.VersionFile.RELEASE;
		}
	}

	private int getBranchForVersion(IndexFileEntry gameVersion) {
		if(gameVersion == null || gameVersion.build == null) return 0;
		switch(gameVersion.build) {
			case "DEV":
				return 1;
			case "PRE":
				return 2;
			default:
				return 0;
		}
	}



	private void updateVersions(JComboBox<String> versionDropdown, JComboBox<String> branchDropdown) {
		if(Objects.equals(branchDropdown.getSelectedItem(), "Release")) {
			for(IndexFileEntry version : releaseVersions) {
				if(version.equals(releaseVersions.get(0))) versionDropdown.addItem(version.build + " (Latest)");
				else versionDropdown.addItem(version.build);
			}
		} else if(Objects.equals(branchDropdown.getSelectedItem(), "Dev")) {
			for(IndexFileEntry version : devVersions) {
				if(version.build.startsWith("2017")) continue;

				if(version.equals(devVersions.get(2))) versionDropdown.addItem(version.build + " (Latest)");
				else versionDropdown.addItem(version.build);
			}
		} else if(Objects.equals(branchDropdown.getSelectedItem(), "Pre-Release")) {
			for(IndexFileEntry version : preReleaseVersions) {
				if(version.equals(preReleaseVersions.get(0))) versionDropdown.addItem(version.build + " (Latest)");
				else versionDropdown.addItem(version.build);
			}
		}
	}

	private void createScroller(JPanel currentPanel) {
		if(centerScrollPane == null) {
			centerScrollPane = new JScrollPane(currentPanel);
			centerScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			centerScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
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

	private IndexFileEntry getLatestVersion(Updater.VersionFile branch) {
		IndexFileEntry currentVersion = getCurrentVersion();
		if(debugMode || (currentVersion != null && !currentVersion.version.startsWith("0.2") && !currentVersion.version.startsWith("0.1"))) return getCurrentVersion();
		switch(branch) {
			case RELEASE:
				return releaseVersions.get(0);
			case DEV:
				return devVersions.get(0);
			case PRE:
				return preReleaseVersions.get(0);
			default:
				return null;
		}
	}

	private void loadVersionList() throws IOException {
		URL url;
		releaseVersions.clear();
		devVersions.clear();
		preReleaseVersions.clear();
		for(Updater.VersionFile branch : Updater.VersionFile.values()) {
			url = new URL(branch.location);
			URLConnection openConnection = url.openConnection();
			openConnection.setConnectTimeout(10000);
			openConnection.setReadTimeout(10000);
			openConnection.setRequestProperty("User-Agent", "StarMade-Updater_" + LAUNCHER_VERSION);
			BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(openConnection.getInputStream()), StandardCharsets.UTF_8));
			String str;
			while((str = in.readLine()) != null) {
				try {
					String[] vPath = str.split(" ", 2);
					String[] vBuild = vPath[0].split("#", 2);
					String version = vBuild[0];
					String build = "";
					if(vBuild.length == 2) build = vBuild[1];
					String path = vPath[1];
					switch(branch) {
						case RELEASE:
							releaseVersions.add(new IndexFileEntry(build, path, version, branch));
							releaseVersions.sort(Collections.reverseOrder());
//						System.err.println("loaded files (sorted) " + releaseVersions);
							break;
						case DEV:
							devVersions.add(new IndexFileEntry(build, path, version, branch));
							devVersions.sort(Collections.reverseOrder());
//						System.err.println("loaded files (sorted) " + devVersions);
							break;
						case PRE:
							preReleaseVersions.add(new IndexFileEntry(build, path, version, branch));
							preReleaseVersions.sort(Collections.reverseOrder());
//						System.err.println("loaded files (sorted) " + preReleaseVersions);
							break;
					}
				} catch(Exception exception) {
					exception.printStackTrace();
				}
			}
			in.close();
			openConnection.getInputStream().close();
		}
	}

	public String getStarMadeStartPath(String installDir) {
		return installDir + "/StarMade.jar";
	}

	public boolean lookForGame(String installDir) {
		return (new File(getStarMadeStartPath(installDir))).exists();
	}
}