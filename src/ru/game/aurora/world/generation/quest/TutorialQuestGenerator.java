package ru.game.aurora.world.generation.quest;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.gui.FadeOutScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.HelpPopupControl;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.player.SellOnlyInventoryItem;
import ru.game.aurora.player.research.BaseResearchWithFixedProgress;
import ru.game.aurora.steam.AchievementManager;
import ru.game.aurora.steam.AchievementNames;
import ru.game.aurora.world.*;
import ru.game.aurora.world.dungeon.DungeonMonster;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.*;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.earth.Earth;

import java.util.Map;

/**
 * Starts a simple tutorial quest
 */
public class TutorialQuestGenerator extends GameEventListener implements WorldGeneratorPart, DialogListener {

    private static final long serialVersionUID = 1L;

    private Earth earth;

    @Override
    public void onDialogEnded(final World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (returnCode == 0) {
            setEarthTutorialDialog(world);
            return;
        }

        // end quest
        world.getGlobalVariables().remove("tutorial.started");
        earth.getOwner().setCanBeLeft(true);
        AchievementManager.getInstance().achievementUnlocked(AchievementNames.scholar);
        earth.setLastVisitTurn(world.getDayCount());

        FadeOutScreenController.makeFade(new IStateChangeListener() {
            @Override
            public void stateChanged(Object param) {
                final Dialog journeyStartDialog = Dialog.loadFromFile("dialogs/game_start_tutorial.json");
                journeyStartDialog.addListener(new DialogListener() {
                    @Override
                    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                        if (returnCode == 1) {
                            GUI.getInstance().pushCurrentScreen();
                            GUI.getInstance().goToScreen("ship_screen");
                        }
                    }
                });
                world.addOverlayWindow(journeyStartDialog);
            }
        });

    }

    private void setEarthTutorialDialog(World world) {
        // refresh the special tutorial dialog on earth
        final Dialog e = Dialog.loadFromFile("dialogs/tutorials/tutorial_end.json");
        e.addListener(this);
        world.getPlayer().getEarthState().getEarthSpecialDialogs().add(e);
    }

    @Override
    public boolean onNewGameStarted(World world) {
        // move player into solar system
        StarSystem solarSystem = (StarSystem) world.getGlobalVariables().get("solar_system");
        solarSystem.setCanBeLeft(false);
        world.getGlobalVariables().put("autosave_disabled", true);
        // we do not need the game to be saved at this moment
        solarSystem.enter(world);
        world.setCurrentRoom(solarSystem);
        world.getGlobalVariables().remove("autosave_disabled");

        earth = (Earth) solarSystem.getPlanets()[2];
        setEarthTutorialDialog(world);
        world.getPlayer().getShip().setPos(earth.getX() + 1, earth.getY() + 2);

        world.getGlobalVariables().put("tutorial.started", true);
        world.getPlayer().getJournal().addQuestEntries("tutorial", "start");

        HelpPopupControl.showHelp("start", "tutorial.1.1");

        world.addOverlayWindow(Dialog.loadFromFile("dialogs/tutorials/tutorial_start.json"));

        // add a ship target
        BasePlanet mars = solarSystem.getPlanets()[3];
        TestTarget shipTarget = new TestTarget((Planet) mars);
        shipTarget.setPos(mars.getX(), mars.getY() - 1);
        solarSystem.getObjects().add(shipTarget);

        return true;
    }

    @Override
    public void updateWorld(World world) {
        world.addListener(this);
    }

    public static class TutorialResearch extends BaseResearchWithFixedProgress {

        public TutorialResearch(String id, String icon, int initialProgress, int score) {
            super(id, icon, initialProgress, score);
        }

        @Override
        public void onCompleted(World world) {
            super.onCompleted(world);
            world.getGlobalVariables().put("tutorial_completed", true);
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/tutorials/tutorial_code_received.json"));
            world.getPlayer().getJournal().questCompleted("tutorial", "end");
        }
    }

    private static class TestDroneDataItem extends SellOnlyInventoryItem {
        public TestDroneDataItem() {
            super("journal", "tutorial.drone_data", "recorder", 0, true);
        }

        @Override
        public boolean canBeSoldTo(World world, Faction faction) {
            return false;
        }

        @Override
        public void onReceived(World world, int amount) {
            world.getPlayer().getResearchState().addNewAvailableProject(
                    world.getResearchAndDevelopmentProjects().getResearchProjects().remove("tutorial_research")
            );
            HelpPopupControl.showHelp("tutorial.5.1", "tutorial.5.2");
            world.getPlayer().getInventory().remove(this);
        }

        @Override
        public boolean isDumpable() {
            return true;
        }
    }

    private static final class TestDrone extends DungeonMonster {
        public TestDrone(Planet mars) {
            super("Drone"
                    , 0
                    , 0
                    , null
                    , null
                    , mars.getMap()
                    , new MonsterDesc(
                    "test_drone"
                    , null
                    , 5
                    , 1
                    , null
                    , "spider_robot"
                    , false
                    , MonsterBehaviour.AGGRESSIVE)

                    , null);
            controller = new MonsterController(mars.getMap(), this);
        }

        @Override
        public ScanGroup getScanGroup() {
            return ScanGroup.OTHER;
        }

        @Override
        public void onAttack(World world, GameObject attacker, int damage) {
            super.onAttack(world, attacker, damage);
            if (hp <= 0) {
                world.getCurrentRoom().getMap().getObjects().add(
                        new PickableInventoryItem(getTargetX(), getTargetY(), new TestDroneDataItem()));
            }
        }
    }

    // NPC ship that should be destroyed. Drops a drone on a Mars when destroyed
    private static final class TestTarget extends NPCShip {
        private static final long serialVersionUID = 1L;

        private Planet mars;

        private boolean scanTutorialShown = false;

        private boolean reloadTutorialShown = false;

        public TestTarget(Planet mars) {
            super(0, 0, "earth_transport", null, null, "Iowa", 5);
            this.mars = mars;
            setSpeed(3);
            setStationary(true);
            setWeapons(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon"));
        }

        @Override
        public String getScanDescription(World world) {
            return Localization.getText("journal", "tutorial.scan_desc");
        }

        @Override
        public void onAttack(World world, GameObject attacker, int dmg) {
            super.onAttack(world, attacker, dmg);
            if (!reloadTutorialShown) {
                HelpPopupControl.showHelp("tutorial.3.1");
                reloadTutorialShown = true;
            }
            if (!isAlive()) {
                world.getPlayer().getJournal().addQuestEntries("tutorial", "ship_destroyed");
                HelpPopupControl.showHelp("tutorial.4.1");

                DungeonMonster drone = new TestDrone(mars);
                mars.setNearestFreePoint(drone, 20, 20);
                mars.getPlanetObjects().add(drone);
                GameLogger.getInstance().logMessage(Localization.getText("journal", "tutorial.drone_landed"));
            }
        }

        @Override
        public void update(GameContainer container, World world) {
            super.update(container, world);
            if (world.isUpdatedThisFrame() && !scanTutorialShown && getDistance(world.getPlayer().getShip()) < 3) {
                HelpPopupControl.showHelp("tutorial.2.1", "tutorial.2.2");
                scanTutorialShown = true;
            }
        }
    }
}
