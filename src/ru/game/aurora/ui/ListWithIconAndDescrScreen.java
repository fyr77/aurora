/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 25.12.12
 * Time: 12:57
 */
package ru.game.aurora.ui;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GUIConstants;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Standart in-game screen for lists of items with icon and description
 */
public abstract class ListWithIconAndDescrScreen implements Room {

    protected List<String> strings = new ArrayList<String>(24);

    protected static final Rectangle captionRect = new Rectangle(1, 1, 12, 1);

    protected static final Rectangle listRect = new Rectangle(1, 3, 7, 10);

    protected static final Rectangle imageRect = new Rectangle(9, 3, 4, 4);

    protected static final Rectangle descriptionRect = new Rectangle(9, 8, 4, 5);

    protected static final Color backgroundColor = new Color(4, 7, 125);

    protected int currentIdx;

    protected Room previousRoom;

    protected World world;

    protected int maxIdx;

    @Override
    public void enter(World world) {
        this.world = world;
        this.previousRoom = world.getCurrentRoom();
    }

    public void draw(Graphics graphics, Camera camera, String caption, String image, String description) {
        EngineUtils.drawRectWithBorder(graphics, captionRect, camera, Color.yellow, backgroundColor);

        graphics.setFont(GUIConstants.captionFont);
        graphics.setColor(Color.yellow);
        graphics.drawString(caption, camera.getRelativeX((int) captionRect.getX()) + 200, camera.getRelativeY((int) captionRect.getY()) + 20);

        drawTabContent(graphics, camera, image, description);
    }

    public void drawTabbed(Graphics graphics, Camera camera, String[] captions, int activeTab, String image, String description)
    {
        final int tabCaptionWidth = (int) (captionRect.getWidth() / captions.length);

        Rectangle rectangle = new Rectangle(captionRect.getX(), captionRect.getY(), tabCaptionWidth - 1, captionRect.getHeight());
        for (int i = 0; i < captions.length; ++i) {
            EngineUtils.drawRectWithBorderAndText(
                    graphics
                    , rectangle
                    , camera
                    , GUIConstants.borderColor
                    , GUIConstants.backgroundColor
                    , captions[i]
                    , i == activeTab ? GUIConstants.captionFont : GUIConstants.dialogFont
                    , i == activeTab ? Color.yellow : Color.gray
                    , true
            );

            rectangle.setX(rectangle.getX() + tabCaptionWidth + 1);
        }

        drawTabContent(graphics, camera, image, description);
    }

    private void drawTabContent(Graphics graphics, Camera camera, String image, String description)
    {
        graphics.setFont(GUIConstants.dialogFont);
        EngineUtils.drawRectWithBorder(graphics, listRect, camera, Color.yellow, backgroundColor);

        EngineUtils.drawRectWithBorder(graphics, descriptionRect, camera, Color.yellow, backgroundColor);

        if (image != null) {
            graphics.drawImage(ResourceManager.getInstance().getImage(image), camera.getRelativeX((int) imageRect.getX()), camera.getRelativeY((int) imageRect.getY()));
            graphics.setColor(GUIConstants.borderColor);
            EngineUtils.drawRect(graphics, imageRect, camera, false);
        } else {
            EngineUtils.drawRectWithBorder(graphics, imageRect, camera, Color.yellow, backgroundColor);
        }


        if (description == null) {
            description = "<Select a research project>";
        }
        EngineUtils.drawString(graphics, description, camera.getRelativeX((int) descriptionRect.getX()) + 10, camera.getRelativeY((int) descriptionRect.getY()) + 10, camera.getTileWidth() * (int) descriptionRect.getWidth() - 20, GUIConstants.dialogFont, Color.yellow);

        if (strings.isEmpty()) {
            graphics.drawString("<No projects in this category>", camera.getRelativeX((int) listRect.getX()) + 10, camera.getRelativeY((int) listRect.getY()) + 10);
        } else {
            for (int i = 0; i < strings.size(); ++i) {
                graphics.setColor(i == currentIdx ? Color.green : Color.yellow);
                graphics.drawString(strings.get(i), camera.getRelativeX((int) listRect.getX()) + 20, camera.getRelativeY((int) (listRect.getY() + i)) + 20);
            }
        }
    }

    @Override
    public void update(GameContainer container, World world) {
        if (container.getInput().isKeyPressed(Input.KEY_UP)) {
            currentIdx--;
            if (currentIdx < 0) {
                currentIdx = maxIdx - 1;
            }
        }

        if (container.getInput().isKeyPressed(Input.KEY_DOWN)) {
            currentIdx++;
            if (currentIdx == maxIdx) {
                currentIdx = 0;
            }
        }

        if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
            world.setCurrentRoom(previousRoom);
        }
    }
}
