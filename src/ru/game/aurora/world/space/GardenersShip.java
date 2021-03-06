package ru.game.aurora.world.space;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.effects.WarpEffect;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.World;

/**
 * Date: 23.10.13
 * Time: 21:36
 */

public class GardenersShip extends NPCShip {
    private static final long serialVersionUID = -1613654729672880450L;

    private final String sprite;

    private float spriteAlpha = 1.0f;

    private boolean timeToLeave = false;

    public GardenersShip(String shipId) {
        super(shipId);
        this.sprite = getDesc().getDrawable().getId();
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);
        if (timeToLeave && world.isUpdatedThisFrame()) {
            warpAway(world, world.getCurrentStarSystem());
        }
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {
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
    public void onAttack(World world, GameObject attacker, int dmg) {
        warpAway(world, world.getCurrentStarSystem());
    }

    public void setAlpha(float alpha) {
        spriteAlpha = alpha;
    }

    public void warpAway(World world, StarSystem ss) {
        final WarpEffect warpEffect = new WarpEffect(this);
        ss.addEffect(warpEffect);
        // show dialog after first wrap seen by player
        if (world.getGlobalVariables().containsKey("gardeners.first_warp")) {
            warpEffect.setEndListener(new IStateChangeListener<World>() {

                private static final long serialVersionUID = -4524072912665603030L;

                @Override
                public void stateChanged(World world) {
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/first_gardener_warp.json"));
                }
            });
            world.getGlobalVariables().remove("gardeners.first_warp");
        }
        isAlive = false;
    }

    public void warpAwayNextTurn() {
        timeToLeave = true;
    }
}
