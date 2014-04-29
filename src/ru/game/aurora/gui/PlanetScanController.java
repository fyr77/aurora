package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;
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
    private World world;

    private BasePositionable shuttlePosition;

    private BasePlanet planetToScan;

    private transient Element landscapePanel;

    private Element myWindow;

    private Draggable shuttleDraggableElement;

    private Element surfaceMapPanel;

    private CheckBox overlayCheckbox;

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
    }

    @Override
    public void onStartScreen() {
        myWindow.setVisible(true);
        planetToScan = world.getCurrentStarSystem().getPlanetAtPlayerShipPosition();

        world.setPaused(true);

        shuttleDraggableElement.getElement().setVisible(planetToScan instanceof Planet); // only on these planets player can see shuttle and change its position
        if (planetToScan instanceof Planet) {
            Planet p = (Planet) planetToScan;
            LandingParty lp = world.getPlayer().getLandingParty();
            final int x = (int) (landscapePanel.getWidth() * (EngineUtils.wrap(lp.getX(), p.getWidth()) / (float) p.getWidth()));
            final int y = (int) (landscapePanel.getHeight() * (EngineUtils.wrap(lp.getY(), p.getHeight()) / (float) p.getHeight()));

            shuttlePosition = new BasePositionable(landscapePanel.getX() + x, landscapePanel.getY() + y);
            shuttleDraggableElement.getElement().setConstraintX(SizeValue.px(shuttlePosition.getX()));
            shuttleDraggableElement.getElement().setConstraintY(SizeValue.px(shuttlePosition.getY()));
            GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();
        }

        EngineUtils.setTextForGUIElement(myWindow.findElementByName("scan_text"), planetToScan.getScanText().toString());

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

        }
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    @NiftyEventSubscriber(id = "bioscan_checkbox")
    public void scanFilterDisabled(final String id, final CheckBoxStateChangedEvent event) {
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

        if (landscapePanel.getX() > spriteCenterX || landscapePanel.getY() > spriteCenterY) {
            //revert position
            shuttleDraggableElement.setConstraintX(SizeValue.px(shuttlePosition.getX()));
            shuttleDraggableElement.setConstraintY(SizeValue.px(shuttlePosition.getY()));
            return;
        }
        final Planet planetToScan1 = (Planet) planetToScan;

        final int x = (int) (planetToScan1.getWidth() * ((spriteCenterX - landscapePanel.getX()) / (float) landscapePanel.getWidth()));
        final int y = (int) (planetToScan1.getHeight() * ((spriteCenterY - landscapePanel.getY()) / (float) landscapePanel.getHeight()));

        if (!planetToScan1.getMap().isTilePassable(x, y)) {
            //can not place shuttle on an obstacle, revert position
            shuttleDraggableElement.setConstraintX(SizeValue.px(shuttlePosition.getX()));
            shuttleDraggableElement.setConstraintY(SizeValue.px(shuttlePosition.getY()));
            return;
        }

        LandingParty lp = world.getPlayer().getLandingParty();

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
        GUI.getInstance().getNifty().gotoScreen(GUI.getInstance().popScreen());
    }

    public void landingParty() {
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen("landing_party_equip_screen");
    }

    public void land() {
        closeScreen();
        world.getCurrentStarSystem().landOnCurrentPlanet(world);
    }
}
