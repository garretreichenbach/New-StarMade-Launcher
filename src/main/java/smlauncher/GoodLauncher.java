package smlauncher;

import smlauncher.contents.MainPanel;

import javax.swing.*;
import java.io.IOException;

public class GoodLauncher {
    public static int W = 960;
    public static int H = 570;
    public static void main(String[] args) throws IOException, UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        VersionList.loadVersionList();

        JFrame frame = new JFrame("Dead Game Launcher 3.0");
        frame.setLayout(null);
        frame.setVisible(true);
        frame.setSize(W+1, H);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        MainPanel mainPanel = new MainPanel();
        frame.setContentPane(mainPanel);

        // Reisize the image because java has issues with repainting
        frame.setSize(W, H);
    }
}
