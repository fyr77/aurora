package ru.game.aurora.dialog;

import ru.game.aurora.world.World;

import java.io.Serializable;
import java.util.Map;

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

    private Condition[] replyConditions;

    public final Map<String, String> flags;

    public Reply(int returnValue, int targetStatementId, String replyText) {
        this.returnValue = returnValue;
        this.targetStatementId = targetStatementId;
        this.replyText = replyText;
        this.replyConditions = null;
        this.flags = null;
    }

    public Reply(int returnValue, int targetStatementId, String replyText, Condition[] replyConditions, Map<String, String> flags) {
        this.returnValue = returnValue;
        this.targetStatementId = targetStatementId;
        this.replyText = replyText;
        this.replyConditions = replyConditions;
        this.flags = flags;
    }

    /**
     * Returns true if this dialog option is available given current world state
     */
    public boolean isVisible(World world, Map<String, String> flags) {
        if (replyConditions == null) {
            return true;
        }

        for (Condition condition : replyConditions) {
            if (!condition.isMet(world, flags)) {
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

    public Condition [] getReplyConditions(){
        return this.replyConditions;
    }

    public void setReplyConditions(Condition [] conditions){
        this.replyConditions = conditions;
    }
}
