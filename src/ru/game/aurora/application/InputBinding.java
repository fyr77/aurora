package ru.game.aurora.application;

import org.newdawn.slick.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

/**
 * Contains input binding
 */
public class InputBinding {

    public static final String key = "inputBinding";

    public static Map<Action, Integer> keyBinding = new EnumMap<Action, Integer>(Action.class);

    public enum Action {
        LEFT, LEFT_SECONDARY,
        RIGHT, RIGHT_SECONDARY,
        UP, UP_SECONDARY,
        DOWN, DOWN_SECONDARY,
        INTERACT,
        SHOOT,
        WEAPON_1,
        WEAPON_2,
        WEAPON_3,
        WEAPON_4,
        ENGINEERING,
        RESEARCH,
        LANDING_PARTY,
        MAP,
        INVENTORY,
        JOURNAL,
        SCAN;

        @Override
        public String toString() {
            return Localization.getText("gui", "input_binding." + name().toLowerCase());
        }
    }

    public static Action[] weapons = new Action[] {Action.WEAPON_1, Action.WEAPON_2, Action.WEAPON_3, Action.WEAPON_4};

    public static void useDefaultBinding() {
        keyBinding = createDefaultBinding();
    }

    public static Map<Action, Integer> createDefaultBinding() {
        Map<Action, Integer> defaultBinding = new EnumMap<Action, Integer>(Action.class);
        defaultBinding.put(Action.LEFT, Input.KEY_A);
        defaultBinding.put(Action.LEFT_SECONDARY, Input.KEY_LEFT);
        defaultBinding.put(Action.RIGHT, Input.KEY_D);
        defaultBinding.put(Action.RIGHT_SECONDARY, Input.KEY_RIGHT);
        defaultBinding.put(Action.UP, Input.KEY_W);
        defaultBinding.put(Action.UP_SECONDARY, Input.KEY_UP);
        defaultBinding.put(Action.DOWN, Input.KEY_S);
        defaultBinding.put(Action.DOWN_SECONDARY, Input.KEY_DOWN);
        defaultBinding.put(Action.INTERACT, Input.KEY_SPACE);
        defaultBinding.put(Action.SHOOT, Input.KEY_F);
        defaultBinding.put(Action.WEAPON_1, Input.KEY_1);
        defaultBinding.put(Action.WEAPON_2, Input.KEY_2);
        defaultBinding.put(Action.WEAPON_3, Input.KEY_3);
        defaultBinding.put(Action.WEAPON_4, Input.KEY_4);
        defaultBinding.put(Action.ENGINEERING, Input.KEY_E);
        defaultBinding.put(Action.RESEARCH, Input.KEY_R);
        defaultBinding.put(Action.LANDING_PARTY, Input.KEY_L);
        defaultBinding.put(Action.MAP, Input.KEY_M);
        defaultBinding.put(Action.INVENTORY, Input.KEY_I);
        defaultBinding.put(Action.JOURNAL, Input.KEY_J);
        defaultBinding.put(Action.SCAN, Input.KEY_C);
        return defaultBinding;
    }

    public static String saveToString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Action, Integer> entry : keyBinding.entrySet()) {
            sb.append(entry.getKey().name()).append("=").append(entry.getValue()).append(';');
        }
        return sb.toString();
    }

    public static void loadFromString(String s) {
        final Logger logger = LoggerFactory.getLogger(InputBinding.class);
        try {
            String[] parts = s.split(";");
            for (String part : parts) {
                String[] parts2 = part.split("=");
                keyBinding.put(Action.valueOf(parts2[0]), Integer.parseInt(parts2[1]));
            }

            if (keyBinding.size() != Action.values().length) {
                logger.error("Input binding string does not contain values for all inputs," +
                        " updating new inputs");
                Map<Action, Integer> defaultBinding = createDefaultBinding();
                int count = 0;
                for (Map.Entry<Action, Integer> entry : defaultBinding.entrySet()) {
                    if (!keyBinding.containsKey(entry.getKey())) {
                        keyBinding.put(entry.getKey(), entry.getValue());
                        ++count;
                    }
                }
                logger.info("Added {} new key bindings", count);

                s = InputBinding.saveToString();
                Configuration.getSystemProperties().put(InputBinding.key, s);
                Configuration.saveSystemProperties();
            }
        } catch (Exception ex) {
            logger.error("Failed to read input binding, will use a default one", ex);
            createDefaultBinding();
        }
    }
}
