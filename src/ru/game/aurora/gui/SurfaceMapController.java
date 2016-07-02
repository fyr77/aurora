package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.WindowClosedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.InputBinding;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.Updatable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.PlanetMapRenderer;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 26.03.14
 * Time: 14:36
 */
public class SurfaceMapController implements ScreenController, Updatable {
    private final World world;

    private Element mapPanel;

    private Element myWindow;

    private CheckBox overlayCheckbox;

    public SurfaceMapController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        myWindow = screen.findElementByName("surface_map_window");
        mapPanel = screen.findElementByName("map_panel");
        overlayCheckbox = myWindow.findNiftyControl("overlay_checkbox", CheckBox.class);
    }

    @Override
    public void onStartScreen() {
        myWindow.setVisible(true);

        if (world.getCurrentRoom() instanceof Planet) {
            Image map = PlanetMapRenderer.createMap(world, (Planet) world.getCurrentRoom(), new Rectangle(0, 0, mapPanel.getWidth(), mapPanel.getHeight()), overlayCheckbox.isChecked(), true);
            EngineUtils.setImageForGUIElement(mapPanel, map);
        } else {
            // todo: map for dungeon
        }

        world.setPaused(true);
    }

    @NiftyEventSubscriber(id = "overlay_checkbox")
    public void scanFilterDisabled(final String id, final CheckBoxStateChangedEvent event) {
        Image planetMap = PlanetMapRenderer.createMap(world
                , (Planet) world.getCurrentRoom()
                , new Rectangle(0, 0, mapPanel.getWidth(), mapPanel.getHeight())
                , overlayCheckbox.isChecked()
                , true
        );
        EngineUtils.setImageForGUIElement(mapPanel, planetMap);
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    @NiftyEventSubscriber(id = "surface_map_window")
    public void onClose(final String id, final WindowClosedEvent event) {
        closeScreen();
    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }

    @Override
    public void update(GameContainer container, World world) {
        final Input input = container.getInput();

        if(input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.MAP))
        || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INTERACT))
        || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INTERACT_SECONDARY))
        || input.isKeyPressed(Input.KEY_ESCAPE)){
            closeScreen();
            return;
        }
    }
}
