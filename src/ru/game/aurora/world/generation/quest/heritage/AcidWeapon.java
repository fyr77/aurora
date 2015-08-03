package ru.game.aurora.world.generation.quest.heritage;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.world.*;
import ru.game.aurora.world.equip.WeaponDesc;
import ru.game.aurora.world.planet.LandingParty;

/**
 * Weapon that throws balls of acid to a random point nearby landing pary.
 * Acid hurts if you step on it
 */
public class AcidWeapon extends WeaponDesc
{
    public AcidWeapon(String id, Drawable drawable, int damage, int range, int price, String shotImage, String shotSound, int reloadTurns, String explosionAnimation, String particlesAnimation, int size) {
        super("acid"
                , null, 5
                , 10
                , 0
                , "acid_shot"
                , "melee_1"
                , 2
                , null
                , null
                , 1);
    }

    @Override
    public Effect createShotEffect(IMovable source, IMovable target, Camera camera, int moveSpeed) {
        return new AcidShot(source
                , target
                , camera
                , moveSpeed
                , this
        );
    }

    @Override
    public Effect createShotEffect(Positionable source, float targetScreenX, float targetScreenY, Camera camera, int moveSpeed, ITileMap map) {
        return new AcidShot(source, targetScreenX, targetScreenY, camera, moveSpeed, "acid_shot", map);
    }

    private static class AcidShot extends BlasterShotEffect
    {

        public AcidShot(IMovable source, IMovable target, Camera camera, int moveSpeed, WeaponDesc weapon) {
            super(source, target, camera, moveSpeed, weapon);
        }

        public AcidShot(Positionable source, float targetScreenX, float targetScreenY, Camera camera, int moveSpeed, String weaponSprite, ITileMap map) {
            super(source, targetScreenX, targetScreenY, camera, moveSpeed, weaponSprite, map);
        }

        @Override
        public void update(GameContainer container, World world) {
            super.update(container, world);
            if (isOver()) {
                // add acid pool
                world.getCurrentRoom().getMap().getObjects().add(
                        new AcidPool(world.getCamera().getPointTileX((int) target.getX())
                                , world.getCamera().getPointTileY((int) target.getY()))
                );
            }
        }
    }

    private static class AcidPool extends BaseGameObject
    {
        private int ttl;

        private static final long serialVersionUID = 1L;

        public AcidPool(int x, int y) {
            super(x, y, "acid_pool");
            ttl = CommonRandom.getRandom().nextInt(5) + 3;
        }

        @Override
        public void update(GameContainer container, World world) {
            final LandingParty landingParty = world.getPlayer().getLandingParty();
            if (world.isUpdatedThisFrame()) {
                if (landingParty.getTargetX() == x
                        && landingParty.getTargetY() == y) {
                    GameLogger.getInstance().logMessage(Localization.getText("weapons", "acid.damage_text"));
                    landingParty.subtractHp(world, CommonRandom.getRandom().nextInt(3) + 1);
                }

                if (ttl --> 0) {
                    isAlive = false;
                }
            }
        }
    }
}
