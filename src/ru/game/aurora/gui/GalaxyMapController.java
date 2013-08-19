/**
 * User: jedi-philosopher
 * Date: 03.01.13
 * Time: 16:45
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.gui.niffy.ImageButtonController;
import ru.game.aurora.gui.niffy.ProgressBarControl;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.GalaxyMapScreen;
import ru.game.aurora.world.space.StarSystem;

public class GalaxyMapController extends GameEventListener implements ScreenController, GameLogger.LoggerAppender {

    private static final long serialVersionUID = 6443855197594505098L;

    private World world;

    private transient Screen myScreen;

    private transient ListBox logList;


    public GalaxyMapController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        myScreen = screen;
        GameLogger.getInstance().addAppender(this);
        logList = screen.findNiftyControl("log_list", ListBox.class);
        if (logList != null) {
            logList.clear();
            logList.addAllItems(GameLogger.getInstance().getLogItems());
            logList.setFocusItemByIndex(logList.getItems().size() - 1);
        }
    }

    @Override
    public void onStartScreen() {
        GUI.getInstance().getNifty().setIgnoreKeyboardEvents(true);
        myScreen = GUI.getInstance().getNifty().getCurrentScreen();
        updateStats();
        updateWeapons();
    }

    @Override
    public void onEndScreen() {

    }

    public void openStarMap() {
        GalaxyMapScreen gms = new GalaxyMapScreen();
        world.setCurrentRoom(gms);
        gms.enter(world);
    }

    public void openResearchScreen() {
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen("research_screen");
    }

    public void openEngineeringScreen() {
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen("engineering_screen");
    }

    public void openLandingPartyScreen() {
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen("landing_party_equip_screen");
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void openMenu() {
        GUI.getInstance().showIngameMenu();
    }

    public void updateStats() {
        ProgressBarControl pb = myScreen.findControl("ship_hp", ProgressBarControl.class);
        if (pb == null) {
            return;
        }
        final Ship ship = world.getPlayer().getShip();
        pb.setProgress(ship.getHull() / (float) ship.getMaxHull());
        pb.setText(String.format("Hull: %d/%d", ship.getHull(), ship.getMaxHull()));

        Element crewStatus = myScreen.findElementByName("crew_status");
        if (crewStatus != null) {
            crewStatus.getRenderer(TextRenderer.class).setText(String.format("Scientists: %d, Engineers: %d, Military: %d", ship.getScientists(), ship.getEngineers(), ship.getMilitary()));
        }


        Element shipCoordinates = myScreen.findElementByName("ship_coordinates");
        if (shipCoordinates != null) {
            shipCoordinates.getRenderer(TextRenderer.class).setText(String.format("Ship coordinates: [%d, %d]", ship.getX(), ship.getY()));
        }
    }

    @Override
    public void onTurnEnded(World world) {
        if (!GUI.getInstance().getNifty().getCurrentScreen().getScreenController().equals(this)) {
            return;
        }
        updateStats();
    }

    @Override
    public void onPlayerShipDamaged(World world) {
        updateStats();
    }

    public void updateWeapons() {
        for (int i = 0; i < 4; ++i) {
            final ImageButtonController buttonControl = myScreen.findNiftyControl("weapon_" + i + "_button", ImageButtonController.class);
            if (buttonControl == null) {
                // this is another window controlled by same controller
                return;
            }
            if (i < world.getPlayer().getShip().getWeapons().size()) {
                buttonControl.enable();
                buttonControl.setImage(ResourceManager.getInstance().getImage(world.getPlayer().getShip().getWeapons().get(i).getWeaponDesc().image));
                //buttonControl.getElement().getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage(world.getPlayer().getShip().getWeapons().get(i).getWeaponDesc().image))));
            } else {
                buttonControl.disable();
            }
        }
    }

    public void weaponClicked(String weaponIdx) {
        StarSystem ss = world.getCurrentStarSystem();
        if (ss == null) {
            return;
        }

        ss.onWeaponButtonPressed(world, Integer.parseInt(weaponIdx));
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

    public void closeLandingPartyLostPopup() {
        GUI.getInstance().getNifty().setIgnoreKeyboardEvents(true);
        GUI.getInstance().getNifty().closePopup(GUI.getInstance().getNifty().getTopMostPopup().getId());
    }
}
