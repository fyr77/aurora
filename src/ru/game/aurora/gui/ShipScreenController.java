package ru.game.aurora.gui;

import com.google.common.collect.Multiset;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.WindowClosedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import org.newdawn.slick.Input;
import ru.game.aurora.application.InputBinding;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;

import java.util.ArrayList;
import java.util.List;

public class ShipScreenController extends DefaultCloseableScreenController {

    private ListBox<ShipUpgrade> modulesListBox;

    private ListBox<Multiset.Entry<InventoryItem>> inventory;

    private World world;

    private Screen myScreen;

    private Element myWindow;

    private Element credCountElement;

    private Element resCountElement;

    public ShipScreenController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        modulesListBox = screen.findNiftyControl("modules", ListBox.class);
        inventory = screen.findNiftyControl("items", ListBox.class);
        myScreen = screen;
        myWindow = screen.findElementByName("ship_window");

        credCountElement = myScreen.findElementByName("cred_count").findElementByName("#count");
        resCountElement = myScreen.findElementByName("res_count").findElementByName("#count");
    }

    @Override
    public void onStartScreen() {
        myWindow.show();
        refresh();
        myScreen.layoutLayers();
        world.setPaused(true);

        EngineUtils.setTextForGUIElement(resCountElement, String.valueOf(world.getPlayer().getResourceUnits()));
        EngineUtils.setTextForGUIElement(credCountElement, String.valueOf(world.getPlayer().getCredits()));
    }

    private void refresh() {
        modulesListBox.clear();
        modulesListBox.addAllItems(World.getWorld().getPlayer().getShip().getUpgrades());

        inventory.clear();
        List<Multiset.Entry<InventoryItem>> ll = new ArrayList<>();
        for (Multiset.Entry<InventoryItem> e : world.getPlayer().getInventory().entrySet()) {
            if (e.getElement().isVisibleInInventory()) {
                ll.add(e);
            }
        }

        inventory.addAllItems(ll);
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    @NiftyEventSubscriber(pattern = ".*use_button")
    public void onUseModuleButtonClicked(String id, ButtonClickedEvent event) {
        final ShipUpgrade usedModule = (ShipUpgrade) event.getButton().getElement().getParent().getUserData();
        modulesListBox.selectItem(usedModule);
        usedModule.onUse(world);
        closeScreen();
    }

    @NiftyEventSubscriber(id = "ship_window")
    public void onClose(final String id, final WindowClosedEvent event) {
        closeScreen();
    }

    @Override
    public void inputUpdate(Input input) {
        super.inputUpdate(input);

        if (input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INVENTORY))) {
            closeScreen();
            return;
        }
    }
}
