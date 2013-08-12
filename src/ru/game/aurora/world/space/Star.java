package ru.game.aurora.world.space;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.util.EngineUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
* Created with IntelliJ IDEA.
* User: Egor.Smirnov
* Date: 12.08.13
* Time: 13:19
* To change this template use File | Settings | File Templates.
*/
public class Star implements Serializable
{
    private static final long serialVersionUID = 1L;
    // 1 is largest star, 4 is smallest
    public final int size;

    public final Color color;

    // colors for drawing in star system view
    // star consists of 3 gradients of main color
    private transient Color coreColor;

    private transient Color outerColor;

    private transient Image myImage;

    public Star(int size, Color color) {
        this.size = size;
        this.color = color;
    }

    public Color getCoreColor() {
        if (coreColor == null) {
            coreColor = EngineUtils.lightenColor(color);
        }
        return coreColor;
    }

    public Color getOuterColor() {
        if (outerColor == null) {
            outerColor = EngineUtils.darkenColor(color, 0.75f);
        }
        return outerColor;
    }

    public Image getImage()
    {
        if (myImage == null) {
            int imageSize = size;
            if (imageSize > 4) {
                imageSize = 4;
            }
            Image template = ResourceManager.getInstance().getImage("star_" + imageSize);
            Map<Color, Color> colorMap = new HashMap<>();
            colorMap.put(new Color(255, 243, 52, 255), color);
            colorMap.put(new Color(255, 172, 80, 255), getOuterColor());
            myImage = EngineUtils.replaceColors(template, colorMap);
        }
        return myImage;
    }
}
