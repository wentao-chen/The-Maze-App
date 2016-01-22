package com.gmail.wentaochen97.maze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Wentao-Admin on 2016-01-08.
 */
public class AldousBroder extends BasicMazeGenerator {

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
        boolean[][] visited = new boolean[height][width];
        int current = startX + startY * width;
        int remaining = width * height - 1;
        int last = current;
        while (remaining > 0) {
            visited[current / width][current % width] = true;
            MazeDirection[] direction = findAvailableNeighbours(current % width, current / width, startX0, startY0, finishX, finishY, width, height);
            MazeDirection selection = direction.length > 0 ? direction[(int) (random.nextDouble() * direction.length)] : null;
            int next = -1;
            if (selection != null) {
                switch (selection) {
                    case EAST:
                        next = current + 1;
                        break;
                    case NORTH:
                        next = current - width;
                        break;
                    case SOUTH:
                        next = current + width;
                        break;
                    case WEST:
                        next = current - 1;
                        break;
                }
            }
            if (next < 0) {
                if (current == last) {
                    break;
                }
                current = last;
            } else {
                if (!visited[next / width][next % width]) {
                    maze.setWall(current % width + next % width, current / width + next / width, false);
                    remaining--;
                }
                last = current;
                current = next;
            }
        }


        return maze;
    }


    private static MazeDirection[] findAvailableNeighbours(int x, int y, Integer startX, Integer startY, Integer finishX, Integer finishY, int width, int height) {
        if (finishX != null && x == finishX && finishY != null && y == finishY) {
            return new MazeDirection[0];
        }
        List<MazeDirection> available = new ArrayList<MazeDirection>();
        if (x > 0 && (startX == null || startY == null || x - 1 != startX || y != startY)) {
            available.add(MazeDirection.WEST);
        }
        if (x < width - 1 && (startX == null || startY == null || x + 1 != startX || y != startY)) {
            available.add(MazeDirection.EAST);
        }
        if (y > 0 && (startX == null || startY == null || x != startX || y - 1 != startY)) {
            available.add(MazeDirection.NORTH);
        }
        if (y < height - 1 && (startX == null || startY == null || x != startX || y + 1 != startY)) {
            available.add(MazeDirection.SOUTH);
        }
        return available.toArray(new MazeDirection[available.size()]);
    }
}