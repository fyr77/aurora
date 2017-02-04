package ru.game.aurora.world.quest;

import ru.game.aurora.application.Localization;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;

/**
 * Created by User on 08.01.2017.
 * Unity quest
 */
public class UnityQuest extends GameEventListener {

    private static final long serialVersionUID = 8713537599537481751L;

    private StarSystem targetSystem;

    public UnityQuest(World world){
        this.targetSystem = (StarSystem)world.getGlobalVariables().get("unity_station_system");
    }

    @Override
    public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject) {
        if (galaxyMapObject == targetSystem) {
            return Localization.getText("journal", "unity.title");
        }
        return null;
    }
}
