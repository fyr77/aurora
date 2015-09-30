package ru.game.aurora.player.earth.upgrades;

import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;

/**
 * Increases relations with all known races by 1
 */
public class DiplomacyUpgrade extends EarthUpgrade {

    private int reputationChange;

    @Override
    public void unlock(World world) {
        super.unlock(world);
        if (((AlienRace) world.getFactions().get(KliskGenerator.NAME)).isKnown()) {
            world.getReputation().updateReputation(HumanityGenerator.NAME, KliskGenerator.NAME, reputationChange);
            world.getReputation().updateReputation(KliskGenerator.NAME, HumanityGenerator.NAME, reputationChange);
        }

        if (((AlienRace) world.getFactions().get(RoguesGenerator.NAME)).isKnown()) {
            world.getReputation().updateReputation(HumanityGenerator.NAME, RoguesGenerator.NAME, reputationChange);
            world.getReputation().updateReputation(RoguesGenerator.NAME, HumanityGenerator.NAME, reputationChange);
        }

        if (((AlienRace) world.getFactions().get(BorkGenerator.NAME)).isKnown()) {
            world.getReputation().updateReputation(HumanityGenerator.NAME, BorkGenerator.NAME, reputationChange);
            world.getReputation().updateReputation(BorkGenerator.NAME, HumanityGenerator.NAME, reputationChange);
        }
    }
}
