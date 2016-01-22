package com.gmail.wentaochen97.maze;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by Wentao-Admin on 2016-01-08.
 */
public class Eller extends BasicMazeGenerator {

    private final Map<MazeOrientation, Double> BIAS = new HashMap<MazeOrientation, Double>();

    public Eller() {
        for (MazeOrientation o : MazeOrientation.values()) {
            BIAS.put(o, 0.5);
        }
    }

    public double getBias(MazeOrientation orientation) {
        return BIAS.get(orientation);
    }

    public void setBias(MazeOrientation orientation, double bias) {
        if (orientation == null) throw new IllegalArgumentException("bias orientation cannot be null");
        synchronized (this) {
            BIAS.put(orientation, bias);
        }
    }

    @Override
    public Maze2D generateMaze(int width, int height) {
        Long seed = getSeed();
        Random random = seed != null ? new Random(seed) : new Random();
        Integer players = getDesiredPlayers();
        Maze2D maze = players != null ? new Maze2D(width, height, players) : new Maze2D(width, height, getStartX(), getStartY(), getFinishX(), getFinishY());
        maze.setGridLayout();
        double horizontalBias = BIAS.get(MazeOrientation.HORIZONTAL);
        double verticalBias = BIAS.get(MazeOrientation.VERTICAL);

        long count = 0;
        Long[] previousID = new Long[width];
        long[] rowID = new long[width];
        for (int i = 0; i < width; i++) {
            previousID[i] = null;
        }

        for (int y = 0; y < height; y++) {
            for (int i = 0; i < width; i++) {
                rowID[i] = previousID[i] != null ? previousID[i] : count++;
                previousID[i] = null;
            }
            for (int i = 1; i < width; i++) {
                if (rowID[i - 1] != rowID[i] && random.nextDouble() < horizontalBias) {
                    maze.setWall(2 * i - 1, 2 * y, false);
                    long id = rowID[i];
                    for (int j = i; j < width; j++) {
                        if (rowID[j] == id) {
                            rowID[j] = rowID[i - 1];
                        }
                    }
                }
            }
            Map<Long, Integer> IDS = new HashMap<>();
            for (int i = 0; i < width; i++) {
                Integer previous = IDS.get(rowID[i]);
                if (previous != null) {
                    IDS.put(rowID[i], previous + 1);
                } else {
                    IDS.put(rowID[i], 0);
                }
            }
            if (y < height - 1) {
                for (long id : IDS.keySet()) {
                    int selection = (int) (random.nextDouble() * IDS.get(id));
                    for (int i = 0; i < width; i++) {
                        if (rowID[i] == id) {
                            selection--;
                            if (selection < 0) {
                                maze.setWall(2 * i, 2 * y + 1, false);
                                previousID[i] = id;
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < width; i++) {
                    if (previousID[i] == null && random.nextDouble() < verticalBias) {
                        maze.setWall(2 * i, 2 * y + 1, false);
                        previousID[i] = rowID[i];
                    }
                }
            }
        }
        Set<Long> IDS = new HashSet<Long>();
        for (int i = 0; i < width; i++) {
            IDS.add(rowID[i]);
        }

        if (width > 0) {
            for (int i = 1; i < width; i++) {
                long id = rowID[i];
                if (id != rowID[0]) {
                    maze.setWall(2 * i - 1, 2 * (height - 1), false);
                    for (int j = i; j < width; j++) {
                        if (rowID[j] == id) {
                            rowID[j] = rowID[0];
                        }
                    }
                }
            }
        }
        return maze;
    }
}