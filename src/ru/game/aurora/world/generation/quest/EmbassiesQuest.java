package ru.game.aurora.world.generation.quest;

import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 14.02.14
 * Time: 18:04
 */
public class EmbassiesQuest implements WorldGeneratorPart {

    private static final long serialVersionUID = 5270614859809196286L;

    public static void updateJournal(World world, String key) {
        world.getPlayer().getJournal().addQuestEntries("embassies", key);
        if (world.getGlobalVariables().containsKey("diplomacy.klisk_visited")
                && world.getGlobalVariables().containsKey("diplomacy.bork_visited")
                && world.getGlobalVariables().containsKey("diplomacy.zorsan_visited")
                && world.getGlobalVariables().containsKey("diplomacy.rogues_visited")) {
            world.getPlayer().getJournal().addQuestEntries("embassies", "end");
            world.getPlayer().getJournal().questCompleted("embassies");
        }
    }

    /**
     * This is listener for a first contact with any alien ship
     * After this, when player returns to earth, a press-conference will be held, and a few story dialogs shown
     */
    private static final class FirstContactListener extends GameEventListener {
        private static final long serialVersionUID = 1L;

        private int counter = 0;

        private boolean kliskContacted = false;

        @Override
        public boolean onPlayerContactedOtherShip(World world, GameObject ship) {
            if (ship.getFaction() != world.getFactions().get(HumanityGenerator.NAME)) {
                if (ship.getFaction() == world.getFactions().get(KliskGenerator.NAME)) {
                    kliskContacted = true;
                }

                if (++counter < 3 || !kliskContacted) {
                    return false;
                }

                Dialog d = Dialog.loadFromFile("dialogs/earth_first_return_1.json");
                d.addListener(new DialogListener() {
                    private static final long serialVersionUID = 4929841605007880780L;

                    @Override
                    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                        Dialog pressConference = Dialog.loadFromFile("dialogs/earth_first_return_2.json");
                        pressConference.addListener(new DialogListener() {
                            private static final long serialVersionUID = 7949033555418969959L;

                            @Override
                            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                                // based on player replies in this dialog, different private messages are sent
                                world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(world, "news_sender", "press_conference_" + flags.get("message"), "news"));
                                world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(world, "news_sender", "press_conference_common", "news"));
                            }
                        });
                        world.addOverlayWindow(pressConference);

                        world.getPlayer().getJournal().addQuestEntries("embassies", "start");
                    }
                });


                world.getPlayer().getEarthState().getEarthSpecialDialogs().add(d);
                isAlive = false;
                return true;
            }
            return false;
        }
    }

    @Override
    public void updateWorld(World world) {
        world.addListener(new FirstContactListener());
    }
}
