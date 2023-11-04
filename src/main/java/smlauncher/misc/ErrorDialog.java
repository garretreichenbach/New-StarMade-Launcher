package smlauncher.misc;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

import static smlauncher.StarMadeLauncher.BUG_REPORT_URL;

/**
 * Simple dialog for showing error messages.
 *
 * @author TheDerpGamer
 */
public class ErrorDialog extends JDialog {

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

	public ErrorDialog(ErrorType type, String description, ErrorCallback callback) {
		setTitle("Error");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		setSize(500, 300);
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);
		setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(panel, BorderLayout.CENTER);

		JLabel label = new JLabel();
		label.setIcon(type.icon);
		label.setText("<html><div style='text-align: center;'>" + description + "</div></html>");
		panel.add(label, BorderLayout.CENTER);

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
	}
}
