package ru.game.aurora.world.generation.aliens;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.shipai.LandAI;
import ru.game.aurora.player.research.ResearchProjectState;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.03.14
 * Time: 14:37
 */
public class EnergySphereEncounterGenerator extends GameEventListener implements WorldGeneratorPart {
    private static final long serialVersionUID = 6518695819057473616L;

    private double chance;

    private static class EnergySphere extends NPCShip {

        private static final long serialVersionUID = -3650001508545188680L;

        public EnergySphere(Ship playerShip) {
            super(0, 0, "energy_sphere", null, null, "Unknown", Integer.MAX_VALUE);
            setAi(new LandAI(playerShip));
            setSpeed(1);
        }

        @Override
        public void onAttack(World world, GameObject attacker, int dmg) {
            // ignores attacks
        }

        @Override
        public void interact(World world) {
            // has contacted with player
            world.getGlobalVariables().put("energy_sphere.started", "0");
            // reset all research progress
            for (ResearchProjectState rps : world.getPlayer().getResearchState().getCurrentProjects()) {
                rps.desc.update(world, -50);
            }
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/energy_sphere_attacked.json"));
            isAlive = false;

            world.getPlayer().getJournal().addQuestEntries("energy_sphere", "start");
        }

        @Override
        public void update(GameContainer container, World world) {
            super.update(container, world);
            if (getDistance(world.getPlayer().getShip()) <= 1) {
                interact(world);
            }
        }
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (ss.isQuestLocation()) {
            return false;
        }

        if (CommonRandom.getRandom().nextDouble() > chance) {
            return false;
        }

        NPCShip energySphere = new EnergySphere(world.getPlayer().getShip());
        ss.setRandomEmptyPosition(energySphere);
        ss.getShips().add(energySphere);
        isAlive = false;
        return true;
    }

    @Override
    public void updateWorld(World world) {
        chance = Configuration.getDoubleProperty("quest.energy_sphere.chance");
        world.addListener(this);
        setGroups(EventGroup.ENCOUNTER_SPAWN);
    }
}
