package smlauncher.misc;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

import static smlauncher.StarMadeLauncher.BUG_REPORT_URL;

// TODO seems like duplicate of starmade.ErrorDialog

/**
 * Simple dialog for showing error messages.
 *
 * @author TheDerpGamer
 */
public class MiscErrorDialog extends JDialog {

	public MiscErrorDialog(ErrorType type, String description, ErrorCallback callback) {
		setTitle("Error");
		setSize(500, 300);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setAlwaysOnTop(true);
		setLayout(new BorderLayout());
		setResizable(false);

		JPanel errorPanel = new JPanel();
		errorPanel.setLayout(new BorderLayout());
		errorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(errorPanel, BorderLayout.CENTER);

		JLabel errorLabel = new JLabel();
		errorLabel.setIcon(type.icon);
		errorLabel.setText("<html><div style='text-align: center;'>" + description + "</div></html>");
		errorPanel.add(errorLabel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		add(buttonPanel, BorderLayout.SOUTH);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(e -> {
			dispose();
			if(type == ErrorType.FATAL) System.exit(-1);
			else if(callback != null) callback.onOk();
		});
		buttonPanel.add(okButton);

		JButton reportButton = new JButton("Report");
		reportButton.addActionListener(e -> {
			if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				try {
					Desktop.getDesktop().browse(new URI(BUG_REPORT_URL)); //Todo: Send error report to server or something
				} catch(Exception exception) {
					exception.printStackTrace();
				}
			}
		});
		buttonPanel.add(reportButton);
	}

	public enum ErrorType {
		INFO(UIManager.getIcon("OptionPane.informationIcon")),
		WARNING(UIManager.getIcon("OptionPane.warningIcon")),
		ERROR(UIManager.getIcon("OptionPane.errorIcon")),
		FATAL(UIManager.getIcon("OptionPane.errorIcon"));

		public final Icon icon;

		ErrorType(Icon icon) {
			this.icon = icon;
		}
	}

	public interface ErrorCallback {
		void onOk();
	}
}
