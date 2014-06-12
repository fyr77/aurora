package ru.game.aurora.world.generation.quest;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.*;
import ru.game.aurora.world.planet.nature.Animal;
import ru.game.aurora.world.planet.nature.AnimalModifier;
import ru.game.aurora.world.planet.nature.AnimalSpeciesDesc;
import ru.game.aurora.world.space.GalaxyMapObject;

import java.util.Collections;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 22.05.14
 * Time: 13:50
 */
public class ColonizationListener extends GameEventListener implements DialogListener {
    private static final long serialVersionUID = 410651695811568220L;

    private BasePositionable colonyCenter;

    private PlanetNPC chief;

    private static enum State {
        INITED,
        COLONISTS_BOARDED,
        COLONISTS_DELIVERED
    }

    private static final boolean[][] colonyMap = new boolean[][]{
            {false, false, false, false, false, false},
            {false, true, true, false, false, false},
            {false, true, true, false, false, false},
            {false, true, false, false, false, false},
            {false, true, false, true, true, false},
            {false, true, true, true, true, false},
            {false, false, false, false, false, false}
    };

    private State state = State.INITED;

    private long time;

    public ColonizationListener(World world) {
        time = world.getTurnCount();
    }

    @Override
    public boolean onReturnToEarth(World world) {
        if (state == State.INITED && world.getTurnCount() - time >= 30) {
            Dialog d = Dialog.loadFromFile("dialogs/quest/colony_search/colony_first_party_departure.json");
            d.addListener(this);
            world.getPlayer().getEarthState().getEarthSpecialDialogs().add(d);
        }

        return false;
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (dialog.getId().equals("colony_first_party_departure")) {
            state = State.COLONISTS_BOARDED;
            world.getPlayer().getJournal().addQuestEntries("colony_search", "colonists_departure");
        }
    }

    public void modifyPlanet(World world, Planet planet) {
        final int initialSize = Configuration.getIntProperty("quest.colony_search.colony_initial_size");
        colonyCenter = planet.findPassableRegion(initialSize, initialSize);

        for (int x = 0; x < initialSize / 2; ++x) {
            for (int y = 0; y < initialSize / 2; ++y) {
                int realX = colonyCenter.getX() + x * 2;
                int realY = colonyCenter.getY() + y * 2;
                if (colonyMap[y][x]) {
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 2; j++) {
                            boolean visible = planet.getSurface().isTileVisible(realX + i, realY + j);
                            planet.getSurface().getSurface()[realY + i][realX + j] = visible ? SurfaceTypes.VISIBILITY_MASK | SurfaceTypes.ASPHALT : SurfaceTypes.ASPHALT;
                        }
                    }
                }
            }
        }

        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 2, colonyCenter.getY() + 2, "colony_boxes", planet, PlanetObject.ScanGroup.OTHER));
        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 2, colonyCenter.getY() + 3, "colony_boxes", planet, PlanetObject.ScanGroup.OTHER));
        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 3, colonyCenter.getY() + 4, "colony_tractor", planet, PlanetObject.ScanGroup.OTHER));

        // workers
        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 3, colonyCenter.getY() + 5, "humanity_tileset", 0, 6, planet));
        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 4, colonyCenter.getY() + 2, "humanity_tileset", 0, 6, planet));

        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 6, colonyCenter.getY() + 8, "colony_tent", planet, PlanetObject.ScanGroup.OTHER));
        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 7, colonyCenter.getY() + 8, "colony_smalltent", planet, PlanetObject.ScanGroup.OTHER));
        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 8, colonyCenter.getY() + 8, "colony_house1", planet));
        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 5, colonyCenter.getY() + 10, "humanity_tileset", 2, 6, planet));
        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 7, colonyCenter.getY() + 10, "colony_tractor", planet));


        AnimalSpeciesDesc guard = new AnimalSpeciesDesc(planet, "Marine", false, false, 10, ResourceManager.getInstance().getLandingPartyWeapons().getEntity("assault"), 1, AnimalSpeciesDesc.Behaviour.FRIENDLY, Collections.<AnimalModifier>emptySet());
        guard.setCanBePickedUp(false);
        guard.setImages(ResourceManager.getInstance().getSpriteSheet("humanity_tileset").getSprite(1, 9), ResourceManager.getInstance().getImage("no_image"));
        planet.getPlanetObjects().add(new Animal(planet, colonyCenter.getX() + 9, colonyCenter.getY() + 7, guard));
        if (world.getGlobalVariables().containsKey("colony_search.explored_fully")) {
            planet.getPlanetObjects().add(new Animal(planet, colonyCenter.getX() + 2, colonyCenter.getY() + 6, guard));
            planet.getPlanetObjects().add(new Animal(planet, colonyCenter.getX() + 7, colonyCenter.getY() + 3, guard));
        }

        chief = new PlanetNPC(colonyCenter.getX() + 8, colonyCenter.getY() + 9, "colony_colonist", planet);
        chief.setDialog(Dialog.loadFromFile("dialogs/quest/colony_search/colony_default.json"));
        planet.getPlanetObjects().add(chief);

    }

    @Override
    public boolean onPlayerLandedPlanet(World world, Planet planet) {
        if (state == State.COLONISTS_BOARDED && planet == world.getGlobalVariables().get("colony_search.coords")) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/quest/colony_search/colony_first_party_arrival.json"));
            modifyPlanet(world, planet);
            state = State.COLONISTS_DELIVERED;
            world.getPlayer().getJournal().addQuestEntries("colony_search", "colonists_arrival");
        }

        return false;
    }

    @Override
    public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject) {
        if (world.getGlobalVariables().containsKey("colony_search.coords")) {
            Planet p = (Planet) world.getGlobalVariables().get("colony_search.coords");
            if (p.getOwner() == galaxyMapObject) {
                return Localization.getText("journal", "colony_search.map_label");
            }
        }

        return null;
    }
}
