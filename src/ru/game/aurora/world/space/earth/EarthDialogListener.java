package ru.game.aurora.world.space.earth;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.Reply;
import ru.game.aurora.dialog.Statement;
import ru.game.aurora.gui.EarthProgressScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.generation.quest.ColonizationListener;
import ru.game.aurora.world.quest.ZorsanFinalBattleGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 10.06.13
 * Time: 16:22
 */

public class EarthDialogListener implements DialogListener {
    private static final long serialVersionUID = 6653410057967364076L;

    private Earth earth;

    public EarthDialogListener(Earth earth) {
        this.earth = earth;
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {

        if (returnCode == 1) {
            // player has chosen to dump research info

            int daysPassed = world.getTurnCount() - earth.getLastVisitTurn();
            Statement stmt;


            if (daysPassed > Configuration.getIntProperty("game.minimumTripDays")) {

                ((EarthProgressScreenController) GUI.getInstance().getNifty().getScreen("earth_progress_screen").getScreenController()).updateStats();
                int totalScore = earth.dumpResearch(world);
                double scorePerTurn = (double) totalScore / (daysPassed);
                stmt = new Statement(0, String.format(Localization.getText("dialogs", "earth.progress_string"), totalScore, scorePerTurn), new Reply(0, 0, ""));

                if (scorePerTurn < Configuration.getDoubleProperty("game.progress.targetScorePerDay")) {
                    world.getPlayer().increaseFailCount();
                    if (world.getPlayer().getFailCount() > Configuration.getIntProperty("game.allowedFails")) {
                        // unsatisfactory
                        stmt.replies[0] = new Reply(0, 3, "continue");
                    } else {
                        // poor
                        stmt.replies[0] = new Reply(0, 2, "continue");
                    }
                } else {
                    // ok
                    stmt.replies[0] = new Reply(0, 1, "continue");
                }
                earth.setLastVisitTurn(world.getTurnCount());
            } else {
                stmt = new Statement(0, Localization.getText("dialogs", "earth.trip_too_short"), new Reply(0, -1, "ok"));
            }
            earth.getProgressDialog().putStatement(stmt);

            world.addOverlayWindow(earth.getProgressDialog());

            if (daysPassed > Configuration.getIntProperty("game.minimumTripDays")) {
                GUI.getInstance().pushCurrentScreen();
                GUI.getInstance().getNifty().gotoScreen("earth_progress_screen");
            }

        } else if (returnCode == 0) {
            // return
            world.setCurrentRoom(earth.getOwner());
            GUI.getInstance().getNifty().gotoScreen("star_system_gui");
        } else if (returnCode == 80) {
            // give reward for diplomacy quest
            int score = 0;

            //TODO: remove this condition in next major release
            if (world.getGlobalVariables().containsKey("klisk_trade.quest_result")) {
                switch ((String) world.getGlobalVariables().get("klisk_trade.quest_result")) {
                    case "perfect":
                        score += 2;
                        break;
                    case "good":
                        score += 1;
                        break;
                }
            }

            switch ((String) world.getGlobalVariables().get("bork.diplomacy_test")) {
                case "injure":
                    score += 2;
                    break;
                case "miss":
                    score += 1;
                    break;
            }

            if (world.getReputation().isHostile(RoguesGenerator.NAME, HumanityGenerator.NAME)) {
                score -= 2;
            }
            if (world.getReputation().isHostile(BorkGenerator.NAME, HumanityGenerator.NAME)) {
                score -= 2;
            }
            if (world.getReputation().isHostile(KliskGenerator.NAME, HumanityGenerator.NAME)) {
                score -= 2;
            }

            Map<String, String> flagsForNextDialog = new HashMap<>();
            if (score >= 3) {
                flagsForNextDialog.put("flag_good", null);
            } else if (score >= 0) {
                flagsForNextDialog.put("flag_ok", null);
            } else {
                flagsForNextDialog.put("flag_bad", null);
            }

            final Dialog d = Dialog.loadFromFile("dialogs/diplomacy_quest_results.json");
            d.addListener(new DialogListener() {

                private static final long serialVersionUID = -2200710202646449526L;

                @Override
                public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                    world.setCurrentRoom(earth.getOwner());
                    GUI.getInstance().getNifty().gotoScreen("star_system_gui");
                }
            });
            world.addOverlayWindow(d, flagsForNextDialog);
        }

        // quest stuff
        if (flags.containsKey("diplomacy_report")) {
            // player has reported results of diplomacy quest
            world.getGlobalVariables().put("diplomacy.all_done", 1);
            flags.remove("diplomacy_report");
        }

        if (flags.containsKey("zorsan_war_info_quest")) {
            world.getGlobalVariables().put("zorsan.escape", 1);
            world.getPlayer().getJournal().addQuestEntries("zorsan_relations", "earth_report");
            flags.remove("zorsan_war_info_quest");
        }

        if (flags.containsKey("colony_info_dumped")) {
            world.getGlobalVariables().remove("colony_search.explored");
            world.addListener(new ColonizationListener(world));
            flags.remove("colony_info_dumped");
        }

        if (flags.containsKey("zorsan_war_info_update")) {
            flags.remove("zorsan_war_info_update");
            world.getGlobalVariables().put("zorsan.war_preparations", 1);
            world.getGlobalVariables().put("zorsan.escape", 1);
            world.getPlayer().getEarthState().getMessages().add(new PrivateMessage("zorsan_attack_1", "news"));
            world.addListener(new GameEventListener() {
                private static final long serialVersionUID = -3584085663658592781L;
                int days;

                @Override
                public boolean onTurnEnded(World world) {
                    if (++days > 500) {
                        Dialog d = Dialog.loadFromFile("dialogs/zorsan/final_battle/zorsan_battle_before_start.json");
                        d.addListener(new ZorsanFinalBattleGenerator());
                        world.getPlayer().getEarthState().getEarthSpecialDialogs().add(d);
                        isAlive = false;
                    }
                    return false;
                }
            });
        }

        earth.getDialogFlags().putAll(flags);
        //reset dialog state
        earth.getEarthDialog().enter(world);
        world.getPlayer().getShip().fullRepair(world);

    }
}
