/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 28.05.13
 * Time: 16:14
 */

package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.slick2d.render.image.ImageSlickRenderImage;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchProjectState;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;

public class ResearchScreenController implements ScreenController {
    private final World world;

    private Element availableResearch;

    private Element completedResearch;

    private TabGroup tg;

    private Element window;

    public ResearchScreenController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        tg = screen.findNiftyControl("research_tabs", TabGroup.class);
        availableResearch = screen.findElementByName("active_list_screen");
        completedResearch = screen.findElementByName("completed_list_screen");
        window = screen.findElementByName("research_window");
    }

    @Override
    public void onStartScreen() {
        window.setVisible(true);
        ListBox l = availableResearch.findNiftyControl("itemsList", ListBox.class);
        EngineUtils.resetScrollbarX(l);
        l.clear();
        l.addAllItems(world.getPlayer().getResearchState().getCurrentProjects());
        l.getElement().layoutElements();

        l = completedResearch.findNiftyControl("itemsList", ListBox.class);
        EngineUtils.resetScrollbarX(l);
        l.clear();
        l.addAllItems(world.getPlayer().getResearchState().getCompletedProjects());
        l.getElement().layoutElements();

        Element statusLines = tg.getSelectedTab().getElement().findElementByName("statusStrings");
        if (statusLines != null) {
            EngineUtils.setTextForGUIElement(statusLines, Localization.getText("gui", "research.active.status"));
        }
        window.layoutElements();
        world.setPaused(true);

    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }


    public void closeScreen() {
        GUI.getInstance().getNifty().gotoScreen(GUI.getInstance().popScreen());
    }


    @NiftyEventSubscriber(id = "research_window")
    public void onClose(final String id, final WindowClosedEvent event) {
        closeScreen();
    }

    @NiftyEventSubscriber(id = "itemsList")
    public void onListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent event) {
        Element imagePanel = tg.getSelectedTab().getElement().findElementByName("selectedItemImg");

        TextRenderer tr = tg.getSelectedTab().getElement().findElementByName("selectedItemText").getRenderer(TextRenderer.class);
        if (event.getSelection().isEmpty()) {
            tr.setText(Localization.getText("gui", "no_item_selected"));
            imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage("no_image"))));
            return;
        }
        Object obj = event.getSelection().get(0);
        ResearchProjectDesc researchProjectDesc;
        Element statusLines = tg.getSelectedTab().getElement().findElementByName("statusStrings");
        if (statusLines != null) {
            EngineUtils.setTextForGUIElement(statusLines, Localization.getText("gui", "research.active.status"));
        }
        if (ResearchProjectDesc.class.isAssignableFrom(obj.getClass())) {
            researchProjectDesc = (ResearchProjectDesc) obj;
        } else if (ResearchProjectState.class.isAssignableFrom(obj.getClass())) {
            researchProjectDesc = ((ResearchProjectState) obj).desc;

            if (statusLines != null) {
                statusLines.getRenderer(TextRenderer.class).setText(Localization.getText("gui", "research.active.status") + "\n" + researchProjectDesc.getStatusString(world, ((ResearchProjectState) obj).scientists));
            }
        } else {
            throw new IllegalStateException("research screen can not show research item of class " + obj.getClass());
        }
        tr.setText(researchProjectDesc.getDescription());


        imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage(researchProjectDesc.getIcon()))));
        GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();
    }

    public void onIncreaseScientistsButtonClicked() {
        ListBox avail = availableResearch.findNiftyControl("itemsList", ListBox.class);
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

    public void onDecreaseScientistsButtonClicked() {
        ListBox avail = availableResearch.findNiftyControl("itemsList", ListBox.class);
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
    @NiftyEventSubscriber(pattern = ".*crease_scientists")
    public void onClicked(String id, ButtonClickedEvent event) {

        int numericId = Integer.parseInt(id.split("#")[0]);
        ListBox itemsList = availableResearch.findNiftyControl("itemsList", ListBox.class);
        // hack. No idea how ids are distributed between list elements, they seem to start from arbitrary number and be sorted in ascending order
        // so in order to get index of clicked element, must subtract from its id id of the first one
        numericId -= Integer.parseInt(itemsList.getElement().findElementByName("#child-root").getElements().get(0).getId());
        itemsList.selectItemByIndex(numericId);
    }
}
