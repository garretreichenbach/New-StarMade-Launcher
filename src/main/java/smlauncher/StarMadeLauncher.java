package smlauncher;

import com.formdev.flatlaf.FlatDarkLaf;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import smlauncher.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class StarMadeLauncher extends JFrame {

	public static final int LAUNCHER_VERSION = 3;
	public static IndexFileEntry GAME_VERSION;

	public static final Color selectedColor = Color.decode("#438094");
	public static final Color deselectedColor = Color.decode("#325561");

	public static boolean useSteam ;
	private static boolean selectVersion;
	private static boolean devMode;
	private static int backup = Updater.BACK_DB;
	public static String installDir = "./StarMade/";
	public static Updater.VersionFile buildBranch = Updater.VersionFile.RELEASE;

	public static void main(String[] args) {
		//FlatDarculaLaf.setup();
		boolean headless = false;
		if(args == null || args.length == 0) startup();
		else {
			for(String arg : args) {
				arg = arg.toLowerCase();
				if(arg.contains("-version")) {
					selectVersion = true;
					if(arg.contains("-dev")) buildBranch = Updater.VersionFile.DEV;
					else if(arg.contains("-pre")) buildBranch = Updater.VersionFile.PRE;
					else if(arg.contains("-archive")) buildBranch = Updater.VersionFile.ARCHIVE;
					else buildBranch = Updater.VersionFile.RELEASE;
				} else if("-no_gui".equals(arg) || "-nogui".equals(arg)) {
					if(GraphicsEnvironment.isHeadless()) {
						displayHelp();
						System.out.println("Please use the '-nogui' parameter to run the launcher in text mode!");
						return;
					} else headless = true;
				} else if("-devmode".equals(arg)) devMode = true;
				if(headless) {
					switch(arg) {
						case "-h":
						case "-help":
							displayHelp();
							return;
						case "-steam":
							useSteam = true;
							break;
						case "-backup":
							backup = Updater.BACK_ALL;
							break;
						case "-backup_all":
							backup = Updater.BACK_ALL;
							break;
						case "-no_backup":
							backup = Updater.BACK_NONE;
							break;
					}
					Updater.withoutGUI((args.length > 1 && "-force".equals(args[1])), installDir, buildBranch, backup, selectVersion);
				} else startup();
			}
		}
	}

	private static void startup() {
		EventQueue.invokeLater(() -> {
			try {
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
			} catch(Exception e) {
				e.printStackTrace();
			}
		});
	}

	public final ArrayList<IndexFileEntry> releaseVersions = new ArrayList<>();
	public final ArrayList<IndexFileEntry> devVersions = new ArrayList<>();
	public final ArrayList<IndexFileEntry> preReleaseVersions = new ArrayList<>();
	public final ArrayList<IndexFileEntry> archiveVersions = new ArrayList<>();
	public int lastUsedBranch;
	private UpdaterThread updaterThread;
	private int mouseX;
	private int mouseY;
	private String userArgs;
	private JPanel mainPanel;
	private JPanel centerPanel;
	private JPanel versionPanel;
	private JPanel serverPanel;
	private JPanel playPanelButtons;
//	private LauncherNewsPanel newsPanel;

	public StarMadeLauncher() {
		super("StarMade Launcher");
		FlatDarkLaf.setup();
		try {
			URL resource = StarMadeLauncher.class.getResource("/icon.png");
			if(resource != null) setIconImage(Toolkit.getDefaultToolkit().getImage(resource));
		} catch(Exception exception) {
			exception.printStackTrace();
			flagForRepair();
		}
		try {
			loadVersionList();
		} catch(IOException exception) {
			exception.printStackTrace();
			//Todo: Offline Mode
		}
		GAME_VERSION = getCurrentVersion();
		boolean needsRepair = false;
		if(GAME_VERSION == null || GAME_VERSION.build == null) {
			needsRepair = true;
			lastUsedBranch = 0;
		} else {
			switch(GAME_VERSION.build) {
				case "RELEASE":
					lastUsedBranch = 0;
					break;
				case "DEV":
					lastUsedBranch = 1;
					break;
				case "PRE":
					lastUsedBranch = 2;
					break;
				case "ARCHIVE":
					lastUsedBranch = 3;
					break;
			}
		}
		//		File launcherFolder = new File("launcher");
		//		if(!launcherFolder.exists()) {
		//			launcherFolder.mkdirs();
		//			repairLauncher();
		//		}
		try {
			setIconImage(ImageIO.read(Objects.requireNonNull(StarMadeLauncher.class.getResource("/icon.png"))));
		} catch(IOException exception) {
			exception.printStackTrace();
		}
		setTitle("StarMade Launcher [" + LAUNCHER_VERSION + "]");
		setBounds(100, 100, 800, 550);
		setMinimumSize(new Dimension(800, 550));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		createMainPanel();
		dispose();

		setUndecorated(true);
		setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

		setResizable(false);
		getRootPane().setDoubleBuffered(true);
		setVisible(true);

		if(needsRepair) flagForRepair();
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
		topLabel.setIcon(getIcon("header_top.png"));
		topPanel.add(topLabel);

		JPanel topPanelButtons = new JPanel();
		topPanelButtons.setDoubleBuffered(true);
		topPanelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		topPanelButtons.setOpaque(false);

		JButton closeButton = new JButton(null, getIcon("close_icon.png")); //Todo: Replace these cus they look like shit
		closeButton.setDoubleBuffered(true);
		closeButton.setOpaque(false);
		closeButton.setContentAreaFilled(false);
		closeButton.setBorderPainted(false);
		closeButton.addActionListener(e -> System.exit(0));

		JButton minimizeButton = new JButton(null, getIcon("minimize_icon.png"));
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
			Image image = ImageIO.read(Objects.requireNonNull(StarMadeLauncher.class.getResource("/left_panel.png")));
			//Resize the image to the left panel
			image = image.getScaledInstance(150, 500, Image.SCALE_SMOOTH);
			leftLabel.setIcon(new ImageIcon(image));
		} catch(IOException exception) {
			exception.printStackTrace();
			flagForRepair();
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
				value.setForeground(selectedColor);
				value.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, selectedColor));
			} else {
				value.setForeground(deselectedColor);
				value.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, deselectedColor));
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
			label.setForeground(selectedColor);
			label.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, selectedColor));
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
		logo.setIcon(getIcon("logo.png"));
		leftInset.add(logo);

		JPanel footerPanel = new JPanel();
		footerPanel.setDoubleBuffered(true);
		footerPanel.setOpaque(false);
		footerPanel.setLayout(new StackLayout());
		mainPanel.add(footerPanel, BorderLayout.SOUTH);

		JLabel footerLabel = new JLabel();
		footerLabel.setDoubleBuffered(true);
		footerLabel.setIcon(getIcon("footer_normalplay_bg.jpg"));
		footerPanel.add(footerLabel);

		JPanel topRightPanel = new JPanel();
		topRightPanel.setDoubleBuffered(true);
		topRightPanel.setOpaque(false);
		topRightPanel.setLayout(new BorderLayout());
		topPanel.add(topRightPanel, BorderLayout.EAST);

		JLabel logoLabel = new JLabel();
		logoLabel.setDoubleBuffered(true);
		logoLabel.setOpaque(false);
		logoLabel.setIcon(getIcon("launcher_schine_logo.png"));
		topRightPanel.add(logoLabel, BorderLayout.EAST);

		JButton normalPlayButton = new JButton("Play");
		normalPlayButton.setFont(new Font("Roboto", Font.BOLD, 12));
		normalPlayButton.setDoubleBuffered(true);
		normalPlayButton.setOpaque(false);
		normalPlayButton.setContentAreaFilled(false);
		normalPlayButton.setBorderPainted(false);

		JButton dedicatedServerButton = new JButton("Dedicated Server");
		dedicatedServerButton.setFont(new Font("Roboto", Font.BOLD, 12));
		dedicatedServerButton.setDoubleBuffered(true);
		dedicatedServerButton.setOpaque(false);
		dedicatedServerButton.setContentAreaFilled(false);
		dedicatedServerButton.setBorderPainted(false);

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

		createPlayPanel(footerPanel);
		createServerPanel(footerPanel);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setDoubleBuffered(true);
		bottomPanel.setOpaque(false);
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		footerPanel.add(bottomPanel, BorderLayout.SOUTH);

		JButton installationSettingsLabel = new JButton("Memory Settings");
		installationSettingsLabel.setIcon(getIcon("memory_options_gear.png"));
		installationSettingsLabel.setFont(new Font("Roboto", Font.BOLD, 12));
		installationSettingsLabel.setDoubleBuffered(true);
		installationSettingsLabel.setOpaque(false);
		installationSettingsLabel.setContentAreaFilled(false);
		bottomPanel.add(installationSettingsLabel);

		JButton launchSettingsLabel = new JButton("Launch Settings");
		launchSettingsLabel.setIcon(getIcon("launch_options_gear.png"));
		launchSettingsLabel.setFont(new Font("Roboto", Font.BOLD, 12));
		launchSettingsLabel.setDoubleBuffered(true);
		launchSettingsLabel.setOpaque(false);
		launchSettingsLabel.setContentAreaFilled(false);
		bottomPanel.add(launchSettingsLabel);

		serverPanel.setVisible(false);
		versionPanel.setVisible(true);

		normalPlayButton.addActionListener(e -> {
			footerLabel.setIcon(getIcon("footer_normalplay_bg.jpg"));
			serverPanel.setVisible(false);
			versionPanel.setVisible(true);
			createPlayPanel(footerPanel);
			footerPanel.repaint();
		});

		dedicatedServerButton.addActionListener(e -> {
			footerLabel.setIcon(getIcon("footer_dedicated_bg.jpg"));
			versionPanel.setVisible(false);
			playPanelButtons.removeAll();
			versionPanel.removeAll();
			serverPanel.setVisible(true);
			createServerPanel(footerPanel);
			footerPanel.repaint();
		});

		centerPanel = new JPanel();
		centerPanel.setDoubleBuffered(true);
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BorderLayout());
		mainPanel.add(centerPanel, BorderLayout.CENTER);

		JLabel background = new JLabel();
		background.setDoubleBuffered(true);
		try {
			Image image = ImageIO.read(Objects.requireNonNull(StarMadeLauncher.class.getResource("/left_panel.png")));
			//Resize the image to the left panel
			image = image.getScaledInstance(800, 500, Image.SCALE_SMOOTH);
			background.setIcon(new ImageIcon(image));
		} catch(IOException exception) {
			exception.printStackTrace();
			flagForRepair();
		}
		centerPanel.add(background, BorderLayout.CENTER);
		//createNewsPanel();
	}

	private void saveLaunchSettings() {
		try {
			FileWriter writer = new FileWriter("launch-settings.json");
			writer.write(getLaunchSettings().toString());
			writer.flush();
			writer.close();
		} catch(IOException exception) {
			exception.printStackTrace();
			flagForRepair();
		}
	}

	private JSONObject getLaunchSettings() {
		File file = new File("launch-settings.json");
		if(!file.exists()) {
			try {
				file.createNewFile();
				JSONObject object = new JSONObject();
				object.put("memory", 2048);
				FileWriter writer = new FileWriter(file);
				writer.write(object.toString());
				writer.flush();
				writer.close();
				return object;
			} catch(IOException exception) {
				exception.printStackTrace();
				flagForRepair();
			}
		}

		try {
			FileReader reader = new FileReader(file);
			String data = IOUtils.toString(reader);
			JSONObject object = new JSONObject(data);
			reader.close();
			return object;
		} catch(IOException exception) {
			exception.printStackTrace();
			flagForRepair();
			return null;
		}
	}

	private int getSystemMemory() {
		return (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024);
	}

	private void createPlayPanel(JPanel footerPanel) {
		JPanel playPanel = new JPanel();
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

		//Version dropdown
		JComboBox<String> versionDropdown = new JComboBox<>();
		versionDropdown.setDoubleBuffered(true);
		versionDropdown.setOpaque(false);

		//Branch dropdown
		JComboBox<String> branchDropdown = new JComboBox<>();
		branchDropdown.setDoubleBuffered(true);
		branchDropdown.setOpaque(false);
		branchDropdown.addItem("Release");
		branchDropdown.addItem("Dev");
		branchDropdown.addItem("Pre-Release");
		branchDropdown.addItem("Archive");
		branchDropdown.addActionListener(e -> {
			versionDropdown.removeAllItems();
			updateVersions(versionDropdown, branchDropdown);
		});
		branchDropdown.setSelectedIndex(lastUsedBranch);
		updateVersions(versionDropdown, branchDropdown);

		versionSubPanel.add(branchDropdown);
		versionSubPanel.add(versionDropdown);
		recreateButtons(playPanel);
	}

	private void recreateButtons(JPanel playPanel) {
		playPanelButtons = new JPanel();
		playPanelButtons.setDoubleBuffered(true);
		playPanelButtons.setOpaque(false);
		playPanelButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
		playPanel.remove(playPanelButtons);
		playPanel.add(playPanelButtons, BorderLayout.EAST);

		if(getLatestVersion(getLastUsedBranch()) != GAME_VERSION && !devMode) {
			JButton updateButton = new JButton(getIcon("update_btn.png"));
			updateButton.setDoubleBuffered(true);
			updateButton.setOpaque(false);
			updateButton.setContentAreaFilled(false);
			updateButton.setBorderPainted(false);
			updateButton.addActionListener(e -> {
				if(updaterThread == null || !updaterThread.isAlive()) updateGame(updateButton);
			});
			playPanelButtons.add(updateButton);
		} else {
			JButton playButton = new JButton(getIcon("launch_btn.png")); //Todo: Reduce button glow so this doesn't look weird
			playButton.setDoubleBuffered(true);
			playButton.setOpaque(false);
			playButton.setContentAreaFilled(false);
			playButton.setBorderPainted(false);
			playButton.addActionListener(e -> {
				dispose();
				if(!checkJavaVersion()) {
					JDialog dialog = new JDialog(this, "Java not found", true);
					dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					dialog.setResizable(false);
					dialog.setLayout(new BorderLayout());
					dialog.setSize(400, 200);
					dialog.setLocationRelativeTo(null);
					dialog.setVisible(true);
					flagForRepair();
					return;
				}
				runStarMade();
				System.exit(0);
			});
			playButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					playButton.setIcon(getIcon("launch_roll.png"));
				}

				@Override
				public void mouseExited(MouseEvent e) {
					playButton.setIcon(getIcon("launch_btn.png"));
				}
			});
			playPanelButtons.add(playButton);
		}
	}

	private boolean checkJavaVersion() {
		if(GAME_VERSION.build.startsWith("0.2")) return new File("./jre8/java.exe").exists();
		else return new File("./jre11/java.exe").exists();
	}

	private String getUserArgs() {
		return userArgs.trim() + " -Xms2048g -Xmx" + getLaunchSettings().getInt("memory") + "g";
	}

	public void runStarMade() { //Todo: Support Linux and Mac
		boolean useJava8 = (GAME_VERSION.build.startsWith("0.2")); //Use Java 11 on version 0.300 and above
		String bundledJavaPath = new File((useJava8) ? "./jre8/java.exe" : "./jre11/java.exe").getPath();
		ProcessBuilder proc = new ProcessBuilder(bundledJavaPath + " " + getUserArgs() +  " -jar StarMade.jar -force");
		try {
			proc.start();
		} catch(IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	private void createServerPanel(JPanel footerPanel) {
		serverPanel = new JPanel();
		//Todo
	}

	private void updateGame(JButton updateButton) {
		String[] options = {"Backup Database", "Backup Everything", "Don't Backup"};
		int choice = JOptionPane.showOptionDialog(this, "Would you like to backup your database, everything, or nothing?", "Backup", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		int backupMode = UpdaterThread.BACKUP_MODE_NONE;
		if(choice == 0) backupMode = UpdaterThread.BACKUP_MODE_DATABASE;
		else if(choice == 1) backupMode = UpdaterThread.BACKUP_MODE_EVERYTHING;

		ImageIcon updateButtonEmpty = getIcon("update_load_empty.png");
		ImageIcon updateButtonFilled = getIcon("update_load_full.png");
		updateButton.setIcon(updateButtonEmpty);

		//Start update process and update progress bar
//		(updaterThread = new UpdaterThread(getLatestVersion(getLastUsedBranch()), backupMode, new File("./")) {
//			@Override
//			public void onProgress(float progress) {
//				int width = updateButtonEmpty.getIconWidth();
//				int height = updateButtonEmpty.getIconHeight();
//				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//				Graphics2D g = image.createGraphics();
//				g.drawImage(updateButtonEmpty.getImage(), 0, 0, null);
//				//Create sub image of filled image
//				int filledWidth = (int) (width * progress);
//				g.drawImage(updateButtonFilled.getImage(), 0, 0, filledWidth, updateButtonFilled.getIconHeight(), 0, 0, filledWidth, updateButtonFilled.getIconHeight(), null);
//				g.dispose();
//				updateButton.setIcon(new ImageIcon(image));
//				updateButton.repaint();
//			}
//
//			@Override
//			public void onFinished() {
//				updateButton.setIcon(getIcon("update_btn.png"));
//				updateButton.repaint();
//			}
//
//			@Override
//			public void onError(Exception exception) {
//				exception.printStackTrace();
//				updateButton.setIcon(getIcon("update_btn.png"));
//				flagForRepair();
//			}
//		}).start();
	}

	private Updater.VersionFile getLastUsedBranch() {
		switch(lastUsedBranch) {
			case 1:
				return Updater.VersionFile.DEV;
			case 2:
				return Updater.VersionFile.PRE;
			case 3:
				return Updater.VersionFile.ARCHIVE;
			default:
				return Updater.VersionFile.RELEASE;
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
				if(version.equals(devVersions.get(0))) versionDropdown.addItem(version.build + " (Latest)");
				else versionDropdown.addItem(version.build);
			}
		} else if(Objects.equals(branchDropdown.getSelectedItem(), "Pre-Release")) {
			for(IndexFileEntry version : preReleaseVersions) {
				if(version.equals(preReleaseVersions.get(0))) versionDropdown.addItem(version.build + " (Latest)");
				else versionDropdown.addItem(version.build);
			}
		} else if(Objects.equals(branchDropdown.getSelectedItem(), "Archive")) {
			for(IndexFileEntry version : archiveVersions) {
				if(version.equals(archiveVersions.get(0))) versionDropdown.addItem(version.build + " (Latest)");
				else versionDropdown.addItem(version.build);
			}
		}
	}

	private void createNewsPanel() {

	}

	private void createForumsPanel() {

	}

	private void createContentPanel() {

	}

	private void createCommunityPanel() {

	}

	private ImageIcon getIcon(String s) {
		try {
			return new ImageIcon(ImageIO.read(Objects.requireNonNull(StarMadeLauncher.class.getResource("/" + s))));
		} catch(IOException exception) {
			return new ImageIcon();
		}
	}

	private ImageIcon getIcon(String s, int width, int height) {
		try {
			return new ImageIcon(ImageIO.read(Objects.requireNonNull(StarMadeLauncher.class.getResource("/" + s))).getScaledInstance(width, height, Image.SCALE_SMOOTH));
		} catch(IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	private boolean isInstallValid() {
		return lookForGame(installDir);
	}

	private void flagForRepair() {
		//Create dialog asking if user wants to repair
		int choice = JOptionPane.showConfirmDialog(this, "The launcher or game installation is invalid. Would you like to repair it?", "Repair", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if(choice == JOptionPane.YES_OPTION) {
			//Todo: Repair launcher
		}
	}

	private IndexFileEntry getLatestVersion(Updater.VersionFile branch) {
		switch(branch) {
			case RELEASE:
				return releaseVersions.get(0);
			case DEV:
				return devVersions.get(0);
			case PRE:
				return preReleaseVersions.get(0);
			case ARCHIVE:
				return archiveVersions.get(0);
			default:
				flagForRepair();
				return null;
		}
	}

	private IndexFileEntry getCurrentVersion() {
		File versionFile = new File("version.txt");
		if(versionFile.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(versionFile));
				String path = reader.readLine();
				String version = reader.readLine();
				String build = reader.readLine();
				reader.close();
				return new IndexFileEntry(build, path, version, getLastUsedBranch());
			} catch(IOException exception) {
				exception.printStackTrace();
				(new ErrorDialog("Error", "IO Error", exception)).setVisible(true);
			}
		}
		return null;
	}

	private void repairLauncher() {
		//Todo: Repair launcher
	}

	private void loadVersionList() throws IOException {
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

	public String getStarMadeStartPath(String installDir) {
		return installDir + File.separator + "StarMade.jar";
	}

	public boolean lookForGame(String installDir) {
		return (new File(getStarMadeStartPath(installDir))).exists();
	}

	public static void displayHelp() {
		System.out.println("StarMade Launcher " + LAUNCHER_VERSION + " Help:");
		System.out.println("-version version selection prompt");
		System.out.println("-no_gui dont start gui (needed for linux dedicated servers)");
		System.out.println("-no_backup dont create backup (default backup is server database only)");
		System.out.println("-backup_all create backup of everything (default backup is server database only)");
		System.out.println("-archive use archive branch (default is release)");
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
}