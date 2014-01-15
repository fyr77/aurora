package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.Updatable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGenerator;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 15.01.14
 * Time: 22:15
 */
public class LoadingScreenController implements ScreenController, Updatable {
    private Animation shuttle_landing;

    private WorldGenerator generator;

    // used for changing number of dots in message while generating world
    private int dotsCount = 0;

    private long lastTimeChecked = 0;

    private Element text;

    private Element shuttle_image;

    public LoadingScreenController() {
    }

    public void setGenerator(WorldGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        shuttle_landing = ResourceManager.getInstance().getAnimation("shuttle_landing");
        shuttle_landing.setAutoUpdate(false);
        shuttle_landing.setLooping(true);
        shuttle_landing.start();

        text = screen.findElementByName("generation_text");
        shuttle_image = screen.findElementByName("shuttle_image");
    }

    @Override
    public void onStartScreen() {
        dotsCount = 0;
        AuroraGame.getUpdatables().add(this);
    }

    @Override
    public void onEndScreen() {
        AuroraGame.getUpdatables().remove(this);
    }

    @Override
    public void update(GameContainer container, World world) {
        if (container.getTime() - lastTimeChecked > 500) {
            if (dotsCount++ > 5) {
                dotsCount = 0;
            }
            lastTimeChecked = container.getTime();
        }

        StringBuilder sb = new StringBuilder(Localization.getText("gui", "generation.prefix")).append(" ");
        sb.append(generator.getCurrentStatus());
        for (int i = 0; i < dotsCount; ++i) {
            sb.append(".");
        }
        EngineUtils.setTextForGUIElement(text, sb.toString());
        final long delta = container.getTime() - AuroraGame.getLastFrameTime();
        shuttle_landing.update(delta);
        EngineUtils.setImageForGUIElement(shuttle_image, shuttle_landing.getCurrentFrame());
    }
}
