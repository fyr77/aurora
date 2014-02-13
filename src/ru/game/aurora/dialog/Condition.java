/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 19.08.13
 * Time: 17:52
 */

package ru.game.aurora.dialog;

import ru.game.aurora.world.World;

import java.io.Serializable;
import java.util.Map;

public class Condition implements Serializable {
    private static final long serialVersionUID = 1L;

    public static enum ConditionType {
        SET,
        NOT_SET,
        EQUAL,
        NOT_EQUAL,
        LESS,
        GREATER
    }

    public final String name;

    public final String value;

    public final ConditionType type;

    public Condition(String name, String value, ConditionType type) {
        this.name = name.trim();
        this.value = (value != null) ? value.trim() : null;
        this.type = type;
    }

    public boolean isMet(World world, Map<String, String> flags) {
        Object val = flags.get(name);
        if (val == null) {
            val = world.getGlobalVariables().get(name);
        }
        switch (type) {
            case SET:
                return val != null;
            case NOT_SET:
                return val == null;
            case EQUAL:
                return (val != null && val.equals(value));
            case NOT_EQUAL:
                return (val == null || !val.equals(value));
            case GREATER: {
                if (val == null) {
                    return false;
                }
                int worldValue = Integer.parseInt((String) val);
                int myValue = Integer.parseInt(value);
                return worldValue > myValue;
            }
            case LESS: {
                if (val == null) {
                    return false;
                }
                int worldValue = Integer.parseInt((String) val);
                int myValue = Integer.parseInt(value);
                return worldValue < myValue;
            }
            default:
                throw new IllegalArgumentException();
        }
    }
}
