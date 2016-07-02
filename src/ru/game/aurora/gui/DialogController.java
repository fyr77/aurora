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
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.Reply;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.Updatable;
import ru.game.aurora.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class DialogController implements ScreenController, Updatable {
    private final World world;

    private final Stack<Dialog> dialogs = new Stack<>();

    private Dialog currentDialog;

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

    @Override
    public void update(GameContainer container, World world){
        if (currentDialog.getStatements() == null) {
            currentDialog.enter(world);
        }
        if (currentDialog.getCurrentStatement() == null) {
            return;
        }

        int idx = -1;

        for (int i = Input.KEY_1; i < Input.KEY_9; ++i) {
            if (container.getInput().isKeyPressed(i)) {
                idx = i - Input.KEY_1;
                break;
            }
        }

        if (idx < 0 || idx >= currentDialog.getCurrentStatement().getAvailableReplies(world, currentDialog.getFlags()).size()) {
            return;
        }

        selectDialog(idx);
    }

    private void updateDialog() {
        final Dialog dialog = this.dialogs.peek();
        currentDialog = dialog;

        EngineUtils.setImageForGUIElement(imagePanel, dialog.getCurrentStatement().customIcon == null ? dialog.getIconName() : dialog.getCurrentStatement().customIcon);
        EngineUtils.setTextForGUIElement(npcText, dialog.getLocalizedNPCText(world));
        // reset horizontal scrollbar
        EngineUtils.resetScrollbarX(replies);
        if (!replies.getItems().isEmpty()) {
            replies.clear();
        }
        replies.addAllItems(dialog.addAvailableRepliesLocalized(world));
        replies.refresh();
        replies.showItemByIndex(0);
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
        selectDialog(selectedIdx);
    }

    private void selectDialog(final int selectedIdx) {
        this.dialogs.peek().useReply(world, selectedIdx);

        if (this.dialogs.peek().isOver()) {
            Dialog d = this.dialogs.pop();
            String prevScreen = GUI.getInstance().popScreen();
            if (prevScreen != null) {
                GUI.getInstance().getNifty().gotoScreen(prevScreen);
            }
            final List<DialogListener> listeners = new ArrayList<>(d.getListeners());
            for (DialogListener listener : listeners) {
                listener.onDialogEnded(world, d, d.getReturnValue(), d.getFlags());
            }
            d.enter(world);
        } else {
            replies.deselectItemByIndex(selectedIdx);
            updateDialog();
        }
    }
}
