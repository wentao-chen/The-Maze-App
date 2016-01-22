package com.gmail.wentaochen97;

import com.gmail.wentaochen97.maze.Maze2D;

/**
 * Created by Wentao-Admin on 2016-01-13.
 */
public class MazeMovableObject {

    private final double RADIUS;
    private final int COLOR;

    public MazeMovableObject(double radius, int color) {
        RADIUS = radius;
        COLOR = color;
    }

    private double x = 0;
    private double y = 0;

    public double getRadius() {
        return RADIUS;
    }

    public int getColor() {
        return COLOR;
    }

    public int getGridX() {
        return (int) Math.floor(x);
    }

    public double getX() {
        return x;
    }

    public synchronized void setX(double x) {
        this.x = x;
    }

    public int getGridY() {
        return (int) Math.floor(y);
    }

    public double getY() {
        return y;
    }

    public synchronized void setY(double y) {
        this.y = y;
    }

    public boolean intersects(MazeMovableObject o) {
        return Math.abs(getX() - o.getX()) <= getRadius() + o.getRadius() && Math.abs(getY() - o.getY()) <= getRadius() + o.getRadius();
    }

    public boolean move(double distance, double angle, Maze2D maze) {
        return moveTo(getX() + distance * Math.cos(angle), getY() - distance * Math.sin(angle), maze);
    }

    public boolean moveTo(double x, double y, Maze2D maze) {
        return moveTo(x, y, maze, Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public boolean moveTo(double x, double y, Maze2D maze, double maxStepX, double maxStepY) {
        double originalX = getX();
        double originalY = getY();
        double distanceX =  Math.abs(x - originalX);
        double distanceY =  Math.abs(y - originalY);
        if (distanceX > maxStepX) {
            if (moveTo(originalX + (x > originalX ? 1 : -1) * distanceX / 2, y, maze, maxStepX, maxStepY)) {
                return moveTo(originalX + (x > originalX ? 1 : -1) * distanceX, y, maze, maxStepX, maxStepY);
            }
            return false;
        }
        if (distanceY > maxStepY) {
            if (moveTo(x, originalY + (y > originalY ? 1 : -1) * distanceY / 2, maze, maxStepX, maxStepY)) {
                return moveTo(x, originalY + (y > originalY ? 1 : -1) * distanceY, maze, maxStepX, maxStepY);
            }
            return false;
        }
        int currentTileX = (int) Math.floor(x) * 2;
        int currentTileY = (int) Math.floor(y) * 2;
        int leftX = (int) Math.floor(x - getRadius()) * 2;
        int rightX = (int) Math.floor(x + getRadius()) * 2;
        int topY = (int) Math.floor(y - getRadius()) * 2;
        int bottomY = (int) Math.floor(y + getRadius()) * 2;
        if (leftX < 0) {
            return false;
        } else if (leftX < currentTileX) {
            if (topY < currentTileY && maze.hasWall(currentTileX - 1, currentTileY - 2)) {
                return false;
            } else if (maze.hasWall(currentTileX - 1, currentTileY)) {
                return false;
            } else if (bottomY > currentTileY && maze.hasWall(currentTileX - 1, currentTileY + 2)) {
                return false;
            }
        }
        if (rightX >= maze.getWidth()) {
            return false;
        } else if (rightX > currentTileX) {
            if (topY < currentTileY && maze.hasWall(currentTileX + 1, currentTileY - 2)) {
                return false;
            } else if (maze.hasWall(currentTileX + 1, currentTileY)) {
                return false;
            } else if (bottomY > currentTileY && maze.hasWall(currentTileX + 1, currentTileY + 2)) {
                return false;
            }
        }
        if (topY < 0) {
            return false;
        } else if (topY < currentTileY) {
            if (leftX < currentTileX && maze.hasWall(currentTileX - 2, currentTileY - 1)) {
                return false;
            } else if (maze.hasWall(currentTileX, currentTileY - 1)) {
                return false;
            } else if (rightX > currentTileX && maze.hasWall(currentTileX + 2, currentTileY - 1)) {
                return false;
            }
        }
        if (bottomY >= maze.getHeight()) {
            return false;
        } else if (bottomY > currentTileY) {
            if (leftX < currentTileX && maze.hasWall(currentTileX - 2, currentTileY + 1)) {
                return false;
            } else if (maze.hasWall(currentTileX, currentTileY + 1)) {
                return false;
            } else if (rightX > currentTileX && maze.hasWall(currentTileX + 2, currentTileY + 1)) {
                return false;
            }
        }
        setX(x);
        setY(y);
        return true;
    }
}
