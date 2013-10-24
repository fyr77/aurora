package ru.game.aurora.world.space;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.effects.WarpEffect;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.world.World;

/**
 * Date: 23.10.13
 * Time: 21:36
 */

public class GardenersShip extends NPCShip implements SpaceObject {

    private static final long serialVersionUID = -1613654729672880450L;

    private String sprite;

    private float spriteAlpha = 1.0f;

    private boolean timeToLeave = false;

    public GardenersShip(int x, int y, String sprite, AlienRace race, NPC captain, String name) {
        super(x, y, sprite, race, captain, name);
        this.sprite = sprite;
        this.setStationary(true);
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);
        if (timeToLeave && world.isUpdatedThisFrame()) {
            warpAway(world.getCurrentStarSystem());
        }
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        org.newdawn.slick.Image img;
        img = ResourceManager.getInstance().getImage(sprite);
        if (spriteAlpha == 0.0f) {
            img.setAlpha(1.0f);
        } else {
            img.setAlpha(spriteAlpha);
        }
        g.drawImage(img, camera.getXCoord(x), camera.getYCoord(y));
    }

    @Override
    public void onAttack(World world, SpaceObject attacker, int dmg) {
        warpAway(world.getCurrentStarSystem());
    }

    public void setAlpha(float alpha) {
        spriteAlpha = alpha;
    }

    public void warpAway (StarSystem ss) {
        ss.addEffect(new WarpEffect(this));
    }

    public void warpAwayNextTurn () {
        timeToLeave = true;
    }
}
