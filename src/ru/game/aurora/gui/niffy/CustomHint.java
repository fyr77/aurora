package ru.game.aurora.gui.niffy;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.effects.EffectImpl;
import de.lessvoid.nifty.effects.EffectProperties;
import de.lessvoid.nifty.effects.Falloff;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyRenderEngine;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.TargetElementResolver;
import ru.game.aurora.gui.GUI;

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

            targetElement.setConstraintX(new SizeValue(Math.min(GUI.getInstance().getNifty().getNiftyMouse().getX(), r.getWidth() - targetElement.getWidth() - 10)+ "px"));
            targetElement.setConstraintY(new SizeValue(Math.min(GUI.getInstance().getNifty().getNiftyMouse().getY(), r.getHeight() - targetElement.getHeight() - 10) + "px"));
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