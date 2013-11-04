package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 04.11.13
 * Time: 16:57
 */
public class RoguesMainDialogListener implements DialogListener {
    private static final long serialVersionUID = 7523069730734608685L;

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode) {
        if (returnCode == 100) {
            // pay fine
            int fine = (int) world.getGlobalVariables().get("rogues.fine");
            if (world.getPlayer().getCredits() < fine) {
                GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.not_enough_credits"));
                return;
            }

            world.getPlayer().changeCredits(-fine);
            world.getGlobalVariables().remove("rogues.fine");
        }
    }
}
