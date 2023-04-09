package smlauncher.contents;

import smlauncher.Images;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
    public MainPanel(){
        setLayout(null);
        // Initialize all the side buttons
        int x = 30;
        CoolButton news = new CoolButton(this, x,30, 150, 40, "button", "NEWS");
        int i = 85;
        CoolButton options = new CoolButton(this, x,30 + i, 150, 40, "button", "OPTIONS");
        CoolButton links = new CoolButton(this, x,30 + (i *2), 150, 40, "button", "LINKS");
        CoolButton info = new CoolButton(this, x,30  + (i * 3), 150, 40, "button", "INFO");

        LaunchPanel bgPanel = new LaunchPanel(this, x, 361, "bar");
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(Images.get("background"), 0, 0, null);
    }
}
