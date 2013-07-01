/**
 * User: jedi-philosopher
 * Date: 03.01.13
 * Time: 16:45
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.button.ButtonControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.gui.niffy.ProgressBarControl;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.GalaxyMapScreen;
import ru.game.aurora.world.space.StarSystem;

public class GalaxyMapController extends GameEventListener implements ScreenController
{

    private static final long serialVersionUID = 6443855197594505098L;

    private World world;

    private Screen myScreen;

    public GalaxyMapController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        myScreen = screen;
    }

    @Override
    public void onStartScreen() {
        GUI.getInstance().getNifty().setIgnoreKeyboardEvents(true);
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

    public void setWorld(World world) {
        this.world = world;
    }

    public void openMenu() {
        GUI.getInstance().showIngameMenu();
    }

    public void updateStats()
    {
        ProgressBarControl pb = myScreen.findControl("ship_hp", ProgressBarControl.class);
        if (pb == null) {
            return;
        }
        final Ship ship = world.getPlayer().getShip();
        pb.setProgress(ship.getHull() / (float) ship.getMaxHull());
        pb.setText(String.format("Hull: %d/%d", ship.getHull(), ship.getMaxHull()));

        Element crewStatus = myScreen.findElementByName("crew_status");
        if (crewStatus == null) {
            return;
        }
        crewStatus.getRenderer(TextRenderer.class).setText(String.format("Scientists: %d, Engineers: %d, Military: %d", ship.getScientists(), ship.getEngineers(), ship.getMilitary()));


    }

    @Override
    public void onTurnEnded(World world) {
        updateStats();
    }

    @Override
    public void onPlayerShipDamaged(World world) {
        updateStats();
    }

    public void updateWeapons()
    {
        for (int i = 0; i < 4; ++i) {
            final ButtonControl buttonControl = myScreen.findControl("weapon_" + i + "_button", ButtonControl.class);
            if (buttonControl == null) {
                // this is another window controlled by same controller
                return;
            }
            if (i < world.getPlayer().getShip().getWeapons().size()) {
                buttonControl.enable();
            } else {
                buttonControl.disable();
            }
        }
    }

    public void weaponClicked(String weaponIdx)
    {
        StarSystem ss = world.getCurrentStarSystem();
        if (ss == null) {
            return;
        }

        ss.onWeaponButtonPressed(world, Integer.parseInt(weaponIdx));
    }
}
