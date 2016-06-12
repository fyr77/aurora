package ru.game.aurora.world.equip;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.world.*;
import ru.game.aurora.world.planet.LandingParty;

/**
 * Created by User on 12.06.2016.
 * Landing party grenade launcher.
 * Grenades deal damage in the area
 */
public class GrenadeLauncher extends WeaponDesc {

    private static final long serialVersionUID = 1L;

    public GrenadeLauncher(String id, Drawable drawable, int damage, int range, int price, String shotImage, String shotSound, int reloadTurns, String explosionAnimation, String particlesAnimation, int size) {
        super(id, drawable, damage, range, price, shotImage, shotSound, reloadTurns, explosionAnimation, particlesAnimation, size);
    }

    @Override
    public Effect createShotEffect(World world, GameObject shooter, GameObject target, Camera camera, int moveSpeed) {
        return new Grenade(shooter
                , target
                , camera
                , moveSpeed
                , this
        );
    }

    @Override
    public Effect createShotEffect(World world, GameObject shooter, Positionable source, float targetScreenX, float targetScreenY, Camera camera, int moveSpeed, ITileMap map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canTargetEmptySpace() {
        return true;
    }

    public final class Grenade extends BlasterShotEffect {

        private static final long serialVersionUID = 1L;

        private GameObject shooter;

        public Grenade(GameObject shooter, IMovable target, Camera camera, int moveSpeed, WeaponDesc weapon) {
            super(shooter, target, camera, moveSpeed, weapon);
            this.shooter = shooter;
        }


        @Override
        public void update(GameContainer container, World world) {
            super.update(container, world);
            if (isOver()) {
                // explode
                final ITileMap map = world.getCurrentRoom().getMap();
                int x = world.getCamera().getPointTileX((int) Math.ceil(target.getX()) - 1);
                int y = world.getCamera().getPointTileY((int) Math.ceil(target.getY())) - 1;
                for (int xx = -1; xx <= 1; ++xx) {
                    for (int yy = -1; yy <= 1; ++yy) {
                        map.getObjects().add(new ExplosionEffect(x + xx, y + yy, "surface_explosion", false, xx == 0 && yy == 0));
                    }
                }
                final boolean wrapped = map.isWrapped();
                int damage = getDamage();
                if (shooter instanceof LandingParty) {
                    damage = ((LandingParty) shooter).calcDamage(world);
                }
                for (GameObject go : map.getObjects()) {
                    if (!go.canBeAttacked()) {
                        continue;
                    }
                    final double distance = !wrapped ? BasePositionable.getDistance(x, y, go.getX(), go.getY())
                            : BasePositionable.getDistanceWrapped(x, y, go.getX(), go.getY(), map.getWidthInTiles(), map.getHeightInTiles());
                    if (distance == 0) {
                        go.onAttack(world, shooter, damage);
                        GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.splash_hit", go.getName(), damage));
                    } else if (distance == 1) {
                        go.onAttack(world, shooter, damage);
                        GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.splash_hit", go.getName(), damage / 2));
                    }
                }

                final LandingParty landingParty = world.getPlayer().getLandingParty();
                int lpDistance = (int) (map.isWrapped() ? BasePositionable.getDistanceWrapped(x, y, landingParty.getX(), landingParty.getY(), map.getWidthInTiles(), map.getHeightInTiles())
                        : BasePositionable.getDistance(x, y, landingParty.getX(), landingParty.getY()));
                if (lpDistance == 0) {
                    landingParty.subtractHp(world, damage);
                    GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.splash_hit", Localization.getText("gui", "landing_party.title"), damage));
                } else if (lpDistance == 1) {
                    landingParty.subtractHp(world, damage / 2);
                    GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.splash_hit", Localization.getText("gui", "landing_party.title"), damage / 2));
                }
            }
        }
    }
}
