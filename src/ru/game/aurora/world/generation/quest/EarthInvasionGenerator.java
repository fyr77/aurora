package ru.game.aurora.world.generation.quest;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.shipai.LeaveSystemAI;
import ru.game.aurora.world.*;
import ru.game.aurora.world.dungeon.DungeonMonster;
import ru.game.aurora.world.dungeon.KillAllMonstersCondition;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.planet.DungeonEntrance;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetObject;
import ru.game.aurora.world.planet.nature.AnimalSpeciesDesc;
import ru.game.aurora.world.quest.JournalEntry;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.SpaceObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * If player sells full info about earth to Klisk, there is some chance that they will sell this info to one of other races
 * This will lead to one of special events in solar system
 */
public class EarthInvasionGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = 1113857719613332116L;

    public static final class RogueAltarWorker extends DungeonMonster {
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
                    ((DungeonMonster) obj).setBehaviour(AnimalSpeciesDesc.Behaviour.AGGRESSIVE);
                }
            }

            // add a victory condition
            myMap.getVictoryConditions().add(new KillAllMonstersCondition("guard"));
            world.getCurrentDungeon().getController().setSuccessListener(new IStateChangeListener() {

                private static final long serialVersionUID = 6517626927654743737L;

                @Override
                public void stateChanged(World world) {
                    world.getGlobalVariables().put("rogues_altar.result", "destroy");

                    world.getPlayer().getEarthState().getEarthSpecialDialogs().add(Dialog.loadFromFile("dialogs/rogues_altar_destroyed.json"));
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

    private static abstract class BaseInvasionQuestListener extends GameEventListener {
        private static final long serialVersionUID = 8170368564115054199L;

        private int count = 0;

        @Override
        public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
            final AlienRace humanity = world.getRaces().get(HumanityGenerator.NAME);
            if (ss == humanity.getHomeworld()) {
                ++count;
            } else {
                return true;
            }

            return count < 4 || !world.getPlayer().getEarthState().getEarthSpecialDialogs().isEmpty() || process(world, ss);

        }

        protected abstract boolean process(World world, StarSystem ss);
    }

    private static final class RogueInvasionAdder extends BaseInvasionQuestListener {
        private static final long serialVersionUID = -2497678330932578786L;

        @Override
        public boolean process(World world, StarSystem ss) {

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

            world.getPlayer().getEarthState().getEarthSpecialDialogs().add(earthDialog);

            // now create dungeon on a moon

            Planet moon = (Planet) ss.getPlanets()[2].getSatellites().get(0);

            DungeonEntrance entrance = new DungeonEntrance(moon, 5, 5, "rogues_altar", new Dungeon(world, new AuroraTiledMap("maps/rogue_altar.tmx"), moon));
            moon.setNearestFreePoint(entrance, 5, 5);
            moon.getMap().getObjects().add(entrance);

            return false;
        }
    }

    private static final class KliskTradeProbe extends NPCShip {
        private static final long serialVersionUID = -8830393993027489642L;

        public KliskTradeProbe(int x, int y, AlienRace klisk) {
            super(x, y, "klisk_drone", klisk, null, "Klisk trade probe");

            Dialog commDialog = Dialog.loadFromFile("dialogs/encounters/klisk_trade_probe_comm.json");
            commDialog.setListener(new DialogListener() {

                private static final long serialVersionUID = 6759215425541397109L;

                @Override
                public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                    world.getGlobalVariables().put("klisk_trader_drone.communicated", true);
                    JournalEntry klisk_trade_drone = world.getPlayer().getJournal().getQuests().get("klisk_trade_drone");

                    klisk_trade_drone.addMessage("drone_desc");
                    klisk_trade_drone.addMessage("task");
                    if (returnCode == 1) {
                        //todo: finish this quest by following the drone
                        setAi(new LeaveSystemAI());
                        klisk_trade_drone.addMessage("buyAll");
                    }

                }
            });

            setCaptain(new NPC(commDialog));

            setWeapons(ResourceManager.getInstance().getWeapons().getEntity("klisk_large_laser"));
        }

        @Override
        public void onAttack(World world, SpaceObject attacker, int dmg) {
            super.onAttack(world, attacker, dmg);
            if (hp <= 0) {
                world.getGlobalVariables().put("klisk_trader_drone.result", "destroy");
                world.getPlayer().getEarthState().getEarthSpecialDialogs().add(Dialog.loadFromFile("dialogs/encounters/klisk_trade_probe_earth_2.json"));
                world.getPlayer().getJournal().getQuests().get("klisk_trade_drone").addMessage("destroyed");
            }
        }
    }

    public static final class KliskTraderAdder extends BaseInvasionQuestListener {
        private static final long serialVersionUID = -5491271253252252436L;

        @Override
        protected boolean process(World world, StarSystem ss) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/klisk_trade_probe_enter.json"));
            world.getPlayer().getJournal().addQuest(new JournalEntry("klisk_trade_drone", "start"));

            Dialog earthDialog = Dialog.loadFromFile("dialogs/encounters/klisk_trade_probe_earth.json");
            earthDialog.setListener(new DialogListener() {
                private static final long serialVersionUID = 6759215425541397109L;

                @Override
                public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                    JournalEntry klisk_trade_drone = world.getPlayer().getJournal().getQuests().get("klisk_trade_drone");
                    klisk_trade_drone.addMessage("drone_desc");
                    klisk_trade_drone.addMessage("problem");
                }
            });
            world.getPlayer().getEarthState().getEarthSpecialDialogs().add(earthDialog);

            KliskTradeProbe probe = new KliskTradeProbe(ss.getPlanets()[2].getX() - 1, ss.getPlanets()[2].getY() - 1, world.getRaces().get(KliskGenerator.NAME));
            ss.getShips().add(probe);
            return true;
        }
    }

    @Override
    public void updateWorld(World world) {
        if (CommonRandom.getRandom().nextBoolean()) {
            world.addListener(new RogueInvasionAdder());
        } else {
            world.addListener(new KliskTraderAdder());
        }
    }
}
