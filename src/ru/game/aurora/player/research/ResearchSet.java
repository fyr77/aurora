/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 28.08.13
 * Time: 23:08
 */
package ru.game.aurora.player.research;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ResearchSet implements Serializable {
    private static final long serialVersionUID = 1;

    private Map<String, ResearchProjectDesc> researchProjects = new HashMap<>();

    public void addProject(ResearchProjectDesc project) {
        researchProjects.put(project.getName(), project);
    }

    public ResearchProjectDesc getProject(String key) {
        return researchProjects.get(key);
    }

    public void remove(String key) {
        researchProjects.remove(key);
    }
}
