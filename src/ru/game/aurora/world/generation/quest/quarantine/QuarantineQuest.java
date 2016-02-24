package ru.game.aurora.world.generation.quest.quarantine;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.player.engineering.upgrades.MedBayUpgrade;
import ru.game.aurora.player.research.ResearchProjectState;
import ru.game.aurora.steam.AchievementManager;
import ru.game.aurora.steam.AchievementNames;
import ru.game.aurora.util.ProbabilitySet;
import ru.game.aurora.world.*;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.nature.Animal;
import ru.game.aurora.world.planet.nature.AnimalCorpseItem;
import ru.game.aurora.world.planet.nature.PlanetaryLifeGenerator;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Quarantine quest. One of crewmember becomes infected with unknown disease and crew starts dying untill
 * player finds a solution.
 */
public class QuarantineQuest extends GameEventListener implements WorldGeneratorPart {

    private static final long serialVersionUID = 1L;

    private Planet targetPlanet = null;

    private Planet lastLandedPlanet = null;

    private boolean researchAvailable = false;

    // countdown before death of next crew member
    private int countdown = 0;

    // 0 - quest started
    // 1 - first scientist dead
    // 2 - first marine dead
    private int state;

    private int daysSinceStart = 0;

    private boolean hasMedbay = false;

    private double researchBoost = 1;

    private int animalsCollected = 0;

    public static void endQuest(World world) {
        for (Iterator<GameEventListener> iterator = world.getListeners().iterator(); iterator.hasNext(); ) {
            GameEventListener listener = iterator.next();
            if (listener instanceof QuarantineQuest) {
                iterator.remove();
            }
        }

        world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/quarantine/quarantine_end.json"));

        // if player has bought medicine from klisk - remove its research
        for (Iterator<ResearchProjectState> iterator = world.getPlayer().getResearchState().getCurrentProjects().iterator(); iterator.hasNext(); ) {
            ResearchProjectState pr = iterator.next();
            if (pr.desc instanceof QuarantineResearch) {
                iterator.remove();
                world.getPlayer().getResearchState().addIdleScientists(pr.scientists);
                break;
            }
        }

        world.getGlobalVariables().remove("quarantine.started");
        world.getPlayer().getJournal().questCompleted("quarantine", "end");
        if (world.getPlayer().getShip().getTotalCrew() == 1) {
            AchievementManager.getInstance().achievementUnlocked(AchievementNames.drDeath);
        }
    }

    private void killCrewMember(World world)
    {
        resetCountdown();
        ProbabilitySet<Integer> pbs = new ProbabilitySet<>(CommonRandom.getRandom());
        final Ship ship = world.getPlayer().getShip();
        pbs.put(0, (double) ship.getMilitary());
        pbs.put(1, (double) ship.getEngineers());
        pbs.put(2, (double) ship.getScientists());

        int i = pbs.getRandom();
        switch (i) {
            case 0:
                ship.setMilitary(ship.getMilitary() - 1);
                break;
            case 1:
                ship.setEngineers(ship.getEngineers() - 1);
                world.getPlayer().getEngineeringState().removeEngineers(1);
                break;
            case 2:
                ship.setScientists(ship.getScientists() - 1);
                world.getPlayer().getResearchState().removeScientists(1);
                break;
        }
        world.onCrewChanged();
    }

    @Override
    public boolean onTurnEnded(World world) {
        if (targetPlanet == null) {
            // quest not yet started
            return false;
        }

        if (!world.getGlobalVariables().containsKey("quarantine.research_started")
                && targetPlanet == world.getCurrentRoom()
                && targetPlanet.getExploredTiles() >= 100
                && animalsCollected >= 3) {
            world.getGlobalVariables().put("quarantine.research_started", "");
            world.getPlayer().getJournal().addQuestEntries("quarantine", "medicine");
            world.getPlayer().getResearchState().addNewAvailableProject(new QuarantineResearch(researchBoost));
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/quarantine/quarantine_materials_collected.json"));
        }

        ++daysSinceStart;

        if (daysSinceStart == Configuration.getIntProperty("quest.quarantine.turnsBetweenDeaths") + 20) {
            Dialog d = Dialog.loadFromFile("dialogs/encounters/quarantine/quarantine_first_death.json");
            world.getPlayer().getJournal().addQuestEntries("quarantine", "confirmed");
            Map<String, String> flags = new HashMap<>();
            // this dialog has options for improvements that may help in disease research, like presense of medbay
            for (ShipUpgrade su : world.getPlayer().getShip().getUpgrades()) {
                if (su instanceof MedBayUpgrade) {
                    if (!((MedBayUpgrade) su).isEnhanced()) {
                        flags.put("has_medbay", "");
                    } else {
                        flags.put("has_advanced_medbay", "");
                        researchBoost *= 1.2;
                    }
                    hasMedbay = true;
                }
            }
            if (hasMedbay) {
                world.getPlayer().getJournal().addQuestEntries("quarantine", "medbay");
            }

            if (world.getResearchAndDevelopmentProjects().getResearchProjects().containsKey("alien_biology")) {
                flags.put("has_biology", "");
                researchBoost *= 1.5;
                world.getPlayer().getJournal().addQuestEntries("quarantine", "biodata");
            }
            researchAvailable = true;
            world.addOverlayWindow(d, flags);
            return true;
        }

        if (daysSinceStart == 100) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/quarantine/quarantine_gordon_1.json"));
        } else if (daysSinceStart == 400) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/quarantine/quarantine_gordon_2.json"));
        } else if (daysSinceStart == 800) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/quarantine/quarantine_gordon_3.json"));
        }

        if (daysSinceStart == 210) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/quarantine/quarantine_henry_1.json"));
        } else if (daysSinceStart == 350) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/quarantine/quarantine_henry_2.json"));
        } else if (daysSinceStart == 700) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/quarantine/quarantine_henry_3.json"));
        }

        if (daysSinceStart == 190) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/quarantine/quarantine_sarah_1.json"));
        } else if (daysSinceStart == 530) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/quarantine/quarantine_sarah_2.json"));
        }


        if (countdown -- == 0) {
            final Ship ship = world.getPlayer().getShip();

            GameLogger.getInstance().logMessage(Localization.getText("journal", "quarantine.crewmember_dies"));
            if (state == 0) {
                // this is quest start
                world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/quarantine/quarantine_start.json"));
                world.getPlayer().getJournal().addQuestEntries("quarantine", "start");
                world.getGlobalVariables().put("quarantine.started", true);
                if (ship.getScientists() > 0) {
                    ship.setScientists(ship.getScientists() - 1);
                    world.getPlayer().getResearchState().removeScientists(1);
                    resetCountdown();
                } else {
                    killCrewMember(world);
                }
                state++;
            } else if (state == 1){
                // add henry dialog
                if (ship.getMilitary() > 0) {
                    ship.setMilitary(ship.getMilitary() - 1);
                    resetCountdown();
                } else {
                    killCrewMember(world);
                }
            } else {
                killCrewMember(world);
            }
        }

        return true;
    }


    private void resetCountdown()
    {
        this.countdown = Configuration.getIntProperty("quest.quarantine.turnsBetweenDeaths") + CommonRandom.getRandom().nextInt(10);
        if (hasMedbay) {
            this.countdown *= 1.5;
        }
    }

    @Override
    public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject) {
        if (targetPlanet != null && targetPlanet.getOwner().equals(galaxyMapObject)) {
            return Localization.getText("journal", "quarantine.star_system_mark");
        }
        return null;
    }

    private boolean checkAndStartQuest(World world) {
        if (world.getDayCount() < Configuration.getIntProperty("quest.quarantine.minTurn")) {
            // too early, lets start this quest after at least a year of travel
            return false;
        }

        if (BasePositionable.getDistance(lastLandedPlanet.getOwner(), (Positionable) world.getGlobalVariables().get("solar_system"))
                < Configuration.getIntProperty("quest.quarantine.minDist")) {
            // this planet is too near
            return false;
        }

        if (lastLandedPlanet.equals(world.getGlobalVariable("colony_search.coords", null))) {
            // we do not want to start this quest on a planet selected for colonization
            return false;
        }

        if (lastLandedPlanet.getFloraAndFauna() == null) {
            // strange situation
            return false;
        }

        if (CommonRandom.getRandom().nextDouble() >= Configuration.getDoubleProperty("quest.quarantine.chance")) {
            return false;
        }

        this.targetPlanet = lastLandedPlanet;
        resetCountdown();

        return true;
    }

    @Override
    public boolean onItemAmountChanged(World world, InventoryItem item, int amount) {
        if (amount < 0 || !AnimalCorpseItem.class.isAssignableFrom(item.getClass())) {
            return false;
        }
        if (targetPlanet == null) {
            return checkAndStartQuest(world);
        }

        if (world.getCurrentRoom() != targetPlanet) {
            return false;
        }

        animalsCollected += amount;

        if (world.getGlobalVariables().containsKey("quarantine.research_started")) {
            return false;
        }

        if (lastLandedPlanet.getExploredTiles() >= 100 && animalsCollected >= 3) {
            world.getGlobalVariables().put("quarantine.research_started", "");
            world.getPlayer().getJournal().addQuestEntries("quarantine", "medicine");
            world.getPlayer().getResearchState().addNewAvailableProject(new QuarantineResearch(researchBoost));
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/quarantine/quarantine_materials_collected.json"));
        }
        return false;
    }


    @Override
    public boolean onPlayerLandedPlanet(World world, Planet planet) {
        lastLandedPlanet = planet;

        if (planet != targetPlanet) {
            return false;
        }

        if (!researchAvailable) {
            return false;
        }

        if (world.getGlobalVariables().containsKey("quarantine.started") &&
                !world.getGlobalVariables().containsKey("quarantine.first_return")) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/quarantine/quarantine_landing_party.json"));
            world.getGlobalVariables().put("quarantine.first_return", "");
            // ensure that there are at least 3 monsters alive there
            if (targetPlanet.getFloraAndFauna() != null) {
                int count = 0;
                for (GameObject go : targetPlanet.getPlanetObjects()) {
                    if (Animal.class.isAssignableFrom(go.getClass())) {
                        ++count;
                        if (count > 3) {
                            break;
                        }
                    }
                }
                if (count < 3) {
                    PlanetaryLifeGenerator.addAnimals(targetPlanet, 3);
                }
            }
        }
        return false;
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (targetPlanet == null) {
            return false;
        }

        if (world.getGlobalVariables().get("solar_system").equals(ss)) {
            world.getGalaxyMap().returnTo(world);
            world.setCurrentRoom(world.getGalaxyMap());
            world.onPlayerLeftSystem(ss);
            GameLogger.getInstance().logMessage(Localization.getText("journal", "quarantine.solar_system"));
            return true;
        }
        return false;
    }

    @Override
    public void updateWorld(World world) {
        world.addListener(this);
    }
}
