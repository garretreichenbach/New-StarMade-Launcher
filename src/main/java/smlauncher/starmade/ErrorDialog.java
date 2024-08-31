package smlauncher.starmade;

import javax.swing.*;
import java.awt.*;

// TODO seems like duplicate of misc.ErrorDialog

/**
 * Simple dialog for showing error messages.
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class ErrorDialog extends JDialog {

	public ErrorDialog(String error, String description, Exception exception) {
		setTitle("Error");
		setSize(400, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		setResizable(false);

		JPanel errorPanel = new JPanel();
		errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
		errorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel errorLabel = new JLabel(error);
		errorLabel.setFont(new Font("Arial", Font.BOLD, 16));
		errorPanel.add(errorLabel);

		JLabel descriptionLabel = new JLabel(description);
		descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		errorPanel.add(descriptionLabel);
		add(errorPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		add(buttonPanel, BorderLayout.SOUTH);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(e -> dispose());
		buttonPanel.add(okButton);
	}
}
