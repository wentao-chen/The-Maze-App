package com.gmail.wentaochen97.maze;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Wentao-Admin on 2016-01-08.
 */
public class Kruskal extends BasicMazeGenerator {

    private final Map<MazeOrientation, Integer> BIAS = new HashMap<MazeOrientation, Integer>();

    public Kruskal() {
        for (MazeOrientation o : MazeOrientation.values()) {
            BIAS.put(o, 1);
        }
    }

    public int getBias(MazeOrientation orientation) {
        return BIAS.get(orientation);
    }

    public void setBias(MazeOrientation orientation, int bias) {
        if (orientation == null) throw new IllegalArgumentException("bias orientation cannot be null");
        if (bias < 0) throw new IllegalArgumentException("bias cannot be less than 0");
        int total = 0;
        for (MazeOrientation o : MazeOrientation.values()) {
            if (o != orientation) {
                total += BIAS.get(o);
            }
        }
        if (total == 0 && bias == 0) throw new IllegalArgumentException("total bias cannot be 0");
        synchronized (this) {
            BIAS.put(orientation, bias);
        }
    }

    @Override
    public synchronized Maze2D generateMaze(int width, int height) {
        Long seed = getSeed();
        Random random = seed != null ? new Random(seed) : new Random();
        Integer players = getDesiredPlayers();
        Maze2D maze = players != null ? new Maze2D(width, height, players) : new Maze2D(width, height, getStartX(), getStartY(), getFinishX(), getFinishY());
        maze.setGridLayout();
        int[][] group = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                group[y][x] = x + y * width;
            }
        }
        int[] horizontalWalls = new int[(width - 1) * height];
        for (int i = 0; i < horizontalWalls.length; i++) {
            horizontalWalls[i] = i;
        }
        int horizontalWallsCount = horizontalWalls.length;
        int[] verticalWalls = new int[width * (height - 1)];
        for (int i = 0; i < verticalWalls.length; i++) {
            verticalWalls[i] = i;
        }
        int verticalWallsCount = verticalWalls.length;
        int groups = width * height;
        while (groups > 1 && horizontalWallsCount > 0 && verticalWallsCount > 0) {
            int horizontalBias = getBias(MazeOrientation.HORIZONTAL);
            int verticalBias = getBias(MazeOrientation.VERTICAL);
            int selection = (int) (random.nextDouble() * (horizontalWallsCount * horizontalBias + verticalWallsCount * verticalBias));
            int selectionWall = -1;
            boolean vertical = false;
            for (int i = 0; i < horizontalWallsCount; i++) {
                selection -= horizontalBias;
                if (selection < 0) {
                    selectionWall = horizontalWalls[i] % (width - 1) * 2 + 1 + (horizontalWalls[i] / (width - 1)) * 2 * (2 * width - 1);
                    horizontalWallsCount--;
                    horizontalWalls[i] = horizontalWalls[horizontalWallsCount];
                    break;
                }
            }
            if (selectionWall == -1) {
                for (int i = 0; i < verticalWallsCount; i++) {
                    selection -= verticalBias;
                    if (selection < 0) {
                        selectionWall = verticalWalls[i] % width * 2 + (verticalWalls[i] / width * 2 + 1) * (2 * width - 1);
                        verticalWallsCount--;
                        verticalWalls[i] = verticalWalls[verticalWallsCount];
                        vertical = true;
                        break;
                    }
                }
            }
            if (selectionWall >= 0) {
                int wallX = selectionWall % (2 * width - 1);
                int wallY = selectionWall / (2 * width - 1);
                int group1 = group[wallY / 2][wallX / 2];
                int group2 = group[wallY / 2 + (vertical ? 1 : 0)][wallX / 2 + (vertical ? 0 : 1)];
                if (group1 != group2) {
                    maze.setWall(wallX, wallY, false);
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            if (group[y][x] == group2) {
                                group[y][x] = group1;
                            }
                        }
                    }
                    groups--;
                }
            }
        }
        return maze;
    }

}