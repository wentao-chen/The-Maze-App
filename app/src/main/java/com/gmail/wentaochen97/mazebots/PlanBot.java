package com.gmail.wentaochen97.mazebots;

import com.gmail.wentaochen97.maze.Maze2D;
import com.gmail.wentaochen97.maze.MazeDirection;

/**
 * Created by Wentao-Admin on 2016-01-14.
 */
public class PlanBot extends MazeRobot {

    public static final int BOT_COLOR = 0xFF996725;

    private final long SCATTER_TIME;
    private final long CHASE_TIME;
    private final int MOVES_AHEAD;

    public PlanBot(double radius, int color, long scatterTime, long chaseTime, int movesAhead) {
        super(radius, color);
        SCATTER_TIME = scatterTime;
        CHASE_TIME = chaseTime;
        MOVES_AHEAD = movesAhead;
    }

    @Override
    protected MazeDirection getNextDirection(MazeDirection currentDirection, double targetXD, double targetYD, double targetYaw, Maze2D maze) {
        if (System.currentTimeMillis() % (SCATTER_TIME + CHASE_TIME) < SCATTER_TIME) {
            return getNextDirectionNearsighted(currentDirection, -1, -1, maze);
        }
        int targetX = (int) Math.floor(targetXD) * 2;
        int targetY = (int) Math.floor(targetYD) * 2;
        MazeDirection targetDirection = MazeDirection.getNearestDirection(getYaw());
        switch (targetDirection) {
            case EAST:
                targetX += MOVES_AHEAD;
                break;
            case NORTH:
                targetY -= MOVES_AHEAD;
                break;
            case SOUTH:
                targetY += MOVES_AHEAD;
                break;
            case WEST:
                targetX -= MOVES_AHEAD;
                break;
        }
        targetX = Math.min(Math.max(targetX, 0), maze.getWidth() - 1);
        targetY = Math.min(Math.max(targetY, 0), maze.getHeight() - 1);
        MazeDirection d = getNextDirectionShortest(currentDirection, targetX / 2, targetY / 2, maze);
        return d != null ? d : getNextDirectionNearsighted(currentDirection, targetX / 2, targetY / 2, maze);
    }
}
