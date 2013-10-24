/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 05.06.13
 * Time: 14:48
 */

package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.WindowClosedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.slick2d.render.image.ImageSlickRenderImage;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.World;


public class StoryScreenController implements ScreenController {
    private World world;

    private StoryScreen story;

    private Element imagePanel;

    private Element textPanel;

    private Element myWindow;

    public StoryScreenController(World world) {
        this.world = world;
    }

    public void setStory(StoryScreen story) {
        this.story = story;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        textPanel = screen.findElementByName("storyText");
        textPanel.getRenderer(TextRenderer.class).setLineWrapping(true);
        imagePanel = screen.findElementByName("imagePanel");
        myWindow = screen.findElementByName("story_window");
    }

    @Override
    public void onStartScreen() {
        myWindow.setVisible(true);
        world.setPaused(true);
        story.start();
        update();
    }

    private void update() {
        StoryScreen.StoryElement elem = story.getCurrentElement();
        if (elem == null) {
            return;
        }

        textPanel.getRenderer(TextRenderer.class).setText(Localization.getText("story", elem.text));
        imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage(elem.imageId))));
        GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    public void nextPage() {
        if (story.next(world)) {
            if (story.isOver()) {
                GUI.getInstance().getNifty().gotoScreen(GUI.getInstance().popScreen());
            }
            update();
        }
    }

    public void prevPage() {
        if (story.prev()) {
            update();
        }
    }

    @NiftyEventSubscriber(id = "story_window")
    public void onClose(final String id, final WindowClosedEvent event) {
        while (!story.isOver()) {
            story.next(world);
        }
        GUI.getInstance().getNifty().gotoScreen(GUI.getInstance().popScreen());
    }
}
