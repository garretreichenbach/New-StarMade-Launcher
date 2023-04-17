package smlauncher.contents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class UrlOpenButton extends JPanel implements MouseListener {
    private final String url;
    private String name;

    public UrlOpenButton(JPanel panel, int w, int h, String url, String name){
        this.name = name;
        addMouseListener(this);
        this.url = url;
        this.setLayout(null);
        this.setVisible(true);
        this.setSize(w, h);
        setPreferredSize(new Dimension(w, h));
        setOpaque(false);

        panel.add(this);

    }
    private ButtonState state = ButtonState.INACTIVE;

    public void paintButton(Graphics g){
        g.setColor(state.getBackground());
        g.fillRect(0,0, getWidth(), getHeight());

        g.setColor(state.getOuterLine());
        g.drawRect(0,0, getWidth() - 1, getHeight() - 1);

        g.setColor(state.getInnerLine());
        g.drawRect(1,1, getWidth() - 3, getHeight() - 3);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        paintButton(g);

        Font font = new Font("Roboto", Font.PLAIN, 14);
        g.setFont(font);
        g.setColor(Color.WHITE);
        PanelSwitcherButton.drawCenteredString(g, name, new Rectangle(0, 0, getWidth(), getHeight()), g.getFont());
    }
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        state = ButtonState.CLICKED;
        repaint();
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        state = ButtonState.HOVER;
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        state = ButtonState.HOVER;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        state = ButtonState.INACTIVE;
        repaint();
    }
}
enum ButtonState{
    INACTIVE(new Color(16,21,28), new Color(16, 60, 77), new Color(9, 12, 22)),
    HOVER(new Color(17, 64, 80), new Color(33, 115, 133), new Color(9, 12, 22)),
    CLICKED(new Color(27, 124, 156), new Color(17, 185, 221), new Color(9, 12, 22)),
    ;

    private final Color background;
    private final Color innerLine;
    private final Color outerLine;

    ButtonState(Color background, Color innerLine, Color outerLine) {
        this.background = background;
        this.innerLine = innerLine;
        this.outerLine = outerLine;
    }

    public Color getBackground() {
        return background;
    }

    public Color getInnerLine() {
        return innerLine;
    }

    public Color getOuterLine() {
        return outerLine;
    }
}