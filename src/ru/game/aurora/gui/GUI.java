/**
 * User: jedi-philosopher
 * Date: 03.01.13
 * Time: 17:03
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import org.newdawn.slick.GameContainer;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;

import java.util.Iterator;
import java.util.Stack;

public class GUI {
    private Nifty nifty;

    private static GUI instance;

    private Element ingameMenuInstance = null;

    private World worldInstance;

    private GameContainer containerInstance;

    private Stack<String> screens = new Stack<>();

    public static void init(Nifty n) {
        instance = new GUI(n);
    }

    public Nifty getNifty() {
        return nifty;
    }

    public void pushCurrentScreen() {
        pushScreen(nifty.getCurrentScreen().getScreenId());
    }

    public void pushScreen(String id) {
        screens.push(id);
    }

    public String popScreen() {
        return screens.pop();
    }

    public void popAndSetScreen() {
        nifty.gotoScreen(screens.pop());
    }

    private GUI(Nifty n) {
        this.nifty = n;
        nifty.fromXml("gui/screens/main_menu.xml", "main_menu");
    }

    public static GUI getInstance() {
        return instance;
    }

    public void onWorldLoaded(GameContainer con, World world) {
        worldInstance = world;
        containerInstance = con;

        GalaxyMapController galaxyMapController = new GalaxyMapController(world);
        // first register controllers
        nifty.registerScreenController(galaxyMapController);
        nifty.registerScreenController(new ResearchScreenController(world));
        nifty.registerScreenController(new DialogController(world));
        nifty.registerScreenController(new StoryScreenController(world));
        nifty.registerScreenController(new ResearchReportScreenController());
        nifty.registerScreenController(new EarthProgressScreenController(world));
        nifty.registerScreenController(new EarthScreenController(world));
        nifty.registerScreenController(new EngineeringScreenController(world));
        final SurfaceGUIController surfaceGUIController = new SurfaceGUIController(world);
        nifty.registerScreenController(surfaceGUIController);

        // load xmls
        nifty.addXml("gui/screens/progress_bar.xml");
        nifty.addXml("gui/screens/space_gui.xml");
        nifty.addXml("gui/screens/list_screen.xml");
        nifty.addXml("gui/screens/research_screen.xml");
        nifty.addXml("gui/screens/ingame_menu.xml");
        nifty.addXml("gui/screens/dialog_screen.xml");
        nifty.addXml("gui/screens/story_screen.xml");
        nifty.addXml("gui/screens/earth_progress.xml");
        nifty.addXml("gui/screens/engineering_screen.xml");
        nifty.addXml("gui/screens/surface_gui.xml");

        // remove old map controller listener, if it already exists (this is a loaded game). it should actually not be saved at all
        for (Iterator<GameEventListener> iter = world.getListeners().iterator(); iter.hasNext();) {
            GameEventListener gameEventListener = iter.next();
            if (gameEventListener instanceof GalaxyMapController || gameEventListener instanceof SurfaceGUIController) {
                iter.remove();
            }
        }

        world.addListener(galaxyMapController);
        world.addListener(surfaceGUIController);

    }

    public World getWorldInstance() {
        return worldInstance;
    }

    public void setWorldInstance(World worldInstance) {
        this.worldInstance = worldInstance;
    }

    public GameContainer getContainerInstance() {
        return containerInstance;
    }

    public void setContainerInstance(GameContainer containerInstance) {
        this.containerInstance = containerInstance;
    }

    /**
     * show menu with 'continue-save-exit' buttons
     */
    public void showIngameMenu() {
        if (ingameMenuInstance != null) {
            return;
        }
        ingameMenuInstance = nifty.createPopup("ingame_menu");
        nifty.showPopup(nifty.getCurrentScreen(), ingameMenuInstance.getId(), null);
        nifty.setIgnoreKeyboardEvents(false);
    }

    public void closeIngameMenu() {
        nifty.closePopup(ingameMenuInstance.getId());
        ingameMenuInstance = null;
        nifty.setIgnoreKeyboardEvents(true);
    }
}
