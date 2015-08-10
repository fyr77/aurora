package ru.game.aurora.application;

import org.newdawn.slick.Input;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains input binding
 */
public class InputBinding {

    public static final String key = "inputBinding";

    public static Map<Action, Integer> keyBinding = new HashMap<>();

    public enum Action
    {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        INTERACT,
        SHOOT,
        WEAPON_1,
        WEAPON_2,
        WEAPON_3,
        WEAPON_4
    }

    public static Action[] weapons = new Action[] {Action.WEAPON_1, Action.WEAPON_2, Action.WEAPON_3, Action.WEAPON_4};

    public static void setDefault()
    {
        keyBinding.put(Action.LEFT, Input.KEY_LEFT);
        keyBinding.put(Action.RIGHT, Input.KEY_RIGHT);
        keyBinding.put(Action.UP, Input.KEY_UP);
        keyBinding.put(Action.DOWN, Input.KEY_DOWN);
        keyBinding.put(Action.INTERACT, Input.KEY_SPACE);
        keyBinding.put(Action.SHOOT, Input.KEY_F);
        keyBinding.put(Action.WEAPON_1, Input.KEY_1);
        keyBinding.put(Action.WEAPON_2, Input.KEY_2);
        keyBinding.put(Action.WEAPON_3, Input.KEY_3);
        keyBinding.put(Action.WEAPON_4, Input.KEY_4);
    }

    public static String saveToString()
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Action, Integer> entry : keyBinding.entrySet()) {
            sb.append(entry.getKey().name()).append("=").append(entry.getValue()).append(';');
        }
        return sb.toString();
    }

    public static void loadFromString(String s) {
        try {
            String[] parts = s.split(";");
            for (String part : parts) {
                String[] parts2 = part.split("=");
                keyBinding.put(Action.valueOf(parts2[0]), Integer.parseInt(parts2[1]));
            }

            if (keyBinding.size() != Action.values().length) {
                LoggerFactory.getLogger(InputBinding.class).error("Input binding string does not contain values for all inputs," +
                        " reverting to a default one");
                setDefault();
            }
        } catch (Exception ex) {
            LoggerFactory.getLogger(InputBinding.class).error("Failed to read input binding, will use a default one", ex);
            setDefault();
        }
    }
}
