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
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.InputBinding;
import ru.game.aurora.application.Localization;
import ru.game.aurora.gui.niffy.TopPanelController;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.*;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;


public class SurfaceGUIController extends GameEventListener implements GameLogger.LoggerAppender, ScreenController, Updatable {
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
        logList.setFocusItemByIndex(0);


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
            logList.removeItemByIndex(logList.getItems().size() - 1);
        }
        logList.insertItem(message, 0);
        logList.setFocusItemByIndex(0);
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

    public void nextTargetPressed() {
        ((IDungeon) world.getCurrentRoom()).getController().aimNextTarget(world);
    }

    public void prevTargetPressed() {
        ((IDungeon) world.getCurrentRoom()).getController().aimPrevTarget(world);
    }

    public void firePressed() { ((IDungeon) world.getCurrentRoom()).getController().updateShootFire(world); }

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

    public void openMap() {
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen("surface_map_screen");
    }

    public void nextTurn() {
        world.setUpdatedNextFrame(true);
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

    @Override
    public void update(GameContainer container, World world) {
        final Input input = container.getInput();

        if (input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INVENTORY))) {
            openInventory();
            return;
        }
        else if(input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.MAP))){
            openMap();
            return;
        }
        else {
            final Room currentRoom = world.getCurrentRoom();
            final GUI gui = GUI.getInstance();

            if (input.isKeyPressed(Input.KEY_ESCAPE) && (currentRoom instanceof Planet || currentRoom instanceof Dungeon)) {
                Element popup = gui.getNifty().getTopMostPopup();
                if (popup != null && popup.findElementByName("menu_window") != null) {
                    gui.closeIngameMenu();
                }
                else {
                    gui.showIngameMenu();
                }
                return;
            }
        }
    }
}
