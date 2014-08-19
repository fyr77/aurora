/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 28.08.13
 * Time: 23:08
 */
package ru.game.aurora.player.research;


import ru.game.aurora.application.JsonConfigManager;
import ru.game.aurora.player.earth.EarthResearch;
import ru.game.aurora.player.engineering.EngineeringProject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RnDSet implements Serializable {

    private static final long serialVersionUID = 1;

    private final Map<String, ResearchProjectDesc> researchProjects = new HashMap<>();

    private final Map<String, EarthResearch> earthResearchProjects = new HashMap<>();

    private final Map<String, EngineeringProject> engineeringProjects = new HashMap<>();

    public RnDSet() {
        JsonConfigManager<BaseResearchWithFixedProgress> rp = new JsonConfigManager<>(BaseResearchWithFixedProgress.class, "resources/items/research");
        researchProjects.putAll(rp.getEntities());
    }

    public Map<String, EarthResearch> getEarthResearchProjects() {
        return earthResearchProjects;
    }

    public Map<String, ResearchProjectDesc> getResearchProjects() {
        return researchProjects;
    }

    public Map<String, EngineeringProject> getEngineeringProjects() {
        return engineeringProjects;
    }
}
