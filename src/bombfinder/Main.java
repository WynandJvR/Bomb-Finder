package bombfinder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
        Grid grid = new Grid();
        
        JButton solveButton = new JButton("Solve Game");
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solveButton.setEnabled(false);
                
                new Thread(() -> {
                    try {
                        SolutionAlgorithm solver = new SolutionAlgorithm(grid);
                        boolean success = solver.solveGame();
                        
                        String message = success ? 
                            "The algorithm successfully solved the game!" : 
                            "The algorithm was unable to solve the game completely.";
                        
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(grid, message);
                            solveButton.setEnabled(true);
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(grid, 
                                "Error while solving: " + ex.getMessage(), 
                                "Error", JOptionPane.ERROR_MESSAGE);
                            solveButton.setEnabled(true);
                        });
                    }
                }).start();
            }
        });
        
        Container controlPanel = grid.getControlPanel();
        if (controlPanel != null) {
            controlPanel.add(solveButton);
            controlPanel.revalidate();
            controlPanel.repaint();
        }
    }
}