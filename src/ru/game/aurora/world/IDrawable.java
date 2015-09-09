package ru.game.aurora.world;


import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;

/**
 * Base interface for game entities that can be rendered on the screen
 */
public interface IDrawable {

    /**
     * Draw this object
     *
     * @param container Slick2D engine object that provides access to input, window and other system stuff
     * @param graphics  Graphics to be rendered on
     * @param camera    Object that should be used for transforming game coordinates into screen coordinates
     * @param world     Game state
     */
    void draw(GameContainer container, Graphics graphics, Camera camera, World world);

}
