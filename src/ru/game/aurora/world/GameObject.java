/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:16
 */
package ru.game.aurora.world;

import jgame.platform.JGEngine;

public interface GameObject
{
    public void update(JGEngine engine);

    public void draw(JGEngine engine);
}
