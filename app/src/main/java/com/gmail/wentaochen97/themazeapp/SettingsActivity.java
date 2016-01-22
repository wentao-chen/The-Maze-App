package com.gmail.wentaochen97.themazeapp;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Toast;

import com.gmail.wentaochen97.maze.GameMode;
import com.gmail.wentaochen97.maze.Maze2D;
import com.gmail.wentaochen97.maze.MazeAlgorithm;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    private static float[] tiltCalibration = new float[3];
    private static Maze2D.GeneratedMaze loadSeedMaze = null;
    private static int customMazeWidth = 10;
    private static int customMazeHeight = 10;
    private static boolean showSolution = false;
    private static boolean newRegularMaze = false;
    private static boolean newFillMaze = false;

    private ListPreference gamePreference = null;
    private Preference key_game_mode_summary = null;
    private ListPreference algorithmPreference = null;
    private Preference useCodePreference = null;
    private Preference customWidthPreference = null;
    private Preference customHeightPreference = null;
    private Preference customCreatePreference = null;
    private Preference customSolvePreference = null;
    private Preference regularCreatePreference = null;
    private PreferenceScreen gameModeSettingsPreferenceScreen = null;

    private Thread updateTimePreferenceThread = null;

    public static float[] getTiltCalibration() {
        float[] a = new float[tiltCalibration.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = tiltCalibration[i];
        }
        return tiltCalibration;
    }

    public static Maze2D.GeneratedMaze getLoadSeedMaze() {
        return loadSeedMaze;
    }

    public static void resetLoadSeedMaze() {
        synchronized (SettingsActivity.class) {
            loadSeedMaze = null;
        }
    }

    public static boolean showSolution() {
        return showSolution;
    }

    public static void resetShowSolution() {
        synchronized (SettingsActivity.class) {
            showSolution = false;
        }
    }

    public static boolean newRegularMaze() {
        return newRegularMaze;
    }

    public static void resetNewRegularMaze() {
        synchronized (SettingsActivity.class) {
            newRegularMaze = false;
        }
    }

    public static boolean newFillMaze() {
        return newFillMaze;
    }

    public static void resetNewFillMaze() {
        synchronized (SettingsActivity.class) {
            newFillMaze = false;
        }
    }

    public static int getCustomMazeWidth() {
        return customMazeWidth;
    }

    public static int getCustomMazeHeight() {
        return customMazeHeight;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    @Override
    protected void onStop() {
        super.onStop();
        synchronized (this) {
            this.updateTimePreferenceThread = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MainActivity.destroy();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        addPreferencesFromResource(R.xml.pref_empty);

        addHeader(R.string.pref_header_game);
        addPreferencesFromResource(R.xml.pref_game);
        this.gameModeSettingsPreferenceScreen = getPreferenceScreen();

        addHeader(R.string.pref_header_other);
        addPreferencesFromResource(R.xml.pref_other);

        Preference key_game_mode_summary = findPreference(getResources().getString(R.string.key_game_mode_summary));
        this.key_game_mode_summary = key_game_mode_summary;

        Preference useCodePreference = findPreference(getResources().getString(R.string.key_use_code));
        this.useCodePreference = useCodePreference;
        useCodePreference.setOnPreferenceChangeListener(USE_CODE_PREFERENCE_LISTENER);

        Preference customWidthPreference = findPreference(getResources().getString(R.string.key_custom_maze_width));
        customWidthPreference.setSummary(String.valueOf(getCustomMazeWidth()));
        this.customWidthPreference = customWidthPreference;
        customWidthPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                try {
                    int width = Math.min(Math.max(Integer.parseInt(value.toString()), getResources().getInteger(R.integer.min_custom_maze_size)), getResources().getInteger(R.integer.max_custom_maze_size));
                    synchronized (SettingsActivity.class) {
                        SettingsActivity.customMazeWidth = width;
                    }
                    preference.setSummary(String.valueOf(width));
                } catch (NumberFormatException e) {
                }
                return true;
            }
        });
        Preference customHeightPreference = findPreference(getResources().getString(R.string.key_custom_maze_height));
        customHeightPreference.setSummary(String.valueOf(getCustomMazeHeight()));
        this.customHeightPreference = customHeightPreference;
        customHeightPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                try {
                    int height = Math.min(Math.max(Integer.parseInt(value.toString()), getResources().getInteger(R.integer.min_custom_maze_size)), getResources().getInteger(R.integer.max_custom_maze_size));
                    synchronized (SettingsActivity.class) {
                        SettingsActivity.customMazeHeight = height;
                    }
                    preference.setSummary(String.valueOf(height));
                } catch (NumberFormatException e) {
                }
                return true;
            }
        });
        Preference customCreatePreference =  findPreference(getResources().getString(R.string.key_custom_maze_create));
        this.customCreatePreference = customCreatePreference;
        customCreatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Maze2D.GeneratedMaze maze = generateCustomMaze(SettingsActivity.this);
                synchronized (SettingsActivity.class) {
                    SettingsActivity.loadSeedMaze = maze;
                }
                if (maze != null) {
                    SettingsActivity.this.finish();
                }
                return true;
            }
        });
        Preference customSolvePreference =  findPreference(getResources().getString(R.string.key_custom_maze_solve));
        this.customSolvePreference = customSolvePreference;
        customSolvePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                synchronized (SettingsActivity.class) {
                    SettingsActivity.showSolution = true;
                }
                SettingsActivity.this.finish();
                return true;
            }
        });
        Preference regularCreatePreference =  findPreference(getResources().getString(R.string.key_maze_create));
        this.regularCreatePreference = regularCreatePreference;
        regularCreatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (MainActivity.getGameMode() == GameMode.REGULAR) {
                    synchronized (SettingsActivity.class) {
                        SettingsActivity.newRegularMaze = true;
                    }
                } else {
                    synchronized (SettingsActivity.class) {
                        SettingsActivity.newFillMaze = true;
                    }
                }
                SettingsActivity.this.finish();
                return true;
            }
        });

        ListPreference algorithmPreference = (ListPreference) findPreference(getResources().getString(R.string.key_algorithm));
        this.algorithmPreference = algorithmPreference;

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        ListPreference gameModePreference = (ListPreference) findPreference(getResources().getString(R.string.key_game_mode));
        this.gamePreference = gameModePreference;
        bindPreferenceSummaryToValue(gameModePreference);
        String[] names = GameMode.getNames(this);
        gameModePreference.setEntries(names);
        gameModePreference.setEntryValues(names);
        GameMode gameMode = MainActivity.getGameMode();
        if (gameMode != null) {
            gameModePreference.setValueIndex(gameMode.getID());
        }

        bindPreferenceSummaryToValue(algorithmPreference);
        names = MazeAlgorithm.getNames(this, true);
        algorithmPreference.setEntries(names);
        algorithmPreference.setEntryValues(names);
        MazeAlgorithm algorithm = MainActivity.getAlgorithm();
        if (algorithm != null) {
            algorithmPreference.setDefaultValue(algorithm.getLocalName(this));
        }

        SwitchPreference showTracerPreference = (SwitchPreference) findPreference(getResources().getString(R.string.key_show_tracer));
        showTracerPreference.setOnPreferenceChangeListener(TOGGLE_TRACER_PREFERENCE_LISTENER);
        showTracerPreference.setDefaultValue(MainActivity.toggleTracer());

        SwitchPreference squareCellsPreference = (SwitchPreference) findPreference(getResources().getString(R.string.key_square_cells));
        squareCellsPreference.setOnPreferenceChangeListener(SQUARE_CELLS_PREFERENCE_LISTENER);
        squareCellsPreference.setDefaultValue(MainActivity.squareCells());

        Preference recalibratePreference = findPreference(getResources().getString(R.string.key_recalibrate));
        recalibratePreference.setOnPreferenceClickListener(RECALIBRATE_PREFERENCE_LISTENER);

        Preference defaultTiltPreference = findPreference(getResources().getString(R.string.key_default_tilt));
        defaultTiltPreference.setOnPreferenceClickListener(DEFAULT_TILT_PREFERENCE_LISTENER);

        prepareGameModeGUI(MainActivity.getGameMode());
    }

    private void addHeader(int titleResID) {
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(titleResID);
        getPreferenceScreen().addPreference(fakeHeader);
    }

    static Maze2D.GeneratedMaze generateCustomMaze(Context context) {
        Maze2D.GeneratedMaze maze = DrawPanel.generateMaze(getCustomMazeWidth(), getCustomMazeHeight(), 1, false);
        maze.getMaze().setDeadEndAsFinish(0.9);
        if (context != null) {
            if (maze != null) {
                Toast.makeText(context.getApplicationContext(), maze.getMaze().getGridWidth() + context.getResources().getString(R.string.display_multiply) + maze.getMaze().getGridHeight() + " " + maze.getAlgorithm().getLocalName(context) + " " + context.getResources().getString(R.string.toast_success_custom_maze), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.toast_fail_custom_maze), Toast.LENGTH_SHORT).show();
            }
        }
        return maze;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    private final Preference.OnPreferenceChangeListener USE_CODE_PREFERENCE_LISTENER = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            Maze2D.GeneratedMaze maze = Maze2D.generateFromSeed(value.toString());
            synchronized (SettingsActivity.class) {
                SettingsActivity.loadSeedMaze = maze;
            }
            if (maze != null) {
                Toast.makeText(getApplicationContext(), maze.getMaze().getGridWidth() + getResources().getString(R.string.display_multiply) + maze.getMaze().getGridHeight() + " " + getResources().getString(R.string.toast_success_seed), Toast.LENGTH_SHORT).show();
                SettingsActivity.this.finish();
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_fail_seed), Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    };

    private static final Preference.OnPreferenceChangeListener TOGGLE_TRACER_PREFERENCE_LISTENER = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            boolean b = value.toString().toLowerCase().contains("t") || value.toString().toLowerCase().contains("f");
            if (b) {
                b = value.toString().toLowerCase().contains("t");
            } else {
                b = !MainActivity.toggleTracer();
            }
            MainActivity.setToggleTracer(b);
            return true;
        }
    };

    private static final Preference.OnPreferenceChangeListener SQUARE_CELLS_PREFERENCE_LISTENER = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            boolean b = value.toString().toLowerCase().contains("t") || value.toString().toLowerCase().contains("f");
            if (b) {
                b = value.toString().toLowerCase().contains("t");
            } else {
                b = !MainActivity.squareCells();
            }
            MainActivity.setSquareCells(b);
            return true;
        }
    };

    private final Preference.OnPreferenceClickListener RECALIBRATE_PREFERENCE_LISTENER = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            final SensorManager SEN_SENSOR_MANAGER = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor senAccelerometer = SEN_SENSOR_MANAGER.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor senMagnetic = SEN_SENSOR_MANAGER.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            SensorEventListener e = new SensorEventListener() {
                private float[] lastAccel = null;
                private float[] lastMagnetic = null;

                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    Sensor mySensor = sensorEvent.sensor;

                    switch (mySensor.getType()) {
                        case Sensor.TYPE_ACCELEROMETER:
                            lastAccel = new float[3];
                            System.arraycopy(sensorEvent.values, 0, lastAccel, 0, 3);
                            break;
                        case Sensor.TYPE_MAGNETIC_FIELD:
                            lastMagnetic = new float[3];
                            System.arraycopy(sensorEvent.values, 0, lastMagnetic, 0, 3);
                            break;
                    }
                    if (lastAccel != null && lastMagnetic != null) {
                        float[] rotation = new float[9];
                        float[] orientation = new float[3];
                        SensorManager.getRotationMatrix(rotation, null, lastAccel, lastMagnetic);
                        SensorManager.getOrientation(rotation, orientation);
                        synchronized (SettingsActivity.class) {
                            SettingsActivity.tiltCalibration = orientation;
                        }
                        SEN_SENSOR_MANAGER.unregisterListener(this);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_recalibrate), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };
            SEN_SENSOR_MANAGER.registerListener(e, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            SEN_SENSOR_MANAGER.registerListener(e, senMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
            return true;
        }
    };

    private final Preference.OnPreferenceClickListener DEFAULT_TILT_PREFERENCE_LISTENER = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            synchronized (SettingsActivity.class) {
                for (int i = 0; i < SettingsActivity.tiltCalibration.length; i++) {
                    SettingsActivity.tiltCalibration[i] = 0;
                }
            }
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_default_tilt), Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                if (listPreference.getKey() == getResources().getString(R.string.key_game_mode)) {
                    SettingsActivity.this.gamePreference = listPreference;
                    GameMode g = GameMode.getByName(SettingsActivity.this, stringValue);
                    g = g != null ? g : GameMode.REGULAR;
                    prepareGameModeGUI(g);
                } else if (listPreference.getKey() == getResources().getString(R.string.key_algorithm)) {
                    MazeAlgorithm a = MazeAlgorithm.getByName(SettingsActivity.this, stringValue);
                    MainActivity.setAlgorithm(a);
                    String[] names = MazeAlgorithm.getNames(SettingsActivity.this, true);
                    preference.setSummary(a != null ? a.getLocalName(SettingsActivity.this) : names[names.length - 1]);
                } else {
                    int index = listPreference.findIndexOfValue(stringValue);
                    preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    private void prepareGameModeGUI(GameMode gameMode) {
        MainActivity.setGameMode(gameMode);
        this.gamePreference.setValueIndex(gameMode.getID());
        this.gamePreference.setSummary(gameMode.getName(this));
        this.key_game_mode_summary.setSummary(gameMode.getDescription(this));
        this.gameModeSettingsPreferenceScreen.removePreference(this.useCodePreference);
        this.gameModeSettingsPreferenceScreen.removePreference(this.customWidthPreference);
        this.gameModeSettingsPreferenceScreen.removePreference(this.customHeightPreference);
        this.gameModeSettingsPreferenceScreen.removePreference(this.customCreatePreference);
        this.gameModeSettingsPreferenceScreen.removePreference(this.customSolvePreference);
        this.gameModeSettingsPreferenceScreen.removePreference(this.regularCreatePreference);
        this.gameModeSettingsPreferenceScreen.removePreference(this.algorithmPreference);
        if (gameMode == GameMode.CUSTOM) {
            this.gameModeSettingsPreferenceScreen.addPreference(this.algorithmPreference);
            this.gameModeSettingsPreferenceScreen.addPreference(this.useCodePreference);
            this.gameModeSettingsPreferenceScreen.addPreference(this.customWidthPreference);
            this.gameModeSettingsPreferenceScreen.addPreference(this.customHeightPreference);
            this.gameModeSettingsPreferenceScreen.addPreference(this.customCreatePreference);
            this.gameModeSettingsPreferenceScreen.addPreference(this.customSolvePreference);
        } else if (gameMode == GameMode.REGULAR || gameMode == GameMode.FILL) {
            final boolean REGULAR_GAME_MODE = gameMode == GameMode.REGULAR;
            this.gameModeSettingsPreferenceScreen.addPreference(this.regularCreatePreference);
            this.regularCreatePreference.setEnabled(REGULAR_GAME_MODE ? MainActivity.canCreateNewRegularMaze() : MainActivity.canCreateNewFillMaze());
            this.regularCreatePreference.setSummary(null);
            if (!this.regularCreatePreference.isEnabled()) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        while (SettingsActivity.this.updateTimePreferenceThread == this) {
                            try {
                                synchronized (this) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (REGULAR_GAME_MODE ? MainActivity.canCreateNewRegularMaze() : MainActivity.canCreateNewFillMaze()) {
                                                SettingsActivity.this.regularCreatePreference.setEnabled(true);
                                                SettingsActivity.this.regularCreatePreference.setSummary(null);
                                                synchronized (SettingsActivity.this) {
                                                    SettingsActivity.this.updateTimePreferenceThread = null;
                                                }
                                            } else {
                                                long time = REGULAR_GAME_MODE ? MainActivity.getRemainingCreateNewRegularMazeTime() : MainActivity.getRemainingCreateNewFillMazeTime();
                                                SettingsActivity.this.regularCreatePreference.setSummary(getResources().getString(R.string.pref_description_create_new_maze) + getResources().getString(R.string.display_colon) + " " + (time >= 3600000 ? time / 3600000 + getResources().getString(R.string.display_hour) + " " : "") + (time >= 60000 ? time / 60000 % 60 + getResources().getString(R.string.display_minute) + " " : "") + (time / 1000 % 60) + getResources().getString(R.string.display_second) + " ");
                                            }
                                        }
                                    });
                                    wait(1000);
                                }
                            } catch (InterruptedException e) {
                            }
                        }
                    };
                };
                synchronized (this) {
                    this.updateTimePreferenceThread = thread;
                }
                thread.start();
            } else {
                synchronized (this) {
                    this.updateTimePreferenceThread = null;
                }
            }
        }
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GamePreferenceFragment.class.getName().equals(fragmentName)
                || OtherPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GamePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_game);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class OtherPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_other);
            setHasOptionsMenu(true);

            findPreference(getResources().getString(R.string.key_show_tracer)).setOnPreferenceChangeListener(TOGGLE_TRACER_PREFERENCE_LISTENER);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
