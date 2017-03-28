/**
 * User: jedi-philosopher
 * Date: 03.01.13
 * Time: 16:45
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.WindowClosedEvent;
import de.lessvoid.nifty.effects.Effect;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.InputBinding;
import ru.game.aurora.application.Localization;
import ru.game.aurora.gui.niffy.CustomHint;
import ru.game.aurora.gui.niffy.ImageButtonController;
import ru.game.aurora.gui.niffy.InteractionTargetSelectorController;
import ru.game.aurora.gui.niffy.TopPanelController;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.*;
import ru.game.aurora.world.equip.WeaponDesc;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetLifeUpdater;
import ru.game.aurora.world.space.GalaxyMap;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.List;

@SuppressWarnings("unused")
public class GalaxyMapController extends GameEventListener implements ScreenController, GameLogger.LoggerAppender, Updatable {

    private static final long serialVersionUID = 6443855197594505098L;

    private World world;

    private transient Screen myScreen;

    private transient ListBox logList;

    private transient TopPanelController topPanelController;


    public GalaxyMapController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        myScreen = screen;
        GameLogger.getInstance().addAppender(this);
    }

    @Override
    public void onStartScreen() {
        GUI.getInstance().getNifty().setIgnoreKeyboardEvents(true);
        myScreen = GUI.getInstance().getNifty().getCurrentScreen();
        topPanelController = myScreen.findControl("top_panel", TopPanelController.class);
        updateStats();
        updateWeapons();

        logList = myScreen.findNiftyControl("log_list", ListBox.class);
        if (logList != null) {
            logList.clear();
            logList.addAllItems(GameLogger.getInstance().getLogItems());
            logList.setFocusItemByIndex(0);
        }
        if (world.getGlobalVariables().containsKey("tutorial.starmap")) {
            world.getGlobalVariables().remove("tutorial.starmap");
            HelpPopupControl.showHelp("galaxy_map", "galaxy_map_2", "galaxy_map_3");
        }
    }

    @Override
    public void onEndScreen() {

    }

    public void openStarMap()  { openScreen("star_map_screen"); }

    public void openResearchScreen() { openScreen("research_screen"); }

    public void openEngineeringScreen() {
        openScreen("engineering_screen");
    }

    public void openShipScreen() {
        openScreen("ship_screen");
    }

    public void openJournal() {
        openScreen("journal_screen");
    }

    public void openLandingPartyScreen() {
        openScreen("landing_party_equip_screen");
    }

    private void openScreen(String screenId){
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen(screenId);
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
            String text = String.format(Localization.getText("gui", "space.ship_coords"), ship.getTargetX(), ship.getTargetY());
            GalaxyMapObject mo = world.getGalaxyMap().getObjectAt(ship.getTargetX(), ship.getTargetY());
            if (mo != null) {
                text += "\n" + mo.getName();
            }
            shipCoordinates.getRenderer(TextRenderer.class).setText(text);
        }

        Element ruText = myScreen.findElementByName("resources_text");
        EngineUtils.setTextForGUIElement(ruText, Localization.getText("gui", "resources") + " " + world.getPlayer().getResourceUnits());
        EngineUtils.setTextForGUIElement(myScreen.findElementByName("credits_text"), Localization.getText("gui", "credits") + " " + world.getPlayer().getCredits());

        Element dateText = myScreen.findElementByName("stardate_text");
        EngineUtils.setTextForGUIElement(dateText, world.getCurrentDateString());
    }

    @Override
    public boolean onCrewChanged(World world) {
        if(topPanelController == null) {
            return false;
        }
        
        Ship ship = world.getPlayer().getShip();
        topPanelController.setCrewStats(ship.getScientists(), ship.getEngineers(), ship.getMilitary());
        return false;
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
                final WeaponDesc weaponDesc = world.getPlayer().getShip().getWeapons().get(i).getWeaponDesc();
                buttonControl.setImage(weaponDesc.getImage());
                // update hint
                List<Effect> effects = buttonControl.getElement().getEffects(EffectEventId.onHover, CustomHint.class);
                final String hintText = String.format(Localization.getText("gui", "space.weapon_tooltip")
                        , weaponDesc.getName()
                        , weaponDesc.getDamageInfo()
                        , weaponDesc.getRange()
                        , weaponDesc.getReloadTurns());

                effects.get(0).getParameters().setProperty("hintText", hintText);
                //buttonControl.getElement().getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage(world.getPlayer().getShip().getWeapons().get(i).getWeaponDesc().image))));
            } else {
                buttonControl.setImage(null);
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
            logList.removeItemByIndex(logList.getItems().size() - 1);
        }
        logList.insertItem(message, 0);
        logList.setFocusItemByIndex(0);
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
            currentStarSystem.interactWithObjectAtShipPosition(world);
        }
    }

    public void rightButtonPressed() {
        scanAction(world);
    }

    public void scanAction(final World world) {
        if (world.getCurrentStarSystem() != null) {
            List<GameObject> objects = world.getCurrentStarSystem().getGameObjectsAtPosition(world.getPlayer().getShip());
            if (objects.isEmpty()) {
                return;
            }
            if (objects.size() == 1) {
                if (BasePlanet.class.isAssignableFrom(objects.get(0).getClass())) {
                    scanPlanet((BasePlanet) objects.get(0));
                } else {
                    scanObject(world, objects.get(0));
                }
                return;
            }

            InteractionTargetSelectorController.open(new IStateChangeListener<GameObject>() {
                private static final long serialVersionUID = -8114467555795780919L;

                @Override
                public void stateChanged(GameObject param) {
                    if (BasePlanet.class.isAssignableFrom(param.getClass())) {
                        scanPlanet((BasePlanet) param);
                    } else {
                        scanObject(world, param);
                    }
                }
            }, objects);
        }
    }

    private void scanObject(World world, GameObject object) {
        final Nifty nifty = GUI.getInstance().getNifty();
        Element popup = nifty.createPopupWithId("object_scan", "object_scan");
        nifty.showPopup(nifty.getCurrentScreen(), popup.getId(), null);
        GUI.getInstance().getNifty().setIgnoreKeyboardEvents(false);
        world.setPaused(true);
        EngineUtils.setTextForGUIElement(popup.findElementByName("scan_text"), object.getScanDescription(world));
    }

    private void scanPlanet(BasePlanet planet) {
        if (planet == null) {
            return;
        }
        if(planet instanceof Planet) {
            PlanetLifeUpdater.updateLife((Planet)planet);
        }
        GUI.getInstance().pushCurrentScreen();
        PlanetScanController psc = (PlanetScanController) GUI.getInstance().getNifty().findScreenController(PlanetScanController.class.getCanonicalName());
        psc.setPlanetToScan(planet);
        GUI.getInstance().getNifty().gotoScreen("planet_scan_screen");
    }

    public void enterStarsystem() {
        if (world.getCurrentStarSystem() != null) {
            return;
        }

        world.getGalaxyMap().enterStarsystemAtPlayerCoordinates();
    }

    @NiftyEventSubscriber(id = "help_window")
    public void onHelpClose(final String id, final WindowClosedEvent event) {
        if (GUI.getInstance().getNifty().getCurrentScreen().findControl("help_popup", HelpPopupControl.class).isHelpSkipChecked()) {
            GUI.getInstance().getWorldInstance().getGlobalVariables().put("skipHelp", true);
        }
    }

    public GalaxyMapObject getGalaxyMapObjectAtMouseCoords() {
        int x = GUI.getInstance().getNifty().getNiftyMouse().getX();
        int y = GUI.getInstance().getNifty().getNiftyMouse().getY();

        x = world.getCamera().getPointTileX(x);
        y = world.getCamera().getPointTileY(y);
        double minDist = Double.POSITIVE_INFINITY;
        GalaxyMapObject result = null;
        for (GalaxyMapObject gmo : world.getGalaxyMap().getGalaxyMapObjects()) {
            double newDist = BasePositionable.getDistance(gmo.getX(), gmo.getY(), x, y);
            if (newDist < minDist) {
                result = gmo;
                minDist = newDist;
            }
        }
        return minDist <= 1.5 ? result : null;
    }

    public void doAttack() {
        closeCurrentPopup();
        world.getCurrentStarSystem().updateShoot(world, false, false, true);
    }

    @Override
    public void update(GameContainer container, World world) {
        final Input input = container.getInput();

        if (input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.ENGINEERING))) {
            openEngineeringScreen();
            return;
        }
        else if(input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.RESEARCH))){
            openResearchScreen();
            return;
        }
        else if(input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.LANDING_PARTY))){
            openLandingPartyScreen();
            return;
        }
        else if(input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.MAP))){
            openStarMap();
            return;
        }
        else if(input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INVENTORY))){
            openShipScreen();
            return;
        }
        else if(input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.JOURNAL))){
            openJournal();
            return;
        }
        else if(input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.SCAN))){
            scanAction(world);
            return;
        }
        else {
            final Room currentRoom = world.getCurrentRoom();
            final GUI gui = GUI.getInstance();

            if (input.isKeyPressed(Input.KEY_ESCAPE) && (currentRoom instanceof GalaxyMap || currentRoom instanceof StarSystem)) {
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
