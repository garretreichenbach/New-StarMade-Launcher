package smlauncher;

import smlauncher.contents.MainPanel;

import javax.swing.*;
import java.io.IOException;

public class GoodLauncher {
    public static void main(String[] args) throws IOException {
        VersionList.loadVersionList();
        JFrame frame = new JFrame("Dead Game Launcher 3.0");
        frame.setLayout(null);
        frame.setVisible(true);
        frame.setSize(961, 540);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        MainPanel mainPanel = new MainPanel();
        frame.setContentPane(mainPanel);

        // Reisize the image because java has issues with repainting
        frame.setSize(960, 540);
    }
}
