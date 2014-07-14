/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 13.07.14
 * Time: 16:35
 */

package ru.game.aurora.world.generation.aliens.bork;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.shipai.CombatAI;
import ru.game.aurora.npc.shipai.LeaveSystemAI;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;


public class FamilyProblemsEventGenerator extends GameEventListener implements WorldGeneratorPart, DialogListener {
    private static final long serialVersionUID = -5462789598982608516L;

    private final double chance;

    private NPCShip smartBrotherShip;

    private NPCShip stupidBrotherShip;

    private NPCShip fatherShip;

    int dialogResult = -1;

    public FamilyProblemsEventGenerator() {
        chance = Configuration.getDoubleProperty("quest.bork_family_problems");
        setGroups(EventGroup.ENCOUNTER_SPAWN);
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (ss.isQuestLocation() || CommonRandom.getRandom().nextDouble() > chance) {
            return false;
        }
        smartBrotherShip = world.getRaces().get(BorkGenerator.NAME).getDefaultFactory().createShip(world, 0);
        smartBrotherShip.setPos(-1, 2);
        stupidBrotherShip = world.getRaces().get(BorkGenerator.NAME).getDefaultFactory().createShip(world, 0);
        stupidBrotherShip.setPos(-1, 4);
        fatherShip = new BorkFatherShip(1, 3, "bork_ship_large", "Fathers ship", 20);
        fatherShip.setStationary(true);
        ss.getShips().add(smartBrotherShip);
        ss.getShips().add(stupidBrotherShip);
        ss.getShips().add(fatherShip);

        Dialog d = Dialog.loadFromFile("dialogs/encounters/family_problems.json");
        d.addListener(this);
        NPC captain = new NPC(d);
        smartBrotherShip.setCaptain(captain);
        stupidBrotherShip.setCaptain(captain);

        return true;
    }

    @Override
    public boolean onPlayerLeftStarSystem(World world, StarSystem ss) {
        if (fatherShip == null) {
            return false;
        }

        ss.getShips().remove(fatherShip);
        ss.getShips().remove(smartBrotherShip);
        ss.getShips().remove(stupidBrotherShip);
        isAlive = false;
        return true;
    }

    @Override
    public void updateWorld(World world) {
        world.addListener(this);
    }

    private final class BorkFatherShip extends NPCShip implements DialogListener {
        private static final long serialVersionUID = -4853984564070741226L;

        private boolean communicated = false;

        public BorkFatherShip(int x, int y, String sprite, String name, int hp) {
            super(x, y, sprite, null, null, name, hp);
            Dialog d = Dialog.loadFromFile("dialogs/encounters/family_problems_ship.json");
            d.addListener(this);
            setCaptain(new NPC(d));
        }

        @Override
        public boolean isCanBeHailed() {
            return !communicated
                    && (dialogResult == 5 || dialogResult == 6 ||
                    (!stupidBrotherShip.isAlive() && !smartBrotherShip.isAlive()));
        }

        @Override
        public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
            world.getPlayer().setResourceUnits(world.getPlayer().getResourceUnits() + 10);
            communicated = true;
        }
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        // 1 - leaving them to fight each other
        // 0 - they attack player
        // 2 - they leave, and the ship is destroyed
        // 3 - stupid brother wins ship
        // 4 - intelligent brother wins ship
        // 5 - they leave, and the ship is left to player
        // 6 - they leave, the ship is left to player, as well as some loot and money
        switch (returnCode) {
            case 1:
                smartBrotherShip.setAi(new CombatAI(stupidBrotherShip));
                smartBrotherShip.getThreatMap().put(stupidBrotherShip, 100);
                stupidBrotherShip.getThreatMap().put(smartBrotherShip, 100);
                stupidBrotherShip.setAi(new CombatAI(smartBrotherShip));
                break;
            case 0:
                smartBrotherShip.setAi(new CombatAI(world.getPlayer().getShip()));
                stupidBrotherShip.setAi(new CombatAI(world.getPlayer().getShip()));
                smartBrotherShip.getThreatMap().put(world.getPlayer().getShip(), 100);
                stupidBrotherShip.getThreatMap().put(world.getPlayer().getShip(), 100);
                break;
            case 2:
                fatherShip.explode(world.getCurrentStarSystem());
                smartBrotherShip.setAi(new LeaveSystemAI());
                stupidBrotherShip.setAi(new LeaveSystemAI());
                world.getReputation().updateReputation(BorkGenerator.NAME, HumanityGenerator.NAME, 1);
                break;
            case 3:
                smartBrotherShip.setAi(new LeaveSystemAI());
                world.getReputation().updateReputation(BorkGenerator.NAME, HumanityGenerator.NAME, 1);
                break;
            case 4:
                stupidBrotherShip.setAi(new LeaveSystemAI());
                world.getReputation().updateReputation(BorkGenerator.NAME, HumanityGenerator.NAME, 1);
                break;
            case 5:
                smartBrotherShip.setAi(new LeaveSystemAI());
                stupidBrotherShip.setAi(new LeaveSystemAI());
                world.getReputation().updateReputation(BorkGenerator.NAME, HumanityGenerator.NAME, 1);
                break;
            case 6:
                smartBrotherShip.setAi(new LeaveSystemAI());
                stupidBrotherShip.setAi(new LeaveSystemAI());
                world.getPlayer().changeCredits(world, 5);
                world.getReputation().updateReputation(BorkGenerator.NAME, HumanityGenerator.NAME, 1);
                break;

        }
        finalizeShips();
        dialogResult = returnCode;
    }

    private void finalizeShips() {
        smartBrotherShip.setCanBeHailed(false);
        stupidBrotherShip.setCanBeHailed(false);
    }
}
