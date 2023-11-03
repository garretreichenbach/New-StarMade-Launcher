package smlauncher.contents;

import smlauncher.LauncherSaveFile;

import javax.swing.*;
import java.awt.*;

public class OptionsPanel extends JPanel {
    public OptionsPanel() {
        setBounds(NewsPane.X, NewsPane.Y, NewsPane.W, NewsPane.H);
        setLayout(null);
        setBackground(new Color(0,0,0,10));
        LauncherSaveFile inst = LauncherSaveFile.getInstance();

        JTextArea installationDir = new JTextArea(inst.installationDir);
        installationDir.setBounds(0,0, 300,20);
        add(installationDir);

        JTextArea javaHome = new JTextArea(inst.jre7Dir);
        javaHome.setBounds(0,20, 300,20);
        add(javaHome);

        JTextArea javaOptions = new JTextArea(inst.jre18Dir);
        javaOptions.setBounds(0,40, 300,20);
        add(javaOptions);
    }
}
