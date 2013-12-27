package ru.game.aurora.tools.dialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.Reply;
import ru.game.aurora.dialog.Statement;

/**
 * Checks most common dialog-related errors
 * Prints output to log
 */
public class DialogValidator
{
    private static final Logger logger = LoggerFactory.getLogger(DialogValidator.class);

    public static boolean validate(Dialog dialog)
    {
        boolean rz = true;

        boolean hasExitStatement = false;

        for (Statement st: dialog.getStatements().values()) {
            if (st.replies.length == 0) {
                logger.error("Statement {} has no replies", st);
                rz = false;
            }

            for (Reply reply : st.replies) {
                if (!dialog.getStatements().containsKey(reply.targetStatementId)) {
                    if (reply.targetStatementId >= 0 ) {
                        logger.error("Reply for statement {} contains link to statement {} which is >= 0 and does not exist", st, reply.targetStatementId);
                        rz = false;
                    } else {
                        hasExitStatement = true;
                    }
                }
            }
        }

        if (!hasExitStatement) {
            logger.error("Dialog does not have any possible way of termination");
            return false;
        }

        return rz;
    }
}
