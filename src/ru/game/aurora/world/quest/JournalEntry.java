package ru.game.aurora.world.quest;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.PlaceholderResolver;
import ru.game.aurora.world.World;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Topic in a game journal. It can be a quest, an alien species info, or a crew member dossier
 */
public class JournalEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;

    private final Set<String> messageIds = new LinkedHashSet<>();

    public JournalEntry(String id) {
        this.id = id;
    }

    public JournalEntry(String id, String... messages) {
        this.id = id;
        Collections.addAll(messageIds, messages);
    }

    public String getId() {
        return id;
    }

    public boolean contains(String id)
    {
        return messageIds.contains(id);
    }

    public String getLocalizedCaption() {
        return Localization.getText("journal", id + ".title");
    }

    public void addMessage(String textId) {
        GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "journal_updated"), getLocalizedCaption()));
        messageIds.add(textId);
    }

    public String getFullText(World world) {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (String messageId : messageIds) {
            if (!isFirst) {
                builder.append("==============================\n");
            }
            builder.append(PlaceholderResolver.resolvePlaceholders(Localization.getText("journal", id + "." + messageId), world.getGlobalVariables())).append("\n");
            isFirst = false;

        }

        return builder.toString();
    }

    @Override
    public String toString() {
        return getLocalizedCaption();
    }
}
