package com.gmail.wentaochen97.mazebots;

import com.gmail.wentaochen97.maze.Maze2D;
import com.gmail.wentaochen97.maze.MazeDirection;

/**
 * Created by Wentao-Admin on 2016-01-14.
 */
public class ChaseBot extends MazeRobot {

    public static final int BOT_COLOR = 0xFF87C8DE;

    private final long SCATTER_TIME;
    private final long CHASE_TIME;

    public ChaseBot(double radius, int color, long scatterTime, long chaseTime) {
        super(radius, color);
        SCATTER_TIME = scatterTime;
        CHASE_TIME = chaseTime;
    }

    @Override
    protected MazeDirection getNextDirection(MazeDirection currentDirection, double targetX, double targetY, double targetYaw, Maze2D maze) {
        if (System.currentTimeMillis() % (SCATTER_TIME + CHASE_TIME) < SCATTER_TIME) {
            return getNextDirectionNearsighted(currentDirection, - 1, maze.getWidth(), maze);
        }
        int targetXInt = (int) Math.floor(targetX);
        int targetYInt = (int) Math.floor(targetY);
        MazeDirection d = getNextDirectionShortest(currentDirection, targetXInt, targetYInt, maze);
        return d != null ? d : getNextDirectionNearsighted(currentDirection, targetXInt, targetYInt, maze);
    }
}
