package ru.game.aurora.world.generation.aliens.bork;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.world.World;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 07.03.14
 * Time: 22:07
 */
public class BorkDialogListener implements DialogListener {
    private static final long serialVersionUID = -6184207223388040791L;

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (flags.containsKey("bork_blockade.withdraw")) {
            world.getGlobalVariables().put("bork_blockade.withdraw", true);
            flags.remove("bork_blockade.withdraw");
        }

        if (flags.containsKey("bork.war_help")) {
            world.getGlobalVariables().put("bork.war_help", true);
            flags.remove("bork.war_help");
        }

        if (flags.containsKey("zorsan_escape_discussed") && !world.getPlayer().getJournal().getQuests().get("zorsan_relations").contains("bork_info")) {
            world.getPlayer().getJournal().addQuestEntries("zorsan_relations", "bork_info");
            flags.remove("zorsan_escape_discussed");
        }
    }
}
