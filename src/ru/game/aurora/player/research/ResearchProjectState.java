/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 */

package ru.game.aurora.player.research;

import java.io.Serializable;

public class ResearchProjectState implements Serializable {
    public ResearchProjectDesc desc;

    public int scientists;

    public ResearchProjectState(ResearchProjectDesc desc, int scientists) {
        this.desc = desc;
        this.scientists = scientists;
    }
}
