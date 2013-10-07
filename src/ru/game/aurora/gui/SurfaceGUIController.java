/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 02.07.13
 * Time: 16:13
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.gui.niffy.TopPanelController;
import ru.game.aurora.world.Dungeon;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.Planet;


public class SurfaceGUIController extends GameEventListener implements ScreenController, GameLogger.LoggerAppender {
    private static final long serialVersionUID = -7781914700046409079L;

    private World world;

    private transient Screen myScreen;

    private transient ListBox logList;

    private transient TopPanelController topPanelController;

    public SurfaceGUIController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        myScreen = screen;
        GameLogger.getInstance().addAppender(this);
        logList = screen.findNiftyControl("log_list", ListBox.class);
        topPanelController = screen.findControl("top_panel", TopPanelController.class);
    }

    @Override
    public void onStartScreen() {
        updateStats();
        logList.clear();
        logList.addAllItems(GameLogger.getInstance().getLogItems());
        logList.setFocusItemByIndex(logList.getItems().size() - 1);
        updateStats();
    }

    @Override
    public void onEndScreen() {

    }

    public void updateStats() {
        final LandingParty landingParty = world.getPlayer().getLandingParty();
        topPanelController.setCrewStats(landingParty.getScience(), landingParty.getEngineers(), landingParty.getMilitary());
        topPanelController.setProgress(Localization.getText("gui", "surface.remaining_oxygen") + " " + landingParty.getOxygen(), landingParty.getOxygen() / (float) LandingParty.MAX_OXYGEN);
    }

    @Override
    public void onTurnEnded(World world) {
        if (!(world.getCurrentRoom() instanceof Planet) && !(world.getCurrentRoom() instanceof Dungeon)) {
            return;
        }
        updateStats();
    }

    @Override
    public void logMessage(String message) {
        if (logList.getItems().size() > GameLogger.MAX_LOG_ENTRIES) {
            logList.removeItemByIndex(0);
        }
        logList.addItem(message);
        logList.setFocusItemByIndex(logList.getItems().size() - 1);
        myScreen.layoutLayers();
    }

    public void weaponClicked() {
        ((Planet) world.getCurrentRoom()).getController().changeMode();
    }

    public void interactClicked() {
        Planet planet = (Planet) world.getCurrentRoom();
        planet.getController().interactWithObject(world);
        if (planet.getShuttle().getDistance(world.getPlayer().getLandingParty()) == 0) {
            planet.leavePlanet(world);
        }
        world.setUpdatedThisFrame(true);
        planet.checkAndConsumeOxygen();
        updateStats();
    }

    public void nextTargetPressed() {
        ((Planet) world.getCurrentRoom()).getController().updateShoot(world, true, false, false);
    }

    public void prevTargetPressed() {
        ((Planet) world.getCurrentRoom()).getController().updateShoot(world, false, true, false);
    }

    public void firePressed() {
        ((Planet) world.getCurrentRoom()).getController().updateShoot(world, false, false, true);
    }

    public void cancelPressed() {
        ((Planet) world.getCurrentRoom()).getController().changeMode();
    }
}
