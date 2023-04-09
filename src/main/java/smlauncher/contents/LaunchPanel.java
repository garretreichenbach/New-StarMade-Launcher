package smlauncher.contents;

import smlauncher.GoodLauncher;
import smlauncher.Images;
import smlauncher.InstalledUtils;
import smlauncher.VersionList;
import smlauncher.util.IndexFileEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.util.Objects;

public class LaunchPanel extends JPanel implements MouseListener {
    private boolean active = false;
    private final int x;
    private final int y;
    private final int w;
    private final int h;
    private final String img;

    public LaunchPanel(JPanel panel, int x, int y, String img) {
        this.x = x;
        this.y = y;

        Image image = Images.get(img);
        int w = image.getWidth(null);
        int h = image.getHeight(null);

        this.w = w;
        this.h = h;
        this.img = img;
        setLayout(null);
        setVisible(true);
        setLocation(x, y);
        setSize(w, h);

        //Version dropdown
        JComboBox<IndexFileEntry> versionDropdown = new JComboBox<>();
        versionDropdown.setOpaque(false);
        versionDropdown.setLocation(15, 65);
        versionDropdown.setSize(150, 30);

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
        IndexFileEntry installedVersion = InstalledUtils.getInstalledVersion();
        branchDropdown.setSelectedIndex(0);
        updateVersions(versionDropdown, branchDropdown);
        branchDropdown.setLocation(15, 15);
        branchDropdown.setSize(150, 30);

        add(versionDropdown);
        add(branchDropdown);

        JButton launchButton = new JButton("Update");
        launchButton.setLocation(w - 120 - 15, h / 2 - 15);
        launchButton.setSize(120, 30);
        launchButton.addActionListener(e -> {
            VersionList.updateGame();
        });

        add(launchButton);

        totalBar = new JProgressBar(0, 1000);
        totalBar.setBounds(200, 27, 500, 30);
        totalBar.setString("download bar");
        totalBar.setStringPainted(true);
        add(totalBar);

        indBar = new JProgressBar(0, 1000);
        indBar.setBounds(200, 57, 500, 25);
        indBar.setString("individual prog bar");
        indBar.setStringPainted(true);
        add(indBar);

        panel.add(this);
    }

    public static JProgressBar totalBar;
    public static JProgressBar indBar;

    private void updateVersions(JComboBox<IndexFileEntry> versionDropdown, JComboBox<String> branchDropdown) {
        Object selected = branchDropdown.getSelectedItem();
        if (Objects.equals(selected, "Release")) {
            VersionList.releaseVersions.forEach(versionDropdown::addItem);
        } else if (Objects.equals(selected, "Dev")) {
            VersionList.devVersions.forEach(versionDropdown::addItem);
        } else if (Objects.equals(selected, "Pre-Release")) {
            VersionList.preReleaseVersions.forEach(versionDropdown::addItem);
        } else if (Objects.equals(selected, "Archive")) {
            VersionList.archiveVersions.forEach(versionDropdown::addItem);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(Images.get(img), 0, 0, null);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        active = true;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        active = false;
        repaint();
    }
}
