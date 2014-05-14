/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.03.13
 * Time: 13:57
 */
package ru.game.aurora.common;

import ru.game.aurora.application.Localization;

import java.io.Serializable;

public class ItemWithTextAndImage implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final String id;

    protected final String icon;

    public ItemWithTextAndImage(String id, String icon) {
        this.id = id;
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public String getId() {
        return id;
    }

    public String getLocalizedName(String group) {
        return Localization.getText(group, id + ".name");
    }

    public String getLocalizedText(String group) {
        return Localization.getText(group, id + ".desc");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemWithTextAndImage that = (ItemWithTextAndImage) o;

        return !(icon != null ? !icon.equals(that.icon) : that.icon != null) && !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (icon != null ? icon.hashCode() : 0);
        return result;
    }
}
