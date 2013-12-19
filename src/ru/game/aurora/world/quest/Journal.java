package ru.game.aurora.world.quest;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains log entries about game world
 */
public class Journal implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<JournalEntry> quests = new LinkedList<>();

    private List<JournalEntry> codex = new LinkedList<>();

    public List<JournalEntry> getQuests() {
        return quests;
    }

    public List<JournalEntry> getCodex() {
        return codex;
    }
}
