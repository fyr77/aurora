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

    public enum ConditionType {
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
                // do note check val for null, as there can be a null mapping for this name
                return flags.containsKey(name) || world.getGlobalVariables().containsKey(name);
            case NOT_SET:
                return !flags.containsKey(name) && !world.getGlobalVariables().containsKey(name);
            case EQUAL:
                return (val != null && val.toString().equals(value));
            case NOT_EQUAL:
                return (val == null || !val.toString().equals(value));
            case GREATER: {
                if (val == null) {
                    return false;
                }
                int worldValue = Integer.parseInt(val.toString());
                int myValue = Integer.parseInt(value);
                return worldValue > myValue;
            }
            case LESS: {
                if (val == null) {
                    return false;
                }
                int worldValue = Integer.parseInt(val.toString());
                int myValue = Integer.parseInt(value);
                return worldValue < myValue;
            }
            default:
                throw new IllegalArgumentException();
        }
    }
}
