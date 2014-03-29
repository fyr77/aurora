package ru.game.aurora.gui.niffy;

import de.lessvoid.nifty.*;
import de.lessvoid.nifty.effects.*;
import de.lessvoid.nifty.elements.*;
import de.lessvoid.nifty.elements.render.*;
import de.lessvoid.nifty.render.*;
import de.lessvoid.nifty.tools.*;

/**
 * Date: 29.03.2014
 * Time: 18:57
 */
public class CustomHint implements EffectImpl {
    private Nifty nifty;
    private Element targetElement;
    private String hintText;

    public void activate(final Nifty niftyParam, final Element element, final EffectProperties parameter) {
        this.nifty = niftyParam;

        TargetElementResolver resolver = new TargetElementResolver(nifty.getCurrentScreen(), element);
        targetElement = resolver.resolve(parameter.getProperty("targetElement"));

        String text = parameter.getProperty("hintText");
        if (text != null) {
            hintText = text;
        }
    }

    public void execute(final Element element, final float normalizedTime, final Falloff falloff, final NiftyRenderEngine r) {
        if (targetElement != null) {
            if (hintText != null) {
                targetElement.findElementByName("content").getRenderer(TextRenderer.class).setText(hintText);
            }
            targetElement.setConstraintX(new SizeValue(element.getX() + element.getWidth() - 50 + "px"));
            targetElement.setConstraintY(new SizeValue(element.getY() + element.getHeight() - 50 + "px"));
            targetElement.show();
            nifty.getCurrentScreen().layoutLayers();
        }
    }

    public void deactivate() {
        if (targetElement != null) {
            targetElement.hide();
        }
    }
}