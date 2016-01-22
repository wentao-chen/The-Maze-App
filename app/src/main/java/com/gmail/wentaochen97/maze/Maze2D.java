package com.gmail.wentaochen97.maze;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.gmail.wentaochen97.mazebots.MazeRobot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Wentao-Admin on 2016-01-07.
 */
public class Maze2D {

    private final boolean[][] MAZE;
    private final int[] START_X;
    private final int[] START_Y;
    private final int[] FINISH_X;
    private final int[] FINISH_Y;

    /*Maze2D(int gridWidth, int gridHeight) {
        this(gridWidth, gridHeight, null, null, null, null);
    }

    Maze2D(int gridWidth, int gridHeight, int startX, int startY, int finishX, int finishY) {
        this(gridWidth, gridHeight, (Integer) startX, (Integer) startY, (Integer) finishX, (Integer) finishY);
    }*/

    Maze2D(int gridWidth, int gridHeight, int players) {
        MAZE = new boolean[2 * gridHeight - 1][2 * gridWidth - 1];
        START_X = new int[players];
        START_Y = new int[players];
        FINISH_X = new int[players];
        FINISH_Y = new int[players];
        for (int i = 0; i < players; i++) {
            START_X[i] = (int) (Math.random() * gridWidth);
            START_Y[i] = (int) (Math.random() * gridHeight);
            FINISH_X[i] = (int) (Math.random() * gridWidth);
            if (FINISH_X[i] == START_X[i]) {
                FINISH_Y[i] = (int) (Math.random() * (gridHeight - 1));
                if (FINISH_Y[i] >= START_Y[i]) {
                    FINISH_Y[i]++;
                }
            } else {
                FINISH_Y[i] = (int) (Math.random() * gridHeight);
            }
        }
    }

    Maze2D(int gridWidth, int gridHeight, int[] startX, int[] startY, int[] finishX, int[] finishY) {
        MAZE = new boolean[2 * gridHeight - 1][2 * gridWidth - 1];
        START_X = new int[startX.length];
        START_Y = new int[startX.length];
        FINISH_X = new int[startX.length];
        FINISH_Y = new int[startX.length];
        for (int i = 0; i < startX.length; i++) {
            START_X[i] = startX[i];
            START_Y[i] = startY[i];
            FINISH_X[i] = finishX[i];
            FINISH_Y[i] = finishY[i];
        }
    }

    public int getWidth() {
        return MAZE[0].length;
    }

    public int getGridWidth() {
        return (MAZE[0].length + 1) / 2;
    }

    public int getHeight() {
        return MAZE.length;
    }

    public int getGridHeight() {
        return (MAZE.length + 1) / 2;
    }

    public int getPlayersCount() {
        return START_X.length;
    }

    public int getStartX(int i) {
        return START_X[i];
    }

    public int getStartY(int i) {
        return START_Y[i];
    }

    public int getFinishX(int i) {
        return FINISH_X[i];
    }

    public int getFinishY(int i) {
        return FINISH_Y[i];
    }

    public void setDeadEndAsFinish(double minDistanceRatio) {
        for (int i = 0; i < START_X.length; i++) {
            int selection = (int) (Math.random() * (getDeadEndsCount() - (isGridDeadEnd(START_X[i], START_Y[i]) ? 1 : 0)));
            int selectionX = -1;
            int selectionY = -1;
            for (int y = 0; y < MAZE.length && selectionY < 0; y++) {
                for (int x = 0; x < MAZE[y].length; x++) {
                    if (isDeadEnd(x, y) && (x != START_X[i] * 2 || y != START_Y[i] * 2)) {
                        if (--selection < 0) {
                            selectionX = x;
                            selectionY = y;
                            break;
                        }
                    }
                }
            }
            if (selectionX >= 0 && selectionY >= 0) {
                synchronized (this) {
                    FINISH_X[i] = selectionX / 2;
                    FINISH_Y[i] = selectionY / 2;
                }
            }
        }
        setStartAwayFromFinish(minDistanceRatio);
    }

    public void setStartAwayFromFinish(double minDistanceRatio) {
        minDistanceRatio = Math.min(Math.max(minDistanceRatio, 0.1), 0.9);
        for (int i = 0; i < START_X.length; i++) {
            Integer[][] distance = new Integer[getHeight()][getWidth()];
            boolean found = true;
            int finishX = getFinishX(i);
            int finishY = getFinishY(i);
            distance[finishY * 2][finishX * 2] = 0;
            while (found) {
                boolean[][] isPath = new boolean[getHeight()][getWidth()];
                for (int y = 0; y < distance.length; y++) {
                    for (int x = 0; x < distance[y].length; x++) {
                        isPath[y][x] = distance[y][x] != null;
                    }
                }
                found = false;
                for (int y = 0; y < distance.length; y++) {
                    for (int x = 0; x < distance[y].length; x++) {
                        if (!hasWall(x, y) && distance[y][x] == null) {
                            if (x > 0 && isPath[y][x - 1]) {
                                distance[y][x] = distance[y][x - 1] + 1;
                                found = true;
                            } else if (x < getWidth() - 1 && isPath[y][x + 1]) {
                                distance[y][x] = distance[y][x + 1] + 1;
                                found = true;
                            } else if (y > 0 && isPath[y - 1][x]) {
                                distance[y][x] = distance[y - 1][x] + 1;
                                found = true;
                            } else if (y < getHeight() - 1 && isPath[y + 1][x]) {
                                distance[y][x] = distance[y + 1][x] + 1;
                                found = true;
                            }
                        }
                    }
                }
            }
            int maxDistance = 0;
            for (int y = 0; y < distance.length; y++) {
                for (int x = 0; x < distance[y].length; x++) {
                    if (distance[y][x] != null && distance[y][x] >= maxDistance) {
                        maxDistance = distance[y][x];
                    }
                }
            }
            int selectDistance = (int) Math.floor((1 - Math.pow(Math.random() * minDistanceRatio + (1 - minDistanceRatio), 6)) * maxDistance) / 2 * 2;
            int count = 0;
            for (int y = 0; y < distance.length; y++) {
                for (int x = 0; x < distance[y].length; x++) {
                    if (distance[y][x] != null && distance[y][x] >= selectDistance) {
                        count++;
                    }
                }
            }
            count = (int) Math.floor(Math.random() * count);
            for (int y = 0; y < distance.length && count >= 0; y++) {
                for (int x = 0; x < distance[y].length; x++) {
                    if (distance[y][x] != null && distance[y][x] >= selectDistance) {
                        if (--count < 0) {
                            START_X[i] = x / 2;
                            START_Y[i] = y / 2;
                            break;
                        }
                    }
                }
            }
        }
    }

    public boolean inMaze(int x, int y) {
        return y >= 0 && y < MAZE.length && x >= 0 && x < MAZE[y].length;
    }

    public boolean hasWall(int x, int y) {
        return inMaze(x, y) && MAZE[y][x];
    }

    void setWall(int x, int y, boolean wall) {
        synchronized (MAZE) {
            MAZE[y][x] = wall;
        }
    }

    void setAll(boolean wall) {
        synchronized (MAZE) {
            for (int y = 0; y < MAZE.length; y++) {
                for (int x = 0; x < MAZE[y].length; x++) {
                    MAZE[y][x] = wall;
                }
            }
        }
    }

    void setGridLayout() {
        synchronized (MAZE) {
            for (int y = 0; y < MAZE.length; y++) {
                for (int x = 0; x < MAZE[y].length; x++) {
                    MAZE[y][x] = x % 2 == 1 || y % 2 == 1;
                }
            }
        }
    }

    public boolean isDeadEnd(int x, int y) {
        return getExits(x, y) == 1;
    }

    public boolean isGridDeadEnd(int x, int y) {
        return isDeadEnd(2 * x, 2 * y);
    }

    public boolean isIntersection(int x, int y) {
        return getExits(x, y) >= 3;
    }

    public boolean isGridIntersection(int x, int y) {
        return isIntersection(2 * x, 2 * y);
    }

    public int getDeadEndsCount() {
        int count = 0;
        for (int y = 0; y < MAZE.length; y += 2) {
            for (int x = 0; x < MAZE[y].length; x += 2) {
                if (isDeadEnd(x, y)) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getIntersections() {
        int count = 0;
        for (int y = 0; y < MAZE.length; y += 2) {
            for (int x = 0; x < MAZE[y].length; x += 2) {
                if (isIntersection(x, y)) {
                    count++;
                }
            }
        }
        return count;
    }

    private int getExits(int x, int y) {
        if (hasWall(x, y)) {
            return 0;
        }
        int count = 0;
        if (x > 0 && !hasWall(x - 1, y)) {
            count++;
        }
        if (x < getWidth() - 1 && !hasWall(x + 1, y)) {
            count++;
        }
        if (y > 0 && !hasWall(x, y - 1)) {
            count++;
        }
        if (y < getHeight() - 1 && !hasWall(x, y + 1)) {
            count++;
        }
        return count;
    }

    public int getWallCount() {
        int count = 0;
        for (int y = 0; y < MAZE.length; y++) {
            for (int x = 0; x < MAZE[y].length; x++) {
                if (hasWall(x, y)) {
                    count++;
                }
            }
        }
        return count;
    }

    private int getAdjoiningWalls(int x, int y) {
        int count = 0;
        for (MazeDirection d : MazeDirection.values()) {
            if (!(x + d.getX() >= 0 && x + d.getX() < getWidth() && y + d.getY() >= 0 && y + d.getY() < getHeight()) || hasWall(x + d.getX(), y + d.getY())) {
                count++;
            }
        }
        return count;
    }

    private boolean shouldRemoveWall(int x, int y) {
        if (x % 2 == 1) {
            if (getAdjoiningWalls(x, y + 1) <= 1 || getAdjoiningWalls(x, y - 1) <= 1) {
                return false;
            }
        } else {
            if (getAdjoiningWalls(x + 1, y) <= 1 || getAdjoiningWalls(x - 1, y) <= 1) {
                return false;
            }
        }
        return true;
    }

    public void setBraidMaze(Random random) {
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (isDeadEnd(x, y)) {
                    List<MazeDirection> available = new ArrayList<>();
                    for (MazeDirection d : MazeDirection.values()) {
                        if (x + d.getX() >= 0 && x + d.getX() < getWidth() && y + d.getY() >= 0 && y + d.getY() < getHeight() && hasWall(x + d.getX(), y + d.getY())) {
                            available.add(d);
                        }
                    }
                    if (available.size() == 1) {
                        MazeDirection d = available.get(0);
                        setWall(x + d.getX(), y + d.getY(), false);
                    }
                }
            }
        }
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (isDeadEnd(x, y)) {
                    List<MazeDirection> available = new ArrayList<>();
                    for (MazeDirection d : MazeDirection.values()) {
                        if (x + d.getX() >= 0 && x + d.getX() < getWidth() && y + d.getY() >= 0 && y + d.getY() < getHeight() && hasWall(x + d.getX(), y + d.getY()) && shouldRemoveWall(x + d.getX(), y + d.getY())) {
                            available.add(d);
                        }
                    }
                    if (available.size() > 0) {
                        MazeDirection d = available.get((int) random.nextDouble() * available.size());
                        setWall(x + d.getX(), y + d.getY(), false);
                    } else {
                        available = new ArrayList<>();
                        for (MazeDirection d : MazeDirection.values()) {
                            if (x + d.getX() >= 0 && x + d.getX() < getWidth() && y + d.getY() >= 0 && y + d.getY() < getHeight() && hasWall(x + d.getX(), y + d.getY())) {
                                available.add(d);
                            }
                        }
                        if (available.size() > 0) {
                            MazeDirection d = available.get((int) random.nextDouble() * available.size());
                            setWall(x + d.getX(), y + d.getY(), false);
                        }
                    }
                }
            }
        }
        for (int y = 1; y < getHeight(); y += 2) {
            for (int x = 1; x < getWidth(); x += 2) {
                if (getAdjoiningWalls(x, y) == 0) {
                    for (MazeDirection d : MazeDirection.values()) {
                        setWall(x + d.getX(), y + d.getY(), true);
                        boolean hasDeadEnd = false;
                        for (int y2 = -1; y2 <= 1 && !hasDeadEnd; y2++) {
                            for (int x2 = -1; x2 <= 1; x2++) {
                                if (isDeadEnd(x + x2, y + y2)) {
                                    hasDeadEnd = true;
                                    break;
                                }
                            }
                        }
                        if (hasDeadEnd) {
                            setWall(x + d.getX(), y + d.getY(), false);
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
    }

    public Maze2D getLabyrinth() {
        int[] startX = new int[getPlayersCount()];
        int[] startY = new int[getPlayersCount()];
        int[] finishX = new int[getPlayersCount()];
        int[] finishY = new int[getPlayersCount()];
        for (int i = 0; i < startX.length; i++) {
            startX[i] = getStartX(i) * 2;
            startY[i] = getStartY(i) * 2;
            finishX[i] = getFinishX(i) * 2;
            finishY[i] = getFinishY(i) * 2;
        }
        Maze2D maze = new Maze2D(getWidth() + 1, getHeight() + 1, startX, startY, finishX, finishY);
        maze.setGridLayout();
        for (int y = 0; y < getHeight(); y += 2) {
            for (int x = 0; x < getWidth(); x++) {
                maze.setWall(x * 2 + 1, y * 2, hasWall(x, y));
                maze.setWall(x * 2 + 1, y * 2 + 2, hasWall(x, y));
            }
        }
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x += 2) {
                maze.setWall(x * 2, y * 2 + 1, hasWall(x, y));
                maze.setWall(x * 2 + 2, y * 2 + 1, hasWall(x, y));
            }
        }
        for (int y = 0; y < getGridHeight(); y++) {
            for (int x = 0; x < getGridWidth(); x++) {
                for (MazeDirection d : MazeDirection.values()) {
                    if (x + d.getX() >= 0 && x + d.getX() < getGridWidth() && y + d.getY() >= 0 && y + d.getY() < getGridHeight() && !hasWall(2 * x + d.getX(), 2 * y + d.getY())) {
                        maze.setWall(x * 4 + 1 + d.getX(), y * 4 + 1 + d.getY(), true);
                    }
                }
            }
        }
        for (int i = 0; i < getPlayersCount(); i++) {
            boolean second = Math.random() < 0.5;
            for (MazeDirection d : MazeDirection.values()) {
                if (maze.getStartX(i) + d.getX() >= 0 && maze.getStartX(i) + d.getX() < maze.getGridWidth() && maze.getStartY(i) + d.getY() >= 0 && maze.getStartY(i) + d.getY() < maze.getGridHeight() && !maze.hasWall(2 * maze.getStartX(i) + d.getX(), 2 * maze.getStartY(i) + d.getY())) {
                    if (second) {
                        second = false;
                    } else {
                        maze.FINISH_X[i] = maze.getStartX(i) + d.getX();
                        maze.FINISH_Y[i] = maze.getStartY(i) + d.getY();
                        maze.setWall(maze.getStartX(i) * 2 + d.getX(), maze.getStartY(i) * 2 + d.getY(), true);
                        break;
                    }
                }
            }
        }
        return maze;
    }

    public double[] getMazeDifficulty(int i) {
        return getMazeDifficulty(getStartX(i), getStartY(i), getFinishX(i), getFinishY(i));
    }

    /**
     * Calculates the difficulty of the current maze based the paper "The Complexity and Difficulty of a Maze" by Michael Scott MacClendon.
     * The algorithm assumes no loops in the maze.<br><br>
     * The returned array has a length of 3:<br>
     * <ul>
     * <li>Index 0 refers to an extrinsic estimate of the complexity of the maze.</li>
     * <li>Index 1 refers to an intrinsic estimate of the complexity of the maze.</li>
     * <li>Index 2 refers to an intrinsic estimate of the difficulty of the maze.</li>
     * </ul>
     * @param startX the starting x-coordinate of the maze
     * @param startY the starting y-coordinate of the maze
     * @param finishX the finishing x-coordinate of the maze
     * @param finishY the finishing y-coordinate of the maze
     * @return an array containing statistics on the difficulty of the current maze
     */
    public double[] getMazeDifficulty(int startX, int startY, int finishX, int finishY) {
        int width = getWidth();
            int[][] hallwayGroup = new int[getHeight()][getWidth()];
        boolean[][] isIntersection = new boolean[getHeight()][getWidth()];
        for (int y = 0; y < hallwayGroup.length; y++) {
            for (int x = 0; x < hallwayGroup[y].length; x++) {
                isIntersection[y][x] = isIntersection(x, y) || (x == startX && y == startY) || (x == finishX && y == finishY);
            }
        }
        int hallwaysCount = floodFillHallwayGroupsMain(hallwayGroup, isIntersection);
        Double[] hallwayComplexity = new Double[hallwaysCount];
        for (int y = 0; y < hallwayGroup.length; y++) {
            for (int x = 0; x < hallwayGroup[y].length; x++) {
                if (isIntersection[y][x]) {
                    if (x > 0 && hallwayGroup[y][x - 1] >= 0 && hallwayComplexity[hallwayGroup[y][x - 1]] == null) {
                        hallwayComplexity[hallwayGroup[y][x - 1]] = findHallwayComplexity(x - 1 + y * width, hallwayGroup, MazeDirection.WEST);
                    } else if (x < hallwayGroup[y].length - 1 && hallwayGroup[y][x + 1] >= 0 && hallwayComplexity[hallwayGroup[y][x + 1]] == null) {
                        hallwayComplexity[hallwayGroup[y][x + 1]] = findHallwayComplexity(x + 1 + y * width, hallwayGroup, MazeDirection.EAST);
                    } else if (y > 0 && hallwayGroup[y - 1][x] >= 0 && hallwayComplexity[hallwayGroup[y - 1][x]] == null) {
                        hallwayComplexity[hallwayGroup[y - 1][x]] = findHallwayComplexity(x + (y - 1) * width, hallwayGroup, MazeDirection.NORTH);
                    } else if (y < hallwayGroup.length - 1 && hallwayGroup[y + 1][x] >= 0 && hallwayComplexity[hallwayGroup[y + 1][x]] == null) {
                        hallwayComplexity[hallwayGroup[y + 1][x]] = findHallwayComplexity(x + (y + 1) * width, hallwayGroup, MazeDirection.SOUTH);
                    }
                }
            }
        }
        double[] difficulty = new double[3];
        double total = 0;
        for (int i = 0; i < hallwayComplexity.length; i++) {
            total += hallwayComplexity[i] != null ? hallwayComplexity[i] : 0;
        }
        difficulty[0] = Math.log10(total);
        boolean[] solution = findSolutionHallways(startX, startY, finishX, finishY, hallwayGroup, hallwaysCount);
        if (solution == null) {
            difficulty[1] = Double.POSITIVE_INFINITY;
            difficulty[2] = Double.POSITIVE_INFINITY;
            return difficulty;
        }
        for (int y = 0; y < hallwayGroup.length; y++) {
            for (int x = 0; x < hallwayGroup[y].length; x++) {
                if (hallwayGroup[y][x] >= 0 && solution[hallwayGroup[y][x]]) {
                    hallwayGroup[y][x] = hallwaysCount;
                }
            }
        }
        hallwayGroup[startY][startX] = hallwaysCount;
        hallwayGroup[finishY][finishX] = hallwaysCount;
        double solutionComplexity = findHallwayComplexity(startX + startY * width, hallwayGroup, null);
        total = solutionComplexity;
        for (int i = 0; i < hallwaysCount; i++) {
            if (!solution[i] && hallwayComplexity[i] != null) {
                total += hallwayComplexity[i];
            }
        }
        difficulty[1] = Math.log10(total);

        boolean[][] visited = new boolean[hallwayGroup.length][hallwayGroup[0].length];
        int[] branch = new int[hallwaysCount];
        for (int i = 0; i < branch.length; i++) {
            branch[i] = -1;
        }
        int branchNo = 0;
        for (int y = 0; y < hallwayGroup.length; y++) {
            for (int x = 0; x < hallwayGroup[y].length; x++) {
                if (floodfillBranches(x, y, hallwayGroup, visited, solution, branch, branchNo)) {
                    branchNo++;
                }
            }
        }
        double[] branchComplexity = new double[branchNo];
        for (int i = 0; i < hallwayComplexity.length; i++) {
            if (hallwayComplexity[i] != null && branch[i] >= 0) {
                branchComplexity[branch[i]] += hallwayComplexity[i];
            }
        }
        total = solutionComplexity;
        for (double d : branchComplexity) {
            total *= d + 1;
        }
        difficulty[2] = Math.log10(total);
        return difficulty;
    }

    private boolean floodfillBranches(int x, int y, int[][] hallwayGroup, boolean[][] visited, boolean[] solution, int[] branch, int branchNo) {
        if (visited[y][x] || hallwayGroup[y][x] < -1 || hallwayGroup[y][x] >= solution.length || (hallwayGroup[y][x] >= 0 && solution[hallwayGroup[y][x]])) {
            return false;
        }
        int height = hallwayGroup.length;
        int width = hallwayGroup[0].length;
        visited[y][x] = true;
        if (hallwayGroup[y][x] >= 0) {
            branch[hallwayGroup[y][x]] = branchNo;
        }
        if (x > 0) {
            floodfillBranches(x - 1, y, hallwayGroup, visited, solution, branch, branchNo);
        }
        if (x < width - 1) {
            floodfillBranches(x + 1, y, hallwayGroup, visited, solution, branch, branchNo);
        }
        if (y > 0) {
            floodfillBranches(x, y - 1, hallwayGroup, visited, solution, branch, branchNo);
        }
        if (y < height - 1) {
            floodfillBranches(x, y + 1, hallwayGroup, visited, solution, branch, branchNo);
        }
        return true;
    }

    private boolean[] findSolutionHallways(int startX, int startY, int finishX, int finishY, int[][] hallwayGroup, int hallwaysCount) {
        startX *= 2;
        startY *= 2;
        finishX *= 2;
        finishY *= 2;
        int width = hallwayGroup[0].length;
        int height = hallwayGroup.length;
        int[] hallwayConnection = new int[hallwaysCount];
        for (int i = 0; i < hallwayConnection.length; i++) {
            hallwayConnection[i] = -2;
        }
        boolean[][] visited = new boolean[hallwayGroup.length][width];
        visited[startY][startX] = true;
        hallwayGroup[startY][startX] = -1;
        hallwayGroup[finishY][finishX] = -1;
        boolean check = false;
        while (!visited[finishY][finishX]) {
            check = false;
            for (int y = 0; y < hallwayGroup.length; y++) {
                for (int x = 0; x < hallwayGroup[y].length; x++) {
                    if (!visited[y][x] && hallwayGroup[y][x] > -1 && hallwayConnection[hallwayGroup[y][x]] >= -1) {
                        visited[y][x] = true;
                        check = true;
                    } else if (!visited[y][x] && hallwayGroup[y][x] >= -1 && (hallwayGroup[y][x] == -1 || hallwayConnection[hallwayGroup[y][x]] == -2)) {
                        int connectedGroup = -2;
                        if (x > 0 && visited[y][x - 1] && hallwayGroup[y][x - 1] >= -1) {
                            connectedGroup = hallwayGroup[y][x - 1];
                        } else if (x < width - 1 && visited[y][x + 1] && hallwayGroup[y][x + 1] >= -1) {
                            connectedGroup = hallwayGroup[y][x + 1];
                        } else if (y > 0 && visited[y - 1][x] && hallwayGroup[y - 1][x] >= -1) {
                            connectedGroup = hallwayGroup[y - 1][x];
                        } else if (y < height - 1 && visited[y + 1][x] && hallwayGroup[y + 1][x] >= -1) {
                            connectedGroup = hallwayGroup[y + 1][x];
                        }
                        if (connectedGroup >= -1) {
                            if (hallwayGroup[y][x] == -1) {
                                hallwayGroup[y][x] = connectedGroup;
                            } else {
                                hallwayConnection[hallwayGroup[y][x]] = connectedGroup;
                            }
                            visited[y][x] = true;
                            check = true;
                        }
                    }
                }
            }
            if (!check) {
                return null;
            }
        }
        boolean[] solution = new boolean[hallwaysCount];
        int current = hallwayGroup[finishY][finishX];
        if (current >= 0) {
            solution[current] = true;
            while (hallwayConnection[current] >= 0) {
                current = hallwayConnection[current];
                solution[current] = true;
            }
            return solution;
        } else {
            return null;
        }
    }

    private boolean directionAlreadyChosen(Integer i, int direction) {
        if (i != null) {
            if (i < 4) {
                return i == direction;
            } else {
                return i % 4 == direction || i / 4 - 1 == direction;
            }
        } else {
            return false;
        }
    }

    private double findHallwayComplexity(int start, int[][] hallwayGroup, MazeDirection direction) {
        int width = hallwayGroup[0].length;
        int height = hallwayGroup.length;
        int group = hallwayGroup[start / width][start % width];
        int totalLength = 1;
        int currentSegmentLength = 1;
        Integer[][] previousChoices = new Integer[height][width];
        double complexity = 0;
        while (true) {
            MazeDirection newDirection = direction;
            if (start % width > 0 && direction != MazeDirection.EAST && hallwayGroup[start / width][start % width - 1] == group && !directionAlreadyChosen(previousChoices[start / width][start % width], 0)) {
                if (previousChoices[start / width][start % width] == null) {
                    previousChoices[start / width][start % width] = 0;
                } else {
                    previousChoices[start / width][start % width] += 1 * 4;
                }
                start -= 1;
                totalLength += 1;
                currentSegmentLength += 1;
                newDirection = MazeDirection.WEST;
            } else if (start % width < width - 1 && direction != MazeDirection.WEST && hallwayGroup[start / width][start % width + 1] == group && !directionAlreadyChosen(previousChoices[start / width][start % width], 1)) {
                if (previousChoices[start / width][start % width] == null) {
                    previousChoices[start / width][start % width] = 1;
                } else {
                    previousChoices[start / width][start % width] += 2 * 4;
                }
                start += 1;
                totalLength += 1;
                currentSegmentLength += 1;
                newDirection = MazeDirection.EAST;
            } else if (start / width > 0 && direction != MazeDirection.SOUTH && hallwayGroup[start / width - 1][start % width] == group && !directionAlreadyChosen(previousChoices[start / width][start % width], 2)) {
                if (previousChoices[start / width][start % width] == null) {
                    previousChoices[start / width][start % width] = 2;
                } else {
                    previousChoices[start / width][start % width] += 3 * 4;
                }
                start -= width;
                totalLength += 1;
                currentSegmentLength += 1;
                newDirection = MazeDirection.NORTH;
            } else if (start / width < height - 1 && direction != MazeDirection.NORTH && hallwayGroup[start / width + 1][start % width] == group && !directionAlreadyChosen(previousChoices[start / width][start % width], 3)) {
                if (previousChoices[start / width][start % width] == null) {
                    previousChoices[start / width][start % width] = 3;
                } else {
                    previousChoices[start / width][start % width] += 4 * 4;
                }
                start += width;
                totalLength += 1;
                currentSegmentLength += 1;
                newDirection = MazeDirection.SOUTH;
            } else {
                if (currentSegmentLength % 2 == 1) {
                    currentSegmentLength += 1;
                    totalLength += 1;
                }
                if (currentSegmentLength > 0) {
                    complexity += 1d / currentSegmentLength;
                }
                return complexity * totalLength / 2d;
            }
            if (direction == null) {
                totalLength = 0;
                currentSegmentLength = 0;
                direction = newDirection;
            } else if (newDirection != direction) {
                if (currentSegmentLength % 2 == 0) {
                    currentSegmentLength += 1;
                    totalLength += 1;
                }
                if (currentSegmentLength > 1) {
                    complexity += 1d / (currentSegmentLength - 1);
                }
                currentSegmentLength = 1;
                direction = newDirection;
            }
        }
    }

    private int floodFillHallwayGroupsMain(int[][] hallwayGroup, boolean[][] isIntersection) {
        for (int y = 0; y < hallwayGroup.length; y++) {
            for (int x = 0; x < hallwayGroup[y].length; x++) {
                hallwayGroup[y][x] = hasWall(x, y) ? -2 : -1;
            }
        }
        int count = 0;
        for (int y = 0; y < hallwayGroup.length; y++) {
            for (int x = 0; x < hallwayGroup[y].length; x++) {
                if (floodFillHallwayGroups(x, y, count, hallwayGroup, isIntersection)) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean floodFillHallwayGroups(int x, int y, int groupNo, int[][] hallwayGroup, boolean[][] isIntersection) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight() || hasWall(x, y) || hallwayGroup[y][x] == groupNo || isIntersection[y][x]) {
            return false;
        }
        hallwayGroup[y][x] = groupNo;
        if (x > 0) {
            if (floodFillHallwayGroups(x - 1, y, groupNo, hallwayGroup, isIntersection)) {
                hallwayGroup[y][x - 1] = groupNo;
            }
        }
        if (x < getWidth() - 1) {
            if (floodFillHallwayGroups(x + 1, y, groupNo, hallwayGroup, isIntersection)) {
                hallwayGroup[y][x + 1] = groupNo;
            }
        }
        if (y > 0) {
            if (floodFillHallwayGroups(x, y - 1, groupNo, hallwayGroup, isIntersection)) {
                hallwayGroup[y - 1][x] = groupNo;
            }
        }
        if (y < getHeight() - 1) {
            if (floodFillHallwayGroups(x, y + 1, groupNo, hallwayGroup, isIntersection)) {
                hallwayGroup[y + 1][x] = groupNo;
            }
        }
        return true;
    }

    public MazeSolution2D getShortestSolution(int startX, int startY, int finishX, int finishY) {
        return getShortestSolution(new ShortestPathFinder(this), startX, startY, finishX, finishY);
    }

    public MazeSolution2D getShortestSolution(MazeSolver solver, int startX, int startY, int finishX, int finishY) {
        return solver.getSolution(startX, startY, finishX, finishY);
    }

    public void drawMaze(Canvas canvas, int width, int height, boolean squareTiles, Paint backgroundPaint, Paint borderPaint , Player[] players, boolean drawVisited, MazeDirection[][][] solution, Paint solutionPaint, boolean drawUnvisited, MazeRobot[] robots) {
        int cellWidth = width / getGridWidth();
        int cellHeight = height / getGridHeight();
        if (squareTiles) {
            cellWidth = Math.min(cellWidth, cellHeight);
            cellHeight = cellWidth;
        }
        int leftX = (width - cellWidth * getGridWidth()) / 2;
        int topY = (height - cellHeight * getGridHeight()) / 2;
        canvas.drawRect(leftX, topY, leftX + cellWidth * getGridWidth(), topY + cellHeight * getGridHeight(), backgroundPaint);
        Paint paint = new Paint();
        Paint paint2 = new Paint();
        if (drawVisited || drawUnvisited) {
            for (Player p : players) {
                paint.setColor(p.getPathColor());
                paint2.setColor(p.getUnvisitedColor());
                for (int y = 0; y < getHeight(); y++) {
                    for (int x = 0; x < getWidth(); x++) {
                        boolean visited = p.isVisitedQuick(x, y);
                        if (drawVisited && !drawUnvisited && visited) {
                            canvas.drawRect(leftX + (x / 2f + 0.25f) * cellWidth, topY + (y / 2f + 0.25f) * cellHeight, leftX + (x / 2f + 0.75f) * cellWidth, topY + (y / 2f + 0.75f) * cellHeight, paint);
                        } else if (drawUnvisited && !visited && !hasWall(x, y)) {
                            canvas.drawRect(leftX + (x / 2f + 0.35f) * cellWidth, topY + (y / 2f + 0.35f) * cellHeight, leftX + (x / 2f + 0.65f) * cellWidth, topY + (y / 2f + 0.65f) * cellHeight, paint2);
                        }
                    }
                }
            }
        }
        for (int y = 0; y < getHeight(); y += 2) {
            for (int x = 1; x < getWidth(); x += 2) {
                if (hasWall(x, y)) {
                    canvas.drawLine(leftX + (x + 1) / 2 * cellWidth, topY + y / 2 * cellHeight, leftX + (x + 1) / 2 * cellWidth, topY + (y + 2) / 2 * cellHeight, borderPaint);
                }
            }
        }
        for (int y = 1; y < getHeight(); y += 2) {
            for (int x = 0; x < getWidth(); x += 2) {
                if (hasWall(x, y)) {
                    canvas.drawLine(leftX + x / 2 * cellWidth, topY + (y + 1) / 2 * cellHeight, leftX + (x + 2) / 2 * cellWidth, topY + (y + 1) / 2 * cellHeight, borderPaint);
                }
            }
        }
        if (!drawUnvisited && solution != null) {
            solutionPaint.setTextSize(cellHeight / 2);
            for (int y = 0; y < getHeight(); y++) {
                for (int x = 0; x < getWidth(); x++) {
                    for (int i = 0; i < solution[y][x].length; i++) {
                        String s = "";
                        if (solution[y][x][i] == MazeDirection.EAST) {
                            s = "\u2192";
                        } else if (solution[y][x][i] == MazeDirection.NORTH) {
                            s = "\u2191";
                        } else if (solution[y][x][i] == MazeDirection.SOUTH) {
                            s = "\u2193";
                        } else if (solution[y][x][i] == MazeDirection.WEST) {
                            s = "\u2190";
                        }
                        canvas.drawText(s, leftX + (x / 2f + 0.5f) * cellWidth - solutionPaint.measureText(s) / 2, topY + (y / 2f + 0.5f) * cellHeight + solutionPaint.getTextSize() / 2, solutionPaint);
                    }
                }
            }
        }
        for (MazeRobot r : robots) {
            paint.setColor(r.getColor());
            canvas.drawRect(leftX + (float) (r.getX() - r.getRadius()) * cellWidth, topY + (float) (r.getY() - r.getRadius()) * cellHeight, leftX + (float) (r.getX() + r.getRadius()) * cellWidth, topY + (float) (r.getY() + r.getRadius()) * cellHeight, paint);
        }
        for (int i = 0; i < players.length; i++) {
            Player p = players[i];
            paint.setColor(p.getFinishColor());
            canvas.drawRect(leftX + (float) (p.getX() - p.getRadius()) * cellWidth, topY + (float) (p.getY() - p.getRadius()) * cellHeight, leftX + (float) (p.getX() + p.getRadius()) * cellWidth, topY + (float) (p.getY() + p.getRadius()) * cellHeight, paint);
            if (!drawUnvisited) {
                paint.setColor(p.getColor());
                canvas.drawRect(leftX + (getFinishX(i) + 0.25f) * cellWidth, topY + (getFinishY(i) + 0.25f) * cellHeight, leftX + (getFinishX(i) + 0.75f) * cellWidth, topY + (getFinishY(i) + 0.75f) * cellHeight, paint);
            }
        }
    }

    public static String getGenerationCode(Maze2D.GeneratedMaze generatedMaze) {
        Maze2D maze = generatedMaze.getMaze();
        String s = maze.getWidth() + "W" + maze.getHeight() + "H";
        for (int i = 0; i < maze.getPlayersCount(); i++) {
            s += (maze.getStartX(i) + maze.getStartY(i) * maze.getGridWidth()) + "Y";
            s += (maze.getFinishX(i) + maze.getFinishY(i) * maze.getGridWidth()) + "B";
        }
        s += (generatedMaze.getSeed() < 0 ? "1" : "0") + Math.abs(generatedMaze.getSeed()) + "S" + generatedMaze.getAlgorithm().getID();
        if (generatedMaze.getDirection1() != null) {
            s += "D" + generatedMaze.getDirection1().getID();
            if (generatedMaze.getDirection2() != null) {
                s += "E" + generatedMaze.getDirection2().getID();
            }
        }
        return s;
    }

    public static GeneratedMaze generateFromSeed(String seed) {
        seed = seed.toUpperCase();
        int wPosition = seed.indexOf("W");
        int hPosition = seed.indexOf("H");
        int bPositionlast = seed.lastIndexOf("B");
        int sPosition = seed.indexOf("S");
        int dPosition = seed.indexOf("D");
        dPosition = dPosition == -1 ? seed.length() : dPosition;
        int ePosition = seed.indexOf("E");
        ePosition = ePosition == -1 ? seed.length() : ePosition;
        try {
            int width = Integer.parseInt(seed.substring(0, wPosition)) / 2 + 1;
            int height = Integer.parseInt(seed.substring(wPosition + 1, hPosition)) / 2 + 1;
            String players = seed.substring(hPosition + 1, bPositionlast + 1);
            int bPosition = players.indexOf("B");
            List<Integer> startX = new ArrayList<>();
            List<Integer> startY = new ArrayList<>();
            List<Integer> finishX = new ArrayList<>();
            List<Integer> finishY = new ArrayList<>();
            while (bPosition >= 0 && bPosition <= bPositionlast) {
                int yPosition = players.indexOf("Y");
                int value = Integer.parseInt(players.substring(0, yPosition));
                if (value >= width * height) {
                    return null;
                }
                startX.add(value % width);
                startY.add(value / width);
                value = Integer.parseInt(players.substring(yPosition + 1, bPosition));
                if (value >= width * height) {
                    return null;
                }
                finishX.add(value % width);
                finishY.add(value / width);
                if (bPosition + 1 >= players.length()) {
                    break;
                }
                players = players.substring(bPosition + 1, players.length());
                bPosition = players.indexOf("B");
            }
            if (startX.size() == 0) {
                return null;
            }
            long seedRandom = (Integer.parseInt(seed.substring(bPositionlast + 1, bPositionlast + 2)) > 0 ? -1 : 1) * Long.parseLong(seed.substring(bPositionlast + 2, sPosition));
            MazeAlgorithm a = MazeAlgorithm.getByID(Integer.parseInt(seed.substring(sPosition + 1, dPosition)));
            MazeDirection d1 = MazeDirection.getByID(dPosition == ePosition ? 0 : Integer.parseInt(seed.substring(dPosition + 1, ePosition)));
            MazeDirection d2 = MazeDirection.getByID(ePosition == seed.length() ? 0 : Integer.parseInt(seed.substring(ePosition + 1, seed.length())));
            BasicMazeGenerator g = a.createMazeGenerator(d1, d2);
            g.setPlayers(startX.size());
            for (int i = 0; i < startX.size(); i++) {
                g.setStart(i, startX.get(i), startY.get(i));
                g.setFinish(i, finishX.get(i), finishY.get(i));
            }
            g.setSeed(seedRandom);
            return new GeneratedMaze(g.generateMaze(width, height), seedRandom, a, a.getAdditionalParameters() >= 1 ? d1 : null, a.getAdditionalParameters() >= 2 ? d2 : null);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
        }
        return null;
    }

    public static class GeneratedMaze {
        private final Maze2D MAZE;
        private final long SEED;
        private final MazeAlgorithm ALGORITHM;
        private final MazeDirection D1;
        private final MazeDirection D2;

        public GeneratedMaze(Maze2D maze, long seed, MazeAlgorithm algorithm, MazeDirection... directions) {
            MAZE = maze;
            SEED = seed;
            ALGORITHM = algorithm;
            D1 = directions.length >= 1 ? directions[0] : null;
            D2 = directions.length >= 2 ? directions[1] : null;
        }

        public Maze2D getMaze() {
            return MAZE;
        }

        public long getSeed() {
            return SEED;
        }

        public MazeAlgorithm getAlgorithm() {
            return ALGORITHM;
        }

        public MazeDirection getDirection1() {
            return D1;
        }

        public MazeDirection getDirection2() {
            return D2;
        }

        public void saveMaze(SharedPreferences.Editor editor, String key) {
            editor.putInt(key + "0", getMaze().getWidth());
            editor.putInt(key + "1", getMaze().getHeight());
            editor.putInt(key + "2", getMaze().getPlayersCount());
            int count = 2;
            for (int i = 0; i < getMaze().getPlayersCount(); i++) {
                editor.putInt(key + (++count), getMaze().getStartX(i));
                editor.putInt(key + (++count), getMaze().getStartY(i));
                editor.putInt(key + (++count), getMaze().getFinishX(i));
                editor.putInt(key + (++count), getMaze().getFinishY(i));
            }
            editor.putLong(key + (++count), getSeed());
            editor.putInt(key + (++count), getAlgorithm().getID());
            for (int y = 0; y < getMaze().getHeight(); y++) {
                for (int x = 0; x < getMaze().getWidth(); x++) {
                    editor.putBoolean(key + (++count), getMaze().hasWall(x, y));
                }
            }
        }

        public static GeneratedMaze loadMaze(SharedPreferences preferences, String key) {
            int width = preferences.getInt(key + "0", -1);
            if (width == -1) {
                return null;
            }
            int height = preferences.getInt(key + "1", 5);
            int playerCount = preferences.getInt(key + "2", 1);
            int[] startX = new int[playerCount];
            int[] startY = new int[playerCount];
            int[] finishX = new int[playerCount];
            int[] finishY = new int[playerCount];
            int count = 2;
            for (int i = 0; i < playerCount; i++) {
                startX[i] = preferences.getInt(key + (++count), 0);
                startY[i] = preferences.getInt(key + (++count), 0);
                finishX[i] = preferences.getInt(key + (++count), width / 2);
                finishY[i] = preferences.getInt(key + (++count), height / 2);
            }
            long seed = preferences.getLong(key + (++count), 0);
            MazeAlgorithm algorithm = MazeAlgorithm.getByID(preferences.getInt(key + (++count), 0));
            Maze2D maze = new Maze2D(width / 2 + 1, height / 2 + 1, startX, startY, finishX, finishY);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    maze.setWall(x, y, preferences.getBoolean(key + (++count), false));
                }
            }
            return new GeneratedMaze(maze, seed, algorithm);
        }
    }
}
