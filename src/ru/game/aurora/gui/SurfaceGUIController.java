/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 02.07.13
 * Time: 16:13
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.WindowClosedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.gui.niffy.TopPanelController;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.IDungeon;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;


public class SurfaceGUIController extends GameEventListener implements ScreenController, GameLogger.LoggerAppender {
    private static final long serialVersionUID = -7781914700046409079L;

    private final World world;

    private transient Screen myScreen;

    private transient ListBox logList;

    private transient TopPanelController topPanelController;

    private transient Element hpElement;

    private transient Element mapButton;

    public SurfaceGUIController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        myScreen = screen;
        GameLogger.getInstance().addAppender(this);
        logList = screen.findNiftyControl("log_list", ListBox.class);
        topPanelController = screen.findControl("top_panel", TopPanelController.class);
        hpElement = screen.findElementByName("health_count").findElementByName("#count");
        mapButton = screen.findElementByName("map_button");
    }

    @Override
    public void onStartScreen() {

        logList.clear();
        logList.addAllItems(GameLogger.getInstance().getLogItems());
        logList.setFocusItemByIndex(logList.getItems().size() - 1);


        if (world.getCurrentRoom() instanceof Planet) {
            mapButton.show();
            if (((Planet) world.getCurrentRoom()).getAtmosphere() == PlanetAtmosphere.BREATHABLE_ATMOSPHERE) {
                topPanelController.setProgressBarEnabled(Localization.getText("gui", "surface.breathable_atmosphere"), false);
            } else {
                topPanelController.setProgressBarEnabled("", true);
            }
        } else {
            mapButton.hide();
            topPanelController.setProgressBarEnabled("", true);
        }
        updateStats();
    }

    @Override
    public void onEndScreen() {

    }

    public void updateStats() {
        final LandingParty landingParty = world.getPlayer().getLandingParty();
        topPanelController.setCrewStats(landingParty.getScience(), landingParty.getEngineers(), landingParty.getMilitary());
        if (topPanelController.isProgressBarEnabled()) {
            topPanelController.setProgress(Localization.getText("gui", "surface.remaining_oxygen") + " " + landingParty.getOxygen(), landingParty.getOxygen() / (float) LandingParty.MAX_OXYGEN);
        }
        EngineUtils.setTextForGUIElement(hpElement, Integer.toString(world.getPlayer().getLandingParty().getHp()));
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
        updateStats();
    }

    public void openMap() {
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen("surface_map_screen");
    }

    public void nextTargetPressed() {
        ((IDungeon) world.getCurrentRoom()).getController().updateShoot(world, true, false, false);
    }

    public void prevTargetPressed() {
        ((IDungeon) world.getCurrentRoom()).getController().updateShoot(world, false, true, false);
    }

    public void firePressed() {
        ((IDungeon) world.getCurrentRoom()).getController().updateShoot(world, false, false, true);
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

    @NiftyEventSubscriber(id = "help_window")
    public void onHelpClose(final String id, final WindowClosedEvent event) {
        if (GUI.getInstance().getNifty().getCurrentScreen().findControl("help_popup", HelpPopupControl.class).isHelpSkipChecked()) {
            GUI.getInstance().getWorldInstance().getGlobalVariables().put("skipHelp", true);
        }
    }

    public void closeCurrentPopup() {
        GUI.getInstance().getNifty().setIgnoreKeyboardEvents(true);
        GUI.getInstance().getNifty().closePopup(GUI.getInstance().getNifty().getTopMostPopup().getId());
        world.setPaused(false);
    }
}
