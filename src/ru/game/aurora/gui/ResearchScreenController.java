/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 28.05.13
 * Time: 16:14
 */

package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.TabGroup;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.slick2d.render.image.ImageSlickRenderImage;
import ru.game.aurora.application.ResourceManager;
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
        completedResearch = screen.findElementByName("completed_list_screen");
    }

    @Override
    public void onStartScreen() {
        ListBox l = availableResearch.findNiftyControl("itemsList", ListBox.class);
        l.clear();
        l.addAllItems(world.getPlayer().getResearchState().getCurrentProjects());

        l = completedResearch.findNiftyControl("itemsList", ListBox.class);
        l.clear();
        l.addAllItems(world.getPlayer().getResearchState().getCompletedProjects());

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

        TextRenderer tr = tg.getSelectedTab().getElement().findElementByName("selectedItemText").getRenderer(TextRenderer.class);
        if (event.getSelection().isEmpty()) {
            tr.setText("<No item selected>");
            imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage("no_image"))));
            return;
        }
        Object obj = event.getSelection().get(0);
        ResearchProjectDesc researchProjectDesc;
        if (ResearchProjectDesc.class.isAssignableFrom(obj.getClass())) {
            researchProjectDesc = (ResearchProjectDesc) obj;
        } else if (ResearchProjectState.class.isAssignableFrom(obj.getClass())) {
            researchProjectDesc = ((ResearchProjectState) obj).desc;
        } else {
            throw new IllegalStateException("research screen can not show research item of class " + obj.getClass());
        }
        tr.setText(researchProjectDesc.getDescription());
        imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage(researchProjectDesc.getIcon()))));
    }

    public void onIncreaseScientistsButtonClicked()
    {
        ListBox avail  = availableResearch.findNiftyControl("itemsList", ListBox.class);
        if (avail.getSelection().isEmpty()) {
            return;
        }
        final int idleScientists = world.getPlayer().getResearchState().getIdleScientists();
        if (idleScientists == 0) {
            return;
        }
        ResearchProjectState rp = (ResearchProjectState) avail.getSelection().get(0);
        rp.scientists++;
        world.getPlayer().getResearchState().setIdleScientists(idleScientists - 1);
        avail.refresh();

    }

    public void onDecreaseScientistsButtonClicked()
    {
        ListBox avail  = availableResearch.findNiftyControl("itemsList", ListBox.class);
        if (avail.getSelection().isEmpty()) {
            return;
        }
        ResearchProjectState rp = (ResearchProjectState) avail.getSelection().get(0);
        if (rp.scientists == 0) {
            return;
        }
        rp.scientists--;
        world.getPlayer().getResearchState().setIdleScientists(world.getPlayer().getResearchState().getIdleScientists() + 1);
        avail.refresh();
    }

    // works for increase/decrease scientists buttons, makes item in list selected (by default clicking on button does not select item in list)
    @NiftyEventSubscriber(pattern=".*crease_scientists")
    public void onClicked(String id, ButtonClickedEvent event)
    {
        int numericId = Integer.parseInt(id.split("#")[0]) - 2;
        availableResearch.findNiftyControl("itemsList", ListBox.class).selectItemByIndex(numericId);
    }
}
