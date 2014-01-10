package ru.game.aurora.world.generation.quest;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.*;
import ru.game.aurora.world.dungeon.DungeonMonster;
import ru.game.aurora.world.dungeon.KillAllMonstersCondition;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.planet.DungeonEntrance;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetObject;
import ru.game.aurora.world.planet.nature.AnimalSpeciesDesc;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * If player sells full info about earth to Klisk, there is some chance that they will sell this info to one of other races
 * This will lead to one of special events in solar system
 */
public class EarthInvasionGenerator implements WorldGeneratorPart
{
    private static final long serialVersionUID = 1113857719613332116L;

    public static final class RogueAltarWorker extends DungeonMonster
    {
        private static final long serialVersionUID = -2775935255374086503L;

        public RogueAltarWorker(AuroraTiledMap map, int groupId, int objectId) {
            super(map, groupId, objectId);
        }

        @Override
        public void onShotAt(World world, int damage) {
            if (getBehaviour() == AnimalSpeciesDesc.Behaviour.AGGRESSIVE) {
                return;
            }
            // make all monsters aggressive
            for (PlanetObject obj : myMap.getObjects()) {
                if (DungeonMonster.class.isAssignableFrom(obj.getClass())) {
                    ((DungeonMonster)obj).setBehaviour(AnimalSpeciesDesc.Behaviour.AGGRESSIVE);
                }
            }

            // add a victory condition
            myMap.getVictoryConditions().add(new KillAllMonstersCondition("guard"));
            world.getCurrentDungeon().getController().setSuccessListener(new IStateChangeListener() {

                private static final long serialVersionUID = 6517626927654743737L;

                @Override
                public void stateChanged(World world) {
                    world.getGlobalVariables().put("rogues_altar.result", "destroy");

                    world.getGlobalVariables().put("earth.special_dialog", Dialog.loadFromFile("dialogs/rogues_altar_destroyed.json"));
                }
            });
        }

        @Override
        public boolean canBePickedUp() {
            return true;
        }

        @Override
        public void onPickedUp(World world) {
            if (!world.getGlobalVariables().containsKey("rogues_altar.earth_communicated")) {
                world.getGlobalVariables().put("rogues_altar.moon_checked", true);
                // player has not yet received task to settle things down
                world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/rogues_altar_1.json"));
                return;
            }

            final Dialog d = Dialog.loadFromFile("dialogs/encounters/rogues_altar_2.json");
            d.setListener(new DialogListener() {
                private static final long serialVersionUID = 7809964677347861595L;

                @Override
                public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                    if (returnCode == 1) {
                        // player decided to fight rogues, make them aggressive
                        onShotAt(world, 0);
                    }
                }
            });
            world.addOverlayWindow(d);

        }
    }

    private static final class RogueInvasionAdder extends GameEventListener
    {
        private static final long serialVersionUID = -2497678330932578786L;

        private int count = 0;

        @Override
        public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
            final AlienRace humanity = world.getRaces().get(HumanityGenerator.NAME);
            if (ss == humanity.getHomeworld()) {
                ++count;
            } else {
                return true;
            }

            if (count < 4 || world.getGlobalVariables().containsKey("earth.special_dialog")) {
                return true;
            }
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/rogues_altar_scientist.json"));

            Dialog earthDialog = Dialog.loadFromFile("dialogs/encounters/rogues_altar_earth.json");

            earthDialog.setListener(new DialogListener() {

                private static final long serialVersionUID = -6367061348256715021L;

                @Override
                public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                    if (flags.containsKey("klisk.philosophy_research")) {
                        // add new research of a klisk philosophy
                    }
                    world.getGlobalVariables().put("rogues_altar.earth_communicated", true);
                }
            });

            world.getGlobalVariables().put("earth.special_dialog", earthDialog);

            // now create dungeon on a moon

            Planet moon = (Planet) humanity.getHomeworld().getPlanets()[2].getSatellites().get(0);

            DungeonEntrance entrance = new DungeonEntrance(moon, 5, 5, "rogues_altar", new Dungeon(world, new AuroraTiledMap("maps/rogue_altar.tmx"), moon));
            moon.setNearestFreePoint(entrance, 5, 5);
            moon.getMap().getObjects().add(entrance);

            return false;
        }
    }

    @Override
    public void updateWorld(World world) {
        if (CommonRandom.getRandom().nextBoolean()) {
            // nothing happens
            //return;
        }

        world.addListener(new RogueInvasionAdder());
    }
}
