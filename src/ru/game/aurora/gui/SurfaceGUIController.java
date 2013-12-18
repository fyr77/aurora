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
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.IDungeon;
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
    public boolean onTurnEnded(World world) {
        if (world.getCurrentRoom() instanceof Planet || world.getCurrentRoom() instanceof IDungeon) {
            updateStats();
        }
        return false;
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
        ((IDungeon) world.getCurrentRoom()).getController().changeMode();
    }

    public void interactClicked() {
        IDungeon dungeon = (IDungeon) world.getCurrentRoom();
        dungeon.getController().interactWithObject(world);
        world.setUpdatedNextFrame(true);
        updateStats();
    }

    public void nextTargetPressed() {
        ((IDungeon) world.getCurrentRoom()).getController().updateShoot(world, true, false, false);
    }

    public void prevTargetPressed() {
        ((IDungeon) world.getCurrentRoom()).getController().updateShoot(world, false, true, false);
    }

    public void firePressed() {
        ((IDungeon) world.getCurrentRoom()).getController().updateShoot(world, false, false, true);
        world.setUpdatedNextFrame(true);
    }

    public void cancelPressed() {
        ((IDungeon) world.getCurrentRoom()).getController().changeMode();
    }

    public void openMenu() {
        GUI.getInstance().showIngameMenu();
    }

    public void openInventory() {
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen("inventory_screen");
    }
}
