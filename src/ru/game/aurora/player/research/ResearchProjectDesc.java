/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 16:33
 */
package ru.game.aurora.player.research;

import ru.game.aurora.player.Player;

/**
 * Base class for research projects
 */
public abstract class ResearchProjectDesc {
    protected String name;

    protected String description;

    protected ResearchProjectDesc(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Update progress of this project. Called once every turn
     *
     * @param scientists How many scientists are currently working on this project
     */
    public abstract void update(Player player, int scientists);

    /**
     * @return A string describing current status of this research
     */
    public abstract String getStatusString(Player player, int scientists);

    public abstract boolean isCompleted();

    /**
     * Returns true if this is a repeatable project (like converting raw data into processed)
     * Such projects are always available and are not added to list of completed projects
     */
    public abstract boolean isRepeatable();

}
