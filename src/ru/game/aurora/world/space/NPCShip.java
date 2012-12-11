/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 18:34
 */
package ru.game.aurora.world.space;

import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;

public class NPCShip extends BasePositionable implements GameObject {
    private String sprite;

    private AlienRace race;

    private NPC capitain;

    private String name;

    public NPCShip(int x, int y, String sprite, AlienRace race, NPC capitain, String name) {
        super(x, y);
        this.sprite = sprite;
        this.race = race;
        this.capitain = capitain;
        this.name = name;
    }

    @Override
    public void update(JGEngine engine, World world) {
        // do nothing currently
    }

    @Override
    public void draw(JGEngine engine, Camera camera) {
        engine.drawImage(sprite, camera.getXCoord(x), camera.getYCoord(y));
    }
}
