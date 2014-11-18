/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 11.09.13
 * Time: 17:20
 */
package ru.game.aurora.world.dungeon;

import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.world.AuroraTiledMap;
import ru.game.aurora.world.BaseGameObject;

/**
 * Person or object that you can meet on planet surface
 */
public class DungeonObject extends BaseGameObject {

    private static final long serialVersionUID = 2L;

    private final String name;

    public DungeonObject(AuroraTiledMap map, int groupId, int objectId) {
        super(map.getMap().getObjectX(groupId, objectId) / AuroraGame.tileSize
                , map.getMap().getObjectY(groupId, objectId) / AuroraGame.tileSize - 1
        ); // -1 because Y coord in editor starts from 1
        this.name = map.getMap().getObjectName(groupId, objectId);
    }

    @Override
    public String getName() {
        return name;
    }

}
