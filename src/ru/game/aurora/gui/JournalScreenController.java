package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import org.newdawn.slick.Input;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.InputBinding;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;
import ru.game.aurora.world.quest.Journal;
import ru.game.aurora.world.quest.JournalEntry;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 19.12.13
 */

public class JournalScreenController extends DefaultCloseableScreenController {
    private ListBox<JournalEntry> activeQuests;

    private ListBox<JournalEntry> completedQuests;

    private ListBox<JournalEntry> codexList;

    private final World world;

    private TabGroup tg;

    private Window myWindow;

    public JournalScreenController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        myWindow = screen.findNiftyControl("journal_window", Window.class);
        tg = screen.findNiftyControl("journal_tabs", TabGroup.class);
        Element activeQuestTab = screen.findElementByName("active_quest_tab");
        Element completedQuestTab = screen.findElementByName("completed_quest_tab");
        Element codexTab = screen.findElementByName("codex_tab");
        activeQuests = activeQuestTab.findNiftyControl("#itemsList", ListBox.class);
        completedQuests = completedQuestTab.findNiftyControl("#itemsList", ListBox.class);
        codexList = codexTab.findNiftyControl("#itemsList", ListBox.class);
    }

    @Override
    public void onStartScreen() {
        myWindow.getElement().setVisible(true);
        Journal journal = world.getPlayer().getJournal();
        EngineUtils.resetScrollbarX(activeQuests);
        EngineUtils.resetScrollbarX(completedQuests);
        activeQuests.clear();
        completedQuests.clear();
        for (JournalEntry e : journal.getQuests().values()) {
            if (e.isCompleted()) {
                completedQuests.addItem(e);
            } else {
                activeQuests.addItem(e);
            }
        }
        EngineUtils.resetScrollbarX(codexList);
        codexList.clear();

        codexList.addAllItems(journal.getCodex().values());
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
        JournalEntry entry = (JournalEntry)event.getSelection().get(0);
        EngineUtils.setTextForGUIElement(tg.getSelectedTab().getElement().findElementByName("#message_text"), entry.getFullText(world));
        myWindow.getElement().layoutElements();

        // Player must see the log entry to discover some answers in the dialogues of the quest "The burden of the metropolis"
        if(entry.getId().equals("metropole_burden")){
            if(entry.contains("document_introduction") && entry.contains("document_taxes") && entry.contains("document_dues") && entry.contains("document_embargo")){
                world.getGlobalVariables().put("metropole_burden.documents_readed", true);
                LoggerFactory.getLogger(JournalScreenController.class).info("'Metropole Burden' quest journal viewed");
            }
        }
    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }

    @NiftyEventSubscriber(id = "journal_window")
    public void onClose(final String id, final WindowClosedEvent event) {
        closeScreen();
    }

    @Override
    public void inputUpdate(Input input) {
        super.inputUpdate(input);

        if(input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.JOURNAL))) {
            closeScreen();
            return;
        }
    }
}