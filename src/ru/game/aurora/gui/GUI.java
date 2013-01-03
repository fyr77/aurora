/**
 * User: jedi-philosopher
 * Date: 03.01.13
 * Time: 17:03
 */
package ru.game.aurora.gui;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.TWLInputAdapter;

import java.io.IOException;

public class GUI extends Widget {
    private de.matthiasmann.twl.GUI gui;

    private TWLInputAdapter twlInputAdapter;

    private static GUI instance;

    public static void init(GameContainer gc) {
        instance = new GUI(gc);
    }

    private GUI(GameContainer gc) {
        setTheme("");
        // save Slick's GL state while loading the theme
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try {
            LWJGLRenderer lwjglRenderer = new LWJGLRenderer();
            ThemeManager theme = ThemeManager.createThemeManager(
                    GUI.class.getClassLoader().getResource("gui/aurora.xml"), lwjglRenderer);
            gui = new de.matthiasmann.twl.GUI(this, lwjglRenderer);
            gui.applyTheme(theme);
        } catch (LWJGLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // restore Slick's GL state
            GL11.glPopAttrib();
        }

        // connect input
        twlInputAdapter = new TWLInputAdapter(gui, gc.getInput());
        gc.getInput().addPrimaryListener(twlInputAdapter);

        Button button = new Button("BBB");
        button.setTheme("button");
        button.setMinSize(30, 10);
        button.setPosition(50, 90);
        button.adjustSize();
        add(button);
    }

    public static GUI getInstance() {
        return instance;
    }

    public void update(GameContainer gc) {
        twlInputAdapter.update();
    }

    public void draw(GameContainer gc, Graphics g) {
        twlInputAdapter.render();
    }

    public void setCurrentScreen(Widget widget) {
        gui.setRootPane(widget);
    }
}
