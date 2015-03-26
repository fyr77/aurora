/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.03.13
 * Time: 14:07
 */

package ru.game.aurora.player.earth;

import ru.game.aurora.application.Localization;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.common.ItemWithTextAndImage;
import ru.game.aurora.world.World;

import java.util.Date;

public class PrivateMessage extends ItemWithTextAndImage {
    private static final long serialVersionUID = 1L;

    private boolean isRead;

    private String receivedAt;

    private String sender;

    public PrivateMessage(World world, String id, String icon) {
        super(id, new Drawable(icon));
        if (Localization.getText("private_messages", id + ".sender" ) != null) {
            sender = id + ".sender";
        }
        receivedAt = world.getCurrentDateString();
    }

    public PrivateMessage(World world, String senderId, String id, String icon) {
        super(id, new Drawable(icon));
        this.sender = senderId;
        receivedAt = world.getCurrentDateString();
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getReceivedAt() {
        return receivedAt != null ? receivedAt : "";
    }

    public String getSender() {
        return sender != null ? Localization.getText("private_messages", sender) : "<hidden>";
    }

    // used for converting representation in gui, do not modify
    @Override
    public String toString() {
        return getLocalizedName("private_messages") + (!isRead ? " (NEW)" : "");
    }
}
