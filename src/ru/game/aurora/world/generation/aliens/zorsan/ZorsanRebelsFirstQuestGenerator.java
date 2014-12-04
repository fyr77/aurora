/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 21.04.14
 * Time: 13:43
 */


package ru.game.aurora.world.generation.aliens.zorsan;

import org.newdawn.slick.Color;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.factions.NeutralFaction;
import ru.game.aurora.npc.shipai.LeaveSystemAI;
import ru.game.aurora.player.Resources;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.player.research.projects.ArtifactResearch;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGenerator;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.AlienArtifact;
import ru.game.aurora.world.planet.MonsterBehaviour;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.nature.Animal;
import ru.game.aurora.world.planet.nature.AnimalModifier;
import ru.game.aurora.world.planet.nature.AnimalSpeciesDesc;
import ru.game.aurora.world.space.*;

import java.util.Collections;
import java.util.Map;

/**
 * After some time from successive escape from zorsan homeworld, player meets zorsan ship, that asks him to join rebels
 */
public class ZorsanRebelsFirstQuestGenerator extends GameEventListener implements WorldGeneratorPart, DialogListener {
    private static final long serialVersionUID = -7613551870732796318L;

    private int starsystemCount = 4;

    private Planet targetPlanet;

    private AlienArtifact artifact;

    private boolean messagePassed = false;

    public ZorsanRebelsFirstQuestGenerator() {
        setGroups(EventGroup.ENCOUNTER_SPAWN);
    }


    @Override
    public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject) {
        if (messagePassed && galaxyMapObject == targetPlanet.getOwner()) {
            return Localization.getText("journal", "zorsan_rebels.title");
        }

        return null;
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (messagePassed || !world.getGlobalVariables().containsKey("zorsan.escape")) {
            return false;
        }
        if (ss.isQuestLocation()) {
            return false;
        }
        if (starsystemCount-- > 0) {
            return false;
        }

        world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/rebels/intro/courier_encountered.json"));

        final NPCShip ship = ((AlienRace) world.getFactions().get(ZorsanGenerator.NAME)).getDefaultFactory().createShip(world, 0);
        ship.setFaction(world.getFactions().get(NeutralFaction.NAME));
        ship.setAi(null);

        Dialog courierDialog = Dialog.loadFromFile("dialogs/zorsan/rebels/intro/courier_dialog.json");
        courierDialog.addListener(new DialogListener() {

            private static final long serialVersionUID = 2455530990003907513L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                world.getPlayer().getJournal().addQuestEntries("zorsan_rebels", "start");
                ship.setAi(new LeaveSystemAI());
                ship.setCanBeHailed(false);
                messagePassed = true;
            }
        });

        ship.setCaptain(new NPC(courierDialog));

        ss.setRandomEmptyPosition(ship);
        ss.getShips().add(ship);

        return true;
    }


    // makes sure that there is at least 1 red giant starsystem for roaming base
    // prepares planet for intro

    private void placeArtifact(World world, StarSystem ss) {

        artifact = new AlienArtifact(10, 20, "builders_pyramid", new ArtifactResearch(new ResearchReport("builders_ruins", "builder_ruins.report")));
        ((Planet) ss.getPlanets()[0]).setNearestFreePoint(artifact, 10, 20);
        ((Planet) ss.getPlanets()[0]).getPlanetObjects().add(artifact);

        Dialog planetDialog = Dialog.loadFromFile("dialogs/zorsan/rebels/intro/planet_dialog.json");
        planetDialog.addListener(this);
        artifact.setFirstUseDialog(planetDialog);
        world.getGlobalVariables().put("zorsan_rebels.start_coords", ss.getCoordsString());
    }

    @Override
    public void updateWorld(World world) {

        final AlienRace alienRace = (AlienRace) world.getFactions().get(ZorsanGenerator.NAME);
        int travelDistance = alienRace.getTravelDistance();

        int redGiantsFound = 0;

        boolean artifactPlaced = false;

        for (GalaxyMapObject gmo : world.getGalaxyMap().getGalaxyMapObjects()) {
            if (!StarSystem.class.isAssignableFrom(gmo.getClass())) {
                continue;
            }

            StarSystem ss = (StarSystem) gmo;

            if (GalaxyMap.getDistance(ss, alienRace.getHomeworld()) > travelDistance) {
                continue;
            }

            if (ss.isQuestLocation()) {
                continue;
            }

            if (ss.getStar().color.equals(Color.red) && ss.getStar().size == 1) {
                ++redGiantsFound;
            }

            if (redGiantsFound >= 3 && artifactPlaced) {
                break;
            }

            if (artifactPlaced) {
                continue;
            }

            if (ss.getPlanets().length < 1) {
                continue;
            }

            if (!(ss.getPlanets()[0] instanceof Planet)) {
                continue;
            }
            artifactPlaced = true;
            targetPlanet = (Planet) ss.getPlanets()[0];
            placeArtifact(world, ss);
        }

        if (!artifactPlaced) {
            StarSystem ss = WorldGenerator.generateRandomStarSystem(world, 10, 10, 3);
            targetPlanet = (Planet) ss.getPlanets()[0];
            placeArtifact(world, ss);
            world.getGalaxyMap().addObjectAtDistance(ss, alienRace.getHomeworld(), alienRace.getTravelDistance() + 5);
        }

        while (redGiantsFound <= 3) {
            StarSystem ss = WorldGenerator.generateRandomStarSystem(new Star(1, Color.red), world, 10, 10, 3);
            world.getGalaxyMap().addObjectAtDistance(ss, alienRace.getHomeworld(), alienRace.getTravelDistance() - CommonRandom.getRandom().nextInt(alienRace.getTravelDistance() / 2));
            ++redGiantsFound;
        }

        world.addListener(this);
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (returnCode == 1) {
            // zorsan attack
            world.getPlayer().getJournal().questCompleted("zorsan_rebels", "attack");
            final int enemies = Configuration.getIntProperty("quest.zorsan_rebels.intro.enemies");
            AnimalSpeciesDesc desc = new AnimalSpeciesDesc(
                    targetPlanet
                    , "Zorsan"
                    , false
                    , false
                    , 7
                    , ResourceManager.getInstance().getWeapons().getEntity("zorsan_laser_rifles")
                    , 1
                    , MonsterBehaviour.AGGRESSIVE
                    , Collections.<AnimalModifier>emptySet()
            );
            desc.setImages(ResourceManager.getInstance().getImage("zorsan_warrior"), null);
            desc.setCanBePickedUp(false);
            for (int i = 0; i < enemies; ++i) {
                Animal animal = new Animal(targetPlanet, 0, 0, desc);
                targetPlanet.setNearestFreePoint(animal, artifact.getX() + CommonRandom.getRandom().nextInt(4), artifact.getY() + CommonRandom.getRandom().nextInt(4));
                targetPlanet.getPlanetObjects().add(animal);
            }
            isAlive = false;
            // without this zorsan will immediately shoot twice
            world.setUpdatedThisFrame(false);
            world.setUpdatedNextFrame(false);
        } else {
            world.getPlayer().getJournal().addQuestEntries("zorsan_rebels", "planet");
            Dialog stationDialog = Dialog.loadFromFile("dialogs/zorsan/rebels/intro/station_dialog.json");
            stationDialog.addListener(new DialogListener() {

                private static final long serialVersionUID = 5826666436706201387L;

                @Override
                public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                    if (dialog.getId().equals("station_dialog")) {
                        Dialog leaderDialog = Dialog.loadFromFile("dialogs/zorsan/rebels/intro/rebel_leader_dialog.json");
                        leaderDialog.addListener(this);
                        world.addOverlayWindow(leaderDialog);
                        return;
                    } else if (dialog.getId().equals("rebel_leader_dialog")) {
                        if (returnCode == 1) {
                            // refused
                            world.getPlayer().getJournal().questCompleted("zorsan_rebels", "refused");
                            flags.put("rebels_reject", "");
                        } else {
                            world.getPlayer().getJournal().addQuestEntries("zorsan_rebels", "agreed");
                            flags.put("rebels_continue", "");
                            world.getPlayer().changeResource(world, Resources.CREDITS, 5);
                        }
                    }

                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/rebels/intro/henry_dialog.json"), flags);
                }
            });
            world.addListener(new RogueBaseEncounter(stationDialog));
            isAlive = false;
        }


    }
}
