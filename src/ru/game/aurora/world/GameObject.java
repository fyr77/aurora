/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:16
 */
package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;

public interface GameObject {

    public void update(GameContainer container, World world);

    public void draw(GameContainer container, Graphics graphics, Camera camera);

}
