package ru.game.aurora.world.equip;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.npc.shipai.LandAI;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;

/**
 * Created by Егор on 29.09.2015.
 * Fires a huge torpedo. Torpedo is like a ship, has a movement speed and can be shot. Deals a lot of damage.
 */
public class TorpedoLauncher extends WeaponDesc {

    public TorpedoLauncher(String id, Drawable drawable, int damage, int range, int price, String shotImage, String shotSound, int reloadTurns, String explosionAnimation, String particlesAnimation, int size) {
        super(id, drawable, damage, range, price, shotImage, shotSound, reloadTurns, explosionAnimation, particlesAnimation, size);
    }

    @Override
    public Effect createShotEffect(World world, GameObject shooter, GameObject target, Camera camera, int moveSpeed) {
        Torpedo t = new Torpedo(shooter.getTargetX(), shooter.getTargetY(), target, shooter.getFaction(), getDamage());
        world.getCurrentRoom().getMap().getObjects().add(t);
        return null;
    }

    public static class Torpedo extends NPCShip {
        private GameObject target;

        private int ttl;

        private int damage;

        public Torpedo(int x, int y, GameObject target, Faction faction, int damage) {
            super(x, y, "torpedo", faction, null, "torpedo", 4);
            setSpeed(1);
            this.target = target;
            setAi(new LandAI(target));
            setMovementSpeed(2);
            ttl = 20;
            this.damage = damage;
        }

        @Override
        public void update(GameContainer container, World world) {
            super.update(container, world);
            if (world.isUpdatedThisFrame()) {
                if (BasePositionable.getDistance(this, target) == 0) {
                    target.onAttack(world, this, damage);
                    world.getCurrentRoom().getMap().getEffects().add(new ExplosionEffect(x, y, "ship_explosion", false, true));
                    isAlive = false;
                } else if (ttl-- <= 0) {
                    isAlive = false;
                    GameLogger.getInstance().logMessage(Localization.getText("gui", "torpedo.selfdestruct"));
                    world.getCurrentRoom().getMap().getEffects().add(new ExplosionEffect(x, y, "ship_explosion", false, true));
                }

            }
        }
    }
}
