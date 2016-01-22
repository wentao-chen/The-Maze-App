package com.gmail.wentaochen97.mazebots;

import com.gmail.wentaochen97.MazeMovableObject;
import com.gmail.wentaochen97.maze.Maze2D;
import com.gmail.wentaochen97.maze.MazeDirection;

/**
 * Created by Wentao-Admin on 2016-01-14.
 */
public class TeamBot extends MazeRobot {

    public static final int BOT_COLOR = 0xFFB0465A;

    private final long SCATTER_TIME;
    private final long CHASE_TIME;
    private final MazeMovableObject TEAM;

    public TeamBot(double radius, int color, long scatterTime, long chaseTime, MazeMovableObject team) {
        super(radius, color);
        SCATTER_TIME = scatterTime;
        CHASE_TIME = chaseTime;
        TEAM = team;
    }

    @Override
    protected MazeDirection getNextDirection(MazeDirection currentDirection, double targetXD, double targetYD, double targetYaw, Maze2D maze) {
        if (System.currentTimeMillis() % (SCATTER_TIME + CHASE_TIME) < SCATTER_TIME) {
            return getNextDirectionNearsighted(currentDirection, maze.getWidth(), maze.getHeight(), maze);
        }
        int movesAhead = TEAM != null ? 2 : 4;
        int targetX = (int) Math.floor(targetXD) * 2;
        int targetY = (int) Math.floor(targetYD) * 2;
        MazeDirection targetDirection = MazeDirection.getNearestDirection(targetYaw);
        switch (targetDirection) {
            case EAST:
                targetX += movesAhead;
                break;
            case NORTH:
                targetY -= movesAhead;
                break;
            case SOUTH:
                targetY += movesAhead;
                break;
            case WEST:
                targetX -= movesAhead;
                break;
        }
        if (TEAM != null) {
            targetX = Math.min(Math.max(targetX, 0), maze.getWidth() - 1);
            targetY = Math.min(Math.max(targetY, 0), maze.getHeight() - 1);
            double dX = targetX + 0.5 - TEAM.getX() * 2;
            double dY = targetY + 0.5 - TEAM.getY() * 2;
            targetX = (int) Math.floor(targetX + dX * 2);
            targetY = (int) Math.floor(targetY + dY * 2);
        }
        targetX = Math.min(Math.max(targetX, 0), maze.getWidth() - 1);
        targetY = Math.min(Math.max(targetY, 0), maze.getHeight() - 1);
        MazeDirection d = getNextDirectionShortest(currentDirection, targetX / 2, targetY / 2, maze);
        return d != null ? d : getNextDirectionNearsighted(currentDirection, targetX / 2, targetY / 2, maze);
    }
}