package com.gmail.wentaochen97.maze;

import android.content.Context;

import com.gmail.wentaochen97.themazeapp.R;

/**
 * Created by Wentao-Admin on 2016-01-08.
 */
public enum MazeAlgorithm {
    ALDOUS_BRODER(R.string.maze_algorithm_name_aldous_broder, 0, 0, 9),
    BINARY_TREE(R.string.maze_algorithm_name_binary_tree, 1, 2, 6),
    ELLER(R.string.maze_algorithm_name_eller, 2, 0, 0),
    HUNT_AND_KILL(R.string.maze_algorithm_name_hunt_and_kill, 3, 0, 8),
    KRUSKAL(R.string.maze_algorithm_name_kruskal, 4, 0, 2),
    PRIM(R.string.maze_algorithm_name_prim, 5, 0, 5),
    RECURSIVE_BACKTRACKER(R.string.maze_algorithm_name_recursive_backtracker, 6, 0, 1),
    RECURSIVE_DIVISION(R.string.maze_algorithm_name_recursive_division, 7, 0, 3),
    SIDEWINDER(R.string.maze_algorithm_name_sidewinder, 8, 1, 4),
    WILSON(R.string.maze_algorithm_name_wilson, 9, 0, 7);

    private final int LOCAL_NAME;
    private final int ID;
    private final int ADDITIONAL_PARAMETERS;
    private final int ORDER;

    MazeAlgorithm(int localName, int id, int additionalParameters, int order) {
        LOCAL_NAME = localName;
        ID = id;
        ADDITIONAL_PARAMETERS = additionalParameters;
        ORDER = order;
    }
    public String getLocalName(Context context) {
        return context.getResources().getString(LOCAL_NAME);
    }

    public int getID() {
        return ID;
    }

    public int getAdditionalParameters() {
        return ADDITIONAL_PARAMETERS;
    }

    public BasicMazeGenerator createMazeGenerator(MazeDirection... directions) {
        switch(this) {
            case ALDOUS_BRODER:
                return new AldousBroder();
            case BINARY_TREE:
                return new BinaryTree(directions[0], directions[1]);
            case ELLER:
                return new Eller();
            case HUNT_AND_KILL:
                return new HuntAndKill();
            case KRUSKAL:
                return new Kruskal();
            case PRIM:
                return new Prim();
            case RECURSIVE_BACKTRACKER:
                return new RecursiveBacktracker();
            case RECURSIVE_DIVISION:
                return new RecursiveDivision();
            case SIDEWINDER:
                return new Sidewinder(directions[0]);
            case WILSON:
                return new Wilson();
            default:
                return null;
        }
    }

    public static String[] getNames(Context context) {
        return getNames(context, false);
    }

    public static String[] getNames(Context context, boolean includeRandom) {
        MazeAlgorithm[] a = MazeAlgorithm.values();
        String[] s = new String[a.length + (includeRandom ? 1 : 0)];
        for (int i = 0; i < a.length; i++) {
            s[i] = a[i].getLocalName(context);
        }
        if (includeRandom) {
            s[s.length - 1] = context.getResources().getString(R.string.maze_algorithm_name_random);
        }
        return s;
    }

    public static MazeAlgorithm getByName(Context context, String name) {
        for (MazeAlgorithm a : MazeAlgorithm.values()) {
            if (name.equals(a.getLocalName(context))) {
                return a;
            }
        }
        return null;
    }

    public static MazeAlgorithm getByID(int id) {
        for (MazeAlgorithm a : MazeAlgorithm.values()) {
            if (id == a.getID()) {
                return a;
            }
        }
        return null;
    }

    public static MazeAlgorithm getByOrder(int order) {
        for (MazeAlgorithm a : MazeAlgorithm.values()) {
            if (order == a.ORDER) {
                return a;
            }
        }
        return null;
    }
}
