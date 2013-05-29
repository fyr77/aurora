/**
 * User: jedi-philosopher
 * Date: 03.01.13
 * Time: 16:45
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.GalaxyMapScreen;

public class GalaxyMapController implements ScreenController {

    private World world;

    private Element researchPopup;

    private Element engineeringPopup;


    public GalaxyMapController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        researchPopup = GUI.getInstance().getNifty().createPopup("research_screen");
    }

    @Override
    public void onStartScreen() {

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
        // open research screen
        //ResearchScreen researchScreen = new ResearchScreen();
        //researchScreen.enter(world);
        //world.setCurrentRoom(researchScreen);
        GUI.getInstance().getNifty().showPopup(GUI.getInstance().getNifty().getCurrentScreen(), researchPopup.getId(), null);
    }

    public void openEngineeringScreen() {
        EngineeringScreen engineeringScreen = new EngineeringScreen();
        engineeringScreen.enter(world);
        world.setCurrentRoom(engineeringScreen);
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
