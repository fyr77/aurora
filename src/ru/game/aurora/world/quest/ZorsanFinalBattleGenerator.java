package ru.game.aurora.world.quest;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.gui.FailScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.shipai.LandAI;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.space.AlienHomeworld;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

    private AlienHomeworld earth;

    private List<NPCShip> currentWave = new LinkedList<>();

    private int dropShipsLanded = 0;

    private List<NPCShip> allyShips = new LinkedList<>();

    class ZorsanTroopTransport extends NPCShip {

        private static final long serialVersionUID = 4933360150674508485L;


        public ZorsanTroopTransport(int x, int y, Positionable target) {
            super(x, y, "zorsan_transport", zorsan, null, "Zorsan transport");
            setHp(20);
            setSpeed(2);
            setAi(new LandAI(target));
        }

        @Override
        public void update(GameContainer container, World world) {
            super.update(container, world);
            if (!ai.isAlive()) {
                dropShipsLanded++;
                if (dropShipsLanded == 1) {
                    // todo: show dialog
                }

                if (dropShipsLanded == 3) {
                    // todo: show dialog
                }

                if (dropShipsLanded == 5) {
                    // todo: show dialog
                }

                if (dropShipsLanded == 6) {
                    GUI.getInstance().getNifty().gotoScreen("fail_screen");
                    FailScreenController controller = (FailScreenController) GUI.getInstance().getNifty().findScreenController(FailScreenController.class.getCanonicalName());
                    controller.set("crew_lost_gameover", "zorsan_captured_earth");
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
                probe.setPos(earth.getX() + 1 + i, earth.getY() + CommonRandom.getRandom().nextInt(3) - 1);
                solarSystem.setRandomEmptyPosition(probe);
                solarSystem.getShips().add(probe);
                allyShips.add(probe);
            }
        }

        if (true == world.getGlobalVariables().get("bork.war_help")) {
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
                probe.setPos(-3 + i, i - 1);
                ship.setPos(-2 + i, i);
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
            ship.setPos(-solarSystem.getRadius() + (i < count / 2 ? 2 + CommonRandom.getRandom().nextInt(4) : 0), i);
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

        if (state == State.FIRST_WAVE_COMBAT && world.getTurnCount() - turnNumber > 5) {
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
            summonThirdAttackWave(world);
            state = State.THIRD_WAVE_COMBAT;
            return true;
        }

        if (state == State.THIRD_WAVE_COMBAT && currentWave.size() == 0) {
            // todo: end dialogs
            world.getGlobalVariables().remove("zorsan.war_preparations");
        }


        return false;
    }

    private void summonThirdAttackWave(World world) {

    }

    private void summonSecondAttackWave(World world) {
        int ships = Configuration.getIntProperty("quest.zorsan_final_battle.second_wave_ships");
        for (int i = 0; i < ships; ++i) {

            NPCShip warship = zorsan.getDefaultFactory().createShip(CommonRandom.getRandom().nextBoolean() ? ZorsanGenerator.CRUISER_SHIP : ZorsanGenerator.SCOUT_SHIP);
            warship.setPos(1 + 2 * i, solarSystem.getRadius() + CommonRandom.getRandom().nextInt(5));
            solarSystem.getShips().add(warship);

            ZorsanTroopTransport transport = new ZorsanTroopTransport(warship.getX(), warship.getY() + 4, earth);
            solarSystem.getShips().add(transport);
        }
    }

    private NPCShip createVoyager(World world) {
        //todo
        return null;
    }

    public void updateWorld(World world) {
        humanity = world.getRaces().get(HumanityGenerator.NAME);
        zorsan = world.getRaces().get(ZorsanGenerator.NAME);
        solarSystem = humanity.getHomeworld();
        earth = (AlienHomeworld) solarSystem.getPlanets()[2];
        solarSystem.setCanBeLeft(false);
        // create humanity ships
        int humanityShips = Configuration.getIntProperty("quest.zorsan_final_battle.humanity_ships");
        for (int i = 0; i < humanityShips; ++i) {
            NPCShip defender = humanity.getDefaultFactory().createShip(0);
            solarSystem.setRandomEmptyPosition(defender);
            solarSystem.getShips().add(defender);
        }

        //todo: show dialogs
        world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_crew_before_attack.json"));

        //todo: set earth dialog

        turnNumber = world.getTurnCount();

        world.addListener(this);
    }
}
