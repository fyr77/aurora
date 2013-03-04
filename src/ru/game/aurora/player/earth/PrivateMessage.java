
/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.03.13
 * Time: 14:07
 */

package ru.game.aurora.player.earth;

import ru.game.aurora.common.ItemWithTextAndImage;

public class PrivateMessage extends ItemWithTextAndImage
{
    private static final long serialVersionUID = -1550150685861280637L;

    private boolean isRead;

    public PrivateMessage(String name, String text, String icon) {
        super(name, text, icon);
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
