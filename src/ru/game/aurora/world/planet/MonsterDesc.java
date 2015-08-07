/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 15.08.14
 * Time: 23:19
 */
package ru.game.aurora.world.planet;


import org.newdawn.slick.Image;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.common.ItemWithTextAndImage;
import ru.game.aurora.world.planet.nature.AnimalModifier;

import java.util.Set;

public class MonsterDesc extends ItemWithTextAndImage {
    private static final long serialVersionUID = -8842157131768062878L;

    public Set<AnimalModifier> modifiers;

    public int hp;

    public int turnsBetweenMoves;

    public String weaponId;

    private transient Image image;

    private transient Image deadImage;

    public boolean canBePickedUp;

    public MonsterBehaviour behaviour;

    public MonsterDesc(String id, Drawable drawable) {
        super(id, drawable);
    }

    public MonsterDesc(String id, Set<AnimalModifier> modifiers, int hp, int turnsBetweenMoves, String weaponId, String image, boolean canBePickedUp, MonsterBehaviour behaviour) {
        super(id, new Drawable(image));
        this.modifiers = modifiers;
        this.hp = hp;
        this.turnsBetweenMoves = turnsBetweenMoves;
        this.weaponId = weaponId;
        this.canBePickedUp = canBePickedUp;
        this.behaviour = behaviour;
    }
}
