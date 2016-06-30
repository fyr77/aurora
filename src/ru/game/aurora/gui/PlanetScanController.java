package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.Localization;
import ru.game.aurora.gui.niffy.CustomHint;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.AlienHomeworld;
import ru.game.aurora.world.space.PlanetMapRenderer;
import ru.game.aurora.world.space.earth.Earth;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 18.03.14
 * Time: 17:32
 */
public class PlanetScanController implements ScreenController {
    private final World world;

    private BasePositionable shuttlePosition;

    private BasePlanet planetToScan;

    private transient Element landscapePanel;

    private Element myWindow;

    private Draggable shuttleDraggableElement;

    private Element surfaceMapPanel;

    private CheckBox overlayCheckbox;

    private Element atmosphereHint;

    public PlanetScanController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        myWindow = screen.findElementByName("planet_scan_window");

        shuttleDraggableElement = screen.findNiftyControl("shuttlePosition", Draggable.class);
        landscapePanel = myWindow.findElementByName("surfaceMapPanel");

        surfaceMapPanel = myWindow.findElementByName("surfaceMapPanel");

        overlayCheckbox = myWindow.findNiftyControl("bioscan_checkbox", CheckBox.class);

        atmosphereHint = myWindow.findElementByName("scan_text");
    }

    public void setPlanetToScan(BasePlanet planetToScan) {
        this.planetToScan = planetToScan;
    }

    @Override
    public void onStartScreen() {
        myWindow.setVisible(true);
        world.setPaused(true);

        shuttleDraggableElement.getElement().setVisible(planetToScan instanceof Planet); // only on these planets player can see shuttle and change its position
        if (planetToScan instanceof Planet) {
            Planet p = (Planet) planetToScan;
            updateShuttleIconPosition();
            shuttlePosition = new BasePositionable(shuttleDraggableElement.getElement().getX(), shuttleDraggableElement.getElement().getY());
            GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();

            atmosphereHint.getEffects(EffectEventId.onHover, CustomHint.class).get(0).getParameters().setProperty("hintText",
                    Localization.getText("hints", "atmosphere." + p.getAtmosphere().descriptionKey()));
        }

        EngineUtils.setTextForGUIElement(myWindow.findElementByName("scan_text"), planetToScan.getScanText());
        updateButtonsState();

        if ((planetToScan instanceof Earth) || (planetToScan instanceof AlienHomeworld)) {
            //todo: load custom map
            EngineUtils.setImageForGUIElement(surfaceMapPanel, (Image) null);
            return;
        }
        if (planetToScan instanceof Planet) {
            Image planetMap = PlanetMapRenderer.createMap(world
                    , (Planet) planetToScan
                    , new Rectangle(0, 0, surfaceMapPanel.getWidth(), surfaceMapPanel.getHeight())
                    , overlayCheckbox.isChecked()
                    , false
            );
            EngineUtils.setImageForGUIElement(surfaceMapPanel, planetMap);

            if (world.getGlobalVariables().containsKey("tutorial.planet_scan")) {
                world.getGlobalVariables().remove("tutorial.planet_scan");
                HelpPopupControl.showHelp("planet_scan", "planet_scan_2", "planet_scan_3");
            }
        }
    }

    private void updateShuttleIconPosition() {
        Planet planetToScan1 = (Planet) planetToScan;
        final Element element = shuttleDraggableElement.getElement();
        LandingParty lp = world.getPlayer().getLandingParty();
        element.setConstraintX(SizeValue.px(
                (int) (landscapePanel.getX() + EngineUtils.wrap(lp.getX(), planetToScan1.getWidth()) * landscapePanel.getWidth() / (float) planetToScan1.getWidth()) - shuttleDraggableElement.getWidth() / 2
        ));
        element.setConstraintY(SizeValue.px(
                (int) (landscapePanel.getY() + EngineUtils.wrap(lp.getY(), planetToScan1.getHeight()) * landscapePanel.getHeight() / (float) planetToScan1.getHeight()) - shuttleDraggableElement.getHeight() / 2
        ));
    }
    
    private void updateButtonsState() {
        if(planetToScan.canBeLanded()) {
            myWindow.findElementByName("land_button").enable();
            myWindow.findElementByName("lp_button").enable();
            
            myWindow.findElementByName("land_button").getNiftyControl(Button.class).setText(Localization.getText("gui", "space.land"));
        } 
        else if (planetToScan.canBeCommunicated()) {
            //if we cannot land, but can communicate with the planet, enable and rename 
                //land button and disable LP button
            
            myWindow.findElementByName("land_button").enable();
            myWindow.findElementByName("lp_button").disable();
            
            myWindow.findElementByName("land_button").getNiftyControl(Button.class).setText(Localization.getText("gui", "space.hail"));
        } 
        else {
            myWindow.findElementByName("land_button").disable();
            myWindow.findElementByName("lp_button").disable();
            
            myWindow.findElementByName("land_button").getNiftyControl(Button.class).setText(Localization.getText("gui", "space.land"));
        }
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    @NiftyEventSubscriber(id = "bioscan_checkbox")
    public void scanFilterDisabled(final String id, final CheckBoxStateChangedEvent event) {
        if (!(planetToScan instanceof Planet)) {
            return;
        }
        Image planetMap = PlanetMapRenderer.createMap(world
                , (Planet) planetToScan
                , new Rectangle(0, 0, surfaceMapPanel.getWidth(), surfaceMapPanel.getHeight())
                , overlayCheckbox.isChecked()
                , false
        );
        EngineUtils.setImageForGUIElement(surfaceMapPanel, planetMap);
    }

    @NiftyEventSubscriber(id = "shuttlePosition")
    public void onShuttleDragStarted(final String id, final DraggableDragStartedEvent event) {
        shuttlePosition.setPos(event.getDraggable().getElement().getX(), event.getDraggable().getElement().getY());
    }

    @NiftyEventSubscriber(id = "shuttlePosition")
    public void onShuttleDragEnded(final String id, final DraggableDragCanceledEvent event) {
        Element shuttleDraggableElement = GUI.getInstance().getNifty().getTopMostPopup().findElementByName("shuttlePosition");

        final int spriteCenterX = shuttleDraggableElement.getX() + shuttleDraggableElement.getWidth() / 2;
        final int spriteCenterY = shuttleDraggableElement.getY() + shuttleDraggableElement.getHeight() / 2;

        if (landscapePanel.getX() > spriteCenterX
                || landscapePanel.getY() > spriteCenterY
                || landscapePanel.getX() + landscapePanel.getWidth() < spriteCenterX
                || landscapePanel.getY() + landscapePanel.getHeight() < spriteCenterY) {
            //revert position
            shuttleDraggableElement.setConstraintX(SizeValue.px(shuttlePosition.getX()));
            shuttleDraggableElement.setConstraintY(SizeValue.px(shuttlePosition.getY()));
            return;
        }
        final Planet planetToScan1 = (Planet) planetToScan;

        final int x = (int) (planetToScan1.getWidth() * ((spriteCenterX - landscapePanel.getX()) / (float) landscapePanel.getWidth()));
        final int y = (int) (planetToScan1.getHeight() * ((spriteCenterY - landscapePanel.getY()) / (float) landscapePanel.getHeight()));

        LandingParty lp = world.getPlayer().getLandingParty();

        if (!planetToScan1.getMap().isTilePassable(x, y)) {
            //can not place shuttle on an obstacle, set on a closest free point
            planetToScan1.setNearestFreePoint(lp, x, y);
            updateShuttleIconPosition();
            return;
        }


        lp.setPos(x, y);
    }

    @NiftyEventSubscriber(id = "planet_scan_window")
    public void onClose(final String id, final WindowClosedEvent event) {
        closeScreen();
    }

    public void onClick() {
        shuttleDraggableElement.setFocus();
    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }

    public void landingParty() {
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen("landing_party_equip_screen");
    }

    public void land() {
        closeScreen();
        world.setCurrentRoom(planetToScan);
        planetToScan.enter(world);
    }
}
