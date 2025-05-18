package bombfinder;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

// Manages and loads icons for the game, such as bomb, flag, and mine number icons.

public class IconManager {
    private ImageIcon bomb;
    private ImageIcon flag;
    private ImageIcon incorrectFlag;
    private ImageIcon[] mineNumber;
    private ImageIcon unseen;
    private ImageIcon frameIcon;
    private static final int ICONSIZE = 30;

    //Constructs an IconManager and initializes all game icons.

    public IconManager() {
        // Load individual icons using the loadIcon method
        bomb = loadIcon("bomb");
        flag = loadIcon("flag");
        incorrectFlag = loadIcon("incorrect_flag");
        unseen = loadIcon("unseen");
        frameIcon = loadIcon("bomb");
        
        // Initialize the mineNumber array (0-8, where 0 is null for no mines)
        mineNumber = new ImageIcon[9];
        mineNumber[0] = null; // No icon for zero mines
        for (int i = 1; i <= 8; i++) {
            mineNumber[i] = loadIcon("mine_" + i); // Load icons for numbers 1-8
        }
    }
    
    public ImageIcon loadIcon(String iconName) {
        try {
            // Define possible resource paths to locate the icon
            String[] possiblePaths = {
                "/icons/" + iconName + ".png",
                "icons/" + iconName + ".png",
                "/bombfinder/icons/" + iconName + ".png",
                "bombfinder/icons/" + iconName + ".png",
                "/" + iconName + ".png",
                iconName + ".png"
            };
            
            // Try to load the icon using getResourceAsStream
            InputStream stream = null;
            for (String path : possiblePaths) {
                stream = getClass().getResourceAsStream(path);
                if (stream != null) {
                    break; // Exit loop if a valid stream is found
                }
            }
            
            // If not found, try using the class loader
            if (stream == null) {
                for (String path : possiblePaths) {
                    stream = getClass().getClassLoader().getResourceAsStream(path);
                    if (stream != null) {
                        break;
                    }
                }
            }
            
            // Return null if no valid stream is found
            if (stream == null) {
                return null;
            }

            // Read the image data from the stream
            byte[] imageBytes = stream.readAllBytes();
            stream.close();
            
            // Create an ImageIcon from the image data
            ImageIcon icon = new ImageIcon(imageBytes);
            if (icon.getIconWidth() <= 0) {
                return null; // Return null if the icon is invalid
            }
            
            // Scale the image to the predefined size (ICONSIZE x ICONSIZE)
            Image scaledImage = icon.getImage().getScaledInstance(ICONSIZE, ICONSIZE, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
            
        } catch (IOException e) {
            return null; // Return null if an I/O error occurs
        } catch (Exception e) {
            return null; // Return null for any other unexpected errors
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
