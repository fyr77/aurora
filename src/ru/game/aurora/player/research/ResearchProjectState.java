/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 */

package ru.game.aurora.player.research;

import java.io.Serializable;

public class ResearchProjectState implements Serializable {

    private static final long serialVersionUID = 6821542618978499031L;

    public ResearchProjectDesc desc;

    public int scientists;

    public ResearchProjectState(ResearchProjectDesc desc) {
        this.scientists = 0;
        this.desc = desc;
    }

    public ResearchProjectState(ResearchProjectDesc desc, int scientists) {
        this.desc = desc;
        this.scientists = scientists;
    }
}
