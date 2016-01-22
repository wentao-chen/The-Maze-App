package com.gmail.wentaochen97.maze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Wentao-Admin on 2016-01-07.
 */
public class RecursiveBacktracker extends BasicMazeGenerator {

    private final Map<MazeDirection, Integer> BIAS = new HashMap<MazeDirection, Integer>();

    public RecursiveBacktracker() {
        for (MazeDirection d : MazeDirection.values()) {
            BIAS.put(d, 1);
        }
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
        Integer startX = null;
        Integer startY = null;
        Integer finishX = null;
        Integer finishY = null;
        Long seed = getSeed();
        Random random = seed != null ? new Random(seed) : new Random();
        Integer players = getDesiredPlayers();
        Maze2D maze = players != null ? new Maze2D(width, height, players) : new Maze2D(width, height, getStartX(), getStartY(), getFinishX(), getFinishY());
        maze.setGridLayout();
        if (startX == null || startX < 0 || startX >= width) {
            startX = (int) (random.nextDouble() * width);
        }
        if (startY == null || startY < 0 || startY >= width) {
            startY = (int) (random.nextDouble() * height);
        }
        boolean[][] visited = new boolean[height][width];
        ArrayList<Integer> stack = new ArrayList<Integer>();
        stack.add(startX + startY * width);
        visited[startY][startX] = true;
        do {
            int current = stack.get(stack.size() - 1);
            MazeDirection[] available = findAvailableNeighbours(visited, current % width, current / width, finishX, finishY);
            if (available.length > 0) {
                int total = 0;
                for (MazeDirection d : available) {
                    total += getBias(d);
                }
                int selection = (int) (random.nextDouble() * total);
                MazeDirection selectionDirection = null;
                for (MazeDirection d : available) {
                    selection -= getBias(d);
                    if (selection < 0) {
                        selectionDirection = d;
                        break;
                    }
                }
                switch (selectionDirection) {
                    case EAST:
                        selection = current + 1;
                        break;
                    case NORTH:
                        selection = current - width;
                        break;
                    case SOUTH:
                        selection = current + width;
                        break;
                    case WEST:
                        selection = current - 1;
                        break;
                    default:
                        return maze;
                }
                stack.add(selection);
                visited[selection / width][selection % width] = true;
                maze.setWall(current % width + selection % width, current / width + selection / width, false);
            } else {
                stack.remove(stack.size() - 1);
            }
        } while (stack.size() > 0);

        return maze;
    }

    private static MazeDirection[] findAvailableNeighbours(boolean[][] visited, int x, int y, Integer finishX, Integer finishY) {
        if (finishX != null && x == finishX && finishY != null && y == finishY) {
            return new MazeDirection[0];
        }
        int width = visited[0].length;
        List<MazeDirection> available = new ArrayList<MazeDirection>();
        if (x > 0 && !visited[y][x - 1]) {
            available.add(MazeDirection.WEST);
        }
        if (x < width - 1 && !visited[y][x + 1]) {
            available.add(MazeDirection.EAST);
        }
        if (y > 0 && !visited[y - 1][x]) {
            available.add(MazeDirection.NORTH);
        }
        if (y < visited.length - 1 && !visited[y + 1][x]) {
            available.add(MazeDirection.SOUTH);
        }
        return available.toArray(new MazeDirection[available.size()]);
    }
}
