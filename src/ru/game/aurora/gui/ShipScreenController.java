package ru.game.aurora.gui;

import com.google.common.collect.Multiset;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
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

        credCountElement = myScreen.findElementByName("cred_count").findElementByName("#count");
        resCountElement = myScreen.findElementByName("res_count").findElementByName("#count");

    }

    @Override
    public void onStartScreen() {
        crewMemberListBox.clear();
        List<CrewMember> l = new ArrayList<>();
        final Ship ship = world.getPlayer().getShip();
        l.addAll(ship.getCrewMembers().values());
        crewMemberListBox.addAllItems(l);

        modulesListBox.clear();
        modulesListBox.addAllItems(ship.getUpgrades());

        inventory.clear();
        List<Multiset.Entry<InventoryItem>> ll = new ArrayList<>();
        ll.addAll(ship.getStorage().entrySet());
        inventory.addAllItems(ll);

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
}
