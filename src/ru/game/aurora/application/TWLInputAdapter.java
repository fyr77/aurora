/**
 * User: jedi-philosopher
 * Date: 01.01.13
 * Time: 22:39
 */
package ru.game.aurora.application;

/*
 * Copyright (c) 2008-2010, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import de.matthiasmann.twl.GUI;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Input;
import org.newdawn.slick.util.InputAdapter;

/**
 * A Slick InputListener which delegates to TWL.
 * <p/>
 * It should be added to Slick's Input class as primary listener:<br>
 * {@code input.addPrimaryListener(new TWLInputAdapter(gui, input));}
 * <p/>
 * Note: if you get an error with one of the @Override annotations then
 * DO NOT comment them out - upgrade to the latest Slick version. These
 * methods must be called by Slick for correct operation.
 *
 * @author Matthias Mann
 */
public class TWLInputAdapter extends InputAdapter {

    private final Input input;
    private final GUI gui;

    private int mouseDown;
    private boolean ignoreMouse;
    private boolean lastPressConsumed;

    public TWLInputAdapter(GUI gui, Input input) {
        if (gui == null) {
            throw new NullPointerException("gui");
        }
        if (input == null) {
            throw new NullPointerException("input");
        }

        this.gui = gui;
        this.input = input;
    }

    @Override
    public void mouseWheelMoved(int change) {
        if (!ignoreMouse) {
            if (gui.handleMouseWheel(change)) {
                consume();
            }
        }
    }

    @Override
    public void mousePressed(int button, int x, int y) {
        if (mouseDown == 0) {
            // only the first button down counts
            lastPressConsumed = false;
        }

        mouseDown |= 1 << button;

        if (!ignoreMouse) {
            if (gui.handleMouse(x, y, button, true)) {
                consume();
                lastPressConsumed = true;
            }
        }
    }

    @Override
    public void mouseReleased(int button, int x, int y) {
        mouseDown &= ~(1 << button);

        if (!ignoreMouse) {
            if (gui.handleMouse(x, y, button, false)) {
                consume();
            }
        } else if (mouseDown == 0) {
            ignoreMouse = false;
        }
    }

    @Override
    public void mouseMoved(int oldX, int oldY, int newX, int newY) {
        if (mouseDown != 0 && !lastPressConsumed) {
            ignoreMouse = true;
            gui.clearMouseState();
        } else if (!ignoreMouse) {
            if (gui.handleMouse(newX, newY, -1, false)) {
                consume();
            }
        }
    }

    @Override
    public void mouseDragged(int oldx, int oldy, int newX, int newY) {
        mouseMoved(oldx, oldy, newX, newY);
    }

    @Override
    public void keyPressed(int key, char c) {
        if (gui.handleKey(key, c, true)) {
            consume();
        }
    }

    @Override
    public void keyReleased(int key, char c) {
        if (gui.handleKey(key, c, false)) {
            consume();
        }
    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        if (!ignoreMouse && lastPressConsumed) {
            consume();
        }
    }

    private void consume() {
        input.consumeEvent();
    }

    @Override
    public void inputStarted() {
        gui.updateTime();
    }

    @Override
    public void inputEnded() {
        gui.handleKeyRepeat();
    }

    /**
     * Call this method from {@code BasicGame.update}
     *
     * @see BasicGame#update(org.newdawn.slick.GameContainer, int)
     */
    public void update() {
        gui.setSize();
        gui.adjustSize();
        gui.handleTooltips();
        gui.updateTimers();
        gui.invokeRunables();
        gui.validateLayout();
        gui.setCursor();
        // gui.update();
    }

    /**
     * Call this method from {@code BasicGame.render}
     *
     * @see BasicGame#render(org.newdawn.slick.GameContainer, org.newdawn.slick.Graphics)
     */
    public void render() {
        gui.draw();
    }
}