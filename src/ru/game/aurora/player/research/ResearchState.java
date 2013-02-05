/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 16:15
 */
package ru.game.aurora.player.research;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.player.research.projects.AnimalResearch;
import ru.game.aurora.player.research.projects.AstronomyResearch;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.nature.AnimalSpeciesDesc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains all data about science and research done by player
 */
public class ResearchState implements Serializable
{
    private static final long serialVersionUID = -5676580254565166442L;

    private int idleScientists;

    private List<ResearchProjectDesc> availableProjects = new ArrayList<ResearchProjectDesc>();

    private List<ResearchProjectDesc> completedProjects = new ArrayList<ResearchProjectDesc>();

    private List<ResearchProjectState> currentProjects = new ArrayList<ResearchProjectState>();

    private Geodata geodata = new Geodata();

    private int processedAstroData;

    public ResearchState(int idleScientists) {
        this.idleScientists = idleScientists;
        availableProjects.add(new AstronomyResearch());
    }

    public Geodata getGeodata() {
        return geodata;
    }

    public List<ResearchProjectDesc> getAvailableProjects() {
        return availableProjects;
    }

    public List<ResearchProjectDesc> getCompletedProjects() {
        return completedProjects;
    }

    public List<ResearchProjectState> getCurrentProjects() {
        return currentProjects;
    }

    public int getIdleScientists() {
        return idleScientists;
    }

    public void setIdleScientists(int idleScientists) {
        this.idleScientists = idleScientists;
    }

    /**
     * Called when turn passes.
     * Updates research progress for current projects
     */
    public void update(World world) {
        for (Iterator<ResearchProjectState> iter = currentProjects.iterator(); iter.hasNext();) {
            ResearchProjectState state = iter.next();
            state.desc.update(world, state.scientists);
            if (state.desc.isCompleted()) {
                iter.remove();
                if (!state.desc.isRepeatable()) {
                    completedProjects.add(state.desc);
                } else {
                    availableProjects.add(state.desc);
                }
                idleScientists += state.scientists;
                if (state.desc.getReport() != null) {
                    world.addOverlayWindow(new ResearchReportScreen(state.desc, state.desc.getReport()));
                }
                GameLogger.getInstance().logMessage("Research project " + state.desc.name + " completed");
            }
        }
    }

    public boolean containsResearchFor(AnimalSpeciesDesc animalSpeciesDesc) {
        for (ResearchProjectState c : currentProjects) {
            if (c.desc instanceof AnimalResearch && ((AnimalResearch) c.desc).getDesc() == animalSpeciesDesc) {
                return true;
            }
        }

        for (ResearchProjectDesc d : completedProjects) {
            if (d instanceof AnimalResearch && ((AnimalResearch) d).getDesc() == animalSpeciesDesc) {
                return true;
            }
        }

        for (ResearchProjectDesc d : availableProjects) {
            if (d instanceof AnimalResearch && ((AnimalResearch) d).getDesc() == animalSpeciesDesc) {
                return true;
            }
        }

        return false;
    }

    public void addProcessedAstroData(int value) {
        processedAstroData += value;
    }

    public int dumpAstroData() {
        int rz = processedAstroData;
        processedAstroData = 0;
        return rz;
    }

}
