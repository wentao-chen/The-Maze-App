package com.gmail.wentaochen97.maze;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Wentao-Admin on 2016-01-07.
 */
public abstract class CustomMazeSolver implements MazeSolver {

    private final Maze2D MAZE;
    private final boolean[][] BLOCKED_PATH;
    private final Map<MazeDirection, boolean[][]> BLOCKED_DIRECTION;

    public CustomMazeSolver(Maze2D maze) {
        if (maze == null) throw new IllegalArgumentException("maze cannot be null");
        MAZE = maze;
        BLOCKED_PATH = new boolean[maze.getHeight()][maze.getWidth()];
        BLOCKED_DIRECTION = new HashMap<MazeDirection, boolean[][]>();
        for (MazeDirection d : MazeDirection.values()) {
            BLOCKED_DIRECTION.put(d, new boolean[maze.getHeight()][maze.getWidth()]);
        }
    }

    public Maze2D getMaze() {
        return MAZE;
    }

    public void setPathBlocked(int x, int y, boolean blocked) {
        synchronized (BLOCKED_PATH) {
            BLOCKED_PATH[y][x] = blocked;
        }
    }

    public boolean isPathOpen(int x, int y) {
        return !MAZE.hasWall(x, y) && !BLOCKED_PATH[y][x];
    }

    public boolean isDirectionBlocked(int x, int y, MazeDirection d) {
        return BLOCKED_DIRECTION.get(d)[y][x];
    }

    public void setDirectionBlocked(int x, int y, MazeDirection d, boolean blocked) {
        if (d == null) throw new IllegalArgumentException("direction cannot be null");
        boolean[][] directionSet = BLOCKED_DIRECTION.get(d);
        synchronized (directionSet) {
            directionSet[y][x] = blocked;
        }
    }
}
