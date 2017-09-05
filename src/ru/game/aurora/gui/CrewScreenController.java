package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import org.newdawn.slick.Input;
import ru.game.aurora.application.InputBinding;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by di Grigio on 02.05.2017.
 */
public class CrewScreenController extends DefaultCloseableScreenController {

    private ListBox<CrewMember> crewMemberListBox;
    private Screen myScreen;
    private Element myWindow;

    @Override
    public void bind(Nifty nifty, Screen screen) {
        crewMemberListBox = screen.findNiftyControl("crew", ListBox.class);
        myScreen = screen;
        myWindow = screen.findElementByName("crew_window");
    }

    @Override
    public void onStartScreen() {
        myWindow.show();
        refresh();
        myScreen.layoutLayers();
        World.getWorld().setPaused(true);
    }

    @Override
    public void onEndScreen() {
        World.getWorld().setPaused(false);
    }

    public void refresh(){
        if (crewMemberListBox == null) {
            //rare case - refresh() is called before this screen was opened for first time
            return;
        }

        crewMemberListBox.clear();
        List<CrewMember> l = new ArrayList<>();
        l.addAll(World.getWorld().getPlayer().getShip().getCrewMembers().values());
        crewMemberListBox.addAllItems(l);
    }

    public void callOfficerPressed() {
        crewMemberListBox.getFocusItem().interact(World.getWorld());
        myScreen.layoutLayers();
        crewMemberListBox.refresh();
    }

    @NiftyEventSubscriber(pattern = ".*callButton")
    public void onCallButtonClicked(String id, ButtonClickedEvent event) {
        crewMemberListBox.setFocusItem((CrewMember) event.getButton().getElement().getParent().getUserData());
    }

    @Override
    public void inputUpdate(Input input) {
        super.inputUpdate(input);

        if (input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.CREW))) {
            closeScreen();
            return;
        }
    }
}
