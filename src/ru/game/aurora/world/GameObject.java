/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:16
 */
package ru.game.aurora.world;

import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;

public interface GameObject {
    public void update(JGEngine engine, World world);

    public void draw(JGEngine engine, Camera camera);
}
