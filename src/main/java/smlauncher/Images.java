package smlauncher;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

public class Images {
    private static HashMap<String, Image> map = new HashMap<>();
    private static ImageFilter filter = new RGBImageFilter() {
        public int filterRGB(int x, int y, int rgb) {
            rgb &= 0x00FFFFFF;
            rgb |= 0xAA000000;
            return rgb;
        }
    };

    public static Image get(String str){
        Image image = map.get(str);
        if(image == null){
            try {
                image = ImageIO.read(GoodLauncher.class.getResourceAsStream("/" + str.toLowerCase(Locale.ENGLISH) + ".png"));

//                if(str.startsWith("button") || str.startsWith("bar")){
//                    ImageProducer filteredImgProd = new FilteredImageSource(image.getSource(), filter);
//                    image = Toolkit.getDefaultToolkit().createImage(filteredImgProd);
//                }

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




