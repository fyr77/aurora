/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 22.05.14
 * Time: 20:58
 */


package ru.game.aurora.common;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.ResourceManager;

import java.io.Serializable;

public class Drawable implements Serializable {
    private static final long serialVersionUID = -6258994732957709478L;

    private String id;

    private int tx = -1;

    private int ty = -1;

    private boolean isAnimation = false;

    private transient Image flippedCopy;

    private transient Image image = null;

    private transient Animation anim = null;

    public Drawable(Image img) {
        image = img;
    }

    public Drawable(Image img, Image flippedCopy) {
        this.image = img;
        this.flippedCopy = flippedCopy;
    }

    public Drawable(String id) {
        this(id, -1, -1, false);
    }

    public Drawable(String id, boolean isAnimation) {
        this(id, -1, -1, isAnimation);
    }

    public Drawable(String id, int tx, int ty) {
        this(id, tx, ty, false);
    }

    public Drawable(String id, int tx, int ty, boolean isAnimation) {
        this.id = id;
        this.tx = tx;
        this.ty = ty;
        this.isAnimation = isAnimation;
    }

    public String getId(){
        return id;
    }

    private void loadImage() {
        if (!isAnimation) {
            if (tx == -1) {
                image = ResourceManager.getInstance().getImage(id);
            } else {
                image = ResourceManager.getInstance().getSpriteSheet(id).getSprite(tx, ty);
            }
        } else {
            anim = ResourceManager.getInstance().getAnimation(id);
            anim.setLooping(true);
            anim.setAutoUpdate(true);
        }
    }

    public Image getImage() {
        if ((!isAnimation && image == null) || (isAnimation && anim == null)) {
            loadImage();
        }
        return isAnimation ? anim.getCurrentFrame() : image;
    }

    public void draw(Graphics g, float x, float y, boolean flipped) {
        if ((!isAnimation && image == null) || (isAnimation && anim == null)) {
            loadImage();
        }

        if (isAnimation) {
            g.drawAnimation(anim, x, y);
        } else {
            if (flipped) {
                g.drawImage(getFlippedCopy(), x, y);
            } else {
                g.drawImage(image, x, y);
            }
        }
    }

    public Image getFlippedCopy() {
        if (isAnimation) {
            return getImage();
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Drawable drawable = (Drawable) o;

        return tx == drawable.tx && ty == drawable.ty && !(id != null ? !id.equals(drawable.id) : drawable.id != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + tx;
        result = 31 * result + ty;
        return result;
    }
}
