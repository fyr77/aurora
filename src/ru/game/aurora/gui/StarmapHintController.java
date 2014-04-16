package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyMouse;
import de.lessvoid.nifty.effects.EffectImpl;
import de.lessvoid.nifty.effects.EffectProperties;
import de.lessvoid.nifty.effects.Falloff;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.render.NiftyRenderEngine;
import de.lessvoid.nifty.tools.SizeValue;
import ru.game.aurora.application.Localization;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 15.04.14
 * Time: 22:20
 */

public class StarmapHintController implements EffectImpl {
    private Element panel;

    private Element nameText;

    private Element exploreText;

    private Element commentsText;

    private StarMapController starMapController;

    @Override
    public void activate(Nifty nifty, Element element, EffectProperties effectProperties) {
        starMapController = (StarMapController) GUI.getInstance().getNifty().findScreenController(StarMapController.class.getCanonicalName());
        panel = nifty.getCurrentScreen().findElementByName("starmap-hint-panel");

        nameText = panel.findElementByName("star_name");
        exploreText = panel.findElementByName("explored_progress");
        commentsText = panel.findElementByName("comment");
    }

    @Override
    public void execute(Element element, float v, Falloff falloff, NiftyRenderEngine niftyRenderEngine) {
        GalaxyMapObject objectAtMouseCoords = starMapController.getGalaxyMapObjectAtMouseCoords();
        if (objectAtMouseCoords == null) {
            panel.hide();
            return;
        }

        if (!StarSystem.class.isAssignableFrom(objectAtMouseCoords.getClass())) {
            panel.hide();
            return;
        }
        final NiftyMouse niftyMouse = GUI.getInstance().getNifty().getNiftyMouse();
        panel.setConstraintX(new SizeValue(niftyMouse.getX() + "px"));
        panel.setConstraintY(new SizeValue(niftyMouse.getY() + "px"));
        updatePanel((StarSystem) objectAtMouseCoords);
        panel.show();
        GUI.getInstance().getNifty().getCurrentScreen().layoutLayers();
    }

    private void updatePanel(StarSystem objectAtMouseCoords) {
        EngineUtils.setTextForGUIElement(nameText, objectAtMouseCoords.getName());
        EngineUtils.setTextForGUIElement(exploreText, objectAtMouseCoords.isVisited() ? Localization.getText("gui", "starmap.visited") : Localization.getText("gui", "starmap.not_visited"));
        final String questText = objectAtMouseCoords.getMessageForStarMap();
        if (questText != null && !questText.isEmpty()) {
            EngineUtils.setTextForGUIElement(commentsText, Localization.getText("gui", "starmap.marks") + "\n" + questText);
        } else {
            EngineUtils.setTextForGUIElement(commentsText, "");
        }
    }

    @Override
    public void deactivate() {
        panel.hide();
    }
}
