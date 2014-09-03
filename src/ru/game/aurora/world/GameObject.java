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


    public Image getImage();

    public String getName();

    public boolean canBeInteracted();

    public void interact(World world);

    public String getInteractMessage();

    public boolean canBeAttacked();

    public void onAttack(World world, GameObject attacker, int damaged);

    public boolean isAlive();

    public String getScanDescription(World world);

    public ScanGroup getScanGroup();

    public Faction getFaction();

}
