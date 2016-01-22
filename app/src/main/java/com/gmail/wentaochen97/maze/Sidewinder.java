package com.gmail.wentaochen97.maze;

import java.util.Random;

/**
 * Created by Wentao-Admin on 2016-01-08.
 */
public class Sidewinder extends BasicMazeGenerator {

    private double stopRunBias = 0.5;
    private MazeDirection direction = null;
    public Sidewinder(MazeDirection direction) {
        if (direction == null) throw new IllegalArgumentException("direction cannot be null");
        this.direction = direction;
    }

    public MazeDirection getDirection() {
        return this.direction;
    }

    public synchronized void setDirection(MazeDirection direction) {
        this.direction = direction;
    }

    public double getStopRunBias() {
        return this.stopRunBias;
    }

    public synchronized void setStopRunBias(double stopRunBias) {
        this.stopRunBias = stopRunBias;
    }

    @Override
    public synchronized Maze2D generateMaze(int width, int height) {
        Long seed = getSeed();
        Random random = seed != null ? new Random(seed) : new Random();
        Integer players = getDesiredPlayers();
        Maze2D maze = players != null ? new Maze2D(width, height, players) : new Maze2D(width, height, getStartX(), getStartY(), getFinishX(), getFinishY());
        maze.setGridLayout();
        double stopRunBias = this.stopRunBias;
        MazeDirection direction = getDirection();
        if (direction.getOrientation() == MazeOrientation.HORIZONTAL) {
            int start = direction == MazeDirection.EAST ? 0 : width - 1;
            int increment = direction == MazeDirection.EAST ? 1 : -1;
            for (int y = 0; y < height - 1; y++) {
                maze.setWall(start * 2, y * 2 + 1, false);
            }
            for (int x = start + increment; 0 <= x && x < width; x += increment) {
                int runStart = 0;
                int current = 1;
                while (current <= height) {
                    while (current < height && random.nextDouble() >= stopRunBias) {
                        maze.setWall(x * 2, current++ * 2 - 1, false);
                    }
                    int selection = runStart + (int) (random.nextDouble() * (current - runStart));
                    maze.setWall(x * 2 - increment, selection * 2, false);
                    runStart = current++;
                }
            }
        } else {
            int start = direction == MazeDirection.SOUTH ? 0 : height - 1;
            int increment = direction == MazeDirection.SOUTH ? 1 : -1;
            for (int x = 0; x < width - 1; x++) {
                maze.setWall(x * 2 + 1, start * 2, false);
            }
            for (int y = start + increment; 0 <= y && y < height; y += increment) {
                int runStart = 0;
                int current = 1;
                while (current <= width) {
                    while (current < width && random.nextDouble() >= stopRunBias) {
                        maze.setWall(current++ * 2 - 1, y * 2, false);
                    }
                    int selection = runStart + (int) (random.nextDouble() * (current - runStart));
                    maze.setWall(selection * 2, y * 2 - increment, false);
                    runStart = current++;
                }
            }
        }
        return maze;
    }
}
