package ru.game.aurora.world.generation.quest;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.shipai.LeaveSystemAI;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.world.*;
import ru.game.aurora.world.dungeon.DungeonMonster;
import ru.game.aurora.world.dungeon.KillAllMonstersCondition;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.planet.DungeonEntrance;
import ru.game.aurora.world.planet.MonsterBehaviour;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.quest.JournalEntry;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * If player sells full info about earth to Klisk, there is some chance that they will sell this info to one of other races
 * This will lead to one of special events in solar system
 */
public class EarthInvasionGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = 1113857719613332116L;

    /**
     * Rogue altar event. Rogues start building an altar on the moon, to pray the Obliterator, that disturbs its gravity field.
     * User can enter this altar as a dungeon, and either kill all rogues and destroy it, or help them fix it.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static final class RogueAltarWorker extends DungeonMonster {
        private static final long serialVersionUID = -2775935255374086503L;

        public RogueAltarWorker(AuroraTiledMap map, int groupId, int objectId) {
            super(map, groupId, objectId);
        }

        @Override
        public void onAttack(World world, GameObject attacker, int damage) {
            if (getBehaviour() == MonsterBehaviour.AGGRESSIVE) {
                return;
            }
            // make all monsters aggressive
            for (GameObject obj : world.getCurrentRoom().getMap().getObjects()) {
                if (DungeonMonster.class.isAssignableFrom(obj.getClass())) {
                    ((DungeonMonster) obj).setBehaviour(MonsterBehaviour.AGGRESSIVE);
                }
            }

            // add a victory condition
            world.getCurrentRoom().getMap().getVictoryConditions().add(new KillAllMonstersCondition("guard"));
            world.getCurrentDungeon().getController().addListener(new IStateChangeListener<World>() {

                private static final long serialVersionUID = 6517626927654743737L;

                @Override
                public void stateChanged(World world) {
                    world.getGlobalVariables().put("rogues_altar.result", "destroy");
                    world.getPlayer().getJournal().addQuestEntries("rogues_altar", "destroy");
                    world.getPlayer().getEarthState().getEarthSpecialDialogs().add(Dialog.loadFromFile("dialogs/rogues_altar_destroyed.json"));
                }
            });
        }

        @Override
        public boolean canBeInteracted() {
            return true;
        }

        @Override
        public void interact(World world) {
            if (!world.getGlobalVariables().containsKey("rogues_altar.earth_communicated")) {
                world.getGlobalVariables().put("rogues_altar.moon_checked", true);
                world.getPlayer().getJournal().addQuestEntries("rogues_altar", "desc", "comm");
                // player has not yet received task to settle things down
                world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/rogues_altar_1.json"));
                return;
            }

            final Dialog d = Dialog.loadFromFile("dialogs/encounters/rogues_altar_2.json");
            d.addListener(new DialogListener() {
                private static final long serialVersionUID = 7809964677347861595L;

                @Override
                public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                    if (returnCode == 1) {
                        // player decided to fight rogues, make them aggressive
                        onAttack(world, RogueAltarWorker.this, 0);
                    }
                }
            });
            world.addOverlayWindow(d);

        }
    }

    /**
     * All invasion events happen:
     * 1) After at least 4th player return to earth
     * 2) If there are no other global events
     */
    private static abstract class BaseInvasionQuestListener extends GameEventListener {
        private static final long serialVersionUID = 8170368564115054199L;

        private int count = 0;

        @Override
        public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
            if (count < 0) {
                return false;
            }
            final AlienRace humanity = (AlienRace) world.getFactions().get(HumanityGenerator.NAME);
            if (ss == humanity.getHomeworld()) {
                ++count;
            } else {
                return true;
            }

            if (count < 4 || !world.getPlayer().getEarthState().getEarthSpecialDialogs().isEmpty()) {
                return false;
            }


            count = -1;
            return process(world, ss);
        }

        protected abstract boolean process(World world, StarSystem ss);
    }

    private static final class RogueInvasionAdder extends BaseInvasionQuestListener {
        private static final long serialVersionUID = -2497678330932578786L;

        @Override
        public boolean process(World world, StarSystem ss) {
            world.getPlayer().getJournal().addQuestEntries("rogues_altar", "enter");
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/rogues_altar_scientist.json"));

            Dialog earthDialog = Dialog.loadFromFile("dialogs/encounters/rogues_altar_earth.json");

            earthDialog.addListener(new DialogListener() {

                private static final long serialVersionUID = -6367061348256715021L;

                @Override
                public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                    if (flags.containsKey("klisk.philosophy_research")) {
                        // add new research of a klisk philosophy
                    }
                    world.getGlobalVariables().put("rogues_altar.earth_communicated", true);
                    world.getPlayer().getJournal().addQuestEntries("rogues_altar", "earth");
                }
            });

            world.getPlayer().getEarthState().getEarthSpecialDialogs().add(earthDialog);

            // now create dungeon on a moon

            Planet moon = (Planet) ss.getPlanets()[2].getSatellites().get(0);

            DungeonEntrance entrance = new DungeonEntrance(moon, 5, 5, "rogues_altar", new Dungeon(world, new AuroraTiledMap("maps/rogue_altar.tmx"), moon));
            moon.setNearestFreePoint(entrance, 5, 5);
            moon.getMap().getObjects().add(entrance);

            world.getPlayer().getEarthState().getMessages().add(new PrivateMessage("rogues_altar", "news"));

            return false;
        }
    }

    /**
     * Klisk event puts a trade probe on earth orbit, that disturbes all solar system communications with advertisements
     * Player can either destroy it, or buy all its stuff
     */
    private static final class KliskTradeProbe extends NPCShip {
        private static final long serialVersionUID = -8830393993027489642L;

        public KliskTradeProbe(int x, int y, Faction klisk) {
            super(x, y, "klisk_drone", klisk, null, "Klisk trade probe", 7);

            Dialog commDialog = Dialog.loadFromFile("dialogs/encounters/klisk_trade_probe_comm.json");
            commDialog.addListener(new DialogListener() {

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
                        world.getPlayer().getEarthState().getMessages().add(new PrivateMessage("klisk_trader_drone_withdraw", "news"));
                    }

                }
            });

            setCaptain(new NPC(commDialog));

            setWeapons(ResourceManager.getInstance().getWeapons().getEntity("klisk_large_laser"));
        }

        @Override
        public void onAttack(World world, GameObject attacker, int dmg) {
            super.onAttack(world, attacker, dmg);
            if (hp <= 0) {
                world.getGlobalVariables().put("klisk_trader_drone.result", "destroy");
                world.getPlayer().getEarthState().getEarthSpecialDialogs().add(Dialog.loadFromFile("dialogs/encounters/klisk_trade_probe_earth_2.json"));
                world.getPlayer().getJournal().getQuests().get("klisk_trade_drone").addMessage("destroyed");
                world.getPlayer().getEarthState().getMessages().add(new PrivateMessage("klisk_trader_drone_destroy", "news"));
            }
        }
    }

    private static final class KliskTraderAdder extends BaseInvasionQuestListener {
        private static final long serialVersionUID = -5491271253252252436L;

        @Override
        protected boolean process(World world, StarSystem ss) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/klisk_trade_probe_enter.json"));
            world.getPlayer().getJournal().addQuest(new JournalEntry("klisk_trade_drone", "start"));

            Dialog earthDialog = Dialog.loadFromFile("dialogs/encounters/klisk_trade_probe_earth.json");
            earthDialog.addListener(new DialogListener() {
                private static final long serialVersionUID = 6759215425541397109L;

                @Override
                public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                    JournalEntry klisk_trade_drone = world.getPlayer().getJournal().getQuests().get("klisk_trade_drone");
                    klisk_trade_drone.addMessage("drone_desc");
                    klisk_trade_drone.addMessage("problem");
                }
            });
            world.getPlayer().getEarthState().getEarthSpecialDialogs().add(earthDialog);

            KliskTradeProbe probe = new KliskTradeProbe(ss.getPlanets()[2].getX() - 1, ss.getPlanets()[2].getY() - 1, world.getFactions().get(KliskGenerator.NAME));
            ss.getShips().add(probe);
            world.getPlayer().getEarthState().getMessages().add(new PrivateMessage("klisk_trader_drone", "news"));
            return true;
        }
    }

    public static final class BorkBlockadeShip extends NPCShip {

        private static final long serialVersionUID = 8123694044569284242L;

        private static int count = Configuration.getIntProperty("quest.bork_blockade.ships");

        private static boolean communicated = false;

        public BorkBlockadeShip(int x, int y, World world) {
            super(x, y, "bork_ship", world.getFactions().get(BorkGenerator.NAME), new NPC(Dialog.loadFromFile("dialogs/encounters/bork_blockade_contact.json")), "Bork ship", 5);
        }

        @Override
        public void onAttack(World world, GameObject attacker, int dmg) {
            super.onAttack(world, attacker, dmg);

            if (hp <= 0) {
                count--;
                if (count == 0) {
                    world.getPlayer().getEarthState().getMessages().add(new PrivateMessage("bork_blockade_destroy", "news"));
                    // all ships destroyed
                    world.getGlobalVariables().put("bork_blockade.result", "destroy");
                    world.getPlayer().getJournal().addQuestEntries("bork_blockade", "destroy");
                    world.getPlayer().getEarthState().getEarthSpecialDialogs().add(Dialog.loadFromFile("dialogs/encounters/bork_blockade_destroyed.json"));
                }
            }
        }

        @Override
        public void interact(World world) {
            super.interact(world);
            if (!communicated) {
                world.getPlayer().getJournal().addQuestEntries("bork_blockade", "comm");
                world.getGlobalVariables().put("bork_blockade.communicated", true);
                communicated = true;
            }
        }
    }

    /**
     * Bork event puts a blockade on solar system
     */
    private static final class BorkBlockadeAdder extends BaseInvasionQuestListener {

        private static final long serialVersionUID = 5403971949552674122L;

        @Override
        protected boolean process(World world, StarSystem ss) {
            world.getPlayer().getJournal().addQuest(new JournalEntry("bork_blockade", "desc"));
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/bork_blockade_enter.json"));
            Dialog earthDialog = Dialog.loadFromFile("dialogs/encounters/bork_blockade_earth.json");
            earthDialog.addListener(new DialogListener() {
                private static final long serialVersionUID = -2735450899833830131L;

                @Override
                public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                    world.getPlayer().getJournal().addQuestEntries("bork_blockade", "order");
                }
            });
            world.getPlayer().getEarthState().getEarthSpecialDialogs().add(earthDialog);

            int shipsCount = Configuration.getIntProperty("quest.bork_blockade.ships");
            BorkBlockadeShip ship = new BorkBlockadeShip(0, 0, world);
            ship.setPos(ss.getPlanets()[2].getX() - 1, ss.getPlanets()[2].getY());
            ss.getShips().add(ship);
            for (int i = 1; i < shipsCount; ++i) {
                ship = new BorkBlockadeShip(0, 0, world);
                ss.setRandomEmptyPosition(ship);
                ss.getShips().add(ship);
            }

            world.getPlayer().getEarthState().getMessages().add(new PrivateMessage("bork_blockade", "news"));

            return true;
        }
    }

    @Override
    public void updateWorld(World world) {
        int val = CommonRandom.getRandom().nextInt(3);
        switch (val) {
            case 0:
                world.addListener(new RogueInvasionAdder());
                break;
            case 1:
                world.addListener(new KliskTraderAdder());
                break;
            case 2:
                world.addListener(new BorkBlockadeAdder());
                break;
        }
    }
}
