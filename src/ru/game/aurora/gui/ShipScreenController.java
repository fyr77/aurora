package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.world.World;

import java.util.ArrayList;
import java.util.List;

public class ShipScreenController implements ScreenController {
    private ListBox<CrewMember> crewMemberListBox;

    private World world;

    public ShipScreenController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        crewMemberListBox = screen.findNiftyControl("crew", ListBox.class);
    }

    @Override
    public void onStartScreen() {
        crewMemberListBox.clear();
        List<CrewMember> l = new ArrayList<>();
        l.addAll(world.getPlayer().getShip().getCrewMembers().values());
        crewMemberListBox.addAllItems(l);
    }

    @Override
    public void onEndScreen() {

    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }
}
