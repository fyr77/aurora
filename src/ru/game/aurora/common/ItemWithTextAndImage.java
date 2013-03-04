/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.03.13
 * Time: 13:57
 */
package ru.game.aurora.common;

import java.io.Serializable;

public class ItemWithTextAndImage implements Serializable
{
    private static final long serialVersionUID = 6564450700449963202L;

    protected final String name;

    protected final String text;

    protected final String icon;

    public ItemWithTextAndImage(String name, String text, String icon) {
        this.name = name;
        this.text = text;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getIcon() {
        return icon;
    }
}
