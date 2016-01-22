package com.gmail.wentaochen97.maze;

import com.gmail.wentaochen97.MazeMovableObject;

/**
 * Created by Wentao-Admin on 2016-01-07.
 */
public class Player extends MazeMovableObject {

    private final double SPEED_MULTIPLY;
    private final int PATH_COLOR;
    private final int FINISH_COLOR;
    private final int UNVISITED_COLOR;

    private boolean[][] visited = null;

    private double velocityX = 0;
    private double velocityY = 0;

    public Player(double radius, double speedMultiply, int color, int pathColor, int finishColor, int unvisitedColor) {
        super(radius, color);
        SPEED_MULTIPLY = speedMultiply;
        PATH_COLOR = pathColor;
        FINISH_COLOR = finishColor;
        UNVISITED_COLOR = unvisitedColor;
    }

    public double getSpeedMultiply() {
        return SPEED_MULTIPLY;
    }

    public int getPathColor() {
        return PATH_COLOR;
    }

    public int getFinishColor() {
        return FINISH_COLOR;
    }

    public int getUnvisitedColor() {
        return UNVISITED_COLOR;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public synchronized void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public synchronized void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public boolean move(double timeInSeconds, Maze2D maze) {
        return move(timeInSeconds, maze, Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public boolean move(double timeInSeconds, Maze2D maze, double maxStepX, double maxStepY) {
        double vx = getVelocityX();
        double vy = getVelocityY();
        if (moveTo(getX() + timeInSeconds * vx, getY() - timeInSeconds * vy, maze, maxStepX, maxStepY)) {
        } else if (moveTo(getX(), getY() - timeInSeconds * vy, maze, maxStepX, maxStepY)) {
        } else if (moveTo(getX() + timeInSeconds * vx, getY(), maze, maxStepX, maxStepY)) {
        } else {
            return false;
        }
        return true;
    }

    public synchronized void clearVisited() {
        this.visited = new boolean[this.visited.length][this.visited[0].length];
    }

    public synchronized void setNewVisitedSize(int width, int height) {
        this.visited = new boolean[height][width];
    }

    public synchronized void setVisited(int x, int y, boolean visited) {
        this.visited[y][x] = visited;
    }

    boolean isVisitedQuick(int x, int y) {
        return this.visited != null && this.visited[y][x];
    }

    public boolean isVisited(int x, int y) {
        return this.visited != null && y >= 0 && y < this.visited.length && x >= 0 && x < this.visited[y].length && this.visited[y][x];
    }
}
