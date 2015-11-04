package ru.game.aurora.player.earth.upgrades;

import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.shipai.LeaveSystemAI;
import ru.game.aurora.player.Resources;
import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * Created by on 29.09.2015.
 * Adds Earth Trade Fleet - civilian ships that can be encountered in friendly star systems.
 * They can help with resources.
 */
public class ETFUpgrade extends EarthUpgrade {

    private static final String[] captains = {
            "etf_cap_1_dialog",
            "etf_cap_2_dialog",
            "etf_cap_3_dialog"
    };

    @Override
    public void unlock(World world) {
        super.unlock(world);
        world.getGlobalVariables().put("earth.trade_fleet", 1);
        world.addListener(new ETFEncounterListener());
    }

    private static final class ETFEncounterListener extends GameEventListener {

        private static final long serialVersionUID = 1L;

        // here we store the ETF ship we generated. We remove it after player leaves star system, so it does not stay there
        private NPCShip generatedShip = null;

        private int resources = 0;

        @Override
        public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {

            if (!ss.isQuestLocation()) {
                // quick check: solar system and alien homeworlds are always quest locations
                return false;
            }
            // check if it is either solar system, colony system or one of friendly alien homeworlds
            boolean isStarSystemSuitable = ss.equals(world.getGlobalVariables().get("solar_system"));
            isStarSystemSuitable |= (world.getGlobalVariables().containsKey("colony_news")
                    && world.getGlobalVariables().containsKey("colony_search.coords")
                    && ((Planet) world.getGlobalVariable("colony_search.coords", null)).getOwner().equals(ss));
            for (Faction f : world.getFactions().values()) {
                isStarSystemSuitable |= (f instanceof AlienRace && ((AlienRace) f).getHomeworld() != null && ((AlienRace) f).getHomeworld().equals(ss));
            }

            if (!isStarSystemSuitable) {
                return false;
            }

            if (CommonRandom.getRandom().nextDouble() > Configuration.getDoubleProperty("upgrades.etf.chance")) {
                return false;
            }

            Dialog etfDialog = Dialog.loadFromFile("dialogs/etf_default.json");
            etfDialog.addListener(new DialogListener() {
                @Override
                public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                    if (returnCode == 1) {
                        if (resources > 0) {
                            world.getPlayer().changeResource(world, Resources.RU, resources);
                            GameLogger.getInstance().logMessage(Localization.getText("gui", "etf.resources_gained", resources));
                            resources = 0;
                        } else {
                            GameLogger.getInstance().logMessage(Localization.getText("gui", "etf.no_resources"));
                        }
                    }
                }
            });

            etfDialog.setIconName(CollectionUtils.selectRandomElement(captains));

            generatedShip = new NPCShip(
                    0
                    , 0
                    , "earth_freighter"
                    , world.getFactions().get(HumanityGenerator.NAME)
                    , new NPC(etfDialog)
                    , "ETF Freighter"
                    , 10
            );
            generatedShip.setWeapons(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon"));
            ss.setRandomEmptyPosition(generatedShip);
            generatedShip.setAi(new LeaveSystemAI());
            ss.getObjects().add(generatedShip);
            return true;
        }

        @Override
        public boolean onPlayerLeftStarSystem(World world, StarSystem ss) {
            if (generatedShip != null) {
                ss.getObjects().remove(generatedShip);
                generatedShip = null;
            }
            return true;
        }
    }
}
