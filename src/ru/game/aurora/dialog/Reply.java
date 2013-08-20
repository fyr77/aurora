package ru.game.aurora.dialog;

import ru.game.aurora.world.World;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 12.03.13
 * Time: 15:32
 */
public class Reply implements Serializable {

    private static final long serialVersionUID = -1616895998816949360L;
    /**
     * If this reply will be the last action of the dialog, this will be that dialog's return value
     */
    public final int returnValue;

    public final int targetStatementId;

    public final String replyText;

    public final Condition[] replyConditions;

    public Reply(int returnValue, int targetStatementId, String replyText) {
        this.returnValue = returnValue;
        this.targetStatementId = targetStatementId;
        this.replyText = replyText;
        this.replyConditions = null;
    }

    public Reply(int returnValue, int targetStatementId, String replyText, Condition[] replyConditions) {
        this.returnValue = returnValue;
        this.targetStatementId = targetStatementId;
        this.replyText = replyText;
        this.replyConditions = replyConditions;
    }

    /**
     * Returns true if this dialog option is available given current world state
     */
    public boolean isVisible(World world) {
        if (replyConditions == null) {
            return true;
        }

        for (Condition condition : replyConditions) {
            if (!condition.isMet(world)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method is used to draw element string representation in GUI, do not modify
     */
    @Override
    public String toString() {
        return replyText;
    }
}
