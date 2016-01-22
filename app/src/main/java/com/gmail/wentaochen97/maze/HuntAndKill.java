package com.gmail.wentaochen97.maze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Wentao-Admin on 2016-01-08.
 */
public class HuntAndKill extends BasicMazeGenerator {

    private final Map<MazeDirection, Integer> BIAS = new HashMap<MazeDirection, Integer>();

    public HuntAndKill() {
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

        do {
            visited[current / width][current % width] = true;
            MazeDirection[] availableDirections = findAvailableNeighbours(visited, current % width, current / width, startX0, startY0, finishX, finishY, false);
            while (availableDirections.length > 0) {
                int total = 0;
                for (MazeDirection d : availableDirections) {
                    total += getBias(d);
                }
                int chosen = (int) (random.nextDouble() * total);
                MazeDirection direction = null;
                for (MazeDirection d : availableDirections) {
                    chosen -= getBias(d);
                    if (chosen < 0) {
                        direction = d;
                        break;
                    }
                }
                int next = current;
                switch (direction) {
                    case EAST:
                        next += 1;
                        break;
                    case NORTH:
                        next -= width;
                        break;
                    case SOUTH:
                        next += width;
                        break;
                    case WEST:
                        next -= 1;
                        break;
                }
                visited[next / width][next % width] = true;
                maze.setWall(current % width + next % width, current / width + next / width, false);
                availableDirections = findAvailableNeighbours(visited, next % width, next / width, startX0, startY0, finishX, finishY, false);
                current = next;
            }
            current = -1;
            for (int y = 0; y < height && current < 0; y++) {
                for (int x = 0; x < width; x++) {
                    if (!visited[y][x]) {
                        MazeDirection[] directions = findAvailableNeighbours(visited, x, y, startX0, startY0, finishX, finishY, true);
                        if (directions.length > 0) {
                            int total = 0;
                            for (MazeDirection d : directions) {
                                total += getBias(d);
                            }
                            int chosen = (int) (random.nextDouble() * total);
                            MazeDirection direction = null;
                            for (MazeDirection d : directions) {
                                chosen -= getBias(d);
                                if (chosen < 0) {
                                    direction = d;
                                    break;
                                }
                            }
                            switch (direction) {
                                case EAST:
                                    maze.setWall(x * 2 + 1, y * 2, false);
                                    break;
                                case NORTH:
                                    maze.setWall(x * 2, y * 2 - 1, false);
                                    break;
                                case SOUTH:
                                    maze.setWall(x * 2, y * 2 + 1, false);
                                    break;
                                case WEST:
                                    maze.setWall(x * 2 - 1, y * 2, false);
                                    break;
                            }
                            current = x + y * width;
                            break;
                        }
                    }
                }
            }
        } while (current >= 0);
        return maze;
    }

    private static MazeDirection[] findAvailableNeighbours(boolean[][] visited, int x, int y, Integer startX, Integer startY, Integer finishX, Integer finishY, boolean match) {
        if (!match && finishX != null && finishY != null && finishX == x && finishY == y) {
            return new MazeDirection[0];
        }
        int width = visited[0].length;
        List<MazeDirection> available = new ArrayList<MazeDirection>();
        if (x > 0 && visited[y][x - 1] == match && (!match || startX == null || startY == null || startX != x - 1 || startY != y) && (!match || finishX == null || finishY == null || finishX != x - 1 || finishY != y)) {
            available.add(MazeDirection.WEST);
        }
        if (x < width - 1 && visited[y][x + 1] == match && (!match || startX == null || startY == null || startX != x + 1 || startY != y) && (!match || finishX == null || finishY == null || finishX != x + 1 || finishY != y)) {
            available.add(MazeDirection.EAST);
        }
        if (y > 0 && visited[y - 1][x] == match && (!match || startX == null || startY == null || startX != x || startY != y - 1) && (!match || finishX == null || finishY == null || finishX != x || finishY != y - 1)) {
            available.add(MazeDirection.NORTH);
        }
        if (y < visited.length - 1 && visited[y + 1][x] == match && (!match || startX == null || startY == null || startX != x || startY != y + 1) && (!match || finishX == null || finishY == null || finishX != x || finishY != y + 1)) {
            available.add(MazeDirection.SOUTH);
        }
        return available.toArray(new MazeDirection[available.size()]);
    }
}
