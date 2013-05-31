/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 28.05.13
 * Time: 16:14
 */

package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.TabGroup;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.Color;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchProjectState;
import ru.game.aurora.world.World;

public class ResearchScreenController implements ScreenController
{
    private World world;

    private Element availableResearch;

    private Element completedResearch;

    private TabGroup tg;

    public ResearchScreenController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        tg = screen.findNiftyControl("research_tabs", TabGroup.class);
        availableResearch = screen.findElementByName("active_list_screen");
        ListBox l = availableResearch.findNiftyControl("itemsList", ListBox.class);
        l.addAllItems(world.getPlayer().getResearchState().getAvailableProjects());
        l.addAllItems(world.getPlayer().getResearchState().getCurrentProjects());

        completedResearch = screen.findElementByName("completed_list_screen");
    }

    @Override
    public void onStartScreen() {

    }

    @Override
    public void onEndScreen() {
    }


    public void closeScreen()
    {
        GUI.getInstance().getNifty().gotoScreen(GUI.getInstance().popScreen());
    }

    @NiftyEventSubscriber(id="itemsList")
    public void onListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent event)
    {
        Element imagePanel = tg.getSelectedTab().getElement().findElementByName("selectedItemImg");
      //  Label textLabel = tg.getSelectedTab().getElement().findNiftyControl("selectedItemText", Label.class);
        TextRenderer tr = tg.getSelectedTab().getElement().findElementByName("selectedItemText").getRenderer(TextRenderer.class);
        tr.setLineWrapping(true);
        tr.setColor(Color.BLACK);


        Object obj = event.getSelection().get(0);

        if (obj instanceof ResearchProjectDesc) {
            tr.setText(((ResearchProjectDesc) obj).getDescription());
        } else if (obj instanceof ResearchProjectState) {
            tr.setText(((ResearchProjectState) obj).desc.getDescription());
        }


    }
}
