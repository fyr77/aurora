/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:09
 */
package ru.game.aurora.world;

import jgame.platform.JGEngine;
import ru.game.aurora.player.Player;
import ru.game.aurora.world.space.GalaxyMap;

public class World
{
    private Room currentRoom;

    private GalaxyMap galaxyMap;

    private Player player;

    private boolean updatedThisFrame;

    public World(int sizeX, int sizeY) {
        player = new Player();
        currentRoom = galaxyMap = new GalaxyMap(sizeX, sizeY);
        currentRoom.enter(player);
        updatedThisFrame = false;
    }

    public void update(JGEngine engine) {
        updatedThisFrame = false;
        currentRoom.update(engine, this);
    }

    public void draw(JGEngine engine) {
        currentRoom.draw(engine);
    }

    public boolean isUpdatedThisFrame() {
        return updatedThisFrame;
    }

    public void setUpdatedThisFrame(boolean updatedThisFrame) {
        this.updatedThisFrame = updatedThisFrame;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public GalaxyMap getGalaxyMap() {
        return galaxyMap;
    }

    public Player getPlayer() {
        return player;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }
}
