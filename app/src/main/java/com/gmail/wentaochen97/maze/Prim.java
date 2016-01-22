package com.gmail.wentaochen97.maze;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by Wentao-Admin on 2016-01-08.
 */
public class Prim extends BasicMazeGenerator {

    private final Map<MazeDirection, Integer> BIAS = new HashMap<MazeDirection, Integer>();
    private boolean modified = false;

    public boolean isModifiedAlgorithm() {
        return this.modified;
    }

    public synchronized void setModifiedAlgorithm(boolean modified) {
        this.modified = modified;
    }

    public int getBias(MazeDirection direction) {
        return BIAS.get(direction);
    }

    public void setBias(MazeDirection direction, int bias) {
        if (direction == null) throw new IllegalArgumentException("bias direction cannot be null");
        if (bias < 0) throw new IllegalArgumentException("bias cannot be less than 0");
        int total = 0;
        for (MazeDirection d : MazeDirection.values()) {
            if (d != direction) {
                total += BIAS.get(d);
            }
        }
        if (total == 0 && bias == 0) throw new IllegalArgumentException("total bias cannot be 0");
        synchronized (this) {
            BIAS.put(direction, bias);
        }
    }

    @Override
    public synchronized Maze2D generateMaze(int width, int height) {
        Long seed = getSeed();
        Random random = seed != null ? new Random(seed) : new Random();
        Integer players = getDesiredPlayers();
        Maze2D maze = players != null ? new Maze2D(width, height, players) : new Maze2D(width, height, getStartX(), getStartY(), getFinishX(), getFinishY());
        maze.setGridLayout();
        Integer startX0 = null;
        Integer startY0 = null;
        Integer startX = startX0;
        Integer startY = startY0;
        Integer finishX = null;
        Integer finishY = null;
        if (startX == null || startX < 0 || startX >= width) {
            startX = (int) (random.nextDouble() * width);
        }
        if (startY == null || startY < 0 || startY >= width) {
            startY = (int) (random.nextDouble() * height);
        }
        if (isModifiedAlgorithm()) {
            boolean startOpened = false;
            boolean finishOpened = false;
            boolean[][] visited = new boolean[height][width];
            Set<Integer> FRONTIER = new HashSet<Integer>();
            visited[startY][startX] = true;
            if (startX > 0) {
                FRONTIER.add(startX - 1 + startY * width);
            }
            if (startX < width - 1) {
                FRONTIER.add(startX + 1 + startY * width);
            }
            if (startY > 0) {
                FRONTIER.add(startX + (startY - 1) * width);
            }
            if (startY < height - 1) {
                FRONTIER.add(startX + (startY + 1) * width);
            }
            while (FRONTIER.size() > 0) {
                int randomSelection = (int) (random.nextDouble() * FRONTIER.size());
                int selection = -1;
                for (int i : FRONTIER) {
                    randomSelection--;
                    if (randomSelection < 0) {
                        selection = i;
                        break;
                    }
                }
                FRONTIER.remove(selection);
                if (!startOpened && startX0 != null && startY0 != null) {
                    startOpened = true;
                    FRONTIER.clear();
                }
                if (visited[selection / width][selection % width]) {
                    continue;
                }
                int visitedSelection = selection;
                int count = 0;
                boolean hasStartLeft = startX0 == null || startY0 == null || selection % width - 1 != startX0 || selection / width != startY0;
                boolean hasStartRight = startX0 == null || startY0 == null || selection % width + 1 != startX0 || selection / width != startY0;
                boolean hasStartTop = startX0 == null || startY0 == null || selection % width != startX0 || selection / width - 1 != startY0;
                boolean hasStartBottom = startX0 == null || startY0 == null || selection % width != startX0 || selection / width + 1 != startY0;
                boolean hasFinishLeft = finishX == null || finishY == null || !finishOpened || selection % width - 1 != finishX || selection / width != finishY;
                boolean hasFinishRight = finishX == null || finishY == null || !finishOpened || selection % width + 1 != finishX || selection / width != finishY;
                boolean hasFinishTop = finishX == null || finishY == null || !finishOpened || selection % width != finishX || selection / width - 1 != finishY;
                boolean hasFinishBottom = finishX == null || finishY == null || !finishOpened || selection % width != finishX || selection / width + 1 != finishY;
                if (selection % width > 0 && visited[selection / width][selection % width - 1] && hasStartLeft && hasFinishLeft) {
                    count++;
                }
                if (selection % width < width - 1 && visited[selection / width][selection % width + 1] && hasStartRight && hasFinishRight) {
                    count++;
                }
                if (selection / width > 0 && visited[selection / width - 1][selection % width] && hasStartTop && hasFinishTop) {
                    count++;
                }
                if (selection / width < height - 1 && visited[selection / width + 1][selection % width] && hasStartBottom && hasFinishBottom) {
                    count++;
                }
                count = (int) (random.nextDouble() * count);
                if (selection % width > 0 && visited[selection / width][selection % width - 1] && hasStartLeft && hasFinishLeft) {
                    count--;
                    visitedSelection = selection - 1;
                }
                if (count >= 0 && selection % width < width - 1 && visited[selection / width][selection % width + 1] && hasStartRight && hasFinishRight) {
                    count--;
                    visitedSelection = selection + 1;
                }
                if (count >= 0 && selection / width > 0 && visited[selection / width - 1][selection % width] && hasStartTop && hasFinishTop) {
                    count--;
                    visitedSelection = selection - width;
                }
                if (count >= 0 && selection / width < height - 1 && visited[selection / width + 1][selection % width] && hasStartBottom && hasFinishBottom) {
                    count--;
                    visitedSelection = selection + width;
                }
                maze.setWall(selection % width + visitedSelection % width, selection / width + visitedSelection / width, false);
                visited[selection / width][selection % width] = true;
                if (finishX == null || finishY == null || selection % width != finishX || selection / width != finishY) {
                    if (selection % width > 0 && !visited[selection / width][selection % width - 1]) {
                        FRONTIER.add(selection - 1);
                    }
                    if (selection % width < width - 1 && !visited[selection / width][selection % width + 1]) {
                        FRONTIER.add(selection + 1);
                    }
                    if (selection / width > 0 && !visited[selection / width - 1][selection % width]) {
                        FRONTIER.add(selection - width);
                    }
                    if (selection / width < height - 1 && !visited[selection / width + 1][selection % width]) {
                        FRONTIER.add(selection + width);
                    }
                } else {
                    finishOpened = true;
                }
            }
            if (startX0 != null && startY0 != null) {
                int count = 0;
                if (startX0 > 0) {
                    count++;
                }
                if (startX0 < width - 1) {
                    count++;
                }
                if (startY0 > 0) {
                    count++;
                }
                if (startY0 < height - 1) {
                    count++;
                }
                count = (int) (random.nextDouble() * count);
                int removeWall = -1;
                if (startX0 > 0) {
                    count--;
                    removeWall = startX0 - 1 + startY0 * width;
                }
                if (count >= 0 && startX0 < width - 1) {
                    count--;
                    removeWall = startX0 + 1 + startY0 * width;
                }
                if (count >= 0 && startY0 > 0) {
                    count--;
                    removeWall = startX0 + (startY0 - 1) * width;
                }
                if (count >= 0 && startY0 < height - 1) {
                    count--;
                    removeWall = startX0 + (startY0 + 1) * width;
                }
                if (removeWall >= 0) {
                    maze.setWall(startX0 + removeWall % width, startY0 + removeWall / width, false);
                }
            }
        } else {
            boolean startOpened = false;
            boolean[][] visited = new boolean[height][width];
            Set<Integer> FRONTIER_WALLS = new HashSet<Integer>();
            visited[startY][startX] = true;
            if (startX > 0) {
                FRONTIER_WALLS.add(2 * startX - 1 + 2 * startY * (width * 2 - 1));
            }
            if (startX < width - 1) {
                FRONTIER_WALLS.add(2 * startX + 1 + 2 * startY * (width * 2 - 1));
            }
            if (startY > 0) {
                FRONTIER_WALLS.add(2 * startX + (2 * startY - 1) * (width * 2 - 1));
            }
            if (startY < height - 1) {
                FRONTIER_WALLS.add(2 * startX + (2 * startY + 1) * (width * 2 - 1));
            }
            while (FRONTIER_WALLS.size() > 0) {
                int randomSelection = (int) (random.nextDouble() * FRONTIER_WALLS.size());
                int selection = -1;
                for (int i : FRONTIER_WALLS) {
                    randomSelection--;
                    if (randomSelection < 0) {
                        selection = i;
                        break;
                    }
                }
                if (!startOpened && startX0 != null && startY0 != null) {
                    startOpened = true;
                    FRONTIER_WALLS.clear();
                }
                int fullWidth = 2 * width - 1;
                if (visited[(selection / fullWidth) / 2][(selection % fullWidth) / 2] && visited[(selection / fullWidth) / 2 + (1 - (selection % fullWidth) % 2)][(selection % fullWidth) / 2 + ((selection % fullWidth) % 2)]) {
                    FRONTIER_WALLS.remove(selection);
                    continue;
                }
                maze.setWall(selection % fullWidth, selection / fullWidth, false);
                int newCell = (selection % fullWidth) / 2 + width * (((selection / fullWidth) / 2));
                if ((selection % fullWidth) % 2 == 0) {
                    if (visited[(selection / fullWidth) / 2][(selection % fullWidth) / 2]) {
                        newCell += width;
                    }
                    visited[(selection / fullWidth) / 2][(selection % fullWidth) / 2] = true;
                    visited[(selection / fullWidth) / 2 + 1][(selection % fullWidth) / 2] = true;
                } else {
                    if (visited[(selection / fullWidth) / 2][(selection % fullWidth) / 2]) {
                        newCell += 1;
                    }
                    visited[(selection / fullWidth) / 2][(selection % fullWidth) / 2] = true;
                    visited[(selection / fullWidth) / 2][(selection % fullWidth) / 2 + 1] = true;
                }
                if (newCell % width > 0 && !visited[newCell / width][newCell % width - 1]) {
                    FRONTIER_WALLS.add((newCell % width) * 2 - 1 + (width * 2 - 1) * (newCell / width) * 2);
                }
                if (newCell % width < width - 1 && !visited[newCell / width][newCell % width + 1]) {
                    FRONTIER_WALLS.add((newCell % width) * 2 + 1 + (width * 2 - 1) * (newCell / width) * 2);
                }
                if (newCell / width > 0 && !visited[newCell / width - 1][newCell % width]) {
                    FRONTIER_WALLS.add((newCell % width) * 2 + (width * 2 - 1) * ((newCell / width) * 2 - 1));
                }
                if (newCell / width < height - 1 && !visited[newCell / width + 1][newCell % width]) {
                    FRONTIER_WALLS.add((newCell % width) * 2 + (width * 2 - 1) * ((newCell / width) * 2 + 1));
                }
                if (finishX != null && finishY != null && newCell % width == finishX && newCell / width == finishY) {
                    FRONTIER_WALLS.remove(finishX * 2 - 1 + finishY * 2 * fullWidth);
                    FRONTIER_WALLS.remove(finishX * 2 + 1 + finishY * 2 * fullWidth);
                    FRONTIER_WALLS.remove(finishX * 2 + (finishY * 2 - 1) * fullWidth);
                    FRONTIER_WALLS.remove(finishX * 2 + (finishY * 2 + 1) * fullWidth);
                }
                FRONTIER_WALLS.remove(selection);
            }
        }
        return maze;
    }
}
