/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 18:34
 */
package ru.game.aurora.world.space;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.shipai.LeaveSystemAI;
import ru.game.aurora.npc.shipai.NPCShipAI;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;

public class NPCShip extends BasePositionable implements GameObject {
    private String sprite;

    private AlienRace race;

    private NPC capitain;

    private String name;

    private NPCShipAI ai;

    public NPCShip(int x, int y, String sprite, AlienRace race, NPC capitain, String name) {
        super(x, y);
        this.sprite = sprite;
        this.race = race;
        this.capitain = capitain;
        this.name = name;

        ai = new LeaveSystemAI();
    }

    public void setAi(NPCShipAI ai) {
        this.ai = ai;
    }

    @Override
    public void update(GameContainer container, World world) {
        if (ai != null) {
            ai.update(this, world);
        }
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        g.drawImage(ResourceManager.getInstance().getImage(sprite), camera.getXCoord(x), camera.getYCoord(y));
    }

    /**
     * Returns true if this ship is hostile to player
     * Hostile ships can not be hailed
     */
    public boolean isHostile() {
        return false;
    }

    public AlienRace getRace() {
        return race;
    }
}
