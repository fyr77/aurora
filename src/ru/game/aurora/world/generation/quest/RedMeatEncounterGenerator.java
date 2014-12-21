package ru.game.aurora.world.generation.quest;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.music.Playlist;
import ru.game.aurora.npc.*;
import ru.game.aurora.npc.shipai.LandAI;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * Player ship gets wrapped by some zerg-style bio-stuff
 */
public class RedMeatEncounterGenerator implements WorldGeneratorPart, DialogListener
{
    private int prisonStartTurn = -1;

    private int turnsInSun = 0;

    private int turnsSinceInfection = -1;

    // player can not enter alien homeworlds, solar system and colony system
    private class MainListener extends GameEventListener
    {
        @Override
        public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
            if (!world.getGlobalVariables().containsKey("red_meat.attached")) {
                isAlive = false;
                return false;
            }

            for (Faction f : world.getFactions().values()) {
                if (f instanceof AlienRace && ((AlienRace) f).getHomeworld() == ss) {
                    world.getGalaxyMap().returnTo(world);
                    world.setCurrentRoom(world.getGalaxyMap());
                    world.onPlayerLeftSystem(ss);
                    if (f.getName().equals(HumanityGenerator.NAME)) {
                        world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/red_meat/red_meat_solar_system.json"));
                    } else {
                        world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/red_meat/red_meat_alien_homeworld.json"));
                        world.getPlayer().getJournal().addQuestEntries("red_meat", "alien_contact");
                    }

                }
            }

            if (world.getGlobalVariables().containsKey("colony_search.coords")) {
                Planet p = (Planet) world.getGlobalVariables().get("colony_search.coords");
                if (p.getOwner() == ss) {
                    //todo: separate dialog for a colony
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/red_meat/red_meat_solar_system.json"));
                    world.getGalaxyMap().returnTo(world);
                    world.setCurrentRoom(world.getGalaxyMap());
                    world.onPlayerLeftSystem(ss);
                }
            }
            return false;
        }

        @Override
        public boolean onTurnEnded(World world) {
            final Ship ship = world.getPlayer().getShip();
            if (prisonStartTurn > 0 && world.getTurnCount() - prisonStartTurn > 1) {
                // crew members return from prison
                ship.setDefaultCrewDialogs(world);
            }

            if (world.getCurrentStarSystem() != null && ship.getX() == 0 && ship.getY() == 0) {
                turnsInSun++;
                GameLogger.getInstance().logMessage(Localization.getText("journal", "burns." + turnsInSun));
            } else {
                turnsInSun = 0;
            }

            if (turnsInSun >= 2) {
                //////////////////////////////// quest ended ////////////////////////////////////
                ship.setSprite("aurora");
                world.getGlobalVariables().remove("red_meat.attached");
                isAlive = false;
                world.getPlayer().getJournal().questCompleted("red_meat", "cleaned");
                world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/red_meat/red_meat_destroyed.json"));
                ship.changeMaxHull(-1);
            }

            turnsSinceInfection++;
            if (turnsSinceInfection == 2) {
                Dialog startDialog = Dialog.loadFromFile("dialogs/encounters/red_meat/red_meat_started.json");
                startDialog.addListener(RedMeatEncounterGenerator.this);
                world.addOverlayWindow(startDialog);
                world.getPlayer().getJournal().addQuestEntries("red_meat", "start");
            } else if (turnsSinceInfection == 20) {
                world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/red_meat/red_meat_henry_1.json"));
            } else if (turnsSinceInfection == 60) {
                world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/red_meat/red_meat_sarah_1.json"));
            }if (turnsSinceInfection == 110) {
                world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/red_meat/red_meat_henry_2.json"));
            }
            return false;
        }
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (dialog.getId().equals("red_meat_started")) {
            final CrewMember henry = world.getPlayer().getShip().getCrewMembers().get("henry");
            if (flags.containsKey("henry_plus")) {
                henry.changeReputation(1);
            }

            if (flags.containsKey("prison")) {
                final CrewMember sarah = world.getPlayer().getShip().getCrewMembers().get("sarah");
                henry.changeReputation(1);
                sarah.changeReputation(-1);
                henry.getDialogFlags().put("red_meat_prison", "");
                sarah.getDialogFlags().put("red_meat_prison", "");

                Dialog dialogWhileInPrison = Dialog.loadFromFile("dialogs/encounters/red_meat/red_meat_officer_call.json");
                henry.setDialog(dialogWhileInPrison);
                sarah.setDialog(dialogWhileInPrison);

                prisonStartTurn = world.getTurnCount();
                world.getPlayer().getJournal().addQuestEntries("red_meat", "discipline");
            }
        }
    }

    private class Spore extends NPCShip
    {

        public Spore(World world) {
            super(0, 0, "spore", null, null, "Spore", 20);
            setStationary(true);
            setAi(new LandAI(world.getPlayer().getShip()));
            enableRepairs(1);
            setSpeed(1);
        }

        @Override
        public void update(GameContainer container, World world) {
            super.update(container, world);

            final double playerDistance = getDistance(world.getPlayer().getShip());
            if (playerDistance < 15 && isStationary()) {
                setStationary(false);
            }

            if (playerDistance <= 1) {
                //////////////////////// quest starts here /////////////////////////////
                isAlive = false;
                world.getPlayer().getShip().setSprite("aurora_corrupted");
                world.getGlobalVariables().put("red_meat.attached", true);
                // todo: change crew default dialogs here
                world.addListener(new MainListener());

                world.getPlayer().getShip().changeMaxHull(1);
                world.getPlayer().getShip().setHull(world.getPlayer().getShip().getHull() + 1);
                world.getPlayer().getResearchState().addNewAvailableProject(world.getResearchAndDevelopmentProjects().getResearchProjects().remove("red_meat"));
                turnsSinceInfection = 0;
            }
        }
    }

    @Override
    public void updateWorld(World world) {
        final SingleShipEvent listener = new SingleShipEvent(Configuration.getDoubleProperty("quest.red_meat.chance"), new Spore(world));
        world.addListener(listener);
        listener.setMinRange(Configuration.getIntProperty("quest.red_meat.minDist"));
    }
}
