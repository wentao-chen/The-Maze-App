package com.gmail.wentaochen97.themazeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.gmail.wentaochen97.maze.BasicMazeGenerator;
import com.gmail.wentaochen97.maze.GameMode;
import com.gmail.wentaochen97.maze.Maze2D;
import com.gmail.wentaochen97.maze.MazeAlgorithm;
import com.gmail.wentaochen97.maze.MazeDirection;
import com.gmail.wentaochen97.maze.MazeOrientation;
import com.gmail.wentaochen97.maze.MazeSolution2D;
import com.gmail.wentaochen97.maze.Player;
import com.gmail.wentaochen97.maze.ShortestPathFinder;
import com.gmail.wentaochen97.mazebots.ChaseBot;
import com.gmail.wentaochen97.mazebots.MazeRobot;
import com.gmail.wentaochen97.mazebots.PlanBot;
import com.gmail.wentaochen97.mazebots.RandomBot;
import com.gmail.wentaochen97.mazebots.SwitchBot;
import com.gmail.wentaochen97.mazebots.TeamBot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by Wentao-Admin on 2016-01-07.
 */
public class DrawPanel extends SurfaceView implements SurfaceHolder.Callback {

    private final Paint BACKGROUND_PAINT = createPaint(getResources().getColor(R.color.maze_background));
    private final Paint BORDER_PAINT = createPaint(getResources().getColor(R.color.maze_border));
    private final Paint TEXT_PAINT = createPaint(getResources().getColor(R.color.text));
    private final Paint SOLUTION_PAINT = createPaint(getResources().getColor(R.color.solution));

    private String MENU_STRING = getResources().getString(R.string.display_menu);

    private final MainActivity ACTIVITY;
    private MainThread currentThread = null;

    private Maze2D.GeneratedMaze currentMaze = null;
    private double mazeDifficulty = 0;
    private MazeDirection[][][] solution = null;
    private final List<Player> PLAYERS = new ArrayList<>();
    private final List<MazeRobot> BOTS = new ArrayList<>();

    private final GestureDetector GESTURE_DETECTOR;
    private final ScaleGestureDetector MOTION_SCALE_DETECTOR;
    private float mScaleFactor = 1f;
    private boolean showedFollowPlayerMessage = false;
    private int followPlayer = -1;
    private Float forceMinZoom = null;
    private double lastRandomUnvisited = 0;

    private GameMode lastGameMode = null;

    // Pan and Zoom Variables
    private static float MIN_ZOOM = 1f;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode;
    private float startX = 0f;
    private float startY = 0f;
    private float translateX = 0f;
    private float translateY = 0f;
    private float previousTranslateX = 0f;
    private float previousTranslateY = 0f;
    private boolean dragged = true;
    private boolean zoomed = true;
    private boolean tapped = true;

    public DrawPanel(final MainActivity context) {
        super(context);
        setKeepScreenOn(true);
        ACTIVITY = context;

        getHolder().addCallback(this);

        MOTION_SCALE_DETECTOR = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {

                float previousX = (translateX * 2 - getWidth()) / 2 / mScaleFactor;
                float previousY = (translateY * 2 - getHeight()) / 2 / mScaleFactor;

                mScaleFactor *= detector.getScaleFactor();
                mScaleFactor = Math.max(MIN_ZOOM, Math.min(mScaleFactor, getMaxZoom()));

                translateX = (previousX  * mScaleFactor * 2  + getWidth()) / 2;
                translateY = (previousY  * mScaleFactor * 2  + getHeight()) / 2;

                zoomed = true;

                invalidate();
                return true;
            }
        });
        GESTURE_DETECTOR = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (e.getX() >= getWidth() - TEXT_PAINT.measureText(MENU_STRING)  * 2 && e.getY() <= TEXT_PAINT.getTextSize() * 2) {
                    ACTIVITY.startActivity(new Intent(ACTIVITY, SettingsActivity.class));
                    return true;
                }
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                synchronized (DrawPanel.this) {
                    DrawPanel.this.tapped = true;
                    if (++DrawPanel.this.followPlayer >= PLAYERS.size()) {
                        DrawPanel.this.followPlayer = 0;
                    }
                }
                super.onLongPress(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                synchronized (DrawPanel.this) {
                    DrawPanel.this.tapped = true;
                    if (++DrawPanel.this.followPlayer >= PLAYERS.size()) {
                        DrawPanel.this.followPlayer = 0;
                    }
                }
                return super.onDoubleTap(e);
            }
        });

        setFocusable(true);

        setGameMode(MainActivity.getGameMode());
    }

    void setGameMode(GameMode gameMode) {
        if (gameMode == GameMode.REGULAR) {
            Maze2D.GeneratedMaze original = null;
            if (!SettingsActivity.newRegularMaze()) {
                if (gameMode == this.lastGameMode) {
                    return;
                }
                this.currentMaze = ACTIVITY.loadSaveMaze(gameMode);
                original = this.currentMaze;
            } else {
                this.currentMaze = null;
                SettingsActivity.resetNewRegularMaze();
                ACTIVITY.resetCreateNewRegularMazeTime();
            }
            long level = MainActivity.regularLevel();
            if (this.currentMaze == null) {
                this.currentMaze = getRegularMaze(level);
            }
            loadMaze(this.currentMaze, null, getForceMinZoomRegular(level), false);
            if (original != null) {
                loadProgress(ACTIVITY.loadSaveMazeProgress(gameMode));
            }
            ACTIVITY.saveMaze(gameMode, this.currentMaze, getCurrentProgress(), true);
        } else if (gameMode == GameMode.FILL) {
            Maze2D.GeneratedMaze original = null;
            if (!SettingsActivity.newFillMaze()) {
                if (gameMode == this.lastGameMode) {
                    return;
                }
                this.currentMaze = ACTIVITY.loadSaveMaze(gameMode);
                if (this.currentMaze != null) {
                    this.currentMaze.getMaze().setBraidMaze(new Random(new Random(this.currentMaze.getSeed()).nextLong()));
                }
                original = this.currentMaze;
            } else {
                this.currentMaze = null;
                SettingsActivity.resetNewFillMaze();
                ACTIVITY.resetCreateNewFillMazeTime();
            }
            long level = MainActivity.fillLevel();
            if (this.currentMaze == null) {
                this.currentMaze = getFillMaze(level);
            }
            loadMaze(this.currentMaze, null, getForceMinZoomFill(level), false, getFillBots(level));
            if (original != null) {
                loadProgress(ACTIVITY.loadSaveMazeProgress(gameMode));
            }
            ACTIVITY.saveMaze(gameMode, this.currentMaze, getCurrentProgress(), true);
        } else if (gameMode == GameMode.CUSTOM) {
            Maze2D.GeneratedMaze original = null;
            this.currentMaze = SettingsActivity.getLoadSeedMaze();
            if (this.currentMaze != null) {
                SettingsActivity.resetLoadSeedMaze();
            } else {
                this.currentMaze = ACTIVITY.loadSaveMaze(gameMode);
                original = this.currentMaze;
                if (this.currentMaze == null) {
                    this.currentMaze = SettingsActivity.generateCustomMaze(null);
                }
            }
            loadMaze(this.currentMaze, null, null, false);
            if (original != null) {
                loadProgress(ACTIVITY.loadSaveMazeProgress(gameMode));
            }
            ACTIVITY.saveMaze(gameMode, this.currentMaze, getCurrentProgress(), true);
            if (SettingsActivity.showSolution()) {
                SettingsActivity.resetShowSolution();
                Maze2D maze = this.currentMaze.getMaze();
                List<Player> players = new ArrayList<>(PLAYERS);
                MazeSolution2D[] mazeSolution2D = new MazeSolution2D[players.size()];
                for (int i = 0; i < mazeSolution2D.length; i++) {
                    mazeSolution2D[i] = new ShortestPathFinder(this.currentMaze.getMaze()).getSolution((int) Math.floor(players.get(i).getX()) * 2, (int) Math.floor(players.get(i).getY()) * 2, maze.getFinishX(i) * 2, maze.getFinishY(i) * 2);
                }

                MazeDirection[][][] solutionGrid = new MazeDirection[maze.getHeight()][maze.getWidth()][];
                for (int y = 0; y < solutionGrid.length; y++) {
                    for (int x = 0; x < solutionGrid[y].length; x++) {
                        Set<MazeDirection> directions = new HashSet<>();
                        for (MazeSolution2D m : mazeSolution2D) {
                            MazeDirection d = m.getDirection(x, y);
                            if (d != null) {
                                directions.add(d);
                            }
                        }
                        solutionGrid[y][x] = directions.toArray(new MazeDirection[directions.size()]);
                    }
                }
                synchronized (this) {
                    this.solution = solutionGrid;
                }
            }
        } else {
            if (gameMode == this.lastGameMode) {
                return;
            }
            this.currentMaze = generateMaze(3, 3, 1, false);
            loadMaze(this.currentMaze, 0.4, null, false);
            ACTIVITY.saveMaze(gameMode, this.currentMaze, getCurrentProgress(), true);
        }
        synchronized (this) {
            this.lastGameMode = gameMode;
        }
    }

    private static Paint createPaint(int color) {
        Paint p = new Paint();
        p.setColor(color);
        return p;
    }

    public void setVelocities(double vx, double vy) {
        for (Player p : new ArrayList<>(PLAYERS)) {
            p.setVelocityX(vx * p.getSpeedMultiply());
            p.setVelocityY(vy * p.getSpeedMultiply());
        }
    }

    private float getMaxZoom() {
        Maze2D maze = getCurrentMaze().getMaze();
        return maze != null ? Math.max(Math.max(maze.getGridWidth(), maze.getGridHeight()) / 1.5f, MIN_ZOOM) : MIN_ZOOM;
    }

    public Maze2D.GeneratedMaze getCurrentMaze() {
        return this.currentMaze;
    }

    public MazeProgress getCurrentProgress() {
        List<Player> players = new ArrayList<>(PLAYERS);
        double[] x = new double[players.size()];
        double[] y = new double[players.size()];
        Maze2D maze = getCurrentMaze().getMaze();
        boolean[][] visited = new boolean[maze.getHeight()][maze.getWidth()];
        for (int i = 0; i < x.length; i++) {
            x[i] = players.get(i).getX();
            y[i] = players.get(i).getY();
        }
        for (int j = 0; j < visited.length; j++) {
            for (int i = 0; i < visited[j].length; i++) {
                for (Player p : players) {
                    if (p.isVisited(i, j)) {
                        visited[j][i] = true;
                        break;
                    }
                }
            }
        }
        return new MazeProgress(x, y, visited, ACTIVITY.getTime());
    }

    public void loadProgress(MazeProgress progress) {
        if (progress != null) {
            for (int i = 0; i < PLAYERS.size(); i++) {
                Player p = PLAYERS.get(i);
                if (i < progress.X.length) {
                    p.setX(progress.X[i]);
                }
                if (i < progress.Y.length) {
                    p.setY(progress.Y[i]);
                }
                p.setNewVisitedSize(progress.VISITED[0].length, progress.VISITED.length);
                for (int y = 0; y < progress.VISITED.length; y++) {
                    for (int x = 0; x < progress.VISITED[y].length; x++) {
                        p.setVisited(x, y, progress.VISITED[y][x]);
                    }
                }
            }
            ACTIVITY.setTime(progress.TIME);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (this.currentThread != null) {
            this.currentThread.setRunning(false);
        }
        synchronized (this) {
            this.currentThread = new MainThread(getHolder(), this);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                DrawPanel.this.currentThread.start();
            }
        }).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (this.currentThread != null) {
            this.currentThread.setRunning(false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MOTION_SCALE_DETECTOR.isInProgress()) {
            return MOTION_SCALE_DETECTOR.onTouchEvent(event);
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                startX = event.getX() - previousTranslateX;
                startY = event.getY() - previousTranslateY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (zoomed) {
                    zoomed = false;
                    break;
                } else if (tapped) {
                    break;
                }
                    translateX = event.getX() - startX;
                translateY = event.getY() - startY;
                double distance = Math.sqrt(Math.pow(event.getX() - (startX + previousTranslateX), 2) + Math.pow(event.getY() - (startY + previousTranslateY), 2));
                if (distance > 0) {
                    dragged = true;
                    synchronized (this) {
                        this.followPlayer = -1;
                    }
                    if (!this.showedFollowPlayerMessage && this.mScaleFactor >= 2 && this.forceMinZoom == null) {
                        synchronized (this) {
                            this.showedFollowPlayerMessage = true;
                        }
                        Toast.makeText(ACTIVITY.getApplicationContext(), getResources().getString(R.string.toast_follow_player_message), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                break;
            case MotionEvent.ACTION_UP:
                mode = NONE;
                dragged = false;
                tapped = false;
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = DRAG;
                tapped = false;
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                break;
        }

        MOTION_SCALE_DETECTOR.onTouchEvent(event);
        GESTURE_DETECTOR.onTouchEvent(event);
        if ((mode == DRAG && mScaleFactor != 1f && dragged) || mode == ZOOM) {
            invalidate();
        }
        return true;
    }

    private int playerToMaze(double i) {
        return Math.max((int) Math.floor(i - 0.19) * 2 + ((i - 0.19) % 1 <= 0.62 ? 0 : 1), 0);
    }

    private void restartFillLevel(boolean showToast) {
        if (showToast) {
            ACTIVITY.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ACTIVITY.getApplicationContext(), getResources().getString(R.string.toast_caught_by_bot), Toast.LENGTH_SHORT).show();
                }
            });
        }
        Maze2D maze = getCurrentMaze().getMaze();
        for (int i = 0; i < PLAYERS.size(); i++) {
            PLAYERS.get(i).setX(maze.getStartX(i) + 0.5);
            PLAYERS.get(i).setY(maze.getStartY(i) + 0.5);
            PLAYERS.get(i).clearVisited();
        }
        placeBotsRandom(maze);
    }

    public void update(double timeInSeconds) {
        Maze2D maze = getCurrentMaze().getMaze();
        GameMode gameMode = MainActivity.getGameMode();
        if (maze != null) {
            double time = (System.nanoTime() - ACTIVITY.getStartTime() + ACTIVITY.getTime()) / 1000000000d;
            if (gameMode == GameMode.FILL && time - this.lastRandomUnvisited >= Math.max(30, maze.getGridWidth() * maze.getGridHeight() / 2)) {
                placeRandomUnvisited(PLAYERS.get(0), maze);
                synchronized (this) {
                    this.lastRandomUnvisited = time;
                }
            }
            for (MazeRobot r : BOTS) {
                r.update(timeInSeconds, PLAYERS.get(0).getX(), PLAYERS.get(0).getY(), Math.atan2(-PLAYERS.get(0).getVelocityY(), PLAYERS.get(0).getVelocityX()), maze);
                for (int i = 0; i < PLAYERS.size(); i++) {
                    if (r.intersects(PLAYERS.get(i))) {
                        restartFillLevel(true);
                        return;
                    }
                }
            }
            boolean[] filled = new boolean[PLAYERS.size()];
            for (int i = 0; i < PLAYERS.size(); i++) {
                Player p = PLAYERS.get(i);
                if (gameMode == GameMode.FILL) {
                    int previousX = playerToMaze(p.getX());
                    int previousY = playerToMaze(p.getY());
                    p.move(timeInSeconds, maze, p.getRadius(), p.getRadius());
                    int maxY = Math.min(Math.max(playerToMaze(p.getY()), previousY), maze.getHeight() - 1);
                    int maxX = Math.min(Math.max(playerToMaze(p.getX()), previousX), maze.getWidth() - 1);
                    for (int y = Math.min(playerToMaze(p.getY()), previousY); y <= maxY; y++) {
                        for (int x = Math.min(playerToMaze(p.getX()), previousX); x <= maxX; x++) {
                            if (!maze.hasWall(x, y)) {
                                p.setVisited(x, y, true);
                            }
                        }
                    }
                } else {
                    int previousX = p.getGridX();
                    int previousY = p.getGridY();
                    p.move(timeInSeconds, maze, p.getRadius(), p.getRadius());
                    int maxY = Math.min(Math.max(p.getGridY(), previousY), maze.getGridHeight() - 1) * 2;
                    int maxX = Math.min(Math.max(p.getGridX(), previousX), maze.getGridWidth() - 1) * 2;
                    for (int y = Math.min(p.getGridY(), previousY) * 2; y <= maxY; y++) {
                        for (int x = Math.min(p.getGridX(), previousX) * 2; x <= maxX; x++) {
                            if (!maze.hasWall(x, y)) {
                                p.setVisited(x, y, true);
                            }
                        }
                    }
                }
                if (gameMode == GameMode.REGULAR || gameMode == GameMode.CUSTOM) {
                    for (int j = 0; j < PLAYERS.size(); j++) {
                        if (p.getGridX() == maze.getFinishX(j) && p.getGridY() == maze.getFinishY(j) && !filled[j]) {
                            filled[j] = true;
                            break;
                        }
                    }
                }
            }
            boolean allPlayersFinished = true;
            if (gameMode == GameMode.REGULAR || gameMode == GameMode.CUSTOM) {
                for (int i = 0; i < PLAYERS.size(); i++) {
                    if (!filled[i]) {
                        allPlayersFinished = false;
                        break;
                    }
                }
            } else if (gameMode == GameMode.FILL) {
                for (int y = 0; y < maze.getHeight() && allPlayersFinished; y++) {
                    for (int x = 0; x < maze.getWidth(); x++) {
                        if (!maze.hasWall(x, y)) {
                            boolean visited = false;
                            for (int i = 0; i < PLAYERS.size(); i++) {
                                if (PLAYERS.get(i).isVisited(x, y)) {
                                    visited = true;
                                    break;
                                }
                            }
                            if (!visited) {
                                allPlayersFinished = false;
                                break;
                            }
                        }
                    }
                }
            }
            if (allPlayersFinished) {
                Maze2D.GeneratedMaze generatedMaze2;
                if (gameMode == GameMode.CUSTOM) {
                    generatedMaze2 = SettingsActivity.generateCustomMaze(null);
                    loadMaze(generatedMaze2, 0.4, null, false);
                    ACTIVITY.saveMaze(gameMode, generatedMaze2, getCurrentProgress(), true);
                } else if (gameMode == GameMode.REGULAR) {
                    MainActivity.setRegularLevel(MainActivity.regularLevel() + 1);
                    long level = MainActivity.regularLevel();
                    generatedMaze2 = getRegularMaze(level);
                    loadMaze(generatedMaze2, getMinDistanceRatioRegular(level), getForceMinZoomRegular(level), false);
                    ACTIVITY.saveMaze(gameMode, generatedMaze2, getCurrentProgress(), true);
                } else if (gameMode == GameMode.FILL) {
                    MainActivity.setFillLevel(MainActivity.fillLevel() + 1);
                    long level = MainActivity.fillLevel();
                    generatedMaze2 = getFillMaze(level);
                    loadMaze(generatedMaze2, 0.5, getForceMinZoomFill(level), false, getFillBots(level));
                    ACTIVITY.saveMaze(gameMode, generatedMaze2, getCurrentProgress(), true);
                } else {
                    generatedMaze2 = generateMaze(3, 3, 1, false);
                    loadMaze(generatedMaze2, 0.4, null, false);
                }
            }
        }
    }

    static Maze2D.GeneratedMaze getRegularMaze(long level) {
        level -= 1;
        int repeat = 1;
        MazeAlgorithm[] algorithms = MazeAlgorithm.values();
        while (true) {
            int minSize = 3;
            int maxSize = Math.min(repeat * 3 + 7, 60);
            int players = Math.min((int) Math.floor(Math.sqrt(repeat)), 5);
            for (int i = minSize; i <= maxSize; i++) {
                int minZoomLevels = Math.min((int) Math.sqrt(i), repeat);
                int sizeRepeatTimes = (repeat / 4 + 1) * (minZoomLevels + players - 1);
                if (level < sizeRepeatTimes) {
                    if (level % sizeRepeatTimes < (repeat / 4 + 1) * minZoomLevels) {
                        return generateMaze(i, i, 1, MazeAlgorithm.getByOrder((int) (level % sizeRepeatTimes) % algorithms.length), false);
                    } else {
                        return generateMaze(i, i, (int) (level % sizeRepeatTimes - (repeat / 4 + 1) * minZoomLevels + 1), MazeAlgorithm.getByOrder((int) (level % sizeRepeatTimes) % algorithms.length), false);
                    }
                } else {
                    level -= sizeRepeatTimes;
                }
            }
            repeat++;
        }
    }

    static double getMinDistanceRatioRegular(long level) {
        level -= 1;
        int repeat = 1;
        while (true) {
            int minSize = 3;
            int maxSize = Math.min(repeat * 3 + 7, 60);
            int players = Math.min((int) Math.floor(Math.sqrt(repeat)), 5);
            for (int i = minSize; i <= maxSize; i++) {
                int minZoomLevels = Math.min((int) Math.sqrt(i), repeat);
                int sizeRepeatTimes = (repeat / 4 + 1) * (minZoomLevels + players - 1);
                if (level < sizeRepeatTimes) {
                    return 0.9 - 0.9 / Math.pow(2, repeat);
                } else {
                    level -= sizeRepeatTimes;
                }
            }
            repeat++;
        }
    }

    static Float getForceMinZoomRegular(long level) {
        level -= 1;
        int repeat = 1;
        while (true) {
            int minSize = 3;
            int maxSize = Math.min(repeat * 3 + 7, 60);
            int players = Math.min((int) Math.floor(Math.sqrt(repeat)), 5);
            for (int i = minSize; i <= maxSize; i++) {
                int minZoomLevels = Math.min((int) Math.sqrt(i), repeat);
                int sizeRepeatTimes = (repeat / 4 + 1) * (minZoomLevels + players - 1);
                if (level < sizeRepeatTimes) {
                    if (level % sizeRepeatTimes < (repeat / 4 + 1) * minZoomLevels) {
                        return (float) Math.pow(1.75, level % sizeRepeatTimes / (repeat / 4 + 1));
                    } else {
                        return null;
                    }
                } else {
                    level -= sizeRepeatTimes;
                }
            }
            repeat++;
        }
    }

    static Maze2D.GeneratedMaze getFillMaze(long level) {
        level -= 1;
        MazeAlgorithm[] algorithms = MazeAlgorithm.values();
        int size = Math.min((int) (level / algorithms.length + 5), 20);
        Maze2D.GeneratedMaze maze = generateMaze(size, size, 1, MazeAlgorithm.getByOrder((int) level % algorithms.length), false);
        maze.getMaze().setBraidMaze(new Random(new Random(maze.getSeed()).nextLong()));
        return maze;
    }

    static Float getForceMinZoomFill(long level) {
        return null;
    }

    static MazeRobot[] getFillBots(long level) {
        level -= 1;
        MazeAlgorithm[] algorithms = MazeAlgorithm.values();
        int size = Math.min((int) (level / algorithms.length + 5), 20);
        int bots = Math.min(size * size / 15, (int) (level / 10) + 1);
        MazeRobot[] robots = new MazeRobot[bots];
        for (int i = 0; i < robots.length; i++) {
            MazeRobot r;
            if (i % 4 == 0) {
                r = new ChaseBot(0.3, ChaseBot.BOT_COLOR, 7000, 20000);
            } else if (Math.random() < Math.pow(2, -(level / 8))) {
                r = new RandomBot(0.3, RandomBot.BOT_COLOR);
            } else if (i % 4 == 1) {
                r = new PlanBot(0.3, PlanBot.BOT_COLOR, 7000, 20000, (int) (Math.random() * 2) + 2);
            } else if (i % 4 == 2) {
                r = new TeamBot(0.3, TeamBot.BOT_COLOR, 7000, 20000, robots[i - 2]);
            } else {
                r = new SwitchBot(0.3, SwitchBot.BOT_COLOR, 7000, 20000);
            }
            r.setSpeed((1 - i * 1d / robots.length) * Math.min((size * size + 15) / 40d, level / 10d + 1));
            robots[i] = r;
        }
        return robots;
    }

    static Maze2D.GeneratedMaze generateMaze(int width, int height, int players, boolean labyrinth) {
        MazeAlgorithm a = MainActivity.getAlgorithm();
        a = a != null ? a : MazeAlgorithm.values()[(int) (Math.random() * MazeAlgorithm.values().length)];
        return generateMaze(width, height, players, a, labyrinth);
    }

    static Maze2D.GeneratedMaze generateMaze(int width, int height, int players, MazeAlgorithm a, boolean labyrinth) {
        MazeDirection d1 = MazeDirection.values()[(int) (Math.random() * MazeDirection.values().length)];
        MazeDirection d2;
        if (d1.getOrientation() == MazeOrientation.HORIZONTAL) {
            d2 = Math.random() < 0.5 ? MazeDirection.NORTH : MazeDirection.SOUTH;
        } else {
            d2 = Math.random() < 0.5 ? MazeDirection.WEST : MazeDirection.EAST;
        }
        BasicMazeGenerator g = a.createMazeGenerator(d1, d2);
        g.setDesiredPlayers(players);
        g.setRandomSeed();
        Maze2D maze;
        if (labyrinth) {
            maze = g.generateMaze(width / 2, height / 2).getLabyrinth();
        } else {
            maze = g.generateMaze(width, height);
        }
        return new Maze2D.GeneratedMaze(maze, g.getSeed(), a, a.getAdditionalParameters() >= 1 ? d1 : null, a.getAdditionalParameters() >= 2 ? d2 : null);
    }

    private int getRandomEmptyLocation(Maze2D maze) {
        boolean[][] available = new boolean[maze.getGridHeight()][maze.getGridWidth()];
        for (int y = 0; y < maze.getGridHeight(); y++) {
            for (int x = 0; x < maze.getGridWidth(); x++) {
                available[y][x] = true;
            }
        }
        for (Player p : PLAYERS) {
            available[p.getGridY()][p.getGridX()] = false;
        }
        for (MazeRobot r : BOTS) {
            available[r.getGridY()][r.getGridX()] = false;
        }
        int count = 0;
        for (int y = 0; y < maze.getGridHeight(); y++) {
            for (int x = 0; x < maze.getGridWidth(); x++) {
                if (available[y][x]) {
                    count++;
                }
            }
        }
        count = (int) (Math.random() * count);
        for (int y = 0; y < maze.getGridHeight(); y++) {
            for (int x = 0; x < maze.getGridWidth(); x++) {
                if (available[y][x]) {
                    if (--count < 0) {
                        return x + y * maze.getGridWidth();
                    }
                }
            }
        }
        return -1;
    }

    private void placeBotsRandom(Maze2D maze) {
        for (MazeRobot r : BOTS) {
            int location = getRandomEmptyLocation(maze);
            r.setCurrentDirection(null);
            if (location >= 0) {
                r.setX(location % maze.getGridWidth() + 0.5);
                r.setY(location / maze.getGridWidth() + 0.5);
            } else {
                r.setX(0.5);
                r.setY(0.5);
            }
        }
    }

    private void placeRandomUnvisited(Player player, Maze2D maze) {
        boolean[][] available = new boolean[maze.getHeight()][maze.getWidth()];
        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                available[y][x] = !maze.hasWall(x, y) && player.isVisited(x, y);
            }
        }
        for (Player p : PLAYERS) {
            available[p.getGridY() * 2][p.getGridX() * 2] = false;
        }
        int count = 0;
        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                if (available[y][x]) {
                    count++;
                }
            }
        }
        count = (int) (Math.random() * count);
        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                if (available[y][x]) {
                    if (--count < 0) {
                        player.setVisited(x, y, false);
                        return;
                    }
                }
            }
        }
    }

    private void loadMaze(final Maze2D.GeneratedMaze maze, Double minDistanceRatio, Float forceMinZoom, boolean labyrinth, MazeRobot... bots) {
        long time = System.nanoTime();
        if (minDistanceRatio != null && !labyrinth) {
            maze.getMaze().setDeadEndAsFinish(minDistanceRatio);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                double diff = 1;
                for (int i = 0; i < maze.getMaze().getPlayersCount(); i++) {
                    diff *= (maze.getMaze().getMazeDifficulty(i)[2] + 1);
                }
                if (DrawPanel.this.currentMaze == maze) {
                    synchronized (DrawPanel.this) {
                        DrawPanel.this.mazeDifficulty = diff;
                    }
                }
            }
        }).start();
        synchronized (this) {
            this.currentMaze = maze;
            this.mazeDifficulty = Double.POSITIVE_INFINITY;
            this.solution = null;
            this.forceMinZoom = forceMinZoom;
            this.lastRandomUnvisited = 0;
        }
        PLAYERS.clear();
        for (int i = 0; i < maze.getMaze().getPlayersCount(); i++) {
            Player p = new Player(0.2, Math.pow(2, (i % 2 == 0 ? 1 : -1) * ((i + 1) / 2)), 0xFF0000FF, 0xFFFFFF00, 0xFFFF0000, 0xFF88B1BF);
            p.moveTo(this.currentMaze.getMaze().getStartX(i) + 0.5, this.currentMaze.getMaze().getStartY(i) + 0.5, this.currentMaze.getMaze());
            p.setNewVisitedSize(this.currentMaze.getMaze().getWidth(), this.currentMaze.getMaze().getHeight());
            PLAYERS.add(p);
        }
        BOTS.clear();
        if (bots != null) {
            for (MazeRobot r : bots) {
                BOTS.add(r);
                r.setSpeed(Math.min(r.getSpeed(), PLAYERS.get(0).getSpeedMultiply()));
            }
            placeBotsRandom(maze.getMaze());
        }
        ACTIVITY.resetStartTime();
        ACTIVITY.resetTime();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        int width = getWidth();
        int height = getHeight();
        int displayWidth = width;
        int displayHeight = height;
        boolean squareCells = MainActivity.squareCells();
        GameMode gameMode = MainActivity.getGameMode();
        Maze2D.GeneratedMaze maze = getCurrentMaze();
        float mScaleFactor = this.mScaleFactor;
        if (this.forceMinZoom != null) {
            mScaleFactor = Math.min(Math.max(mScaleFactor, this.forceMinZoom), getMaxZoom());
        }
        if (canvas != null && maze != null) {
            Maze2D maze2D = maze.getMaze();
            if (this.followPlayer >= 0 || this.forceMinZoom != null) {
                int followPlayer = this.followPlayer;
                if (followPlayer < 0) {
                    followPlayer = PLAYERS.size() - 1;
                }
                double extraGridWidth = maze2D.getGridHeight() * displayWidth / displayHeight - maze2D.getGridWidth();
                double extraGridHeight = maze2D.getGridWidth() * displayHeight / displayWidth - maze2D.getGridHeight();
                if (!squareCells || extraGridWidth < 0) {
                    extraGridWidth = 0;
                }
                if (!squareCells || extraGridHeight < 0) {
                    extraGridHeight = 0;
                }
                double zoomGridWidth = (maze2D.getGridWidth() + extraGridWidth) / mScaleFactor;
                double zoomGridHeight = (maze2D.getGridHeight() + extraGridHeight) / mScaleFactor;
                translateX = (1 - mScaleFactor) * displayWidth * (mScaleFactor == 1 ? 0 : (float) ((PLAYERS.get(followPlayer).getX() + (extraGridWidth - zoomGridWidth) / 2) / (maze2D.getGridWidth() + extraGridWidth - zoomGridWidth)));
                translateY = (1 - mScaleFactor) * displayHeight * (mScaleFactor == 1 ? 0 : (float) ((PLAYERS.get(followPlayer).getY() + (extraGridHeight - zoomGridHeight) / 2) / (maze2D.getGridHeight() + extraGridHeight - zoomGridHeight)));
            }
            canvas.save();
            canvas.scale(mScaleFactor, mScaleFactor);
            if ((translateX * -1) < 0) {
                translateX = 0;
            } else if ((translateX * -1) > (mScaleFactor - 1) * displayWidth) {
                translateX = (1 - mScaleFactor) * displayWidth;
            }
            if (translateY * -1 < 0) {
                translateY = 0;
            } else if ((translateY * -1) > (mScaleFactor - 1) * displayHeight) {
                translateY = (1 - mScaleFactor) * displayHeight;
            }
            previousTranslateX = translateX;
            previousTranslateY = translateY;
            canvas.translate(translateX / mScaleFactor, translateY / mScaleFactor);

            maze2D.drawMaze(canvas, width, height, squareCells, BACKGROUND_PAINT, BORDER_PAINT, PLAYERS.toArray(new Player[PLAYERS.size()]), MainActivity.toggleTracer(), this.solution, SOLUTION_PAINT, gameMode == GameMode.FILL, BOTS.toArray(new MazeRobot[BOTS.size()]));

            canvas.restore();

            TEXT_PAINT.setTextSize(height / 65f);
            float drawY = 0;
            Resources r = getResources();
            String mazeString = r.getString(R.string.display_maze) + r.getString(R.string.display_colon) + " " + maze.getMaze().getGridWidth() + r.getString(R.string.display_multiply) + maze.getMaze().getGridHeight();
            canvas.drawText(mazeString, 0, TEXT_PAINT.getTextSize(), TEXT_PAINT);
            double time = (System.nanoTime() - ACTIVITY.getStartTime() + ACTIVITY.getTime()) / 1000000000d;
            String timeString = r.getString(R.string.display_time) + r.getString(R.string.display_colon) + " " + (time >= 3600 ? (long) (time / 3600) + r.getString(R.string.display_hour) : "") + (time >= 60 ? (long) (time / 60) % 60 + r.getString(R.string.display_minute) : "") + Math.floor(time % 60 * 1000) / 1000 + r.getString(R.string.display_second);
            canvas.drawText(MENU_STRING, Math.max(width - TEXT_PAINT.measureText(MENU_STRING), TEXT_PAINT.measureText(mazeString + "\t")), TEXT_PAINT.getTextSize(), TEXT_PAINT);
            double sumSpeed = 0;
            for (Player p : PLAYERS) {
                sumSpeed += Math.sqrt(Math.pow(p.getVelocityX(), 2) + Math.pow(p.getVelocityY(), 2));
            }
            if (gameMode.isDisplaysSeed()) {
                canvas.drawText(r.getString(R.string.display_generation_seed) + r.getString(R.string.display_colon) + " " + Maze2D.getGenerationCode(maze), 0, TEXT_PAINT.getTextSize() * 2, TEXT_PAINT);
                drawY += TEXT_PAINT.getTextSize();
            }
            String speedString = r.getString(R.string.display_speed) + r.getString(R.string.display_colon) + " " + Math.floor(sumSpeed / PLAYERS.size() * 100000) / 100000;
            canvas.drawText(timeString, 0, TEXT_PAINT.getTextSize() * 2 + drawY, TEXT_PAINT);
            canvas.drawText(speedString, Math.max(width - TEXT_PAINT.measureText(speedString), TEXT_PAINT.measureText(timeString + "\t")), TEXT_PAINT.getTextSize() * 2 + drawY, TEXT_PAINT);
            canvas.drawText(r.getString(R.string.display_zoom) + r.getString(R.string.display_colon) + " " + Math.floor(mScaleFactor * 100) / 100 + r.getString(R.string.display_multiply_zoom), 0, TEXT_PAINT.getTextSize() * 3 + drawY, TEXT_PAINT);
            drawY = 0;
            if (gameMode.isDisplaysDifficulty() && !Double.isInfinite(this.mazeDifficulty)) {
                canvas.drawText(r.getString(R.string.display_difficulty) + r.getString(R.string.display_colon) + " " + this.mazeDifficulty, 0, height - drawY - TEXT_PAINT.getTextSize() / 3, TEXT_PAINT);
                drawY = TEXT_PAINT.getTextSize();
            }
            if (gameMode == GameMode.REGULAR) {
                canvas.drawText(r.getString(R.string.display_level) + r.getString(R.string.display_colon) + " " + MainActivity.regularLevel(), 0, height - drawY - TEXT_PAINT.getTextSize() / 3, TEXT_PAINT);
            } else if (gameMode == GameMode.FILL) {
                canvas.drawText(r.getString(R.string.display_level) + r.getString(R.string.display_colon) + " " + MainActivity.fillLevel(), 0, height - drawY - TEXT_PAINT.getTextSize() / 3, TEXT_PAINT);
            }
            String algorithmString = r.getString(R.string.display_algorithm) + r.getString(R.string.display_colon) + " " + maze.getAlgorithm().getLocalName(getContext());
            canvas.drawText(algorithmString, width - TEXT_PAINT.measureText(algorithmString), height - TEXT_PAINT.getTextSize() / 3, TEXT_PAINT);

        }
    }

    static class MazeProgress {
        private final double[] X;
        private final double[] Y;
        private final boolean[][] VISITED;
        private final long TIME;

        public MazeProgress(double[] x, double[] y, boolean[][] visited, long time) {
            X = new double[x.length];
            System.arraycopy(x, 0, X, 0, X.length);
            Y = new double[y.length];
            System.arraycopy(y, 0, Y, 0, Y.length);
            VISITED = new boolean[visited.length][];
            for (int i = 0; i < visited.length; i++) {
                VISITED[i] = new boolean[visited[i].length];
                System.arraycopy(visited[i], 0, VISITED[i], 0, VISITED[i].length);
            }
            TIME = time;
        }

        void save(SharedPreferences.Editor editor, String key, int precisionDecimals) {
            double precision = Math.pow(10, precisionDecimals);
            editor.putInt(key + "0", X.length);
            int count = 0;
            for (double x : X) {
                editor.putFloat(key + (++count), (float) (Math.floor(x * precision) / precision));
            }
            for (double y : Y) {
                editor.putFloat(key + (++count), (float) (Math.floor(y * precision) / precision));
            }
            editor.putInt(key + (++count),  VISITED.length);
            editor.putInt(key + (++count), VISITED[0].length);
            editor.putLong(key + (++count), TIME);
            for (int y = 0; y < VISITED.length; y++) {
                for (int x = 0; x < VISITED[y].length; x++) {
                    editor.putBoolean(key + (++count), VISITED[y][x]);
                }
            }
        }

        static MazeProgress load(SharedPreferences preferences, String key) {
            double[] x = new double[preferences.getInt(key + "0", 0)];
            if (x.length == 0) {
                return null;
            }
            double[] y = new double[x.length];
            int count = 0;
            for (int i = 0; i < x.length; i++) {
                x[i] = preferences.getFloat(key + (++count), 0.5f);
            }
            for (int i = 0; i < y.length; i++) {
                y[i] = preferences.getFloat(key + (++count), 0.5f);
            }
            int height = preferences.getInt(key + (++count), 3);
            int width = preferences.getInt(key + (++count), 3);
            long time = preferences.getLong(key + (++count), System.currentTimeMillis());
            boolean[][] visited = new boolean[height][width];
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    visited[j][i] = preferences.getBoolean(key + (++count), false);
                }
            }
            return new MazeProgress(x, y, visited, time);
        }
    }
}
