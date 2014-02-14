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
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.Reply;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;

import java.util.Stack;


public class DialogController implements ScreenController {
    private World world;

    private Stack<Dialog> dialogs = new Stack<>();

    private Element imagePanel;

    private Element npcText;

    private ListBox<String> replies;

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
        world.setPaused(true);
        updateDialog();
    }

    public void updateDialog() {
        final Dialog dialog = this.dialogs.peek();
        EngineUtils.setImageForGUIElement(imagePanel, dialog.getCurrentStatement().customIcon == null ? dialog.getIconName() : dialog.getCurrentStatement().customIcon);
        EngineUtils.setTextForGUIElement(npcText, dialog.getLocalizedNPCText(world));

        if (!replies.getItems().isEmpty()) {
            replies.clear();
        }
        replies.addAllItems(dialog.addAvailableRepliesLocalized(world));
        replies.refresh();
        screen.layoutLayers();
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    @NiftyEventSubscriber(id = "replyList")
    public void onListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent<Reply> event) {
        if (event.getSelectionIndices().isEmpty()) {
            return;
        }
        int selectedIdx = event.getSelectionIndices().get(0);
        this.dialogs.peek().useReply(world, selectedIdx);
        if (this.dialogs.peek().isOver()) {
            Dialog d = this.dialogs.pop();
            String prevScreen = GUI.getInstance().popScreen();
            if (prevScreen != null) {
                GUI.getInstance().getNifty().gotoScreen(prevScreen);
            }
            for (DialogListener listener : d.getListeners()) {
                listener.onDialogEnded(world, d, d.getReturnValue(), d.getFlags());
            }
            d.enter(world);
        } else {
            replies.deselectItemByIndex(selectedIdx);
            updateDialog();
        }

    }
}
