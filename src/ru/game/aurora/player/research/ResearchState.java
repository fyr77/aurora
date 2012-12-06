/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 16:15
 */
package ru.game.aurora.player.research;

import ru.game.aurora.player.research.projects.Cartography;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all data about science and research done by player
 */
public class ResearchState {

    private int idleScientists;

    private List<ResearchProjectDesc> availableProjects = new ArrayList<ResearchProjectDesc>();

    private List<ResearchProjectDesc> completedProjects = new ArrayList<ResearchProjectDesc>();

    private List<ResearchProjectState> currentProjects = new ArrayList<ResearchProjectState>();

    private Geodata geodata = new Geodata();

    public ResearchState(int idleScientists) {
        this.idleScientists = idleScientists;
        availableProjects.add(new Cartography(geodata));
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
}
