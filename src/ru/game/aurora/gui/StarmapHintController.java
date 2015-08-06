package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyMouse;
import de.lessvoid.nifty.effects.EffectImpl;
import de.lessvoid.nifty.effects.EffectProperties;
import de.lessvoid.nifty.effects.Falloff;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.render.NiftyRenderEngine;
import de.lessvoid.nifty.tools.SizeValue;
import org.newdawn.slick.Image;
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

    private Element starText;

    private Element starImage;

    private StarMapController starMapController;

    private GalaxyMapController galaxyMapController;

    @Override
    public void activate(Nifty nifty, Element element, EffectProperties effectProperties) {
        starMapController = (StarMapController) GUI.getInstance().getNifty().findScreenController(StarMapController.class.getCanonicalName());
        galaxyMapController = (GalaxyMapController) GUI.getInstance().getNifty().findScreenController(GalaxyMapController.class.getCanonicalName());
        panel = nifty.getCurrentScreen().findElementByName("starmap-hint-panel");

        starText = panel.findElementByName("star_text");
        starImage = panel.findElementByName("star_image");
    }

    @Override
    public void execute(Element element, float v, Falloff falloff, NiftyRenderEngine niftyRenderEngine) {
        GalaxyMapObject objectAtMouseCoords = GUI.getInstance().getNifty().getCurrentScreen().getScreenId().equals("star_map_screen")
                ? starMapController.getGalaxyMapObjectAtMouseCoords()
                : galaxyMapController.getGalaxyMapObjectAtMouseCoords()
                ;
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
        final Image image = objectAtMouseCoords.getStar().getImage();
        if (image != null) {
            EngineUtils.setImageForGUIElement(starImage, image);
            starImage.getLayoutPart().getBoxConstraints().setWidth(SizeValue.px(image.getWidth() / 2));
            starImage.getLayoutPart().getBoxConstraints().setHeight(SizeValue.px(image.getHeight() / 2));
            panel.layoutElements();
        }
        StringBuilder textBuilder = new StringBuilder(objectAtMouseCoords.getName());
        textBuilder.append('\n');
        if (objectAtMouseCoords.isVisited()) {
            textBuilder.append(Localization.getText("gui", "starmap.visited")).append('\n');
            textBuilder.append(String.format(Localization.getText("gui", "starmap.planet_count"), objectAtMouseCoords.getPlanets().length)).append('\n');
            textBuilder.append(String.format(Localization.getText("gui", "starmap.astro_data"), objectAtMouseCoords.getAstronomyData())).append('\n');
        } else {
            textBuilder.append(Localization.getText("gui", "starmap.not_visited"));
        }
        textBuilder.append('\n');
        final String questText = objectAtMouseCoords.getMessageForStarMap();
        if (questText != null && !questText.isEmpty()) {
            textBuilder.append(Localization.getText("gui", "starmap.marks")).append("\n").append(questText);
        }
        EngineUtils.setTextForGUIElement(starText, textBuilder.toString());
    }

    @Override
    public void deactivate() {
        panel.hide();
    }
}
