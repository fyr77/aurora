/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 16:33
 */
package ru.game.aurora.player.research;

import ru.game.aurora.application.Localization;
import ru.game.aurora.player.earth.EarthResearch;
import ru.game.aurora.player.engineering.EngineeringProject;
import ru.game.aurora.world.Listenable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Base class for research projects
 */
public abstract class ResearchProjectDesc extends Listenable {
    private static final long serialVersionUID = 1L;

    /**
     * unique id
     */
    protected String id;

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
    protected List<String> makesAvailable;

    protected List<String> makesAvailableEngineering;

    /**
     * This stuff is launched when this research is completed and its results are dumped on earth
     */
    protected List<String> earthProgress;

    /**
     * If this project requires visiting of specific star systems, this is list of them.
     * It is used to mark them on a global map.
     */
    protected List<StarSystem> targetStarSystems;

    protected ResearchProjectDesc(String id, String icon) {
        this.id = id;
        this.icon = icon;
    }

    protected ResearchProjectDesc(String id, String icon, ResearchReport report) {
        this.id = id;
        this.icon = icon;
        this.report = report;
    }

    public void onCompleted(World world) {
        super.fireEvent(world);
    }

    public void addNextResearch(ResearchProjectDesc desc) {
        if (makesAvailable == null) {
            makesAvailable = new LinkedList<String>();
        }
        makesAvailable.add(desc.getId());
    }

    public void addEngineeringResult(EngineeringProject desc) {
        if (makesAvailableEngineering == null) {
            makesAvailableEngineering = new LinkedList<>();
        }
        makesAvailableEngineering.add(desc.getId());
    }

    public void addEarthProgressResearch(EarthResearch r) {
        if (earthProgress == null) {
            earthProgress = new ArrayList<String>();
        }
        earthProgress.add(r.getId());
    }

    public List<String> getMakesAvailable() {
        return makesAvailable;
    }

    public List<String> getMakesAvailableEngineering() {
        return makesAvailableEngineering;
    }

    public List<String> getEarthProgress() {
        return earthProgress;
    }

    public String getName() {
        return Localization.getText("research", id + ".name");
    }

    public String getDescription() {
        return Localization.getText("research", id + ".desc");
    }

    public String getIcon() {
        return icon;
    }

    public String getId() {
        return id;
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
    public void onReturnToEarth(World world) {
    }

    /**
     * This method is used in nifty-gui lists. Do not change (or add custom view converted to research screen)
     */
    @Override
    public String toString() {
        return getName();
    }

    public List<StarSystem> getTargetStarSystems() {
        if (targetStarSystems == null) {
            targetStarSystems = new ArrayList<>();
        }
        return targetStarSystems;
    }
}
