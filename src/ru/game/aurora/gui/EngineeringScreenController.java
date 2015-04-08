/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 27.06.13
 * Time: 18:50
 */
package ru.game.aurora.gui;

import com.sun.xml.internal.ws.api.pipe.Engine;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.slick2d.render.image.ImageSlickRenderImage;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.player.engineering.EngineeringProject;
import ru.game.aurora.player.engineering.EngineeringState;
import ru.game.aurora.player.engineering.HullRepairs;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;

import java.util.Map;


public class EngineeringScreenController implements ScreenController {
    private final World world;

    EngineeringState engineeringState;

    private Element pointsText;

    private Element engiText;

    private Element ruText;

    private ListBox projectsList;

    private Element window;

    private Element imagePanel;

    private Element textElement;


    public EngineeringScreenController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        window = screen.findElementByName("engineering_window");
        pointsText = screen.findElementByName("hullPointsToRepair");
        engiText = screen.findElementByName("assignedEngineers");
        ruText = screen.findElementByName("requiredRuText");
        projectsList = screen.findNiftyControl("itemsList", ListBox.class);
        imagePanel = screen.findElementByName("selectedItemImg");
        textElement = screen.findElementByName("selectedItemText");
    }

    @Override
    public void onStartScreen() {
        window.setVisible(true);
        EngineUtils.resetScrollbarX(projectsList);
        projectsList.clear();
        projectsList.addAllItems(world.getPlayer().getEngineeringState().getProjects());
        engineeringState = world.getPlayer().getEngineeringState();
        updateLabels();
        world.setPaused(true);
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    private void updateLabels() {
        pointsText.getRenderer(TextRenderer.class).setText(Localization.getText("gui", "engineering.repairs.hull_points_to_repair") + " " + engineeringState.getHullRepairs().remainingPoints);
        engiText.getRenderer(TextRenderer.class).setText(Localization.getText("gui", "engineering.repairs.assigned_engineers") + " " + engineeringState.getHullRepairs().engineersAssigned);
        ruText.getRenderer(TextRenderer.class).setText(String.format(Localization.getText("gui", "engineering.repairs.resource_units_required"), engineeringState.getHullRepairs().calcResCost(world), world.getPlayer().getResourceUnits() + (engineeringState.getHullRepairs().remainingPoints > 0 ? +HullRepairs.POINT_RES_COST : 0)));
    }

    public void onHullPointsDecreased() {
        if (engineeringState.getHullRepairs().remainingPoints > 0) {
            engineeringState.getHullRepairs().remainingPoints--;
            if (engineeringState.getHullRepairs().remainingPoints == 0) {
                // returning resource unis
                world.getPlayer().setResourceUnits(world.getPlayer().getResourceUnits() + HullRepairs.POINT_RES_COST);
                engineeringState.getHullRepairs().resetProgress();
            }
            updateLabels();
        }
    }

    public void onHullPointsIncreased() {
        if (world.getPlayer().getShip().getHull() + engineeringState.getHullRepairs().remainingPoints < world.getPlayer().getShip().getMaxHull()) {
            if (HullRepairs.POINT_RES_COST > world.getPlayer().getResourceUnits()) {
                return;
            }
            if (engineeringState.getHullRepairs().remainingPoints == 0) {
                engineeringState.getHullRepairs().resetProgress();
                // subtracting res for first point
                world.getPlayer().setResourceUnits(world.getPlayer().getResourceUnits() - HullRepairs.POINT_RES_COST);
            }
            engineeringState.getHullRepairs().remainingPoints++;
            updateLabels();
        }
    }

    public void onEngineersDecreased() {
        if (engineeringState.getHullRepairs().engineersAssigned > 0) {
            engineeringState.getHullRepairs().engineersAssigned--;
            engineeringState.addIdleEngineers(1);
            updateLabels();
        }
    }

    public void onEngineersIncreased() {
        if (engineeringState.getIdleEngineers() > 0 && engineeringState.getHullRepairs().remainingPoints > 0) {
            engineeringState.getHullRepairs().engineersAssigned++;
            engineeringState.setIdleEngineers(engineeringState.getIdleEngineers() - 1);
            updateLabels();
        }
    }

    @NiftyEventSubscriber(id = "engineering_window")
    public void onClose(final String id, final WindowClosedEvent event) {
        closeScreen();
    }

    public void closeScreen() {
        GUI.getInstance().getNifty().gotoScreen(GUI.getInstance().popScreen());
    }


    @NiftyEventSubscriber(id = "itemsList")
    public void onListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent event) {


        if (event.getSelection().isEmpty()) {
            EngineUtils.setTextForGUIElement(textElement, Localization.getText("gui", "no_item_selected"));
            imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage("no_image"))));
            return;
        }
        EngineeringProject ep = (EngineeringProject) event.getSelection().get(0);
        String info = ep.getLocalizedText("engineering");
        final Map<InventoryItem, Integer> cost = ep.getCost();
        if (!cost.isEmpty()) {
            info = info + "\n\n";
            for (Map.Entry<InventoryItem, Integer> entry : cost.entrySet()) {
                info += entry.getKey().getName() + ": " + entry.getValue() + "\n";
            }

            if (ep.isProjectStarted()) {
                info = info + String.format(Localization.getText("gui", "engineering.upgrade_in_progress"), ep.getRemainingDays(world));
            } else {
                if (!ep.checkEnoughResources(world)) {
                    info = info + Localization.getText("gui", "logging.not_enough_resources");
                }
            }
        }
        EngineUtils.setTextForGUIElement(textElement, info);
        imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ep.getDrawable().getImage())));
        GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();
    }

    public void onIncreaseEngineersButtonClicked() {
        if (projectsList.getSelection().isEmpty()) {
            return;
        }
        final int idleEngineers = world.getPlayer().getEngineeringState().getIdleEngineers();
        if (idleEngineers == 0) {
            return;
        }
        EngineeringProject rp = (EngineeringProject) projectsList.getSelection().get(0);
        if (!rp.isProjectStarted() && !rp.checkEnoughResources(world)) {
            return;
        }
        rp.changeEngineers(1, world);
        world.getPlayer().getEngineeringState().setIdleEngineers(idleEngineers - 1);
        projectsList.refresh();
    }

    public void onDecreaseEngineersButtonClicked() {
        if (projectsList.getSelection().isEmpty()) {
            return;
        }
        EngineeringProject ep = (EngineeringProject) projectsList.getSelection().get(0);
        if (ep.getEngineersAssigned() == 0) {
            return;
        }
        ep.changeEngineers(-1, world);
        world.getPlayer().getEngineeringState().setIdleEngineers(world.getPlayer().getEngineeringState().getIdleEngineers() + 1);
        projectsList.refresh();
    }


    // works for increase/decrease scientists buttons, makes item in list selected (by default clicking on button does not select item in list)
    @NiftyEventSubscriber(pattern = ".*crease_engineers")
    public void onClicked(String id, ButtonClickedEvent event) {

        projectsList.selectItem(event.getButton().getElement().getParent().getUserData());
    }
}
