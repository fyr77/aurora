/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.03.13
 * Time: 14:07
 */

package ru.game.aurora.player.earth;

import ru.game.aurora.common.ItemWithTextAndImage;

public class PrivateMessage extends ItemWithTextAndImage {
    private static final long serialVersionUID = 1L;

    private boolean isRead;

    public PrivateMessage(String id, String icon) {
        super(id, icon);
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    // used for converting representation in gui, do not modify
    @Override
    public String toString() {
        return getLocalizedName("private_messages");
    }
}
