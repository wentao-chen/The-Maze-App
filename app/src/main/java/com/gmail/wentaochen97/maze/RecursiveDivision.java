package com.gmail.wentaochen97.maze;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Wentao-Admin on 2016-01-08.
 */
public class RecursiveDivision extends BasicMazeGenerator {

    private final Map<MazeOrientation, Integer> BIAS = new HashMap<MazeOrientation, Integer>();

    public RecursiveDivision() {
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
        synchronized (this) {
            BIAS.put(orientation, bias);
        }
    }

    @Override
    public synchronized Maze2D generateMaze(int width, int height) {
        Long seed = getSeed();
        Random random = seed != null ? new Random(seed) : new Random();
        Integer players = getDesiredPlayers();
        Maze2D maze = players != null ? new Maze2D(width, height, players) : new Maze2D(width, height, getStartX(), getStartY(), getFinishX(), getFinishY());
        maze.setAll(false);
        splitMaze(random, maze, 0, 0, width, height, null, null, getBias(MazeOrientation.HORIZONTAL), getBias(MazeOrientation.VERTICAL));
        return maze;
    }

    private void splitMaze(Random random, Maze2D maze, int leftX, int topY, int width, int height, Integer startX, Integer startY, int horizontalBias, int verticalBias) {
        if (width == 1 || height == 1) {
            return;
        }
		/*if (startX != null && startY != null && startX >= leftX && startX < leftX + width && startY >= topY && startY < topY + height) {
			boolean isCorner = (startX == leftX && (startY == topY || startY == topY + height - 1)) || (startX == leftX + width - 1 && (startY == topY || startY == topY + height - 1));
			if (!isCorner) {
				boolean isVerticalSide = startX == leftX || startX == leftX + width - 1;
				boolean isHorizontalSide = startY == topY || startY == topY + height - 1;

			}
		}*/
        boolean verticalDivider = horizontalBias > 0 && verticalBias > 0 ? (int) (random.nextDouble() * (horizontalBias + verticalBias)) < verticalBias : (int) (random.nextDouble() * ((width - 1) + (height - 1))) < width - 1;

        if (verticalDivider) {
            int selectionX = leftX * 2 + (int) (random.nextDouble() * (width - 1)) * 2 + 1;
            int selectionY = (int) (random.nextDouble() * height) * 2;
            for (int y = 0; y < 2 * height - 1; y++) {
                if (y != selectionY) {
                    maze.setWall(selectionX, topY * 2 + y, true);
                }
            }
            splitMaze(random, maze, leftX, topY, selectionX / 2 - leftX + 1, height, startX, startY, horizontalBias, verticalBias);
            splitMaze(random, maze, selectionX / 2 + 1, topY, ((leftX + width) * 2 - 1 - selectionX) / 2, height, startX, startY, horizontalBias, verticalBias);
        } else {
            int selectionX = (int) (random.nextDouble() * width) * 2;
            int selectionY = topY * 2 + (int) (random.nextDouble() * (height - 1)) * 2 + 1;
            for (int x = 0; x < 2 * width - 1; x++) {
                if (x != selectionX) {
                    maze.setWall(leftX * 2 + x, selectionY, true);
                }
            }
            splitMaze(random, maze, leftX, topY, width, selectionY / 2 - topY + 1, startX, startY, horizontalBias, verticalBias);
            splitMaze(random, maze, leftX, selectionY / 2 + 1, width, ((topY + height) * 2 - 1 - selectionY) / 2, startX, startY, horizontalBias, verticalBias);
        }
    }

}
