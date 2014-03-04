package ru.game.aurora.world.quest;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.gui.FailScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.shipai.LandAI;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.StarshipWeaponDesc;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.SpaceObject;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.earth.Earth;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 20.02.14
 * Time: 23:06
 */
public class ZorsanFinalBattleGenerator extends GameEventListener {
    private enum State {
        CREATED,
        REINFORCEMENTS_ARRIVED,
        FIRST_WAVE_COMBAT,
        FIRST_WAVE_REINFORCEMENTS,
        FIRST_WAVE_INTERMISSION,
        ALL_REINFORCEMENTS_ARRIVED,
        SECOND_WAVE_COMBAT,
        THIRD_WAVE_COMBAT,
    }

    private State state = State.CREATED;

    private int turnNumber;

    private static final long serialVersionUID = 2198763272604170716L;

    private StarSystem solarSystem;

    private AlienRace humanity;

    private AlienRace zorsan;

    private Earth earth;

    private List<NPCShip> currentWave = new LinkedList<>();

    private int dropShipsLanded = 0;

    private List<NPCShip> allyShips = new LinkedList<>();

    class ZorsanTroopTransport extends NPCShip {

        private static final long serialVersionUID = 4933360150674508485L;


        public ZorsanTroopTransport(int x, int y, Positionable target) {
            super(x, y, "zorsan_transport", zorsan, null, "Zorsan transport", 25);
            setSpeed(1);
            setAi(new LandAI(target));
            setLoot(ZorsanGenerator.getDefaultLootTable());
        }

        @Override
        public void update(GameContainer container, World world) {
            if (!isAlive()) {
                return;
            }

            doMove(container);

            if (world.isUpdatedThisFrame()) {
                curSpeed--;
            }
            if (curSpeed > 0) {
                return;
            }
            curSpeed = speed;
            ai.update(this, world, solarSystem);
            if (!ai.isAlive()) {
                dropShipsLanded++;
                if (dropShipsLanded == 1) {
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_first_dropship.json"));
                }

                if (dropShipsLanded == 3) {
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_half_dropships.json"));
                }

                if (dropShipsLanded == 5) {
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_almost_all_dropships.json"));
                }

                if (dropShipsLanded == 6) {
                    Dialog endDialog = Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_earth_captured.json");
                    endDialog.addListener(new DialogListener() {
                        private static final long serialVersionUID = -426755352470714542L;

                        @Override
                        public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                            GUI.getInstance().getNifty().gotoScreen("fail_screen");
                            FailScreenController controller = (FailScreenController) GUI.getInstance().getNifty().findScreenController(FailScreenController.class.getCanonicalName());
                            controller.set("crew_lost_gameover", "zorsan_captured_earth");
                        }
                    });
                    world.addOverlayWindow(endDialog);
                }
            }
        }
    }


    private void summonFirstWaveOfReinforcements(World world) {
        String val = (String) world.getGlobalVariables().get("klisk_trader_drone.result");
        if ("withdraw".equals(val)) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_klisk_arrive.json"));
            final int ships = Configuration.getIntProperty("quest.zorsan_final_battle.klisk_ships");

            for (int i = 0; i < ships; ++i) {
                NPCShip probe = world.getRaces().get(KliskGenerator.NAME).getDefaultFactory().createShip(KliskGenerator.TRADE_PROBE);
                probe.setCanBeHailed(false);
                probe.setStationary(false);
                probe.setSpeed(2);
                probe.setPos(earth.getX() + 1 + i, earth.getY() + CommonRandom.getRandom().nextInt(3) - 1);
                solarSystem.getShips().add(probe);
                allyShips.add(probe);
            }
        }

        if (world.getGlobalVariables().containsKey("bork.war_help")) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_bork_arrive.json"));
            final int ships = Configuration.getIntProperty("quest.zorsan_final_battle.bork_ships");

            for (int i = 0; i < ships; ++i) {
                NPCShip ship = world.getRaces().get(BorkGenerator.NAME).getDefaultFactory().createShip(0);
                ship.setCanBeHailed(false);
                ship.setPos(2 + i, i);
                solarSystem.getShips().add(ship);
                allyShips.add(ship);
            }
        }
    }

    private void summonSecondWaveOfReinforcements(World world) {

        if ("help".equals(world.getGlobalVariables().get("rogues_altar.result"))) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_rogues_arrive.json"));

            final int ships = Configuration.getIntProperty("quest.zorsan_final_battle.rogues_ships");
            for (int i = 0; i < ships; ++i) {
                NPCShip ship = world.getRaces().get(RoguesGenerator.NAME).getDefaultFactory().createShip(RoguesGenerator.SCOUT_SHIP);
                NPCShip probe = world.getRaces().get(RoguesGenerator.NAME).getDefaultFactory().createShip(RoguesGenerator.PROBE_SHIP);
                ship.setCanBeHailed(false);
                probe.setPos(earth.getX() + i, earth.getY() + i - 1);
                ship.setPos(earth.getX() - 2 + i, earth.getY() + i);
                solarSystem.getShips().add(ship);
                solarSystem.getShips().add(probe);
                allyShips.add(ship);
                allyShips.add(probe);
            }
        }

        if ("pay".equals(world.getGlobalVariables().get("bork_blockade.result"))) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_bork_event_arrive.json"));
            final int ships = Configuration.getIntProperty("quest.zorsan_final_battle.bork_event_ships");

            for (int i = 0; i < ships; ++i) {
                NPCShip ship = world.getRaces().get(BorkGenerator.NAME).getDefaultFactory().createShip(0);
                ship.setCanBeHailed(false);
                ship.setPos(2 + i, i);
                solarSystem.getShips().add(ship);
                allyShips.add(ship);
            }
        }
    }

    private void summonFirstAttackWave(World world) {
        // first wave consists of some zorsan ships
        int count = Configuration.getIntProperty("quest.zorsan_final_battle.first_wave_ships");
        for (int i = 0; i < count; ++i) {
            // spawn in 2 lines, one straight and one broken
            NPCShip ship = zorsan.getDefaultFactory().createShip(0);
            ship.setPos(-solarSystem.getRadius() - (i < (count / 2) ? 4 : 0) - CommonRandom.getRandom().nextInt(6), i * 2);
            currentWave.add(ship);
            solarSystem.getShips().add(ship);
        }

        world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_first_wave.json"));
    }

    private void summonFirstAttackWaveReinforcements(World world) {
        world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_first_wave_reinforcements.json"));
        int count = Configuration.getIntProperty("quest.zorsan_final_battle.first_wave_ships");
        for (int i = 0; i < count / 2; ++i) {
            NPCShip ship = zorsan.getDefaultFactory().createShip(ZorsanGenerator.CRUISER_SHIP);
            ship.setPos(solarSystem.getRadius(), i);
            currentWave.add(ship);
            solarSystem.getShips().add(ship);
        }
    }

    private Dialog preprocessDialog(Dialog dialog) {
        if (dropShipsLanded >= 5) {
            // put america leader portrait instead of martan
            dialog.setIconName("usa_leader");
        }
        return dialog;
    }


    @Override
    public boolean onTurnEnded(World world) {
        if (state == State.CREATED && world.getTurnCount() - turnNumber > 5) {
            summonFirstWaveOfReinforcements(world);
            state = State.REINFORCEMENTS_ARRIVED;
            return true;
        }

        if (state == State.REINFORCEMENTS_ARRIVED && world.getTurnCount() - turnNumber > 5) {
            summonFirstAttackWave(world);
            turnNumber = world.getTurnCount();
            state = State.FIRST_WAVE_COMBAT;
            return true;
        }

        if (state == State.FIRST_WAVE_COMBAT && world.getTurnCount() - turnNumber > 30) {
            // suddenly some ships appear from another side
            summonFirstAttackWaveReinforcements(world);
            state = State.FIRST_WAVE_REINFORCEMENTS;
            return true;
        }

        // clean up dead attacking ships
        for (Iterator<NPCShip> iter = currentWave.iterator(); iter.hasNext(); ) {
            NPCShip ship = iter.next();
            if (!ship.isAlive()) {
                iter.remove();
            }
        }

        if (state == State.FIRST_WAVE_REINFORCEMENTS && currentWave.isEmpty()) {
            state = State.FIRST_WAVE_INTERMISSION;
            turnNumber = world.getTurnCount();
            repairAllies();
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_first_wave_ended.json"));
            return true;
        }

        if (state == State.FIRST_WAVE_INTERMISSION && world.getTurnCount() - turnNumber > 5) {
            state = State.ALL_REINFORCEMENTS_ARRIVED;
            turnNumber = world.getTurnCount();
            summonSecondWaveOfReinforcements(world);
            return true;
        }

        if (state == State.ALL_REINFORCEMENTS_ARRIVED && world.getTurnCount() - turnNumber > 5) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_second_wave.json"));
            summonSecondAttackWave(world);
            state = State.SECOND_WAVE_COMBAT;
            return true;
        }

        if (state == State.SECOND_WAVE_COMBAT && currentWave.size() <= 1) {
            world.addOverlayWindow(preprocessDialog(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_third_wave.json")));
            summonThirdAttackWave(world);
            repairAllies();
            state = State.THIRD_WAVE_COMBAT;
            return true;
        }

        if (state == State.THIRD_WAVE_COMBAT && currentWave.size() == 0) {
            // todo: end dialogs
            solarSystem.setCanBeLeft(true);
            for (NPCShip s : allyShips) {
                if (s.isAlive()) {
                    solarSystem.getShips().remove(s);
                }
            }
            world.getGlobalVariables().remove("zorsan.war_preparations");
        }


        return false;
    }

    private void repairAllies() {
        for (NPCShip s : allyShips) {
            s.setHp(s.getMaxHP());
        }
        for (SpaceObject s : solarSystem.getShips()) {
            if (s instanceof NPCShip && s.getRace().getName().equals(HumanityGenerator.NAME)) {
                ((NPCShip) s).setHp(((NPCShip) s).getMaxHP());
            }
        }
    }

    private void summonThirdAttackWave(World world) {
        int ships = Configuration.getIntProperty("quest.zorsan_final_battle.third_wave_ships");
        for (int i = 0; i < ships; ++i) {

            NPCShip warship = zorsan.getDefaultFactory().createShip(CommonRandom.getRandom().nextBoolean() ? ZorsanGenerator.CRUISER_SHIP : ZorsanGenerator.SCOUT_SHIP);
            warship.setPos(1 + 2 * i, -solarSystem.getRadius() - CommonRandom.getRandom().nextInt(5));
            currentWave.add(warship);
            solarSystem.getShips().add(warship);
        }

        NPCShip bigBoss = new NPCShip(0, 0, "zorsan_boss", zorsan, null, "Zorsan Planet Killer", 30);
        currentWave.add(bigBoss);
        solarSystem.getShips().add(bigBoss);
        bigBoss.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("zorsan_cannon"), ResourceManager.getInstance().getWeapons().getEntity("zorsan_small_cannon"), ResourceManager.getInstance().getWeapons().getEntity("zorsan_boss_cannon"));
        // todo: add areal damage
        bigBoss.setPos(-1, -solarSystem.getRadius());
    }

    private void summonSecondAttackWave(World world) {
        int ships = Configuration.getIntProperty("quest.zorsan_final_battle.second_wave_ships");
        for (int i = 0; i < ships; ++i) {

            NPCShip warship = zorsan.getDefaultFactory().createShip(CommonRandom.getRandom().nextBoolean() ? ZorsanGenerator.CRUISER_SHIP : ZorsanGenerator.SCOUT_SHIP);
            warship.setPos(2 * i, solarSystem.getRadius());
            solarSystem.getShips().add(warship);
            currentWave.add(warship);

            ZorsanTroopTransport transport = new ZorsanTroopTransport(warship.getX(), warship.getY() + 8, earth);
            solarSystem.getShips().add(transport);
            currentWave.add(transport);
        }
    }

    private NPCShip createVoyager(World world) {
        List<StarshipWeaponDesc> weapons = new LinkedList<>();
        //default weapons
        weapons.add(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon2"));
        weapons.add(ResourceManager.getInstance().getWeapons().getEntity("humanity_missiles"));

        if (world.getGlobalVariables().containsKey("klisk.war_help")) {
            weapons.add(ResourceManager.getInstance().getWeapons().getEntity("klisk_large_laser"));
        }

        int hp = 15;
        if (world.getGlobalVariables().containsKey("rogues.war_help")) {
            hp *= 2;
            weapons.add(ResourceManager.getInstance().getWeapons().getEntity("plasma_cannon"));
        }

        NPCShip ship = new NPCShip(0, 0, "voyager", humanity, new NPC(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_voyager.json")), "Voyager", hp);
        ship.setSpeed(1);
        ship.setPos(earth.getX() + CommonRandom.getRandom().nextInt(8) - 2, earth.getY() + CommonRandom.getRandom().nextInt(6) - 3);
        ship.enableRepairs(4);
        StarshipWeaponDesc[] descs = weapons.toArray(new StarshipWeaponDesc[weapons.size()]);
        ship.setWeapons(descs);
        return ship;
    }

    public void updateWorld(World world) {
        humanity = world.getRaces().get(HumanityGenerator.NAME);
        zorsan = world.getRaces().get(ZorsanGenerator.NAME);
        solarSystem = humanity.getHomeworld();
        earth = (Earth) solarSystem.getPlanets()[2];
        solarSystem.setCanBeLeft(false);

        world.getReputation().setHostile(HumanityGenerator.NAME, ZorsanGenerator.NAME);
        world.getReputation().setHostile(ZorsanGenerator.NAME, HumanityGenerator.NAME);

        solarSystem.getReputation().setHostile(HumanityGenerator.NAME, ZorsanGenerator.NAME);
        solarSystem.getReputation().setHostile(ZorsanGenerator.NAME, HumanityGenerator.NAME);

        // create humanity ships
        int humanityShips = Configuration.getIntProperty("quest.zorsan_final_battle.humanity_ships");
        for (int i = 0; i < humanityShips; ++i) {
            NPCShip defender = humanity.getDefaultFactory().createShip(0);
            defender.setPos(earth.getX() + CommonRandom.getRandom().nextInt(6) - 3, earth.getY() + CommonRandom.getRandom().nextInt(6) - 3);
            solarSystem.getShips().add(defender);
        }

        NPCShip voyager = createVoyager(world);
        solarSystem.setRandomEmptyPosition(voyager);
        solarSystem.getShips().add(voyager);
        world.getGlobalVariables().put("voyager", voyager);

        world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_crew_before_attack.json"));

        //todo: set earth dialog
        world.getPlayer().setResourceUnits(world.getPlayer().getResourceUnits() + 20);
        world.getPlayer().getEarthState().getEarthSpecialDialogs().add(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_humanity_default.json"));

        turnNumber = world.getTurnCount();

        world.addListener(this);
        ZorsanGenerator.removeWarDataDrop();
    }
}
