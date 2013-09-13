package ru.game.aurora.world.dungeon;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.tiled.TiledMap;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.world.ITileMap;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.LandingParty;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 13.09.13
 * Time: 12:40
 */
public class DungeonMonster extends DungeonObject
{
    private static final long serialVersionUID = 2369918580875698530L;

    private LandingPartyWeapon weapon;

    private int speed;

    private int turnsBeforeMove;

    private int hp;

    private transient ITileMap owner;

    public DungeonMonster(TiledMap map, int groupId, int objectId) {
        super(map, groupId, objectId);
        weapon = ResourceManager.getInstance().getLandingPartyWeapons().getEntity(map.getObjectProperty(groupId, objectId, "weapon", null));
        turnsBeforeMove = speed = Integer.parseInt(map.getObjectProperty(groupId, objectId, "speed", "0"));
        hp = Integer.parseInt(map.getObjectProperty(groupId, objectId, "hp", "1"));

    }

    @Override
    public boolean isAlive() {
        return hp > 0;
    }

    @Override
    public void update(GameContainer container, World world) {
        if (hp <= 0) {
            return;
        }
        if (!world.isUpdatedThisFrame()) {
            return;
        }
        if (--turnsBeforeMove == 0) {
            turnsBeforeMove = speed;
            int newX = x + CommonRandom.getRandom().nextInt(2) - 1;
            int newY = y + CommonRandom.getRandom().nextInt(2) - 1;
            // if we want to attack landing party and it is close enough, move closer

            LandingParty party = world.getPlayer().getLandingParty();


            final double distance = this.getDistance(party);
            if (distance < 1.5 * weapon.getRange()) { //1.5 because of diagonal cells
                party.subtractHp(weapon.getDamage());
                GameLogger.getInstance().logMessage(getName() + " attacks! " + weapon.getDamage() + " damage done, " + party.getHp() + " hp remaining");
                world.getCurrentDungeon().getController().setCurrentEffect(new BlasterShotEffect(this, world.getPlayer().getLandingParty(), world.getCamera(), 800, weapon.getShotImage()));
                newX = x;
                newY = y;
            } else if (distance < 5 * weapon.getRange()) {
                if (x < party.getX() - 1) {
                    newX = x + 1;
                } else if (x > party.getX() + 1) {
                    newX = x - 1;
                }

                if (y < party.getY() - 1) {
                    newY = y + 1;
                } else if (y > party.getY() + 1) {
                    newY = y - 1;
                }
            }

            owner = world.getCurrentDungeon().getMap();
            if (owner.isTilePassable(newX, newY)) {
                owner.setTilePassable(x, y, true);
                x = newX;
                y = newY;
                owner.setTilePassable(x, y, false);
            }

        }
    }

    @Override
    public boolean canBeShotAt() {
        return hp > 0;
    }

    @Override
    public void onShotAt(int damage) {
        hp -= damage;
        if (hp <= 0) {
            // clean obstacle flag
            owner.setTilePassable(x, y, true);
        }
    }
}
