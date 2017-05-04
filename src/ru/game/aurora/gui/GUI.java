/**
 * User: jedi-philosopher
 * Date: 03.01.13
 * Time: 17:03
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.EndOfScreenAction;
import de.lessvoid.nifty.slick2d.render.font.AbstractSlickRenderFont;
import de.lessvoid.nifty.slick2d.render.font.UnicodeSlickRenderFont;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.MainMenuController;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.gui.password_input.PasswordInputController;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Stack;

public class GUI {
    private static GUI instance;
    private final Nifty nifty;
    private final Stack<String> screens = new Stack<>();

    private Element ingameMenuInstance = null;
    private World worldInstance;
    private GameContainer containerInstance;
    private String nextScreen = null;

    private GUI(GameContainer con, Nifty n) {
        this.nifty = n;
        Localization.registerGUIBungles(nifty);
        nifty.registerMouseCursor("hand", "gui/images/icon-hand-clean-md.png", 0, 0);
        hackUnicodeFont("dpix_8pt.ttf");
        nifty.registerScreenController(new MainMenuController(con));
        nifty.registerScreenController(new LoadingScreenController());
        nifty.registerScreenController(new ExitConfirmationScreenController());
        nifty.registerScreenController(new InputBindingScreenController(con.getInput()));
        nifty.addXml("gui/screens/main_menu.xml");
        nifty.addXml("gui/screens/saveload_screen.xml");
        nifty.addXml("gui/screens/misc_screens.xml");
        nifty.addXml("gui/screens/loading_screen.xml");
        nifty.addXml("gui/screens/settings_screen.xml");
        nifty.addXml("gui/screens/input_binding_screen.xml");
    }

    public static void init(GameContainer con, Nifty n) {
        instance = new GUI(con, n);
    }

    public static GUI getInstance() {
        return instance;
    }

    public Nifty getNifty() {
        return nifty;
    }

    /**
     * Hack: nifty-gui wraps slick unicode font. To correctly render symbols, loadGlyphs() must be called on that internal font
     * representation. But nifty loads only ASCII glyphs, and then hides it inside, providing no way for user to load other glyphs
     * This method should be called for ALL UTF-8 FONTS, BEFORE loading any nifty screens. It hacks private field and calls loadGlyphs() for cyrillic
     *
     * @param name
     */
    private void hackUnicodeFont(String name) {
        UnicodeSlickRenderFont font = (UnicodeSlickRenderFont) nifty.createFont(name);
        Field field;
        try {
            field = AbstractSlickRenderFont.class.getDeclaredField("internalFont");
            field.setAccessible(true);
            UnicodeFont realFont = (UnicodeFont) field.get(font);
            realFont.addGlyphs("АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя,.-–—_:;#№\"’'+=/|\\!@?%^&*()…");
            realFont.loadGlyphs();

        } catch (NoSuchFieldException | SlickException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }


    }

    public void goToScreen(String id) {
        if (nifty.gotoScreen(id)) {
            nextScreen = null;
            return;
        } else {
            nextScreen = id;
        }

        nifty.scheduleEndOfFrameElementAction(nifty.getCurrentScreen(), null, new EndOfScreenAction(), new EndNotify() {
            @Override
            public void perform() {
                nifty.gotoScreen(nextScreen);
                nextScreen = null;
            }
        });
    }

    public String peekScreen() {
        return screens.isEmpty() ? "" : screens.peek();
    }

    public void pushCurrentScreen() {
        if (nextScreen == null) {
            pushScreen(nifty.getCurrentScreen().getScreenId());
        } else {
            pushScreen(nextScreen);
        }
    }

    public void pushScreen(String id) {
        screens.push(id);
    }

    public String popScreen() {
        if (screens.isEmpty()) {
            LoggerFactory.getLogger(this.getClass()).error("Screens stack is empty");
            return null;
        }
        return screens.pop();
    }

    public void popAndSetScreen() {
        if(screens.isEmpty()){
            LoggerFactory.getLogger(this.getClass()).error("Screens stack is empty");
        }
        nifty.gotoScreen(screens.pop());
    }

    public void onWorldLoaded(GameContainer con, World world) {
        worldInstance = world;
        containerInstance = con;

        GalaxyMapController galaxyMapController = new GalaxyMapController(world);
        // first register controllers
        nifty.registerScreenController(galaxyMapController);
        nifty.registerScreenController(new ResearchScreenController(world, con));
        nifty.registerScreenController(new TradeScreenController(world));
        nifty.registerScreenController(new DialogController(con, world));
        nifty.registerScreenController(new StoryScreenController(world));
        nifty.registerScreenController(new ResearchReportScreenController(world));
        nifty.registerScreenController(new EarthProgressScreenController(world));
        nifty.registerScreenController(new LandingPartyEquipScreenController(world));
        nifty.registerScreenController(new EarthScreenController(world));
        nifty.registerScreenController(new EngineeringScreenController(world, con));
        nifty.registerScreenController(new FailScreenController(world));
        nifty.registerScreenController(new PasswordInputController(world, con));

        final SurfaceGUIController surfaceGUIController = new SurfaceGUIController(world);
        nifty.registerScreenController(surfaceGUIController);
        nifty.registerScreenController(new IntroDialogController(world));
        nifty.registerScreenController(new CountrySelectScreenController(world));
        nifty.registerScreenController(new InventoryController(world));
        nifty.registerScreenController(new JournalScreenController(world));
        nifty.registerScreenController(new PlanetScanController(world));
        nifty.registerScreenController(new ShipScreenController(world));
        nifty.registerScreenController(new CrewScreenController());
        nifty.registerScreenController(new SurfaceMapController(world));
        nifty.registerScreenController(new StarMapController(world));

        // load xmls
        String[] screens = {
                "gui/screens/interaction_target_selection_control.xml", 
                "gui/screens/help_popup.xml",
                "gui/screens/image_button.xml",
                "gui/screens/shoot_panel.xml",
                "gui/screens/progress_bar.xml",
                "gui/screens/top_panel.xml",
                "gui/screens/space_gui.xml",
                "gui/screens/country_select_screen.xml",
                "gui/screens/fail_screen.xml",
                "gui/screens/list_screen.xml",
                "gui/screens/research_screen.xml",
                "gui/screens/ingame_menu.xml",
                "gui/screens/trade_screen.xml",
                "gui/screens/dialog_screen.xml",
                "gui/screens/humanity_progress_screen.xml",
                "gui/screens/story_screen.xml",
                "gui/screens/earth_progress.xml",
                "gui/screens/engineering_screen.xml",
                "gui/screens/surface_gui.xml",
                "gui/screens/landing_party_equip_screen.xml",
                "gui/screens/ship.xml",
                "gui/screens/intro_dialog.xml",
                "gui/screens/inventory_screen.xml",
                "gui/screens/journal_screen.xml",
                "gui/screens/planet_scan_screen.xml",
                "gui/screens/surface_map.xml",
                "gui/screens/starmap.xml",
                "gui/screens/password_input.xml",
        };
        
        for(String screen : screens) {
            String override = ResourceManager.getOverridedResources().get(screen);
            if(override != null) {
                nifty.addXml(override);
            }
            else {
                nifty.addXml(screen);
            }
        }

        // remove old map controller listener, if it already exists (this is a loaded game). it should actually not be saved at all
        for (Iterator<GameEventListener> iter = world.getListeners().iterator(); iter.hasNext(); ) {
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
        worldInstance.setPaused(true);
    }

    public void resetIngameMenu() {
        ingameMenuInstance = null;
    }

    public void closeIngameMenu() {
        if (ingameMenuInstance == null) {
            return;
        }
        nifty.closePopup(ingameMenuInstance.getId());
        ingameMenuInstance = null;
        worldInstance.setPaused(false);
    }
}
