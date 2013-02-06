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

    private static final long serialVersionUID = -259133742530809689L;

    public StarResearchProject(StarSystem starSystem) {
        super(String.format("Star exploration at [%d, %d]"
                , starSystem.getGlobalMapX()
                , starSystem.getGlobalMapY())
                , "Collecting of detailed data about anomalous star in specific star system, located at given star coordinates. In order to perform measurements, starship must stay close to star"
                , "star_research"
                , 30, starSystem, new BasePositionable(0, 0));
    }

}
