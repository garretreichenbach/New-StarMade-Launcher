package smlauncher.contents;

import smlauncher.Images;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class CoolButton extends JPanel implements MouseListener {
    private boolean active = false;
    private final int x;
    private final int y;
    private final int w;
    private final int h;
    private final String img;
    private String name;

    public CoolButton(JPanel panel, int x, int y, int w, int h, String img, String name){
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

        panel.add(this);

    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if(active){
            g.drawImage(Images.get(img + "A"), 0, 0, null);
        }else{
            g.drawImage(Images.get(img), 0, 0, null);
        }
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        g.setColor(Color.WHITE);
        drawCenteredString(g, name, new Rectangle(0, 0, getWidth(), getHeight()), g.getFont());
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

    /**
     * https://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java
     * Draw a String centered in the middle of a Rectangle.
     *
     * @param g The Graphics instance.
     * @param text The String to draw.
     * @param rect The Rectangle to center the text in.
     */
    public void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
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
