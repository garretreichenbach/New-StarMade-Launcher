package smlauncher.mainui.settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * A generic dialog for changing launch settings.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class SettingsDialog extends JDialog {

	private final JPanel dialogPanel;

	public SettingsDialog(String title, int width, int height) {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		setResizable(false);
		setTitle(title);
		setSize(width, height);
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);
		setLayout(new BorderLayout());
//		setBackground(Palette.paneColor);
//		setForeground(Palette.foregroundColor);

		dialogPanel = new JPanel();
		dialogPanel.setDoubleBuffered(true);
		dialogPanel.setOpaque(true);
//		dialogPanel.setBackground(Palette.paneColor);
//		dialogPanel.setForeground(Palette.foregroundColor);
		dialogPanel.setLayout(new BorderLayout());
		add(dialogPanel, BorderLayout.CENTER);
	}

	protected void addToDialog(JComponent component, Object constraints) {
		dialogPanel.add(component, constraints);
	}

	protected static JPanel createSaveSettingsPanel(ActionListener saveAction, ActionListener cancelAction) {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setDoubleBuffered(true);
		buttonPanel.setOpaque(true);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// Commit changes
		JButton saveButton = new JButton("Save");
		saveButton.setFont(new Font("Roboto", Font.BOLD, 12));
		saveButton.setDoubleBuffered(true);
		buttonPanel.add(saveButton);

		// Go back
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setFont(new Font("Roboto", Font.BOLD, 12));
		cancelButton.setDoubleBuffered(true);
		buttonPanel.add(cancelButton);
		saveButton.addActionListener(saveAction);
		cancelButton.addActionListener(cancelAction);
		return buttonPanel;
	}

}
