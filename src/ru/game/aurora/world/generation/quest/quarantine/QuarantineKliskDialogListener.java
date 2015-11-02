package ru.game.aurora.world.generation.quest.quarantine;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.player.Resources;
import ru.game.aurora.world.World;

import java.util.Map;

/**
 * Klisk can sell (or give for free in case of good rep) a cure
 */
public class QuarantineKliskDialogListener implements DialogListener {
    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (returnCode == 2) {
            // klisk give medicine for free
            world.getPlayer().getJournal().addQuestEntries("quarantine", "klisk_get");
            QuarantineQuest.endQuest(world);
        } else if (returnCode == 1) {
            // attempt to buy
            if (world.getPlayer().getCredits() >= 10) {
                world.getPlayer().changeResource(world, Resources.CREDITS, -10);
                world.getPlayer().getJournal().addQuestEntries("quarantine", "klisk_buy");
                QuarantineQuest.endQuest(world);
            } else {
                GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.not_enough_credits"));
            }
        }
    }
}
