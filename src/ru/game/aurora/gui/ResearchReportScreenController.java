/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.06.13
 * Time: 14:57
 */

package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.Window;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.slick2d.render.image.ImageSlickRenderImage;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.player.research.ResearchProjectDesc;


public class ResearchReportScreenController implements ScreenController
{
    private ResearchProjectDesc research;

    private Window window;

    private Element icon;

    private Element text;

    private ListBox nextResearch;

    public void setResearch(ResearchProjectDesc research) {
        this.research = research;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        window = screen.findNiftyControl("research_report_window", Window.class);
        icon = screen.findElementByName("image_panel");
        text = screen.findElementByName("report_text");
        nextResearch = screen.findNiftyControl("new_research", ListBox.class);
    }

    @Override
    public void onStartScreen() {
        window.setTitle(research.getName() + " report");
        icon.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage(research.getReport().icon))));
        text.getRenderer(TextRenderer.class).setText(research.getReport().getText());
        nextResearch.clear();
        if (research.getMakesAvailable() != null) {
            nextResearch.addAllItems(research.getMakesAvailable());
        }
        if (research.getMakesAvailableEngineering() != null) {
            nextResearch.addAllItems(research.getMakesAvailableEngineering());
        }
        GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();
    }

    @Override
    public void onEndScreen() {

    }

    public void closeScreen()
    {
        GUI.getInstance().popAndSetScreen();
    }
}
