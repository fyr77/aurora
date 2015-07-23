package ru.game.aurora.world.dungeon;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.world.AuroraTiledMap;
import ru.game.aurora.world.World;

/**
 * This is a placeholder that can be set in map editor and later be replaced by some other object by code
 */
public class DungeonPlaceholder extends DungeonObject {

    public DungeonPlaceholder(AuroraTiledMap map, int groupId, int objectId) {
        super(map, groupId, objectId);
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {
        // nothing
    }

    @Override
    public boolean isAlive() {
        // placeholders disappear and are not kept in memory, they are needed only on map load
        return false;
    }
}
