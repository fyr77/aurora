/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 04.06.13
 * Time: 22:46
 */

package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.slick2d.render.image.ImageSlickRenderImage;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.Reply;
import ru.game.aurora.world.World;


public class DialogController implements ScreenController {
    private World world;

    private Dialog dialog;

    private Element imagePanel;

    private Element npcText;

    private ListBox replies;

    private Screen screen;

    public DialogController(World world) {
        this.world = world;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        imagePanel = screen.findElementByName("dialogIcon");
        npcText = screen.findElementByName("npcText");
        npcText.getRenderer(TextRenderer.class).setLineWrapping(true);
        replies = screen.findNiftyControl("replyList", ListBox.class);
        this.screen = screen;
    }

    @Override
    public void onStartScreen() {
        updateDialog();
    }

    public void updateDialog() {
        imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage(dialog.getIconName()))));
        npcText.getRenderer(TextRenderer.class).setText(dialog.getCurrentStatement().npcText);

        if (!replies.getItems().isEmpty()) {
            replies.clear();
        }
        for (Reply r : dialog.getCurrentStatement().getAvailableReplies(world)) {
            replies.addItem(r);
        }
        screen.resetLayout();
    }

    @Override
    public void onEndScreen() {

    }

    @NiftyEventSubscriber(id = "replyList")
    public void onListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent<Reply> event) {
        int selectedIdx = event.getSelectionIndices().get(0);
        dialog.useReply(world, selectedIdx);
        if (dialog.isOver()) {
            GUI.getInstance().getNifty().gotoScreen(GUI.getInstance().popScreen());
        } else {
            replies.deselectItem(selectedIdx);
            updateDialog();
        }

    }
}
