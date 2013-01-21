/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 16:33
 */
package ru.game.aurora.player.research;

import ru.game.aurora.world.World;

import java.io.Serializable;

/**
 * Base class for research projects
 */
public abstract class ResearchProjectDesc implements Serializable {
    protected String name;

    protected String description;

    protected String icon;

    protected ResearchProjectDesc(String name, String description, String icon) {
        this.name = name;
        this.description = description;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    /**
     * Update progress of this project. Called once every turn
     *
     * @param scientists How many scientists are currently working on this project
     */
    public abstract void update(World world, int scientists);

    /**
     * @return A string describing current status of this research
     */
    public abstract String getStatusString(World world, int scientists);

    public abstract boolean isCompleted();

    /**
     * Returns true if this is a repeatable project (like converting raw data into processed)
     * Such projects are always available and are not added to list of completed projects
     */
    public abstract boolean isRepeatable();

    /**
     * Get resulting score for this research
     */
    public abstract int getScore();


}
