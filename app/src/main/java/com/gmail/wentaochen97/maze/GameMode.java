package com.gmail.wentaochen97.maze;

import android.content.Context;

import com.gmail.wentaochen97.themazeapp.R;

/**
 * Created by Wentao-Admin on 2016-01-08.
 */
public enum GameMode {
    REGULAR(R.string.game_mode_name_regular, R.string.game_mode_description_regular, 0, true, false),
    FILL(R.string.game_mode_name_fill, R.string.game_mode_description_fill, 1, false, false),
    CUSTOM(R.string.game_mode_name_custom, R.string.game_mode_description_custom, 2, true, true);

    private final int NAME_RESOURCE;
    private final int DESCRIPTION_RESOURCE;
    private final int ID;
    private final boolean DISPLAYS_DIFFICULTY;
    private final boolean DISPLAYS_SEED;

    GameMode(int nameResource, int descriptionResource, int id, boolean displaysDifficulty, boolean displaysSeed) {
        NAME_RESOURCE = nameResource;
        DESCRIPTION_RESOURCE = descriptionResource;
        ID = id;
        DISPLAYS_DIFFICULTY = displaysDifficulty;
        DISPLAYS_SEED = displaysSeed;
    }

    public String getName(Context context) {
        return context.getString(NAME_RESOURCE);
    }

    public String getDescription(Context context) {
        return context.getString(DESCRIPTION_RESOURCE);
    }

    public int getID() {
        return ID;
    }

    public boolean isDisplaysDifficulty() {
        return DISPLAYS_DIFFICULTY;
    }

    public boolean isDisplaysSeed() {
        return DISPLAYS_SEED;
    }

    public static String[] getNames(Context context) {
        GameMode[] g = GameMode.values();
        String[] s = new String[g.length];
        for (int i = 0; i < g.length; i++) {
            s[i] = g[i].getName(context);
        }
        return s;
    }

    public static GameMode getByName(Context context, String name) {
        for (GameMode a : GameMode.values()) {
            if (name.equals(a.getName(context))) {
                return a;
            }
        }
        return null;
    }

    public static GameMode getByID(int id) {
        for (GameMode g : GameMode.values()) {
            if (id == g.getID()) {
                return g;
            }
        }
        return null;
    }
}
