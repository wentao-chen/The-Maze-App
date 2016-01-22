package com.gmail.wentaochen97.maze;

/**
 * Created by Wentao-Admin on 2016-01-07.
 */
public class MazeSolution2D {

    private final MazeDirection[][] MAZE;
    private final int START_X;
    private final int START_Y;
    private final int FINISH_X;
    private final int FINISH_Y;

    MazeSolution2D(int width, int height, int startX, int startY, int finishX, int finishY) {
        MAZE = new MazeDirection[height][width];
        if (startX < 0 || startX >= width) throw new IllegalArgumentException("Invalid start x-coordinate");
        if (startY < 0 || startY >= height) throw new IllegalArgumentException("Invalid start y-coordinate");
        if (finishX < 0 || finishX >= width) throw new IllegalArgumentException("Invalid finish x-coordinate");
        if (finishY < 0 || finishY >= height) throw new IllegalArgumentException("Invalid finish y-coordinate");
        START_X = startX;
        START_Y = startY;
        FINISH_X = finishX;
        FINISH_Y = finishY;
    }

    public MazeDirection getFirstDirection() {
        return MAZE[START_Y][START_X];
    }

    public MazeDirection getDirection(int x, int y) {
        return MAZE[y][x];
    }

    void setDirection(int x, int y, MazeDirection d) {
        synchronized (MAZE) {
            MAZE[y][x] = d;
        }
    }

    public int getStartX() {
        return START_X;
    }

    public int getStartY() {
        return START_Y;
    }

    public int getFinishX() {
        return FINISH_X;
    }

    public int getFinishY() {
        return FINISH_Y;
    }

    public int getSolutionLength() {
        int x = getStartX();
        int y = getStartY();
        int count = 0;
        while (x != FINISH_X || y != FINISH_Y) {
            MazeDirection d = getDirection(x, y);
            if (d != null) {
                x += d.getX();
                y += d.getY();
                count++;
            } else {
                return Integer.MAX_VALUE;
            }
        }
        return count;
    }
}
