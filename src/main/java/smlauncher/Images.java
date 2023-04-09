package smlauncher;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

public class Images {
    private static HashMap<String, Image> map = new HashMap<>();
    public static Image get(String str){
        Image image = map.get(str);
        if(image == null){
            try {
                image = ImageIO.read(GoodLauncher.class.getResourceAsStream("/" + str.toLowerCase(Locale.ENGLISH) + ".png"));
                map.put(str, image);
                return image;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return image;
    }

    public static ImageIcon getII(String str){
        Image image = get(str);
        return new ImageIcon(image);
    }
}




