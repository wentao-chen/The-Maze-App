package com.gmail.wentaochen97.maze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Wentao-Admin on 2016-01-08.
 */
public class BinaryTree extends BasicMazeGenerator {

    private final Map<MazeOrientation, Integer> BIAS = new HashMap<MazeOrientation, Integer>();

    private MazeDirection bias1 = null;
    private MazeDirection bias2 = null;

    public BinaryTree(MazeDirection bias1, MazeDirection bias2) {
        if (bias1.getOrientation() == bias2.getOrientation()) throw new IllegalArgumentException("bias must have different orientations");
        this.bias1 = bias1;
        this.bias2 = bias2;
        for (MazeOrientation o : MazeOrientation.values()) {
            BIAS.put(o, 1);
        }
    }

    public int getBias(MazeOrientation orientation) {
        return BIAS.get(orientation);
    }

    public void setBias(MazeOrientation orientation, int bias) {
        if (orientation == null) throw new IllegalArgumentException("bias orientation cannot be null");
        if (bias < 0) throw new IllegalArgumentException("bias cannot be less than 0");
        int total = 0;
        for (MazeOrientation o : MazeOrientation.values()) {
            if (o != orientation) {
                total += BIAS.get(o);
            }
        }
        if (total == 0 && bias == 0) throw new IllegalArgumentException("total bias cannot be 0");
        synchronized (this) {
            BIAS.put(orientation, bias);
        }
    }

    public MazeDirection getBias1() {
        return bias1;
    }

    public synchronized void setBias1(MazeDirection bias1) {
        if (getBias2().getOrientation() == bias1.getOrientation()) throw new IllegalArgumentException("bias must have different orientations");
        this.bias1 = bias1;
    }

    public MazeDirection getBias2() {
        return bias2;
    }

    public synchronized void setBias2(MazeDirection bias2) {
        if (getBias1().getOrientation() == bias2.getOrientation()) throw new IllegalArgumentException("bias must have different orientations");
        this.bias2 = bias2;
    }

    @Override
    public synchronized Maze2D generateMaze(int width, int height) {
        Long seed = getSeed();
        Random random = seed != null ? new Random(seed) : new Random();
        Integer players = getDesiredPlayers();
        Maze2D maze = players != null ? new Maze2D(width, height, players) : new Maze2D(width, height, getStartX(), getStartY(), getFinishX(), getFinishY());
        maze.setGridLayout();
        Integer startX = null;
        Integer startY = null;
        Integer finishX = null;
        Integer finishY = null;
        MazeDirection bias1 = getBias1();
        MazeDirection bias2 = getBias2();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                MazeDirection[] directions = getPossibleDirections(x, y, width, height, bias1, bias2, startX, startY, finishX, finishY);
                if (directions.length > 0) {
                    int total = 0;
                    for (MazeDirection d  : directions) {
                        total += getBias(d.getOrientation());
                    }
                    int selection = (int) (random.nextDouble() * total);
                    MazeDirection direction = null;
                    for (MazeDirection d  : directions) {
                        selection -= getBias(d.getOrientation());
                        if (selection < 0) {
                            direction = d;
                            break;
                        }
                    }
                    switch (direction) {
                        case EAST:
                            maze.setWall(x * 2 + 1, y * 2, false);
                            break;
                        case NORTH:
                            maze.setWall(x * 2, y * 2 - 1, false);
                            break;
                        case SOUTH:
                            maze.setWall(x * 2, y * 2 + 1, false);
                            break;
                        case WEST:
                            maze.setWall(x * 2 - 1, y * 2, false);
                            break;
                    }
                }
            }
        }

        return maze;
    }

    private MazeDirection[] getPossibleDirections(int x, int y, int width, int height, MazeDirection bias1, MazeDirection bias2, Integer startX, Integer startY, Integer finishX, Integer finishY) {
        List<MazeDirection> directions = new ArrayList<MazeDirection>();
        if ((bias1 == MazeDirection.EAST && x < width - 1) || (bias1 == MazeDirection.WEST && x > 0) || (bias1 == MazeDirection.NORTH && y > 0) || (bias1 == MazeDirection.SOUTH && y < height - 1)) {
            directions.add(bias1);
        }
        if ((bias2 == MazeDirection.EAST && x < width - 1) || (bias2 == MazeDirection.WEST && x > 0) || (bias2 == MazeDirection.NORTH && y > 0) || (bias2 == MazeDirection.SOUTH && y < height - 1)) {
            directions.add(bias2);
        }
        if (finishX != null && finishY != null && directions.size() > 1) {
            for (int i = 0; i < directions.size(); i++) {
                MazeDirection d = directions.get(i);
                if ((d == MazeDirection.EAST && x + 1 == finishX && y == finishY) || (d == MazeDirection.WEST && x - 1 == finishX && y == finishY) || (d == MazeDirection.NORTH && x == finishX && y - 1 == finishY) || (d == MazeDirection.SOUTH && x == finishX && y + 1 == finishY)) {
                    directions.remove(i);
                    break;
                }
            }
        }
        if (startX != null && startY != null && directions.size() > 1) {
            for (int i = 0; i < directions.size(); i++) {
                MazeDirection d = directions.get(i);
                if ((d == MazeDirection.EAST && x + 1 == startX && y == startY) || (d == MazeDirection.WEST && x - 1 == startX && y == startY) || (d == MazeDirection.NORTH && x == startX && y - 1 == startY) || (d == MazeDirection.SOUTH && x == startX && y + 1 == startY)) {
                    directions.remove(i);
                    break;
                }
            }
        }
        return directions.toArray(new MazeDirection[directions.size()]);
    }
}
