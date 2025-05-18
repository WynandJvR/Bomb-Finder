package bombfinder;

import java.util.*;

public class SolutionAlgorithm {
    private final int rows;
    private final int cols;
    private final boolean[][] bombs;
    private final boolean[][] revealed;
    private final boolean[][] flagged;
    private final Grid grid;
    
    private final Set<Point> knownSafeCells = new HashSet<>();
    private final Set<Point> knownBombCells = new HashSet<>();
    
    public SolutionAlgorithm(Grid grid) {
        this.grid = grid;
        this.rows = grid.getRows();
        this.cols = grid.getCols();
        this.bombs = grid.getBombs();
        this.revealed = grid.getRevealed();
        this.flagged = grid.getFlagged();
    }
    
    public boolean makeMove() {
        knownSafeCells.clear();
        knownBombCells.clear();
        
        findSafeAndBombCells();
        
        if (!knownSafeCells.isEmpty()) {
            Point safeMove = knownSafeCells.iterator().next();
            grid.revealCellProgrammatically(safeMove.x, safeMove.y);
            return true;
        }
        
        if (!knownBombCells.isEmpty()) {
            Point bombMove = knownBombCells.iterator().next();
            grid.flagCellProgrammatically(bombMove.x, bombMove.y);
            return true;
        }
        
        return makeGuess();
    }
    
    private void findSafeAndBombCells() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!revealed[i][j]) continue;
                
                int adjacentBombs = grid.getAdjacentBombs(i, j);
                
                if (adjacentBombs == 0) continue;
                
                List<Point> unknownCells = new ArrayList<>();
                List<Point> flaggedCells = new ArrayList<>();
                
                for (int di = -1; di <= 1; di++) {
                    for (int dj = -1; dj <= 1; dj++) {
                        if (di == 0 && dj == 0) continue;
                        
                        int ni = i + di;
                        int nj = j + dj;
                        
                        if (ni >= 0 && ni < rows && nj >= 0 && nj < cols) {
                            if (flagged[ni][nj]) {
                                flaggedCells.add(new Point(ni, nj));
                            }
                            else if (!revealed[ni][nj]) {
                                unknownCells.add(new Point(ni, nj));
                            }
                        }
                    }
                }
                
                if (flaggedCells.size() == adjacentBombs && !unknownCells.isEmpty()) {
                    knownSafeCells.addAll(unknownCells);
                }
                
                if (unknownCells.size() + flaggedCells.size() == adjacentBombs && !unknownCells.isEmpty()) {
                    knownBombCells.addAll(unknownCells);
                }
            }
        }
    }
    
    private boolean makeGuess() {
        double lowestProbability = 1.0;
        Point bestGuess = null;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!revealed[i][j] && !flagged[i][j]) {
                    boolean adjacentToRevealed = false;
                    for (int di = -1; di <= 1 && !adjacentToRevealed; di++) {
                        for (int dj = -1; dj <= 1 && !adjacentToRevealed; dj++) {
                            if (di == 0 && dj == 0) continue;
                            
                            int ni = i + di;
                            int nj = j + dj;
                            
                            if (ni >= 0 && ni < rows && nj >= 0 && nj < cols && revealed[ni][nj]) {
                                adjacentToRevealed = true;
                            }
                        }
                    }
                    
                    if (!adjacentToRevealed) {
                        double probability = (double) grid.getNumBombs() / (rows * cols);
                        if (probability < lowestProbability) {
                            lowestProbability = probability;
                            bestGuess = new Point(i, j);
                        }
                    }
                }
            }
        }
        
        if (bestGuess != null) {
            grid.revealCellProgrammatically(bestGuess.x, bestGuess.y);
            return true;
        }
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!revealed[i][j] && !flagged[i][j]) {
                    grid.revealCellProgrammatically(i, j);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean solveGame() {
        while (!grid.isGameOver()) {
            if (!makeMove()) {
                return false;
            }
            
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        return grid.checkWin();
    }
    
    private static class Point {
        final int x;
        final int y;
        
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return x == point.x && y == point.y;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}