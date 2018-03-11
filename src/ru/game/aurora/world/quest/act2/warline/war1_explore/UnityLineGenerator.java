package ru.game.aurora.world.quest.act2.warline.war1_explore;

import org.slf4j.LoggerFactory;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.gui.FadeOutScreenController;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.player.Resources;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.NPCShip;

import java.util.Map;

/**
 * Created by di Grigio on 04.04.2017.
 * Part of Act2, War Line, quest 1 - Explore
 */
class UnityLineGenerator implements WorldGeneratorPart {

    private static final long serialVersionUID = 4645325290972943602L;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UnityLineGenerator.class);

    public UnityLineGenerator(){

    }

    @Override
    public void updateWorld(World world) {
        if(!world.getGlobalVariables().containsKey("unity_station_ship")){
            logger.error("Unity station not found");
            return;
        }

        NPCShip spaceStation = (NPCShip)world.getGlobalVariables().get("unity_station_ship");
        if(spaceStation == null){
            logger.error("Unity station object is null");
            return;
        }

        final Dialog unityDocking = Dialog.loadFromFile("dialogs/act2/warline/war1_explore/unity/war1_explore_unity_docking.json");
        final Dialog unityInside =  Dialog.loadFromFile("dialogs/act2/warline/war1_explore/unity/war1_explore_unity_inside.json");

        unityDocking.addListener(new DialogListener() {
            private static final long serialVersionUID = 247448446725085142L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                logger.info("Docking to Unity. Player have " + world.getPlayer().getCredits() + " Cr");

                // check avaible credits
                final int credits = world.getPlayer().getCredits();
                if(credits >= Configuration.getIntProperty("war1_explore.bork_information_price")){
                    flags.put("can_pay_bork", "");
                }

                if(credits >= Configuration.getIntProperty("war1_explore.klisk_information_price")){
                    flags.put("can_pay_klisk", "");
                }

                world.addOverlayWindow(unityInside, flags);
            }
        });

        unityInside.addListener(new DialogListener() {
            private static final long serialVersionUID = 8801547435026296L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if(returnCode == 1){
                    // buy info from borks
                    int price = Configuration.getIntProperty("war1_explore.bork_information_price");
                    logger.info("Player buy intelligence from bork, payed " + price + " Cr");
                    buyInfo(world, price);
                }
                else if(returnCode == 2){
                    // but info from klisk
                    int price = Configuration.getIntProperty("war1_explore.klisk_information_price");
                    logger.info("Player buy intelligence from klisk, payed " + price + " Cr");
                    buyInfo(world, price);
                }

                // set default dialogue
                disposeQuestLine(world);
            }
        });

        spaceStation.setCaptain(new NPC(unityDocking));
        logger.info("Unity line successful generated");
    }

    public static void disposeQuestLine(World world) {
        NPCShip spaceStation = (NPCShip)world.getGlobalVariables().get("unity_station_ship");
        spaceStation.setCaptain(new NPC(Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_docking_visited.json")));
        logger.info("Unity station set default dialogue");
    }

    private void buyInfo(final World world, final int price) {
        FadeOutScreenController.makeFade(new IStateChangeListener() {
            private static final long serialVersionUID = 404705850513424891L;
            @Override
            public void stateChanged(Object param) {
                world.getPlayer().changeResource(world, Resources.CREDITS, -price);
                world.getGlobalVariables().put("war1_explore.intelligence_get", true);
                world.getPlayer().getJournal().addQuestEntries("war1_explore", "success_uinty");
                world.addDays(3);
                world.getGlobalVariables().put("warlineResult", 3);

                RebelsLineGenerator.disposeQuestLine(world);
            }
        });
    }
}
