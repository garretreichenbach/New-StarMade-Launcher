package smlauncher.contents;

import smlauncher.Images;
import smlauncher.libt.BBCodeToHTMLConverter;
import smlauncher.libt.SteamNewsAPI;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class NewsPane extends JEditorPane {
    public static int X = 226;
    public static int Y = 45;
    public NewsPane(MainPanel mainPanel) {
        mainPanel.add(this);
        Image bg = Images.get("npane");
        setBounds(226, 45, bg.getWidth(null), bg.getHeight(null));
        setBackground(new Color(0, 0, 0, 0));
        setForeground(Color.white);
        setOpaque(false);
        setContentType("text/html");

        StringBuilder contents = new StringBuilder();
        contents.append("<hr>");
//        for (SteamNewsAPI.NewsPost post : SteamNewsAPI.getPosts()) {
        SteamNewsAPI.NewsPost post = SteamNewsAPI.getPosts().get(1);
        System.out.println(post.getUrl());
        ArrayList<String> lines = BBCodeToHTMLConverter.convert(post.getContents());
        lines = BBCodeToHTMLConverter.insertColors(lines, "#ffffff");

        for (String line : lines) {
            contents.append(line);
        }
        setText(contents.toString());


    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(Images.get("npane"), 0, 0, null);
        super.paintComponent(g);
    }
}
