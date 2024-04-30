package smlauncher.mainui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * A dialog that prompts the user to download the latest launcher version.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class LauncherUpdateDialog extends JDialog {

	public LauncherUpdateDialog(
			String title, int width, int height,
			ActionListener updateAction, ActionListener cancelAction
	) {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		setResizable(false);
		setTitle(title);
		setSize(width, height);
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);
		setLayout(new BorderLayout());

		JPanel descPanel = createDescPanel();
		add(descPanel);

		JPanel buttonPanel = createButtonPanel(updateAction, cancelAction);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private JPanel createDescPanel() {
		JPanel descPanel = new JPanel();
		descPanel.setDoubleBuffered(true);
		descPanel.setOpaque(true);
		descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.Y_AXIS));

		JLabel descLabel = new JLabel("A new launcher update is available, please update to continue.");
		descLabel.setDoubleBuffered(true);
		descLabel.setOpaque(true);
		descLabel.setFont(new Font("Roboto", Font.BOLD, 16));
		descPanel.add(descLabel);
		return descPanel;
	}

	private JPanel createButtonPanel(ActionListener updateAction, ActionListener cancelAction) {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setDoubleBuffered(true);
		buttonPanel.setOpaque(true);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		JButton updateButton = new JButton("Update");
		updateButton.setDoubleBuffered(true);
		updateButton.setOpaque(true);
		updateButton.setFont(new Font("Roboto", Font.BOLD, 12));
		updateButton.addActionListener(e -> {
			// Close the dialog and perform the action
			dispose();
			updateAction.actionPerformed(e);
		});
		buttonPanel.add(updateButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setDoubleBuffered(true);
		cancelButton.setOpaque(true);
		cancelButton.setFont(new Font("Roboto", Font.BOLD, 12));
		cancelButton.addActionListener(e -> {
			// Close the dialog and perform the action
			dispose();
			cancelAction.actionPerformed(e);
		});
		buttonPanel.add(cancelButton);
		return buttonPanel;
	}

}
