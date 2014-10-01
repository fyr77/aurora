/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 27.06.13
 * Time: 18:50
 */
package ru.game.aurora.gui;

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


public class EngineeringScreenController implements ScreenController {
    private final World world;

    EngineeringState engineeringState;

    private Element pointsText;

    private Element engiText;

    private Element ruText;

    private TabGroup tg;

    private ListBox projectsList;

    private Element window;


    public EngineeringScreenController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        window = screen.findElementByName("engineering_window");
        tg = screen.findNiftyControl("engineering_tabs", TabGroup.class);
        pointsText = screen.findElementByName("hullPointsToRepair");
        engiText = screen.findElementByName("assignedEngineers");
        ruText = screen.findElementByName("requiredRuText");
        projectsList = screen.findNiftyControl("itemsList", ListBox.class);
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
        if (tg.getTabCount() == 0) {
            return;
        }
        if (tg.getSelectedTabIndex() != 1) {
            return;
        }
        Element imagePanel = tg.getSelectedTab().getElement().findElementByName("selectedItemImg");
        TextRenderer tr = tg.getSelectedTab().getElement().findElementByName("selectedItemText").getRenderer(TextRenderer.class);
        if (event.getSelection().isEmpty()) {
            tr.setText(Localization.getText("gui", "no_item_selected"));
            imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage("no_image"))));
            return;
        }
        EngineeringProject ep = (EngineeringProject) event.getSelection().get(0);
        String info = ep.getLocalizedText("engineering");
        if (!ep.getCost().isEmpty()) {
            info = info + "\n\n";
            if (ep.isProjectStarted()) {
                info = info + String.format(Localization.getText("gui", "engineering.upgrade_in_progress"), ep.getRemainingDays(world));
            } else {
                if (!ep.checkEnoughResources(world)) {
                    info = info + Localization.getText("gui", "logging.not_enough_resources");
                } else {
                    info = info + String.format(Localization.getText("gui", "engineering.resource_cost"), ep.getCost());
                }
            }
        }
        tr.setText(info);
        imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ep.getDrawable().getImage())));
        GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();
    }

    public void onIncreaseEngineersButtonClicked() {
        ListBox avail = tg.getSelectedTab().getElement().findNiftyControl("itemsList", ListBox.class);
        if (avail.getSelection().isEmpty()) {
            return;
        }
        final int idleEngineers = world.getPlayer().getEngineeringState().getIdleEngineers();
        if (idleEngineers == 0) {
            return;
        }
        EngineeringProject rp = (EngineeringProject) avail.getSelection().get(0);
        if (!rp.isProjectStarted() && !rp.checkEnoughResources(world)) {
            return;
        }
        rp.changeEngineers(1, world);
        world.getPlayer().getEngineeringState().setIdleEngineers(idleEngineers - 1);
        avail.refresh();
    }

    public void onDecreaseEngineersButtonClicked() {
        ListBox avail = tg.getSelectedTab().getElement().findNiftyControl("itemsList", ListBox.class);
        if (avail.getSelection().isEmpty()) {
            return;
        }
        EngineeringProject ep = (EngineeringProject) avail.getSelection().get(0);
        if (ep.getEngineersAssigned() == 0) {
            return;
        }
        ep.changeEngineers(-1, world);
        world.getPlayer().getEngineeringState().setIdleEngineers(world.getPlayer().getEngineeringState().getIdleEngineers() + 1);
        avail.refresh();
    }


    // works for increase/decrease scientists buttons, makes item in list selected (by default clicking on button does not select item in list)
    @NiftyEventSubscriber(pattern = ".*crease_engineers")
    public void onClicked(String id, ButtonClickedEvent event) {

        int numericId = Integer.parseInt(id.split("#")[0]);
        ListBox itemsList = tg.getSelectedTab().getElement().findNiftyControl("itemsList", ListBox.class);
        // hack. No idea how ids are distributed between list elements, they seem to start from arbitrary number and be sorted in ascending order
        // so in order to get index of clicked element, must subtract from its id id of the first one
        numericId -= Integer.parseInt(itemsList.getElement().findElementByName("#child-root").getElements().get(0).getId());
        itemsList.selectItemByIndex(numericId);
    }
}
