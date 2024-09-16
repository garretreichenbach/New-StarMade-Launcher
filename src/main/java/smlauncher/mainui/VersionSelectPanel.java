package smlauncher.mainui;

import smlauncher.LaunchSettings;
import smlauncher.VersionRegistry;
import smlauncher.starmade.GameBranch;
import smlauncher.starmade.IndexFileEntry;
import smlauncher.util.Palette;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VersionSelectPanel extends JPanel {

	private final JComboBox<String> branchDropdown, versionDropdown;
	private final VersionRegistry versionRegistry;
	private final JTextField portField;

	public VersionSelectPanel(boolean serverMode, GameBranch lastUsedBranch, VersionRegistry versionRegistry) {
		this.versionRegistry = versionRegistry;

		setDoubleBuffered(true);
		setOpaque(false);
		setLayout(new BorderLayout());

		JPanel versionSubPanel = new JPanel();
		versionSubPanel.setDoubleBuffered(true);
		versionSubPanel.setOpaque(false);
		versionSubPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(versionSubPanel, BorderLayout.SOUTH);

		//Change color of arrow
		UIDefaults defaults = new UIDefaults();
		defaults.put("ComboBox:\"ComboBox.arrowButton\"[Enabled].backgroundPainter", Palette.buttonColor);

		//Branch dropdown
		branchDropdown = new DropdownMenu(defaults);
		branchDropdown.addItem("Release");
		branchDropdown.addItem("Dev");
		branchDropdown.addItem("Pre-Release");
		branchDropdown.setSelectedIndex(lastUsedBranch.index);
		versionSubPanel.add(branchDropdown);

		//Version dropdown
		versionDropdown = new DropdownMenu(defaults);
		versionSubPanel.add(versionDropdown);

		// Item Listeners
		branchDropdown.addItemListener(e -> onSelectBranch());
		versionDropdown.addItemListener(e -> onSelectVersion());

		// Initial State
		setDropdownVersionsList(lastUsedBranch, versionRegistry);
		selectLastUsedVersion();

		portField = new PortField("4242");
		versionSubPanel.add(portField);
		setServerMode(serverMode);
	}

	// Item Listeners
	private void onSelectBranch() {
		int branchIndex = branchDropdown.getSelectedIndex();

		//Update UI components
		GameBranch branch = GameBranch.getForIndex(branchIndex);
		setDropdownVersionsList(branch, versionRegistry);
		onSelectBranch(branchIndex);
	}

	public void onSelectBranch(int branchIndex) {
	}

	private void onSelectVersion() {
		if(versionDropdown.getSelectedIndex() == -1) return;
		onSelectVersion(versionDropdown.getItemAt(versionDropdown.getSelectedIndex()).split(" ")[0]);
	}

	public void onSelectVersion(String version) {
	}

	// Private UI Helper Methods

	// TODO maybe save and don't re-add every time
	private void setDropdownVersionsList(GameBranch branch, VersionRegistry versionRegistry) {
		List<IndexFileEntry> versions = versionRegistry.getVersions(branch);
		if (versions == null) return;

		// Add versions to dropdown
		versionDropdown.removeAllItems();
		for (IndexFileEntry version : versions) {
			if (version.equals(versions.get(0))) versionDropdown.addItem(version.version + " (Latest)");
			else versionDropdown.addItem(version.version);
		}
	}

	private void selectLastUsedVersion() {
		String lastUsedVersion = LaunchSettings.getLastUsedVersion();
		if (lastUsedVersion.isEmpty()) lastUsedVersion = "NONE";
		//Select last used version in dropdown if it exists
		for (int i = 0; i < versionDropdown.getItemCount(); i++) {
			if (versionDropdown.getItemAt(i).equals(lastUsedVersion)) {
				versionDropdown.setSelectedIndex(i);
				break;
			}
		}
	}

	// Getters and Setters

	public void setServerMode(boolean serverMode) {
		portField.setVisible(serverMode);
	}

	public int getPort() {
		return Integer.parseInt(portField.getText());
	}

}
