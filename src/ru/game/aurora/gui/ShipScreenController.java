package ru.game.aurora.gui;

import com.google.common.collect.Multiset;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.WindowClosedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.InventoryItem;

import java.util.ArrayList;
import java.util.List;

public class ShipScreenController implements ScreenController {
    private ListBox<CrewMember> crewMemberListBox;

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
        crewMemberListBox = screen.findNiftyControl("crew", ListBox.class);
        modulesListBox = screen.findNiftyControl("modules", ListBox.class);
        inventory = screen.findNiftyControl("items", ListBox.class);
        myScreen = screen;
        myWindow = screen.findElementByName("ship_window");

        credCountElement = myScreen.findElementByName("cred_count").findElementByName("#count");
        resCountElement = myScreen.findElementByName("res_count").findElementByName("#count");

    }

    public void refresh() {
        if (crewMemberListBox == null) {
            //rare case - refresh() is called before this screen was opened for first time
            return;
        }
        crewMemberListBox.clear();
        List<CrewMember> l = new ArrayList<>();
        final Ship ship = world.getPlayer().getShip();
        l.addAll(ship.getCrewMembers().values());
        crewMemberListBox.addAllItems(l);

        modulesListBox.clear();
        modulesListBox.addAllItems(ship.getUpgrades());

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
    public void onStartScreen() {
        myWindow.show();
        refresh();
        myScreen.layoutLayers();
        world.setPaused(true);

        EngineUtils.setTextForGUIElement(resCountElement, String.valueOf(world.getPlayer().getResourceUnits()));
        EngineUtils.setTextForGUIElement(credCountElement, String.valueOf(world.getPlayer().getCredits()));
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }

    public void callOfficerPressed() {
        crewMemberListBox.getFocusItem().interact(world);
        myScreen.layoutLayers();
        crewMemberListBox.refresh();
    }

    @NiftyEventSubscriber(pattern = ".*callButton")
    public void onCallButtonClicked(String id, ButtonClickedEvent event) {
        crewMemberListBox.setFocusItem((CrewMember) event.getButton().getElement().getParent().getUserData());
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
}
