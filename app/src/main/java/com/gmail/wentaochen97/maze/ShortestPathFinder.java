package com.gmail.wentaochen97.maze;

/**
 * Created by Wentao-Admin on 2016-01-07.
 */
public class ShortestPathFinder extends CustomMazeSolver {

    public ShortestPathFinder(Maze2D maze) {
        super(maze);
    }

    @Override
    public MazeSolution2D getSolution(int startX, int startY, int finishX, int finishY) {
        Maze2D maze = getMaze();
        MazeDirection[][] solution = new MazeDirection[maze.getHeight()][maze.getWidth()];
        boolean found = true;
        while (solution[finishY][finishX] == null && found) {
            boolean[][] isPath = new boolean[maze.getHeight()][maze.getWidth()];
            for (int y = 0; y < solution.length; y++) {
                for (int x = 0; x < solution[y].length; x++) {
                    isPath[y][x] = solution[y][x] != null || (startX == x && startY == y);
                }
            }
            found = false;
            for (int y = 0; y < solution.length; y++) {
                for (int x = 0; x < solution[y].length; x++) {
                    if (isPathOpen(x, y) && solution[y][x] == null) {
                        if (x > 0 && isPath[y][x - 1] && !isDirectionBlocked(x - 1, y, MazeDirection.EAST)) {
                            solution[y][x] = MazeDirection.WEST;
                            found = true;
                        } else if (x < maze.getWidth() - 1 && isPath[y][x + 1] && !isDirectionBlocked(x + 1, y, MazeDirection.WEST)) {
                            solution[y][x] = MazeDirection.EAST;
                            found = true;
                        } else if (y > 0 && isPath[y - 1][x] && !isDirectionBlocked(x, y - 1, MazeDirection.SOUTH)) {
                            solution[y][x] = MazeDirection.NORTH;
                            found = true;
                        } else if (y < maze.getHeight() - 1 && isPath[y + 1][x] && !isDirectionBlocked(x, y + 1, MazeDirection.NORTH)) {
                            solution[y][x] = MazeDirection.SOUTH;
                            found = true;
                        }
                    }
                }
            }
        }
        if (!found) {
            return null;
        }
        MazeSolution2D solution2D = new MazeSolution2D(maze.getWidth(), maze.getHeight(), startX, startY, finishX, finishY);
        int currentX = finishX;
        int currentY = finishY;
        while (currentX != startX || currentY != startY) {
            switch (solution[currentY][currentX]) {
                case EAST:
                    solution2D.setDirection(++currentX, currentY, MazeDirection.WEST);
                    break;
                case NORTH:
                    solution2D.setDirection(currentX, --currentY, MazeDirection.SOUTH);
                    break;
                case SOUTH:
                    solution2D.setDirection(currentX, ++currentY, MazeDirection.NORTH);
                    break;
                case WEST:
                    solution2D.setDirection(--currentX, currentY, MazeDirection.EAST);
                    break;
            }
        }
        return solution2D;
    }
}
