package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;
import ru.game.aurora.world.quest.Journal;
import ru.game.aurora.world.quest.JournalEntry;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 19.12.13
 */

public class JournalScreenController implements ScreenController {
    private ListBox<JournalEntry> questList;

    private ListBox<JournalEntry> codexList;

    private World world;

    private TabGroup tg;

    private Window myWindow;

    public JournalScreenController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        myWindow = screen.findNiftyControl("journal_window", Window.class);
        tg = screen.findNiftyControl("journal_tabs", TabGroup.class);
        Element questTab = screen.findElementByName("quest_tab");
        Element codexTab = screen.findElementByName("codex_tab");
        questList = questTab.findNiftyControl("#itemsList", ListBox.class);
        codexList = codexTab.findNiftyControl("#itemsList", ListBox.class);
    }

    @Override
    public void onStartScreen() {
        myWindow.getElement().setVisible(true);
        Journal journal = world.getPlayer().getJournal();
        questList.clear();
        for (JournalEntry e : journal.getQuests().values()) {
            questList.addItem(e);
        }
        EngineUtils.resetScrollbarX(questList);
        codexList.clear();
        for (JournalEntry e : journal.getCodex().values()) {
            codexList.addItem(e);
        }
        EngineUtils.resetScrollbarX(codexList);
        selectFirstItemInCurrentList();
        world.setPaused(true);
    }

    private void selectFirstItemInCurrentList() {
        ListBox currentTabList = tg.getSelectedTab().getElement().findNiftyControl("#itemsList", ListBox.class);
        if (currentTabList.getSelectedIndices().isEmpty()) {
            currentTabList.selectItemByIndex(0);
        }
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    @NiftyEventSubscriber(id = "journal_tabs")
    public void onTabChanged(final String id, final TabSelectedEvent event) {
        selectFirstItemInCurrentList();
    }

    @NiftyEventSubscriber(pattern = ".*itemsList")
    public void onItemClicked(final String id, final ListBoxSelectionChangedEvent event) {
        if (event.getSelection().isEmpty()) {
            return;
        }
        EngineUtils.setTextForGUIElement(tg.getSelectedTab().getElement().findElementByName("#message_text"), ((JournalEntry) event.getSelection().get(0)).getFullText(world));
        myWindow.getElement().layoutElements();
    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }

    @NiftyEventSubscriber(id = "journal_window")
    public void onClose(final String id, final WindowClosedEvent event) {
        closeScreen();
    }
}
