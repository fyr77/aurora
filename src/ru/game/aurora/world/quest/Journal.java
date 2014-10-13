package ru.game.aurora.world.quest;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Contains log entries about game world
 */
public class Journal implements Serializable {
    private static final long serialVersionUID = 1L;

    private final SortedMap<String, JournalEntry> quests = new TreeMap<>();

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

    public void questCompleted(String entryId) {
        JournalEntry je = quests.get(entryId);
        if (je != null) {
            je.setCompleted(true);
        }
    }

    public void addCodex(JournalEntry entry) {
        codex.put(entry.getId(), entry);
    }
}
