package com.gmail.wentaochen97.mazebots;

import com.gmail.wentaochen97.maze.CustomMazeSolver;
import com.gmail.wentaochen97.maze.Maze2D;
import com.gmail.wentaochen97.maze.MazeDirection;
import com.gmail.wentaochen97.maze.MazeSolution2D;
import com.gmail.wentaochen97.maze.ShortestPathFinder;

/**
 * Created by Wentao-Admin on 2016-01-14.
 */
public class SwitchBot extends MazeRobot {

    public static final int BOT_COLOR = 0xFFD9CC14;

    private final long SCATTER_TIME;
    private final long CHASE_TIME;

    public SwitchBot(double radius, int color, long scatterTime, long chaseTime) {
        super(radius, color);
        SCATTER_TIME = scatterTime;
        CHASE_TIME = chaseTime;
    }

    @Override
    protected MazeDirection getNextDirection(MazeDirection currentDirection, double targetXD, double targetYD, double targetYaw, Maze2D maze) {
        if (System.currentTimeMillis() % (SCATTER_TIME + CHASE_TIME) < SCATTER_TIME) {
            return getNextDirectionNearsighted(currentDirection, -1, maze.getHeight(), maze);
        }
        int proximityTrigger = 8;
        int targetX = (int) Math.floor(targetXD) * 2;
        int targetY = (int) Math.floor(targetYD) * 2;
        int x = (int) Math.floor(getX()) * 2;
        int y = (int) Math.floor(getY()) * 2;
        CustomMazeSolver solver = new ShortestPathFinder(maze);
        if (currentDirection != null) {
            solver.setDirectionBlocked(x, y, currentDirection.getOpposite(), true);
        }
        MazeSolution2D solution = solver.getSolution(x, y, targetX, targetY);
        if (solution == null || solution.getSolutionLength() <= proximityTrigger) {
            return getNextDirectionNearsighted(currentDirection, -1, maze.getHeight(), maze);
        }
        MazeDirection d = getNextDirectionShortest(currentDirection, targetX / 2, targetY / 2, maze);
        return d != null ? d : getNextDirectionNearsighted(currentDirection, targetX / 2, targetY / 2, maze);
    }
}
