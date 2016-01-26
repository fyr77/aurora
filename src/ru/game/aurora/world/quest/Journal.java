package ru.game.aurora.world.quest;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Contains log entries about game world and quests
 */
public class Journal implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * List of quest records
     */
    private final SortedMap<String, JournalEntry> quests = new TreeMap<>();

    /**
     * List of information entries about characters, races etc
     */
    private final SortedMap<String, JournalEntry> codex = new TreeMap<>();

    public Map<String, JournalEntry> getQuests() {
        return quests;
    }

    public Map<String, JournalEntry> getCodex() {
        return codex;
    }

    public void addQuest(JournalEntry entry) {
        quests.put(entry.getId(), entry);
    }

    /**
     * Adds a new paragraph of text to the given quest.
     * This messages will be added to the bottom of quest text.
     * These are actually ids, so calling addQuestEntries("quest1", "start") will add a message
     * from journal.properties with id quest1.start
     *
     * @param entryId  Quest id
     * @param messages Message ids to add.
     */
    public void addQuestEntries(String entryId, String... messages) {
        JournalEntry journalEntry = quests.get(entryId);
        if (journalEntry == null) {
            journalEntry = new JournalEntry(entryId, messages);
            quests.put(entryId, journalEntry);
            return;
        }
        for (String m : messages) {
            journalEntry.addMessage(m);
        }
    }

    public void questCompleted(String entryId, String... messages) {
        JournalEntry je = quests.get(entryId);
        if (je != null) {
            for (String m : messages) {
                je.addMessage(m);
            }
            je.setCompleted(true);
        }
    }

    public void addCodex(JournalEntry entry) {
        codex.put(entry.getId(), entry);
    }
}
