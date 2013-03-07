/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 16:33
 */
package ru.game.aurora.player.research;

import ru.game.aurora.player.earth.EarthResearch;
import ru.game.aurora.world.World;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Base class for research projects
 */
public abstract class ResearchProjectDesc implements Serializable
{
    private static final long serialVersionUID = 929590331247291625L;

    /**
     * Research name, will be used in list of research projects
     */
    protected String name;

    /**
     * Brief description, will be used in list of research projects
     */
    protected String description;

    /**
     * 256x256 icon id
     */
    protected String icon;

    /**
     * Optional report that will be shown to player when research is completed
     * Can be null
     */
    protected ResearchReport report;

    /**
     * After completing this research, these researches will become available
     */
    protected List<ResearchProjectDesc> makesAvailable;

    /**
     * This stuff is launched when this research is completed and its results are dumped on earth
     */
    protected List<EarthResearch> earthProgress;

    protected ResearchProjectDesc(String name, String description, String icon) {
        this.name = name;
        this.description = description;
        this.icon = icon;
    }

    protected ResearchProjectDesc(String name, String description, String icon, ResearchReport report) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.report = report;
    }

    public void addNextResearch(ResearchProjectDesc desc)
    {
        if (makesAvailable == null) {
            makesAvailable = new LinkedList<ResearchProjectDesc>();
        }
        makesAvailable.add(desc);
    }

    public void addEarthProgressResearch(EarthResearch r)
    {
        if (earthProgress == null) {
            earthProgress = new ArrayList<EarthResearch>();
        }
        earthProgress.add(r);
    }

    public List<ResearchProjectDesc> getMakesAvailable() {
        return makesAvailable;
    }

    public List<EarthResearch> getEarthProgress() {
        return earthProgress;
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

    public ResearchReport getReport() {
        return report;
    }

    public void setReport(ResearchReport report) {
        this.report = report;
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

    /**
     * Called when completed project is dumped on earth and its score is calculated
     */
    public void onReturnToEarth(World world)
    {
    }
}
