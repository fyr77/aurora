package ru.game.aurora.world.generation.quest;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.BasePlanetObject;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetObject;
import ru.game.aurora.world.planet.SurfaceTypes;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 22.05.14
 * Time: 13:50
 */
public class ColonizationListener extends GameEventListener implements DialogListener
{
    private static final long serialVersionUID = 410651695811568220L;

    private BasePositionable colonyCenter;

    private static enum State
    {
        INITED,
        COLONISTS_BOARDED,
        COLONISTS_DELIVERED
    }

    private static final boolean[][] colonyMap = new boolean[][]{
            {true, true, false, false, false},
            {true, true, false, false, false},
            {true, false, false, false, false},
            {true, false, true, true, false},
            {true, true, true, true, true}
    };

    private State state = State.INITED;

    private long time;

    public ColonizationListener(World world)
    {
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

    public void modifyPlanet(Planet planet)
    {
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

        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX(), colonyCenter.getY(), "colony_boxes", planet, PlanetObject.ScanGroup.OTHER));
        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX(), colonyCenter.getY() + 1, "colony_boxes", planet, PlanetObject.ScanGroup.OTHER));
        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 1, colonyCenter.getY() + 1, "colony_tractor", planet, PlanetObject.ScanGroup.OTHER));

        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 4, colonyCenter.getY() + 6, "colony_tent", planet, PlanetObject.ScanGroup.OTHER));
        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 6, colonyCenter.getY() + 6, "colony_smalltent", planet, PlanetObject.ScanGroup.OTHER));
        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 7, colonyCenter.getY() + 6, "colony_house1", planet));
        planet.getPlanetObjects().add(new BasePlanetObject(colonyCenter.getX() + 6, colonyCenter.getY() + 9, "colony_tractor", planet));

    }

    @Override
    public boolean onPlayerLandedPlanet(World world, Planet planet) {
        if (state == State.COLONISTS_BOARDED && planet == world.getGlobalVariables().get("colony_search.coords")) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/quest/colony_search/colony_first_party_arrival.json"));
            modifyPlanet(planet);
            state = State.COLONISTS_DELIVERED;
            world.getPlayer().getJournal().addQuestEntries("colony_search", "colonists_arrival");
        }

        return false;
    }
}
