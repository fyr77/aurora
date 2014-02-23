package ru.game.aurora.world.quest;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
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

    private List<NPCShip> currentWave = new LinkedList<>();


    private void summonFirstWaveOfReinforcements(World world) {
        //todo here arrive borks and klisk
    }

    private void summonSecondWaveOfReinforcements(World world) {
        // todo here arrive rogues
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

    }

    private NPCShip createVoyager(World world) {
        //todo
        return null;
    }

    public void updateWorld(World world) {
        humanity = world.getRaces().get(HumanityGenerator.NAME);
        zorsan = world.getRaces().get(ZorsanGenerator.NAME);
        solarSystem = humanity.getHomeworld();
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
