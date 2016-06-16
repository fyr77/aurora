package ru.game.aurora.world.generation.quest.ambush;

import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.shipai.CombatAI;
import ru.game.aurora.npc.shipai.LandAI;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * Created by User on 15.06.2016.
 * Created after Zorsan esape.
 * Player meets a Bork that asks for help as his family was kidnapped.
 * After player agrees to follow him he leads player into a zorsan trap
 */
public class AmbushQuest extends GameEventListener implements DialogListener {
    private static final long serialVersionUID = 1L;

    private double countdown;

    /**
     * 0 - waiting for quest to start
     * 1 - in the next star system player will meet a bork ship
     * 2 - bork ship spawned
     */
    private int state = 0;

    private NPCShip borkShip;

    public AmbushQuest() {
        setGroups(EventGroup.ENCOUNTER_SPAWN);
        countdown = Configuration.getIntProperty("quest.ambush.countdown");
    }

    @Override
    public boolean onTurnEnded(World world) {
        countdown -= world.getCurrentRoom().getTurnToDayRelation();
        if (state == 0 && countdown <= 0) {
            state = 1;
        }

        if (state == 2 && borkShip.getDistance(world.getPlayer().getShip()) <= 3) {
            GameLogger.getInstance().logMessage(Localization.getText("journal", "ambush.communicate"));
        }
        return false;
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (ss.isQuestLocation()) {
            return false;
        }
        if (state == 1 && CommonRandom.getRandom().nextDouble() < Configuration.getDoubleProperty("quest.ambush.chance")) {

            Dialog borkDialog = Dialog.loadFromFile("dialogs/encounters/ambush_bork.json");
            borkDialog.addListener(this);

            borkShip = new NPCShip(1, 1, "bork_ship_large", world.getFactions().get(BorkGenerator.NAME), new NPC(borkDialog), "Bork Ship", 15);
            borkShip.setSpeed(1);
            borkShip.setWeapons(
                    ResourceManager.getInstance().getWeapons().getEntity("bork_cannon")
                    , ResourceManager.getInstance().getWeapons().getEntity("bork_missiles")
                    , ResourceManager.getInstance().getWeapons().getEntity("zorsan_cannon")
            );
            borkShip.enableRepairs(4);
            borkShip.setAi(new LandAI(world.getPlayer().getShip()));

            state = 2;
            return true;
        }

        return false;
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (returnCode == 0) {
            // player refused to help, bork attacks
            borkShip.setHostile(true);
            borkShip.setAi(new CombatAI(world.getPlayer().getShip()));
        }
    }
}
