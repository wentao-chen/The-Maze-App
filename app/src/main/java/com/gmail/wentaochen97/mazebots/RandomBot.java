package com.gmail.wentaochen97.mazebots;

import com.gmail.wentaochen97.maze.Maze2D;
import com.gmail.wentaochen97.maze.MazeDirection;

import java.util.List;

/**
 * Created by Wentao-Admin on 2016-01-14.
 */
public class RandomBot extends MazeRobot {

    public static final int BOT_COLOR = 0xFF676ED6;

    public RandomBot(double radius, int color) {
        super(radius, color);
    }

    @Override
    protected MazeDirection getNextDirection(MazeDirection currentDirection, double targetX, double targetY, double targetYaw, Maze2D maze) {
        List<MazeDirection> directions = getAvailableDirections(maze);
        return directions.get((int) (Math.random() * directions.size()));
    }
}
