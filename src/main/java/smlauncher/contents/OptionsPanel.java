package smlauncher.contents;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by Jake on 2023-04-26

 * To make:
 * Max/Initial memory
 * Serber port
 * JVM args
 * program args
 * Force download all button
 * install dir
 */

public class OptionsPanel extends JPanel {
    public static JSlider maxMemoryGb;
    public static JTextArea maxMemoryText;
    public OptionsPanel() {
        setBounds(NewsPane.X, NewsPane.Y, NewsPane.W, NewsPane.H);
        setLayout(null);
        setBackground(Color.LIGHT_GRAY);

        maxMemoryGb = new JSlider(JSlider.HORIZONTAL, 1, 32, 2);
        maxMemoryText = new JTextArea("Max Memory (" + maxMemoryGb.getValue() + " GB)");
        maxMemoryGb.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                maxMemoryText.setText("Max Memory (" + maxMemoryGb.getValue() + " GB)");
            }
        });
        maxMemoryText.setEditable(false);
        maxMemoryGb.setBounds(130,0, 170, 20);
        maxMemoryText.setBounds(0,0, 130, 20);
        add(maxMemoryGb);
        add(maxMemoryText);


        JTextArea portInput = new JTextArea("4242");
        JTextArea portText = new JTextArea("Server Port: ");
        portText.setEditable(false);
        portText.setBounds(0,40, 80, 15);
        portInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == 8) {
                    //backspace
                    return;
                }
                if(e.getKeyChar() < '0' || e.getKeyChar() > '9'){
                    e.setKeyChar('0');
                }
            }
        });
        portInput.setBounds(80, 40, 170, 15);
        add(portInput);
        add(portText);

        JTextArea programArgsText = new JTextArea("Program Args: ");
        JTextArea programArgsInput = new JTextArea("-force");
        programArgsText.setEditable(false);
        programArgsText.setBounds(0, 60, 100, 15);
        programArgsInput.setBounds(110, 60, 400, 15);
        add(programArgsText);
        add(programArgsInput);

        JTextArea jvmArgsText = new JTextArea("JVM Args: ");
        JTextArea jvmArgsInput = new JTextArea("-Xincgc");
        jvmArgsText.setEditable(false);
        jvmArgsText.setBounds(0, 80, 100, 15);
        jvmArgsInput.setBounds(110, 80, 400, 15);
        add(jvmArgsText);
        add(jvmArgsInput);
    }
}
