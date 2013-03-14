package ru.game.aurora.dialog;

import ru.game.aurora.world.World;

import java.io.Serializable;
import java.util.Map;

/**
* Created with IntelliJ IDEA.
* User: Egor.Smirnov
* Date: 12.03.13
* Time: 15:32
* To change this template use File | Settings | File Templates.
*/
public class Reply implements Serializable {

    private static final long serialVersionUID = -1616895998816949360L;
    /**
     * If this reply will be the last action of the dialog, this will be that dialog's return value
     */
    public final int returnValue;

    public final int targetStatementId;

    public final String replyText;

    /**
     * This reply will only be visible if global game state contains given global variables with given values
     */
    public final Map<String, String> replyConditions;

    public Reply(int returnValue, int targetStatementId, String replyText) {
        this.returnValue = returnValue;
        this.targetStatementId = targetStatementId;
        this.replyText = replyText;
        this.replyConditions = null;
    }

    public Reply(int returnValue, int targetStatementId, String replyText, Map<String, String> replyConditions) {
        this.returnValue = returnValue;
        this.targetStatementId = targetStatementId;
        this.replyText = replyText;
        this.replyConditions = replyConditions;
    }

    /**
     * Returns true if this dialog option is available given current world state
     */
    public boolean isVisible(World world)
    {
        if (replyConditions == null) {
            return true;
        }

        for (Map.Entry<String, String> replyCondition : replyConditions.entrySet()) {
            if (!world.getGlobalVariables().containsKey(replyCondition.getKey())) {
                return false;
            }

            Serializable val = world.getGlobalVariables().get(replyCondition.getKey());
            String desiredVal = replyCondition.getValue();
            if (desiredVal == null) {
                return true;
            }
            if ((val != null && !val.equals(desiredVal)) || (val == null)) {
                return false;
            }
        }
        return true;
    }
}
