package ru.game.aurora.world.generation.quest.quarantine;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.world.*;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.nature.AnimalCorpseItem;
import ru.game.aurora.world.space.StarSystem;

/**
 * Created by Егор on 04.09.2015.
 * Quarantine quest. One of crewmember becomes infected with unknown disease and crew starts dying untill
 * player finds a solution.
 */
public class QuarantineQuest extends GameEventListener implements WorldGeneratorPart {

    private static final long serialVersionUID = 1L;

    private Planet targetPlanet = null;

    // countdown before death of next crew member
    private int countdown = 0;

    // 0 - quest started
    // 1 - first scientist dead
    // 2 - first marine dead
    private int state;

    private void killCrewMember()
    {
        resetCountdown();
    }

    @Override
    public boolean onTurnEnded(World world) {
        if (targetPlanet == null) {
            // quest not yet started
            return false;
        }

        if (countdown -- == 0) {
            final Ship ship = world.getPlayer().getShip();

            GameLogger.getInstance().logMessage(Localization.getText("journal", "quarantine.crewmember_dies"));
            if (state == 0) {
                // this is quest start
                world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/quarantine"));
                if (ship.getScientists() > 0) {
                    ship.setScientists(ship.getScientists() - 1);
                    resetCountdown();
                } else {
                    killCrewMember();
                }
                state++;
            } else if (state == 1){
                // add henry dialog
            } else {
                killCrewMember();
            }
        }

        return true;
    }

    private void resetCountdown()
    {
        this.countdown = Configuration.getIntProperty("quest.quarantine.turnsBetweenDeaths") + CommonRandom.getRandom().nextInt(10);
    }

    @Override
    public boolean onPlayerLeftPlanet(World world, Planet planet) {
        if (world.getTurnCount() < Configuration.getIntProperty("quest.quarantine.minDist")) {
            // too early, lets start this quest after at least a year of travel
            return false;
        }

        if (BasePositionable.getDistance(planet.getOwner(), (Positionable) world.getGlobalVariables().get("solar_system"))
                > Configuration.getIntProperty("quest.quarantine.minTurn")) {
            // this planet is too near
            return false;
        }

        boolean found = false;
        // check that landing party inventory contains at least one animal corpse
        for (InventoryItem item : world.getPlayer().getLandingParty().getInventory()) {
            if (item instanceof AnimalCorpseItem) {
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        if (CommonRandom.getRandom().nextDouble() >= Configuration.getDoubleProperty("quest.quarantine.chance")) {
            return false;
        }

        this.targetPlanet = planet;
        resetCountdown();
        return false;
    }

    @Override
    public void updateWorld(World world) {
        world.addListener(this);
    }
}
