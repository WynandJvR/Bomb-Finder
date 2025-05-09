package bombfinder;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class IconManager {
    private ImageIcon bomb;
    private ImageIcon flag;
    private ImageIcon incorrectFlag;
    private ImageIcon[] mineNumber;
    private ImageIcon unseen;
    private ImageIcon frameIcon;
    private static final int ICONSIZE = 30;

    public IconManager() {
        bomb = loadIcon("bomb");
        flag = loadIcon("flag");
        incorrectFlag = loadIcon("incorrect_flag");
        unseen = loadIcon("unseen");
        frameIcon = loadIcon("bomb");
        
        mineNumber = new ImageIcon[9];
        mineNumber[0] = null;
        for (int i = 1; i <= 8; i++) {
            mineNumber[i] = loadIcon("mine_" + i);
        }
    }

    public ImageIcon loadIcon(String iconName) {
        try {
            String[] possiblePaths = {
                "/icons/" + iconName + ".png",
                "icons/" + iconName + ".png",
                "/bombfinder/icons/" + iconName + ".png",
                "bombfinder/icons/" + iconName + ".png",
                "/" + iconName + ".png",
                iconName + ".png"
            };
            
            InputStream stream = null;
            for (String path : possiblePaths) {
                stream = getClass().getResourceAsStream(path);
                if (stream != null) {
                    break;
                }
            }
            
            if (stream == null) {
                for (String path : possiblePaths) {
                    stream = getClass().getClassLoader().getResourceAsStream(path);
                    if (stream != null) {
                        break;
                    }
                }
            }
            
            if (stream == null) {
                return null;
            }

            byte[] imageBytes = stream.readAllBytes();
            stream.close();
            
            ImageIcon icon = new ImageIcon(imageBytes);
            if (icon.getIconWidth() <= 0) {
                return null;
            }
            
            Image scaledImage = icon.getImage().getScaledInstance(ICONSIZE, ICONSIZE, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
            
        } catch (IOException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public ImageIcon getBomb() {
        return bomb;
    }

    public ImageIcon getFlag() {
        return flag;
    }

    public ImageIcon getIncFlag() {
        return incorrectFlag;
    }

    public ImageIcon getMineNumber(int number) {
        if (number >= 0 && number < mineNumber.length) {
            return mineNumber[number];
        }
        return null;
    }

    public ImageIcon getUnseen() {
        return unseen;
    }

    public Image getFrameIcon() {
        return frameIcon != null ? frameIcon.getImage() : null;
    }
}