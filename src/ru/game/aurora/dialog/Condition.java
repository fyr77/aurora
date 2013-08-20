/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 19.08.13
 * Time: 17:52
 */

package ru.game.aurora.dialog;

import ru.game.aurora.world.World;

import java.io.Serializable;

public class Condition implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static enum ConditionType
    {
        SET,
        NOT_SET,
        EQUAL,
        NOT_EQUAL
    }

    public final String name;

    public final String value;

    public final ConditionType type;

    public Condition(String name, String value, ConditionType type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public boolean isMet(World world)
    {
        Object val = world.getGlobalVariables().get(name);
        switch (type) {
            case SET:
                return val != null;
            case NOT_SET:
                return val == null;
            case EQUAL:
                return (val != null && val.equals(value));
            case NOT_EQUAL:
                return (val == null || !val.equals(value));
            default:
                throw new IllegalArgumentException();
        }
    }
}