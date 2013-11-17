package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.LandingParty;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 25.10.13
 * Time: 14:11
 */
public class MonsterController implements Serializable {
    private static final long serialVersionUID = -8278864000200488198L;

    private IMonster myMonster;

    private int turnsBeforeMove;

    private LandingPartyWeapon weapon;

    private ITileMap map;

    public MonsterController(ITileMap map, IMonster myMonster) {
        this.map = map;
        this.myMonster = myMonster;
        this.turnsBeforeMove = myMonster.getSpeed();
        this.weapon = myMonster.getWeapon();
    }

    public void update(GameContainer container, World world) {
        if (myMonster.nowMoving()) {
            return;
        }
        if (myMonster.getHp() <= 0) {
            return;
        }
        if (!world.isUpdatedThisFrame()) {
            return;
        }
        if (--turnsBeforeMove == 0) {
            turnsBeforeMove = myMonster.getSpeed();
            final int x = myMonster.getX();
            final int y = myMonster.getY();

            int newX = x + CommonRandom.getRandom().nextInt(2) - 1;
            int newY = y + CommonRandom.getRandom().nextInt(2) - 1;
            // if we want to attack landing party and it is close enough, move closer

            LandingParty party = world.getPlayer().getLandingParty();


            final double distance = party.getDistance(myMonster);
            if (distance < 1.5 * weapon.getRange()) { //1.5 because of diagonal cells
                if (!map.lineOfSightExists(x, y, party.getX(), party.getY())) {
                    // can't shoot because no line of sight
                    return;
                }
                party.subtractHp(world, weapon.getDamage());
                GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.animal_attack"), myMonster.getName(), weapon.getDamage(), party.getHp()));
                if (weapon.getId().equals("melee")) {
                    world.getCurrentDungeon().getController().setCurrentEffect(new ExplosionEffect(world.getPlayer().getLandingParty().getX(), world.getPlayer().getLandingParty().getY(), "slash", false));
                } else {
                    world.getCurrentDungeon().getController().setCurrentEffect(new BlasterShotEffect(myMonster, world.getPlayer().getLandingParty(), world.getCamera(), 800, weapon));
                }
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

            if (map.isTilePassable(newX, newY)) {
                map.setTilePassable(x, y, true);
                if (newX > x) {
                    myMonster.moveRight();
                } else if (newX < x) {
                    myMonster.moveLeft();
                }

                if (newY < y) {
                    myMonster.moveUp();
                } else if (newY > y) {
                    myMonster.moveDown();
                }
                map.setTilePassable(newX, newY, false);
            }

        }
    }
}
