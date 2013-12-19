package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.WindowClosedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 17.12.13
 * Time: 22:27
 */
public class InventoryController implements ScreenController {
    private Element myWindow;

    private ListBox<InventoryItem> items;

    private World world;

    public InventoryController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        items = screen.findNiftyControl("items", ListBox.class);
        myWindow = screen.findElementByName("inventory_window");
    }

    @Override
    public void onStartScreen() {
        myWindow.setVisible(true);
        items.clear();
        for (Map.Entry<InventoryItem, Integer> entry : world.getPlayer().getLandingParty().getInventory().entrySet()) {
            for (int i = 0; i < entry.getValue(); ++i) {
                items.addItem(entry.getKey());
            }
        }
    }

    @Override
    public void onEndScreen() {

    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }

    @NiftyEventSubscriber(id = "inventory_window")
    public void onClose(final String id, final WindowClosedEvent event) {
        closeScreen();
    }
}
