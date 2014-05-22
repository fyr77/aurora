/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 22.05.14
 * Time: 20:58
 */


package ru.game.aurora.common;

import org.newdawn.slick.Image;
import ru.game.aurora.application.ResourceManager;

import java.io.Serializable;

public class Drawable implements Serializable {
    private static final long serialVersionUID = -6258994732957709478L;

    private String id;

    private int tx;

    private int ty;

    public Drawable(String id) {
        this(id, -1, -1);
    }

    public Drawable(String id, int tx, int ty) {
        this.id = id;
        this.tx = tx;
        this.ty = ty;
    }

    public Image getImage() {
        if (tx == -1) {
            return ResourceManager.getInstance().getImage(id);
        } else {
            return ResourceManager.getInstance().getSpriteSheet(id).getSprite(tx, ty);
        }
    }
}
