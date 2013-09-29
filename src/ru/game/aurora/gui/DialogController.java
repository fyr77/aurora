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

import java.util.Stack;


public class DialogController implements ScreenController {
    private World world;

    private Stack<Dialog> dialogs = new Stack<>();

    private Element imagePanel;

    private Element npcText;

    private ListBox replies;

    private Screen screen;

    public DialogController(World world) {
        this.world = world;
    }

    public void pushDialog(Dialog dialog) {
        this.dialogs.push(dialog);
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
        final Dialog dialog = this.dialogs.peek();
        imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage(dialog.getIconName()))));
        npcText.getRenderer(TextRenderer.class).setText(dialog.getLocalizedNPCText());

        if (!replies.getItems().isEmpty()) {
            replies.clear();
        }
        replies.addAllItems(dialog.addAvailableRepliesLocalized(world));
        screen.layoutLayers();
    }

    @Override
    public void onEndScreen() {

    }

    @NiftyEventSubscriber(id = "replyList")
    public void onListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent<Reply> event) {
        if (event.getSelectionIndices().isEmpty()) {
            return;
        }
        int selectedIdx = event.getSelectionIndices().get(0);
        this.dialogs.peek().useReply(world, selectedIdx);
        if (this.dialogs.peek().isOver()) {
            this.dialogs.pop();
            GUI.getInstance().getNifty().gotoScreen(GUI.getInstance().popScreen());
        } else {
            replies.deselectItem(selectedIdx);
            updateDialog();
        }

    }
}
