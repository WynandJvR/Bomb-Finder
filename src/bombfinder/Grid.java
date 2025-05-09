package bombfinder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class Grid extends JFrame {

    private JPanel grid;
    private JPanel controlPanel;
    private IconManager iconManager;
    private JButton[][] buttons;
    private boolean[][] bombs;
    private boolean[][] revealed;
    private boolean[][] flagged;
    private boolean gameOver;
    private static int ROWS = 15;
    private static int COLS = 15;
    private static int NUM_BOMBS = 40;
    private JLabel statusLabel;
    private JButton restartButton;

    public Grid() {
        // Initialize icon manager first to make sure icons are loaded
        iconManager = new IconManager();
        
        // Create a more complete user interface
        setLayout(new BorderLayout());
        
        // Create control panel with restart button at the top
        controlPanel = new JPanel();
        restartButton = new JButton("New Game");
        restartButton.addActionListener(e -> restartGame());
        controlPanel.add(restartButton);
        add(controlPanel, BorderLayout.NORTH);
        
        // Add status bar at the bottom
        statusLabel = new JLabel("Find all bombs! Left click to reveal, right click to flag.");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        add(statusLabel, BorderLayout.SOUTH);
        
        // Game grid in the center
        grid = new JPanel();
        grid.setLayout(new GridLayout(ROWS, COLS));
        add(grid, BorderLayout.CENTER);
        
        // Initialize game arrays
        buttons = new JButton[ROWS][COLS];
        bombs = new boolean[ROWS][COLS];
        revealed = new boolean[ROWS][COLS];
        flagged = new boolean[ROWS][COLS];
        gameOver = false;

        // Set the window icon
        Image frameIcon = iconManager.getFrameIcon();
        if (frameIcon != null) {
            setIconImage(frameIcon);
        } else {
            System.err.println("Frame icon is null, window will use default icon");
        }

        // Initialize game
        setGrid();
        placeBombs();

        // Window setup
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Bomb Finder");
        this.setSize(600, 650); // Added some extra height for status bar
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
        // Report on icon status to help with debugging
        reportIconStatus();
    }
    
    private void reportIconStatus() {
        System.out.println("Icon Status Report:");
        System.out.println("- Bomb icon: " + (iconManager.getBomb() != null ? "Loaded" : "Missing"));
        System.out.println("- Flag icon: " + (iconManager.getFlag() != null ? "Loaded" : "Missing"));
        System.out.println("- Incorrect flag icon: " + (iconManager.getIncFlag() != null ? "Loaded" : "Missing"));
        System.out.println("- Unseen icon: " + (iconManager.getUnseen() != null ? "Loaded" : "Missing"));
        System.out.println("- Frame icon: " + (iconManager.getFrameIcon() != null ? "Loaded" : "Missing"));
        for (int i = 1; i <= 8; i++) {
            System.out.println("- Mine " + i + " icon: " + (iconManager.getMineNumber(i) != null ? "Loaded" : "Missing"));
        }
    }

    public void setGrid() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                buttons[i][j] = new JButton();
                
                // Set initial icon or fallback text
                ImageIcon emptyIcon = iconManager.getUnseen();
                if (emptyIcon != null) {
                    buttons[i][j].setIcon(emptyIcon);
                } else {
                    buttons[i][j].setText("?");
                    buttons[i][j].setBackground(Color.LIGHT_GRAY);
                }

                // For accessibility and better UI
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].setMargin(new Insets(0, 0, 0, 0));

                // Add action for left click (reveal cell)
                int row = i, col = j;
                buttons[i][j].addActionListener(e -> revealCell(row, col));
                
                // Add action for right click (flag cell)
                buttons[i][j].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            flagCell(row, col);
                        }
                    }
                });

                grid.add(buttons[i][j]);
            }
        }
    }

    public void placeBombs() {
        Random rand = new Random();
        int bombsPlaced = 0;
        
        // Status update
        statusLabel.setText("Bombs to find: " + NUM_BOMBS);
        
        while (bombsPlaced < NUM_BOMBS) { 
            int row = rand.nextInt(ROWS);
            int col = rand.nextInt(COLS);

            if (!bombs[row][col]) {
                bombs[row][col] = true;
                bombsPlaced++;
            }
        }
    }

    private void revealCell(int row, int col) {
        // Don't do anything if the game is over or the cell is already revealed or flagged
        if (gameOver || revealed[row][col] || flagged[row][col]) return;
        
        revealed[row][col] = true;
        buttons[row][col].setEnabled(false);

        if (bombs[row][col]) {
            // Player hit a bomb
            ImageIcon bombIcon = iconManager.getBomb();
            if (bombIcon != null) {
                buttons[row][col].setIcon(bombIcon);
            } else {
                buttons[row][col].setText("💣");
                buttons[row][col].setBackground(Color.RED);
            }
            
            gameOver = true;
            statusLabel.setText("Game Over! You hit a bomb!");
            JOptionPane.showMessageDialog(this, "Game Over! You hit a bomb!\nClick 'Play Again' to start a new game.");
            revealAllBombs();
            
        } else {
            // Cell is safe, count adjacent bombs
            int adjacentBombs = countAdjacentBombs(row, col);
            
            if (adjacentBombs > 0) {
                // Cell has numbered hint
                ImageIcon numberIcon = iconManager.getMineNumber(adjacentBombs);
                if (numberIcon != null) {
                    buttons[row][col].setIcon(numberIcon);
                } else {
                    // Fallback without icons - colored numbers
                    buttons[row][col].setText(String.valueOf(adjacentBombs));
                    buttons[row][col].setIcon(null); // Remove the unseen icon
                    
                    // Set text color based on number
                    Color[] numberColors = {
                        Color.BLUE,        // 1
                        new Color(0, 128, 0),  // 2 (dark green)
                        Color.RED,         // 3
                        new Color(128, 0, 128), // 4 (purple)
                        new Color(128, 0, 0),   // 5 (maroon)
                        new Color(64, 224, 208), // 6 (turquoise)
                        Color.BLACK,       // 7
                        Color.GRAY         // 8
                    };
                    
                    if (adjacentBombs > 0 && adjacentBombs <= numberColors.length) {
                        buttons[row][col].setForeground(numberColors[adjacentBombs - 1]);
                    }
                    
                    buttons[row][col].setBackground(Color.WHITE);
                }
            } else {
                // Empty cell, set background and recursively reveal adjacent cells
                buttons[row][col].setIcon(null);
                buttons[row][col].setBackground(Color.WHITE);
                revealAdjacentCells(row, col);
            }
            
            // Check if the player has won
            if (checkWin()) {
                gameOver = true;
                statusLabel.setText("You Win! All non-bomb cells revealed!");
                JOptionPane.showMessageDialog(this, "You Win! All non-bomb cells revealed!\nClick 'Play Again' to start a new game.");
            }
        }
    }

    private void flagCell(int row, int col) {
        if (gameOver || revealed[row][col]) return;
        
        flagged[row][col] = !flagged[row][col];
        
        if (flagged[row][col]) {
            // Flag the cell
            ImageIcon flagIcon = iconManager.getFlag();
            if (flagIcon != null) {
                buttons[row][col].setIcon(flagIcon);
            } else {
                buttons[row][col].setText("🚩");
                buttons[row][col].setBackground(Color.YELLOW);
            }
        } else {
            // Unflag the cell
            ImageIcon unseenIcon = iconManager.getUnseen();
            if (unseenIcon != null) {
                buttons[row][col].setIcon(unseenIcon);
            } else {
                buttons[row][col].setText("?");
                buttons[row][col].setBackground(Color.LIGHT_GRAY);
            }
        }
        
        // Count and display remaining flags
        int flagsPlaced = countFlags();
        statusLabel.setText("Bombs to find: " + (NUM_BOMBS - flagsPlaced));
    }
    
    private int countFlags() {
        int count = 0;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (flagged[i][j]) count++;
            }
        }
        return count;
    }

    private int countAdjacentBombs(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int r = row + i, c = col + j;
                if (r >= 0 && r < ROWS && c >= 0 && c < COLS && bombs[r][c]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void revealAdjacentCells(int row, int col) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int r = row + i, c = col + j;
                if (r >= 0 && r < ROWS && c >= 0 && c < COLS && !revealed[r][c] && !flagged[r][c]) {
                    revealCell(r, c);
                }
            }
        }
    }

    private void revealAllBombs() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (bombs[i][j]) {
                    // Show all bombs
                    ImageIcon bombIcon = iconManager.getBomb();
                    if (bombIcon != null) {
                        buttons[i][j].setIcon(bombIcon);
                    } else {
                        buttons[i][j].setText("💣");
                        buttons[i][j].setBackground(Color.RED);
                    }
                } else if (flagged[i][j] && !revealed[i][j]) {
                    // Show incorrect flags
                    ImageIcon incorrectFlagIcon = iconManager.getIncFlag();
                    if (incorrectFlagIcon != null) {
                        buttons[i][j].setIcon(incorrectFlagIcon);
                    } else {
                        buttons[i][j].setText("❌");
                        buttons[i][j].setBackground(Color.ORANGE);
                    }
                }
            }
        }
        
        // Update the restart button text
        restartButton.setText("Play Again");
        restartButton.setBackground(new Color(144, 238, 144)); // Light green
        restartButton.setForeground(Color.BLACK);
        restartButton.setFont(new Font(restartButton.getFont().getName(), Font.BOLD, 14));
    }

    private boolean checkWin() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (!bombs[i][j] && !revealed[i][j]) {
                    return false;
                }
            }
        }
        
        // Update the restart button on win
        restartButton.setText("Play Again");
        restartButton.setBackground(new Color(144, 238, 144)); // Light green
        restartButton.setForeground(Color.BLACK);
        restartButton.setFont(new Font(restartButton.getFont().getName(), Font.BOLD, 14));
        
        return true;
    }
    
    /**
     * Restarts the game by resetting all game state and creating a new board
     */
    public void restartGame() {
        // Clear the grid panel
        grid.removeAll();
        
        // Reset game state
        gameOver = false;
        bombs = new boolean[ROWS][COLS];
        revealed = new boolean[ROWS][COLS];
        flagged = new boolean[ROWS][COLS];
        
        // Reset the button appearance
        restartButton.setText("New Game");
        restartButton.setBackground(null); // Reset to default background
        restartButton.setForeground(null); // Reset to default foreground
        restartButton.setFont(new Font(restartButton.getFont().getName(), Font.PLAIN, 12));
        
        // Recreate the grid and place new bombs
        setGrid();
        placeBombs();
        
        // Reset status
        statusLabel.setText("Find all bombs! Left click to reveal, right click to flag.");
        
        // Refresh the UI
        grid.revalidate();
        grid.repaint();
        
        System.out.println("Game restarted with " + NUM_BOMBS + " bombs");
    }
}