package com.gmail.wentaochen97.maze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Wentao-Admin on 2016-01-08.
 */
public class Wilson extends BasicMazeGenerator {

    private final Map<MazeDirection, Integer> BIAS = new HashMap<MazeDirection, Integer>();

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
    public Maze2D generateMaze(int width, int height) {
        Long seed = getSeed();
        Random random = seed != null ? new Random(seed) : new Random();
        Integer players = getDesiredPlayers();
        Maze2D maze = players != null ? new Maze2D(width, height, players) : new Maze2D(width, height, getStartX(), getStartY(), getFinishX(), getFinishY());
        maze.setGridLayout();
        Integer startX = null;
        Integer startY = null;
        Integer finishX = null;
        Integer finishY = null;

        boolean[][] visited = new boolean[height][width];
        visited[(int) (random.nextDouble() * height)][(int) (random.nextDouble() * width)] = true;
        int remaining = height * width - 1;
        while (remaining > 0) {
            MazeDirection[][] directions = new MazeDirection[height][width];
            int count = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (!visited[y][x]) {
                        count++;
                    }
                }
            }
            count = (int) (random.nextDouble() * count);
            int current = 0;
            for (int y = 0; y < height && count >= 0; y++) {
                for (int x = 0; x < width && count >= 0; x++) {
                    if (!visited[y][x]) {
                        count--;
                        current = x + y * width;
                    }
                }
            }
            int start = current;
            while (!visited[current / width][current % width]) {
                MazeDirection[] available = findAvailableNeighbours(current % width, current / width, startX, startY, finishX, finishY, width, height);
                MazeDirection nextDirection = available[(int) (random.nextDouble() * available.length)];
                directions[current / width][current % width] = nextDirection;
                switch (nextDirection) {
                    case EAST:
                        current += 1;
                        break;
                    case NORTH:
                        current -= width;
                        break;
                    case SOUTH:
                        current += width;
                        break;
                    case WEST:
                        current -= 1;
                        break;
                }
            }
            current = start;
            while (!visited[current / width][current % width]) {
                visited[current / width][current % width] = true;
                MazeDirection nextDirection = directions[current / width][current % width];
                switch (nextDirection) {
                    case EAST:
                        maze.setWall(2 * (current % width) + 1, 2 * (current / width), false);
                        current += 1;
                        break;
                    case NORTH:
                        maze.setWall(2 * (current % width), 2 * (current / width) - 1, false);
                        current -= width;
                        break;
                    case SOUTH:
                        maze.setWall(2 * (current % width), 2 * (current / width) + 1, false);
                        current += width;
                        break;
                    case WEST:
                        maze.setWall(2 * (current % width) - 1, 2 * (current / width), false);
                        current -= 1;
                        break;
                }
                remaining--;
            }
        }

        return maze;
    }

    private static MazeDirection[] findAvailableNeighbours(int x, int y, Integer startX, Integer startY, Integer finishX, Integer finishY, int width, int height) {
        List<MazeDirection> available = new ArrayList<MazeDirection>();
        if (x > 0 && (startX == null || startY == null || x - 1 != startX || y != startY) && (finishX == null || finishY == null || x - 1 != finishX || y != finishY)) {
            available.add(MazeDirection.WEST);
        }
        if (x < width - 1 && (startX == null || startY == null || x + 1 != startX || y != startY) && (finishX == null || finishY == null || x + 1 != finishX || y != finishY)) {
            available.add(MazeDirection.EAST);
        }
        if (y > 0 && (startX == null || startY == null || x != startX || y - 1 != startY) && (finishX == null || finishY == null || x != finishX || y - 1 != finishY)) {
            available.add(MazeDirection.NORTH);
        }
        if (y < height - 1 && (startX == null || startY == null || x != startX || y + 1 != startY) && (finishX == null || finishY == null || x != finishX || y + 1 != finishY)) {
            available.add(MazeDirection.SOUTH);
        }
        return available.toArray(new MazeDirection[available.size()]);
    }
}
