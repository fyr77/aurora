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

    private transient Image flippedCopy;

    private transient Image image = null;

    public Drawable(Image img) {
        image = img;
    }

    public Drawable(String id) {
        this(id, -1, -1);
    }

    public Drawable(String id, int tx, int ty) {
        this.id = id;
        this.tx = tx;
        this.ty = ty;

        loadImage();
    }

    private void loadImage() {
        if (tx == -1) {
            image = ResourceManager.getInstance().getImage(id);
        } else {
            image = ResourceManager.getInstance().getSpriteSheet(id).getSprite(tx, ty);
        }
    }

    public Image getImage() {
        if (image == null) {
            loadImage();
        }
        return image;
    }

    public Image getFlippedCopy() {
        if (flippedCopy != null) {
            return flippedCopy;
        }
        if (tx == -1) {
            flippedCopy = ResourceManager.getInstance().getFlippedImage(id);
        } else {
            flippedCopy = ResourceManager.getInstance().getSpriteSheet(id).getSprite(tx, ty).getFlippedCopy(true, false);
        }
        return flippedCopy;
    }
}
