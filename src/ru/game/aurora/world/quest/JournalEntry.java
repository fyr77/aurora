package ru.game.aurora.world.quest;

import ru.game.aurora.application.Localization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Topic in a game journal. It can be a quest, an alien species info, or a crew member dossier
 */
public class JournalEntry implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String id;

    private List<String> messageIds = new ArrayList<>();

    public JournalEntry(String id)
    {
        this.id = id;
    }

    public JournalEntry(String id, String... messages)
    {
        this.id = id;
        Collections.addAll(messageIds, messages);
    }

    public String getLocalizedCaption()
    {
        return Localization.getText("journal", id + ".title");
    }

    public void addMessage(String textId)
    {
        messageIds.add(textId);
    }

    public String getFullText()
    {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (String messageId : messageIds) {
            if (!isFirst) {
                builder.append("==============================\n");
            }
            builder.append(Localization.getText("journal", id + "." + messageId)).append("\n");
            isFirst = false;

        }

        return builder.toString();
    }

    @Override
    public String toString() {
        return getLocalizedCaption();
    }
}
