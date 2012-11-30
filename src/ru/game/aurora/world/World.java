/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:09
 */
package ru.game.aurora.world;

import jgame.platform.JGEngine;
import ru.game.aurora.player.Player;
import ru.game.aurora.world.space.GalaxyMap;

public class World implements GameObject
{
    private Room currentRoom;

    private Player player;

    public World() {
        player = new Player();
        currentRoom = new GalaxyMap(100, 100);
        currentRoom.enter(player);
    }

    @Override
    public void update(JGEngine engine) {
        currentRoom.update(engine);
    }

    @Override
    public void draw(JGEngine engine) {
        currentRoom.draw(engine);
    }
}
