package ru.game.aurora.world.quest;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Contains log entries about game world
 */
public class Journal implements Serializable
{
    private static final long serialVersionUID = 1L;

    private SortedMap<String, JournalEntry> quests = new TreeMap<>();

    private SortedMap<String, JournalEntry> codex = new TreeMap<>();

    public Map<String, JournalEntry> getQuests() {
        return quests;
    }

    public Map<String, JournalEntry> getCodex() {
        return codex;
    }

    public void addQuest(JournalEntry entry) {
        quests.put(entry.getId(), entry);
    }

    public void addCodex(JournalEntry entry) {
        codex.put(entry.getId(), entry);
    }
}
