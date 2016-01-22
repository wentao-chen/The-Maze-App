package com.gmail.wentaochen97.themazeapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.gmail.wentaochen97.maze.GameMode;
import com.gmail.wentaochen97.maze.Maze2D;
import com.gmail.wentaochen97.maze.MazeAlgorithm;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements SensorEventListener {

    private static MainActivity mainActivity = null;

    private static final long CREATE_NEW_REGULAR_MAZE_COOL_DOWN = 2 * 60 * 60 * 1000;
    private static final long CREATE_NEW_FILL_MAZE_COOL_DOWN = 2 * 60 * 60 * 1000;

    private SensorManager senSensorManager = null;
    private Sensor senAccelerometer = null;
    private Sensor senMagnetic = null;
    private DrawPanel drawPanel = null;

    private float[] lastAcceleration = new float[3];
    private float[] lastMagnetic = new float[3];

    private static boolean toggleTracer = true;
    private static boolean squareCells = true;
    private static GameMode gameMode = GameMode.REGULAR;
    private static MazeAlgorithm algorithm = MazeAlgorithm.ELLER;
    private static long regularLevel = 1;
    private static long fillLevel = 1;
    private static long lastCreateNewRegularMazeTime = 0;
    private static long lastCreateNewFillMazeTime = 0;
    private final Map<GameMode, Maze2D.GeneratedMaze> MAZE_BUFFER = new HashMap<>();
    private final Map<GameMode, DrawPanel.MazeProgress> MAZE_PROGRESS_BUFFER = new HashMap<>();

    private long time = 0;
    private long startTime = 0;

    public static boolean toggleTracer() {
        return toggleTracer;
    }

    static void setToggleTracer(boolean b) {
        synchronized (MainActivity.class) {
            toggleTracer = b;
        }
    }

    public static boolean squareCells() {
        return squareCells;
    }

    static void setSquareCells(boolean b) {
        synchronized (MainActivity.class) {
            squareCells = b;
        }
    }

    public static MazeAlgorithm getAlgorithm() {
        return algorithm;
    }

    static void setAlgorithm(MazeAlgorithm a) {
        synchronized (MainActivity.class) {
            algorithm = a;
        }
    }

    public static GameMode getGameMode() {
        return gameMode;
    }

    static void setGameMode(GameMode g) {
        if (g == null) g = GameMode.REGULAR;
        if (g != gameMode) {
            synchronized (MainActivity.class) {
                gameMode = g;
            }
        }
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void resetStartTime() {
        this.startTime = System.nanoTime();
    }

    public long getTime() {
        return this.time;
    }

    public void resetTime() {
        this.time = 0;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public static long regularLevel() {
        return regularLevel;
    }

    static void setRegularLevel(long level) {
        synchronized (MainActivity.class) {
            regularLevel = level;
        }
    }

    public static long fillLevel() {
        return fillLevel;
    }

    static void setFillLevel(long level) {
        synchronized (MainActivity.class) {
            fillLevel = level;
        }
    }

    public static long lastCreateNewRegularMazeTime() {
        return lastCreateNewRegularMazeTime;
    }

    public static long getRemainingCreateNewRegularMazeTime() {
        return lastCreateNewRegularMazeTime + CREATE_NEW_REGULAR_MAZE_COOL_DOWN - System.currentTimeMillis();
    }

    public static boolean canCreateNewRegularMaze() {
        return System.currentTimeMillis() - lastCreateNewRegularMazeTime() >= CREATE_NEW_REGULAR_MAZE_COOL_DOWN;
    }

    public void resetCreateNewRegularMazeTime() {
        synchronized (MainActivity.class) {
            lastCreateNewRegularMazeTime = System.currentTimeMillis();
        }
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(getResources().getString(R.string.save_last_create_regular_maze_time), lastCreateNewRegularMazeTime);
        editor.commit();
    }

    public static long lastCreateNewFillMazeTime() {
        return lastCreateNewFillMazeTime;
    }

    public static long getRemainingCreateNewFillMazeTime() {
        return lastCreateNewFillMazeTime + CREATE_NEW_FILL_MAZE_COOL_DOWN - System.currentTimeMillis();
    }

    public static boolean canCreateNewFillMaze() {
        return System.currentTimeMillis() - lastCreateNewFillMazeTime() >= CREATE_NEW_FILL_MAZE_COOL_DOWN;
    }

    public void resetCreateNewFillMazeTime() {
        synchronized (MainActivity.class) {
            lastCreateNewFillMazeTime = System.currentTimeMillis();
        }
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(getResources().getString(R.string.save_last_create_fill_maze_time), lastCreateNewFillMazeTime);
        editor.commit();
    }

    Maze2D.GeneratedMaze loadSaveMaze(GameMode gameMode) {
        Maze2D.GeneratedMaze maze = null;
        if (gameMode != null) {
            maze = MAZE_BUFFER.get(gameMode);
            if (maze != null) {
                return maze;
            }
        }
        int res = R.string.save_regular_level_maze;
        if (gameMode == GameMode.REGULAR) {
            res = R.string.save_regular_level_maze;
        } else if (gameMode == GameMode.FILL) {
            res = R.string.save_fill_level_maze;
        } else if (gameMode == GameMode.CUSTOM) {
            res = R.string.save_custom_level_maze;
        }
        maze = Maze2D.GeneratedMaze.loadMaze(getPreferences(Context.MODE_PRIVATE), getResources().getString(res));
        return maze;
    }

    void saveMaze(GameMode gameMode, Maze2D.GeneratedMaze maze, DrawPanel.MazeProgress progress, boolean temporarySave) {
        if (gameMode != null) {
            MAZE_BUFFER.put(gameMode, maze);
            if (temporarySave) {
                saveMazeProgress(gameMode, progress, null, temporarySave);
                return;
            }
        }
        saveMaze(gameMode, maze, null);
        saveMazeProgress(gameMode, progress, null, temporarySave);
    }

    private void saveMaze(GameMode gameMode, Maze2D.GeneratedMaze maze, SharedPreferences.Editor editor) {
        int res = R.string.save_regular_level_maze;
        if (gameMode == GameMode.REGULAR) {
            res = R.string.save_regular_level_maze;
        } else if (gameMode == GameMode.FILL) {
            res = R.string.save_fill_level_maze;
        } else if (gameMode == GameMode.CUSTOM) {
            res = R.string.save_custom_level_maze;
        }
        if (editor != null) {
            maze.saveMaze(editor, getResources().getString(res));
        } else {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor2 = sharedPref.edit();
            maze.saveMaze(editor2, getResources().getString(res));
            editor2.commit();
        }
    }

    private void saveMazeProgress(GameMode gameMode, DrawPanel.MazeProgress progress, SharedPreferences.Editor editor, boolean temporarySave) {
        if (gameMode != null) {
            MAZE_PROGRESS_BUFFER.put(gameMode, progress);
            if (temporarySave) {
                return;
            }
        }
        int resource = R.string.save_regular_level_maze_progress;
        if (gameMode == GameMode.REGULAR) {
            resource = R.string.save_regular_level_maze_progress;
        } else if (gameMode == GameMode.FILL) {
            resource = R.string.save_fill_level_maze_progress;
        } else if (gameMode == GameMode.CUSTOM) {
            resource = R.string.save_custom_level_maze_progress;
        }
        if (editor != null) {
            progress.save(editor, getResources().getString(resource), 3);
        } else {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor2 = sharedPref.edit();
            progress.save(editor2, getResources().getString(resource), 3);
            editor2.commit();
        }
    }

    DrawPanel.MazeProgress loadSaveMazeProgress(GameMode gameMode) {
        if (gameMode != null) {
            DrawPanel.MazeProgress progress = MAZE_PROGRESS_BUFFER.get(gameMode);
            if (progress != null) {
                return progress;
            }
        }
        int resource = R.string.save_regular_level_maze_progress;
        if (gameMode == GameMode.REGULAR) {
            resource = R.string.save_regular_level_maze_progress;
        } else if (gameMode == GameMode.FILL) {
            resource = R.string.save_fill_level_maze_progress;
        } else if (gameMode == GameMode.CUSTOM) {
            resource = R.string.save_custom_level_maze_progress;
        }
        return DrawPanel.MazeProgress.load(getPreferences(Context.MODE_PRIVATE), getResources().getString(resource));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mainActivity = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        drawPanel = new DrawPanel(this);
        setContentView(drawPanel);

        setToggleTracer(getPreferences(Context.MODE_PRIVATE).getBoolean(getResources().getString(R.string.save_toggle_tracer), toggleTracer()));
        setSquareCells(getPreferences(Context.MODE_PRIVATE).getBoolean(getResources().getString(R.string.save_square_cells), squareCells()));
        MazeAlgorithm algorithm = getAlgorithm();
        setAlgorithm(MazeAlgorithm.getByID(getPreferences(Context.MODE_PRIVATE).getInt(getResources().getString(R.string.save_algorithm), algorithm != null ? algorithm.getID() : -1)));
        GameMode gameMode = getGameMode();
        setGameMode(GameMode.getByID(getPreferences(Context.MODE_PRIVATE).getInt(getResources().getString(R.string.save_game_mode), gameMode.getID())));
        setRegularLevel(getPreferences(Context.MODE_PRIVATE).getLong(getResources().getString(R.string.save_regular_level), regularLevel()));
        setFillLevel(getPreferences(Context.MODE_PRIVATE).getLong(getResources().getString(R.string.save_fill_level), fillLevel()));
        synchronized (MainActivity.class) {
            lastCreateNewRegularMazeTime = getPreferences(Context.MODE_PRIVATE).getLong(getResources().getString(R.string.save_last_create_regular_maze_time), 0);
            lastCreateNewFillMazeTime = getPreferences(Context.MODE_PRIVATE).getLong(getResources().getString(R.string.save_last_create_fill_maze_time), 0);
        }

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senMagnetic = senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.senSensorManager.unregisterListener(this);
        this.time += System.nanoTime() - this.startTime;
        saveMazeProgress(getGameMode(), this.drawPanel.getCurrentProgress(), null, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.startTime = System.nanoTime();
        this.senSensorManager.registerListener(this, this.senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        this.senSensorManager.registerListener(this, this.senMagnetic, SensorManager.SENSOR_DELAY_NORMAL);

        this.drawPanel.setGameMode(getGameMode());
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getResources().getString(R.string.save_toggle_tracer), toggleTracer());
        editor.putBoolean(getResources().getString(R.string.save_square_cells), squareCells());
        MazeAlgorithm algorithm = getAlgorithm();
        editor.putInt(getResources().getString(R.string.save_algorithm), algorithm != null ? algorithm.getID() : -1);
        editor.putInt(getResources().getString(R.string.save_game_mode), getGameMode().getID());
        editor.putLong(getResources().getString(R.string.save_regular_level), regularLevel());
        editor.putLong(getResources().getString(R.string.save_fill_level), fillLevel());
        saveMaze(getGameMode(), this.drawPanel.getCurrentMaze(), editor);
        saveMazeProgress(getGameMode(), this.drawPanel.getCurrentProgress(), editor, false);
        editor.commit();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        switch (mySensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(sensorEvent.values, 0, lastAcceleration, 0, 3);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(sensorEvent.values, 0, lastMagnetic, 0, 3);
                break;
        }
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER || mySensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float[] rotation = new float[9];
            float[] orientation = new float[3];
            SensorManager.getRotationMatrix(rotation, null, lastAcceleration, lastMagnetic);
            SensorManager.getOrientation(rotation, orientation);
            Maze2D maze = drawPanel.getCurrentMaze().getMaze();
            double speed = maze != null ? Math.min(Math.pow(Math.log(maze.getGridWidth() * maze.getGridHeight()), 2) / 2, 10) : 0;
            float[] orientationCalibration = SettingsActivity.getTiltCalibration();
            drawPanel.setVelocities((orientation[2] - orientationCalibration[2]) * speed, (orientation[1] - orientationCalibration[1]) * speed);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static void destroy() {
        if (mainActivity != null) {
            mainActivity.onStop();
        }
    }
}
