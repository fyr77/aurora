package ru.game.aurora.world.dungeon;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.Camera;
import ru.game.aurora.world.AuroraTiledMap;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.LandingParty;

/**
 * Created by Егор on 10.01.2016.
 * Triggers a callback when player steps over it
 */
public class DungeonTrigger extends DungeonObject {

    private static final long serialVersionUID = 1;

    private IStateChangeListener<World> listener;

    private int width;

    private int height;

    private boolean reusable = false;

    private boolean enabled = true;

    public DungeonTrigger(String name, int x, int y, IStateChangeListener<World> listener) {
        super(name, x, y);
        this.listener = listener;
        this.width = 1;
        this.height = 1;
    }

    public DungeonTrigger(AuroraTiledMap map, int groupId, int objectId) {
        super(map, groupId, objectId);
        this.width = map.getMap().getObjectWidth(groupId, objectId) / AuroraGame.tileSize;
        this.height = map.getMap().getObjectHeight(groupId, objectId) / AuroraGame.tileSize;
    }

    public void setListener(IStateChangeListener<World> listener) {
        this.listener = listener;
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {
        // nothing
    }

    @Override
    public void update(GameContainer container, World world) {
        if (!enabled || !world.isUpdatedThisFrame()) {
            return;
        }

        LandingParty p = world.getPlayer().getLandingParty();
        if (getX() <= p.getX() && getX() + width > p.getX() && getY() <= p.getY() && getY() + height > p.getY()) {
            listener.stateChanged(world);
            isAlive = reusable;
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
