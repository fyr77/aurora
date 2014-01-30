/**
 * User: jedi-philosopher
 * Date: 03.01.13
 * Time: 16:45
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.gui.niffy.ImageButtonController;
import ru.game.aurora.gui.niffy.TopPanelController;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.AlienHomeworld;
import ru.game.aurora.world.space.GalaxyMapScreen;
import ru.game.aurora.world.space.SpaceObject;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.earth.Earth;

public class GalaxyMapController extends GameEventListener implements ScreenController, GameLogger.LoggerAppender {

    private static final long serialVersionUID = 6443855197594505098L;

    private World world;

    private BasePositionable shuttlePosition;

    private transient Screen myScreen;

    private transient ListBox logList;

    private transient TopPanelController topPanelController;

    private BasePlanet planetToScan;

    private transient Element landscapePanel;


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
        topPanelController = myScreen.findControl("top_panel", TopPanelController.class);
        updateStats();
        updateWeapons();
    }

    @Override
    public void onEndScreen() {

    }

    public void openStarMap() {
        if (world.getCurrentRoom().equals(world.getGalaxyMap())) {
            GalaxyMapScreen gms = new GalaxyMapScreen();
            world.setCurrentRoom(gms);
            gms.enter(world);
            myScreen.findNiftyControl("starmap_button", Button.class).setText(Localization.getText("gui", "space.galaxy_map_return"));
        } else {
            world.setCurrentRoom(world.getGalaxyMap());
            myScreen.findNiftyControl("starmap_button", Button.class).setText(Localization.getText("gui", "space.galaxy_map"));
        }
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

    public void openJournal() {
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen("journal_screen");
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void openMenu() {
        GUI.getInstance().showIngameMenu();
    }

    public void nextTurn() {
        world.setUpdatedNextFrame(true);
    }

    public void updateStats() {

        final Ship ship = world.getPlayer().getShip();
        topPanelController.setProgress(String.format(Localization.getText("gui", "space.hull"), ship.getHull(), ship.getMaxHull()), ship.getHull() / (float) ship.getMaxHull());
        topPanelController.setCrewStats(ship.getScientists(), ship.getEngineers(), ship.getMilitary());

        Element shipCoordinates = myScreen.findElementByName("ship_coordinates");
        if (shipCoordinates != null) {
            shipCoordinates.getRenderer(TextRenderer.class).setText(String.format(Localization.getText("gui", "space.ship_coords"), ship.getX(), ship.getY()));
        }
    }

    @Override
    public boolean onTurnEnded(World world) {
        if (GUI.getInstance().getNifty().getCurrentScreen().getScreenController().equals(this)) {
            updateStats();
        }
        return false;
    }

    @Override
    public boolean onPlayerShipDamaged(World world) {
        updateStats();
        return false;
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

    public void nextTargetPressed() {
        world.getCurrentStarSystem().updateShoot(world, true, false, false);
    }

    public void prevTargetPressed() {
        world.getCurrentStarSystem().updateShoot(world, false, true, false);
    }

    public void firePressed() {
        world.getCurrentStarSystem().updateShoot(world, false, false, true);
        cancelPressed(); // close after shooting
    }

    public void cancelPressed() {
        // cancel shoot mode, weapon idx actually means nothing here
        world.getCurrentStarSystem().onWeaponButtonPressed(world, 0);
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

    public void closeCurrentPopup() {
        GUI.getInstance().getNifty().setIgnoreKeyboardEvents(true);
        GUI.getInstance().getNifty().closePopup(GUI.getInstance().getNifty().getTopMostPopup().getId());
        world.setPaused(false);
    }

    public void leftButtonPressed() {
        StarSystem currentStarSystem = world.getCurrentStarSystem();
        if (currentStarSystem != null) {
            if (currentStarSystem.getPlanetAtPlayerShipPosition() != null) {
                land();
                return;
            }
            SpaceObject so = currentStarSystem.getSpaceObjectAtPlayerShipPosition();
            if (so != null) {
                so.onContact(world);
                world.onPlayerContactedAlienShip(so);
            }
        }
    }

    public void rightButtonPressed() {
        if (world.getCurrentStarSystem() != null) {
            BasePlanet planet = world.getCurrentStarSystem().getPlanetAtPlayerShipPosition();
            if (planet != null) {
                scanPlanet(planet);
            }

            SpaceObject spaceObject = world.getCurrentStarSystem().getSpaceObjectAtPlayerShipPosition();
            if (spaceObject != null) {
                scanObject(spaceObject);
            }
        }
    }

    private void land() {
        world.getCurrentStarSystem().landOnCurrentPlanet(world);
    }

    private void scanObject(SpaceObject object) {
        final Nifty nifty = GUI.getInstance().getNifty();
        Element popup = nifty.createPopup("object_scan");
        nifty.showPopup(nifty.getCurrentScreen(), popup.getId(), null);
        GUI.getInstance().getNifty().setIgnoreKeyboardEvents(false);
        world.setPaused(true);
        EngineUtils.setTextForGUIElement(popup.findElementByName("scan_text"), object.getScanDescription(world));
    }

    public void scanPlanet(BasePlanet planet) {
        if (planet == null) {
            return;
        }
        planetToScan = planet;

        world.setPaused(true);
        final Nifty nifty = GUI.getInstance().getNifty();
        Element popup = nifty.createPopup("planet_scan");
        nifty.showPopup(nifty.getCurrentScreen(), popup.getId(), null);
        GUI.getInstance().getNifty().setIgnoreKeyboardEvents(false);

        Draggable shuttleDraggableElement = popup.findNiftyControl("shuttlePosition", Draggable.class);
        shuttleDraggableElement.getElement().setVisible(planet instanceof Planet); // only on these planets player can see shuttle and change its position
        landscapePanel = popup.findElementByName("surfaceMapPanel");


        if (planetToScan instanceof Planet) {
            Planet p = (Planet) planetToScan;
            LandingParty lp = world.getPlayer().getLandingParty();
            final int x = (int) (landscapePanel.getWidth() * (EngineUtils.wrap(lp.getX(), p.getWidth()) / (float) p.getWidth()));
            final int y = (int) (landscapePanel.getHeight() * (EngineUtils.wrap(lp.getY(), p.getHeight()) / (float) p.getHeight()));

            shuttlePosition = new BasePositionable(landscapePanel.getX() + x, landscapePanel.getY() + y);
            shuttleDraggableElement.getElement().setConstraintX(SizeValue.px(shuttlePosition.getX()));
            shuttleDraggableElement.getElement().setConstraintY(SizeValue.px(shuttlePosition.getY()));
            popup.layoutElements();
        }

        EngineUtils.setTextForGUIElement(popup.findElementByName("scan_text"), planet.getScanText().toString());

        if ((planet instanceof Earth) || (planet instanceof AlienHomeworld)) {
            //todo: load custom map
            return;
        }
        if (planet instanceof Planet) {
            world.getCurrentStarSystem().setSurfaceRenderTarget(popup.findElementByName("surfaceMapPanel"), popup.findNiftyControl("bioscan_checkbox", CheckBox.class).isChecked());
        }
    }

    @NiftyEventSubscriber(id = "bioscan_checkbox")
    public void scanFilterDisabled(final String id, final CheckBoxStateChangedEvent event) {
        Element popup = GUI.getInstance().getNifty().getTopMostPopup();
        if (popup == null) {
            return;
        }
        world.getCurrentStarSystem().setSurfaceRenderTarget(popup.findElementByName("surfaceMapPanel"), popup.findNiftyControl("bioscan_checkbox", CheckBox.class).isChecked());
    }

    public void enterStarsystem() {
        if (world.getCurrentStarSystem() != null) {
            return;
        }

        world.getGalaxyMap().enterStarsystemAtPlayerCoordinates();
    }

    @NiftyEventSubscriber(id = "shuttlePosition")
    public void onShuttleDragStarted(final String id, final DraggableDragStartedEvent event) {
        shuttlePosition.setPos(event.getDraggable().getElement().getX(), event.getDraggable().getElement().getY());
    }

    @NiftyEventSubscriber(id = "shuttlePosition")
    public void onShuttleDragEnded(final String id, final DraggableDragCanceledEvent event) {
        Element shuttleDraggableElement = GUI.getInstance().getNifty().getTopMostPopup().findElementByName("shuttlePosition");

        if (landscapePanel.getX() > shuttleDraggableElement.getX() || landscapePanel.getY() > shuttleDraggableElement.getY()) {
            //revert position
            shuttleDraggableElement.setConstraintX(SizeValue.px(shuttlePosition.getX()));
            shuttleDraggableElement.setConstraintY(SizeValue.px(shuttlePosition.getY()));
            return;
        }


        LandingParty lp = world.getPlayer().getLandingParty();
        final int x = (int) (((Planet) planetToScan).getWidth() * ((shuttleDraggableElement.getX() + shuttleDraggableElement.getWidth() / 2 - landscapePanel.getX()) / (float) landscapePanel.getWidth()));
        final int y = (int) (((Planet) planetToScan).getHeight() * ((shuttleDraggableElement.getY() + shuttleDraggableElement.getHeight() / 2 - landscapePanel.getY()) / (float) landscapePanel.getHeight()));
        lp.setPos(x, y);
    }

}
