/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 27.06.13
 * Time: 18:50
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.WindowClosedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.player.engineering.EngineeringState;
import ru.game.aurora.player.engineering.HullRepairs;
import ru.game.aurora.world.World;


public class EngineeringScreenController implements ScreenController
{
    private World world;

    EngineeringState engineeringState;

    private Element pointsText;

    private Element engiText;

    private Element ruText;

    public EngineeringScreenController(World world) {
        this.world = world;
        engineeringState = world.getPlayer().getEngineeringState();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        pointsText = screen.findElementByName("hullPointsToRepair");
        engiText = screen.findElementByName("assignedEngineers");
        ruText = screen.findElementByName("requiredRuText");
    }

    @Override
    public void onStartScreen() {
        updateLabels();
    }

    @Override
    public void onEndScreen() {

    }

    private void updateLabels()
    {
        pointsText.getRenderer(TextRenderer.class).setText("Hull points to repair: " + engineeringState.getHullRepairs().remainingPoints);
        engiText.getRenderer(TextRenderer.class).setText("Assigned engineers: " + engineeringState.getHullRepairs().engineersAssigned);
        ruText.getRenderer(TextRenderer.class).setText("Requires " + engineeringState.getHullRepairs().calcResCost() + " out of " + world.getPlayer().getResourceUnits() + " resource units");
    }

    public void onHullPointsDecreased()
    {
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

    public void onHullPointsIncreased()
    {
        if (world.getPlayer().getShip().getHull() + engineeringState.getHullRepairs().remainingPoints < world.getPlayer().getShip().getMaxHull()) {
            if (engineeringState.getHullRepairs().remainingPoints == 0) {
                engineeringState.getHullRepairs().resetProgress();
            }
            engineeringState.getHullRepairs().remainingPoints++;
            updateLabels();
        }
    }

    public void onEngineersDecreased()
    {
        if (engineeringState.getHullRepairs().engineersAssigned > 0) {
            engineeringState.getHullRepairs().engineersAssigned--;
            engineeringState.addIdleEngineers(1);
            updateLabels();
        }
    }

    public void onEngineersIncreased()
    {

        if (engineeringState.getIdleEngineers() > 0) {
            engineeringState.getHullRepairs().engineersAssigned++;
            engineeringState.setIdleEngineers(engineeringState.getIdleEngineers() - 1);
            updateLabels();
        }
    }

    @NiftyEventSubscriber(id="story_window")
    public void onClose(final String id, final WindowClosedEvent event)
    {
        closeScreen();
    }

    public void closeScreen()
    {
        GUI.getInstance().getNifty().gotoScreen(GUI.getInstance().popScreen());
    }
}
