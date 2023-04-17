package smlauncher.contents;

import smlauncher.Images;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PanelSwitcherButton extends JPanel implements MouseListener {
    private boolean active = false;
    private final int x;
    private final int y;
    private final int w;
    private final int h;
    private final String img;
    private String name;
    public static int idSelect = 0;
    int id;
    public PanelSwitcherButton(JPanel panel, int x, int y, int w, int h, String img, String name){
        id = idSelect;
        idSelect++;
        this.name = name;
        addMouseListener(this);
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.img = img;
        this.setLayout(null);
        this.setVisible(true);
        this.setLocation(x, y);
        this.setSize(w, h);
        setOpaque(false);

        panel.add(this);

    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String currentImage = img;
        if(held){
            currentImage += "S";
        }else if(active){
            currentImage += "A";
        }

        g.drawImage(Images.get(currentImage), 0, 0, null);

        Font font = new Font("Roboto", Font.BOLD, 20);
        g.setFont(font);
        g.setColor(Color.WHITE);
        drawCenteredString(g, name, new Rectangle(0, 0, getWidth(), getHeight()), g.getFont());
    }
    @Override
    public void mouseClicked(MouseEvent e) {
    }
    boolean held = false;

    @Override
    public void mousePressed(MouseEvent e) {
        held = true;
        MainPanel.inst.switchActivePane(id);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        held = false;
        repaint();
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

    /**
     * https://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java
     * Draw a String centered in the middle of a Rectangle.
     *
     * @param g The Graphics instance.
     * @param text The String to draw.
     * @param rect The Rectangle to center the text in.
     */
    public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }
}
