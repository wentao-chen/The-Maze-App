package com.gmail.wentaochen97.maze;

/**
 * Created by Wentao-Admin on 2016-01-07.
 */
public enum MazeDirection {
    NORTH(MazeOrientation.VERTICAL, 0), EAST(MazeOrientation.HORIZONTAL, 1), SOUTH(MazeOrientation.VERTICAL, 2), WEST(MazeOrientation.HORIZONTAL, 3);

    private final MazeOrientation ORIENTATION;
    private final int ID;

    private MazeDirection(MazeOrientation orientation, int id) {
        ORIENTATION = orientation;
        ID = id;
    }

    public int getX() {
        switch (this) {
            case EAST:
                return 1;
            case WEST:
                return -1;
            default:
                return 0;
        }
    }

    public int getY() {
        switch (this) {
            case SOUTH:
                return 1;
            case NORTH:
                return -1;
            default:
                return 0;
        }
    }

    public int getID() {
        return ID;
    }

    public static MazeDirection getByID(int id) {
        for (MazeDirection d : MazeDirection.values()) {
            if (id == d.getID()) {
                return d;
            }
        }
        return null;
    }

    public MazeOrientation getOrientation() {
        return ORIENTATION;
    }

    public MazeDirection getOpposite() {
        switch (this) {
            case EAST:
                return WEST;
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            case WEST:
                return EAST;
        }
        return null;
    }

    public static MazeDirection getNearestDirection(double angle) {
        angle = (angle % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
        if (angle < Math.PI / 4) {
            return EAST;
        } else if (angle < Math.PI * 3 / 4) {
            return NORTH;
        } else if (angle < Math.PI * 5 / 4) {
            return WEST;
        } else if (angle < Math.PI * 7 / 4) {
            return SOUTH;
        } else {
            return EAST;
        }
    }
}