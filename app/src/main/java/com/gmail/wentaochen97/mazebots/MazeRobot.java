package com.gmail.wentaochen97.mazebots;

import com.gmail.wentaochen97.MazeMovableObject;
import com.gmail.wentaochen97.maze.CustomMazeSolver;
import com.gmail.wentaochen97.maze.Maze2D;
import com.gmail.wentaochen97.maze.MazeDirection;
import com.gmail.wentaochen97.maze.MazeOrientation;
import com.gmail.wentaochen97.maze.MazeSolution2D;
import com.gmail.wentaochen97.maze.ShortestPathFinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wentao-Admin on 2016-01-13.
 */
public abstract class MazeRobot extends MazeMovableObject {

    private double speed = 1;

    private MazeDirection currentDirection = null;
    private double yaw = (int) (Math.random() * 4) * Math.PI / 2;

    public MazeRobot(double radius, int color) {
        super(radius, color);
    }

    public MazeDirection getCurrentDirection() {
        return this.currentDirection;
    }

    public void setCurrentDirection(MazeDirection d) {
        this.currentDirection = d;
    }

    public double getSpeed() {
        return this.speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    protected double getYaw() {
        return this.yaw;
    }

    private void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public void update(double time, double targetX, double targetY, double targetYaw, Maze2D maze) {
        MazeDirection current = getCurrentDirection();
        double centerX = Math.floor(getX()) + 0.5;
        double centerY = Math.floor(getY()) + 0.5;
        if (current == null && getX() == centerX && getY() == centerY) {
            current = getNextDirection(current, targetX, targetY, targetYaw, maze);
        } else if (current == null) {
            setYaw(Math.atan2(getY() - centerY, centerX - getX()));
        } else if (current.getOrientation() == MazeOrientation.HORIZONTAL) {
            setY(centerY);
        } else if (current.getOrientation() == MazeOrientation.VERTICAL) {
            setX(centerX);
        }
        double d = Double.MAX_VALUE;
        if (current != null) {
            switch (current) {
                case EAST:
                    d = centerX - getX();
                    break;
                case NORTH:
                    d = getY() - centerY;
                    break;
                case SOUTH:
                    d = centerY - getY();
                    break;
                case WEST:
                    d = getX() - centerX;
                    break;
            }
        } else {
            d = Math.sqrt(Math.pow(centerX - getX(), 2) + Math.pow(centerY - getY(), 2));
        }
        if (d > 0 && d <= time * getSpeed()) {
            setX(centerX);
            setY(centerY);
            MazeDirection direction = null;
            if (maze.isIntersection((int) Math.floor(centerX) * 2, (int) Math.floor(centerY) * 2)) {
                direction = getNextDirection(current, targetX, targetY, targetYaw, maze);
                if (direction != null) {
                    boolean safeDirection = false;
                    switch (direction) {
                        case EAST:
                            safeDirection = centerX < maze.getWidth() - 1 && !maze.hasWall((int) Math.floor(centerX) * 2 + 1, (int) Math.floor(centerY) * 2);
                            break;
                        case NORTH:
                            safeDirection = centerY >= 1 && !maze.hasWall((int) Math.floor(centerX) * 2, (int) Math.floor(centerY) * 2 - 1);
                            break;
                        case WEST:
                            safeDirection = centerX >= 1 && !maze.hasWall((int) Math.floor(centerX) * 2 - 1, (int) Math.floor(centerY) * 2);
                            break;
                        case SOUTH:
                            safeDirection = centerY < maze.getHeight() - 1 && !maze.hasWall((int) Math.floor(centerX) * 2, (int) Math.floor(centerY) * 2 + 1);
                            break;
                    }
                    if (!safeDirection || direction.getOpposite() == current) {
                        direction = null;
                    }
                }
            }
            if (direction == null) {
                List<MazeDirection> available = getAvailableDirections(maze);
                if (current != null && available.size() > 1) {
                    available.remove(current.getOpposite());
                }
                if (available.size() > 0) {
                    direction = available.get((int) (Math.random() * available.size()));
                }
            }
            if (direction != null) {
                synchronized (this) {
                    this.currentDirection = direction;
                }
                switch (direction) {
                    case EAST:
                        setYaw(0);
                        break;
                    case NORTH:
                        setYaw(Math.PI / 2);
                        break;
                    case SOUTH:
                        setYaw(Math.PI * 3 / 2);
                        break;
                    case WEST:
                        setYaw(Math.PI);
                        break;
                }
            }
        } else {
            move(time * getSpeed(), getYaw(), maze);
        }
    }

    List<MazeDirection> getAvailableDirections(Maze2D maze) {
        List<MazeDirection> list = new ArrayList<MazeDirection>();
        int x = getGridX() * 2;
        int y = getGridY() * 2;
        if (x > 0 && !maze.hasWall(x - 1, y)) {
            list.add(MazeDirection.WEST);
        }
        if (x < maze.getWidth() - 1 && !maze.hasWall(x + 1, y)) {
            list.add(MazeDirection.EAST);
        }
        if (y > 0 && !maze.hasWall(x, y - 1)) {
            list.add(MazeDirection.NORTH);
        }
        if (y < maze.getHeight() - 1 && !maze.hasWall(x, y + 1)) {
            list.add(MazeDirection.SOUTH);
        }
        return list;
    }

    protected MazeDirection getNextDirectionNearsighted(MazeDirection currentDirection, int targetX, int targetY, Maze2D maze) {
        double closest = Double.MAX_VALUE;
        MazeDirection closestD = null;
        for (MazeDirection d : MazeDirection.values()) {
            if (currentDirection == null || currentDirection.getOpposite() != d) {
                int newX = getGridX() * 2 + d.getX();
                int newY = getGridY() * 2 + d.getY();
                if (newX >= 0 && newX < maze.getWidth() && newY >= 0 && newY < maze.getHeight()) {
                    double distance = Math.sqrt(Math.pow(targetX * 2 - newX, 2) + Math.pow(targetY * 2 - newY, 2));
                    if (distance < closest) {
                        closest = distance;
                        closestD = d;
                    }
                }
            }
        }
        return closestD;
    }

    protected MazeDirection getNextDirectionShortest(MazeDirection currentDirection, int targetX, int targetY, Maze2D maze) {
        int x = getGridX();
        int y = getGridY();
        CustomMazeSolver solver = new ShortestPathFinder(maze);
        if (currentDirection != null) {
            solver.setDirectionBlocked(x * 2, y * 2, currentDirection.getOpposite(), true);
        }
        MazeSolution2D solution = solver.getSolution(x * 2, y * 2, targetX * 2, targetY * 2);
        return solution != null ? solution.getFirstDirection() : null;
    }

    protected abstract MazeDirection getNextDirection(MazeDirection currentDirection, double targetX, double targetY, double targetYaw, Maze2D maze);
}
