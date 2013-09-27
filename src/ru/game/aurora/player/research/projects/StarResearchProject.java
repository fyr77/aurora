/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 29.01.13
 * Time: 14:44
 */

package ru.game.aurora.player.research.projects;

import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.space.StarSystem;

public class StarResearchProject extends SpaceObjectResearchProject
{

    private static final long serialVersionUID = 1L;

    public StarResearchProject(StarSystem starSystem) {
        super("star_exploration"
                , "star_research"
                , 30, starSystem, new BasePositionable(0, 0));
    }

    @Override
    public String getName() {
        return String.format(super.getName(), starSystem.getGlobalMapX(), starSystem.getGlobalMapY());
    }
}
