/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:16
 */
package ru.game.aurora.world;

import org.newdawn.slick.Image;
import ru.game.aurora.npc.Faction;

import java.io.Serializable;

public interface GameObject extends Serializable, Updatable, IMovable, IDrawable {


    Image getImage();

    String getName();

    boolean canBeInteracted();

    void interact(World world);

    String getInteractMessage();

    boolean canBeAttacked();

    void onAttack(World world, GameObject attacker, int damaged);

    boolean isAlive();

    String getScanDescription(World world);

    ScanGroup getScanGroup();

    Faction getFaction();

}
