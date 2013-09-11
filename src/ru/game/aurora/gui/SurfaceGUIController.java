/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 02.07.13
 * Time: 16:13
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.gui.niffy.ProgressBarControl;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.Planet;


public class SurfaceGUIController extends GameEventListener implements ScreenController, GameLogger.LoggerAppender {
    private static final long serialVersionUID = -7781914700046409079L;

    private World world;

    private transient Screen myScreen;

    private transient ListBox logList;

    private transient ProgressBarControl oxygen;

    public SurfaceGUIController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        myScreen = screen;
        GameLogger.getInstance().addAppender(this);
        logList = screen.findNiftyControl("log_list", ListBox.class);
        oxygen = screen.findControl("oxygen", ProgressBarControl.class);
    }

    @Override
    public void onStartScreen() {
        updateStats();
        logList.clear();
        logList.addAllItems(GameLogger.getInstance().getLogItems());
        logList.setFocusItemByIndex(logList.getItems().size() - 1);
    }

    @Override
    public void onEndScreen() {

    }

    public void updateStats() {
        LandingParty landingParty = world.getPlayer().getLandingParty();
        Element crewStatus = myScreen.findElementByName("crew_status");
        if (crewStatus == null) {
            return;
        }
        crewStatus.getRenderer(TextRenderer.class).setText(String.format("Scientists: %d, Engineers: %d, Military: %d", landingParty.getScience(), landingParty.getEngineers(), landingParty.getMilitary()));

        oxygen.setProgress(landingParty.getOxygen() / (float) LandingParty.MAX_OXYGEN);
        oxygen.setText("Remaining oxygen: " + landingParty.getOxygen());
    }

    @Override
    public void onTurnEnded(World world) {
        if (!(world.getCurrentRoom() instanceof Planet)) {
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
    }
}
