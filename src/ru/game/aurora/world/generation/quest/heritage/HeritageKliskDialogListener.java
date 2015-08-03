package ru.game.aurora.world.generation.quest.heritage;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.player.Resources;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;

import java.util.Map;

/**
 */
public class HeritageKliskDialogListener implements DialogListener {

    private static class KliskMutantCrewMember extends CrewMember {

        public KliskMutantCrewMember() {
            super("klisk_mutant", "klisk_mutant_dialog");
        }

        @Override
        public void onAdded(World world) {
            world.getPlayer().updateResearchMultiplier(1.1);
        }

        @Override
        public void onRemoved(World world) {
            world.getPlayer().updateResearchMultiplier(10.0 / 11.0);
        }
    }


    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (flags.containsKey("second_paid")) {
            world.getPlayer().changeResource(world, Resources.CREDITS, 10);
            world.getGlobalVariables().remove("heritage.second_monster_killed");
        }
        if (flags.containsKey("third_paid")) {
            world.getPlayer().changeResource(world, Resources.CREDITS, 10);
            world.getGlobalVariables().remove("heritage.third_monster_killed");
        }
        if (flags.containsKey("fourth_paid")) {
            world.getPlayer().changeResource(world, Resources.CREDITS, 10);
            world.getGlobalVariables().remove("heritage.fourth_monster_killed");
        }

        if (returnCode == 20) {
            world.getPlayer().getJournal().questCompleted("heritage", "end_give_to_klisk");
            world.getPlayer().changeResource(world, Resources.CREDITS, 15);
            world.getReputation().updateReputation(KliskGenerator.NAME, HumanityGenerator.NAME, 3);
            world.getGlobalVariables().put("heritage.quest_completed", true);

        } else if (returnCode == 30) {
            world.getPlayer().getJournal().questCompleted("heritage", "end_save");
            world.getReputation().updateReputation(KliskGenerator.NAME, HumanityGenerator.NAME, -2);
            world.getGlobalVariables().put("heritage.quest_completed", true);
            world.getPlayer().getShip().addCrewMember(world, new KliskMutantCrewMember());
        } else if (returnCode == 10) {
            world.getPlayer().getJournal().questCompleted("heritage", "end_lie");
            world.getGlobalVariables().put("heritage.quest_completed", true);
            world.getPlayer().getShip().addCrewMember(world, new KliskMutantCrewMember());
        }
    }
}
