/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 */

package ru.game.aurora.player.research;

public class ResearchProjectState {
    public ResearchProjectDesc desc;

    public int scientists;

    public ResearchProjectState(ResearchProjectDesc desc, int scientists) {
        this.desc = desc;
        this.scientists = scientists;
    }
}
