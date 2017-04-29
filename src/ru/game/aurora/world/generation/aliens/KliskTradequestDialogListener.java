package ru.game.aurora.world.generation.aliens;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.shipai.LandAI;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * Common listener for Klisk initial trade quest
 */
public class KliskTradequestDialogListener extends GameEventListener implements DialogListener {
    private static final long serialVersionUID = -1387556231542083021L;

    private final StarSystem targetSystem;

    private NPCShip npcStation;

    public KliskTradequestDialogListener(StarSystem targetSystem) {
        this.targetSystem = targetSystem;
    }

    @Override
    public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject) {
        if (galaxyMapObject == targetSystem) {
            return Localization.getText("journal", "klisk_trade.title");
        }
        return null;
    }

    private void processArrival(World world, int returnCode) {
        if (returnCode == 0) {
            // player refused to perform trading
            world.getGlobalVariables().put("klisk_trade.result", "refused");
            world.getPlayer().getJournal().addQuestEntries("klisk_trade", "refused");
            world.getGlobalVariables().remove("klisk_trade.started");
            npcStation.setCaptain(new NPC(KliskGenerator.loadTradeStationDefaultDialog(world.getFactions().get(KliskGenerator.NAME))));
            return;
        }

        // now put player into target star system

        Dialog tradeDialog = Dialog.loadFromFile("dialogs/klisk/klisk_trade_quest_trade.json");
        tradeDialog.addListener(this);
        world.addOverlayWindow(tradeDialog);
    }


    // this is a quest ship that will be used in target system
    // when it docks with the station, it will reset its dialog to a quest one
    private class KliskQuestShip extends NPCShip {

        private static final long serialVersionUID = -5895915582359778144L;

        private final NPCShip target;

        public KliskQuestShip(World world, NPCShip target, int x, int y) {
            super("klisk_ship", x, y);
            setCaptain(new NPC(Dialog.loadFromFile("dialogs/klisk/klisk_trade_quest_ship_default.json")));
            setAi(new LandAI(target));
            this.target = target;
        }

        @Override
        public void update(GameContainer container, World world) {
            super.update(container, world);
            if (!ai.isAlive()) {
                // docked
                Dialog arrivalDialog = Dialog.loadFromFile("dialogs/klisk/klisk_trade_quest_arrival.json");
                arrivalDialog.addListener(KliskTradequestDialogListener.this);
                target.setCaptain(new NPC(arrivalDialog));
            }
        }
    }

    private void processPartyInvitation(World world, int returnCode) {

        npcStation = null;

        for (GameObject so : targetSystem.getShips()) {
            if (so instanceof NPCShip) {
                npcStation = (NPCShip) so;
                break;
            }
        }

        if (npcStation == null) {
            throw new IllegalStateException("NPC station not found");
        }

        KliskQuestShip ship = new KliskQuestShip(world, npcStation, world.getPlayer().getShip().getX() + 1, world.getPlayer().getShip().getY());
        targetSystem.getShips().add(ship);

        if (returnCode == 1) {
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/klisk/klisk_trade_quest_party.json"));
            world.getPlayer().getJournal().addQuestEntries("klisk_trade", "party");
        }
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        switch (dialog.getId()) {
            case "klisk_trade_quest_captain":
                processPartyInvitation(world, returnCode);
                break;
            case "klisk_trade_quest_arrival":
                processArrival(world, returnCode);
                break;
            case "klisk_trade_quest_trade":
                processTrade(world, returnCode, flags);
                break;

        }
    }

    private void processTrade(World world, int returnCode, Map<String, String> flags) {
        world.getGlobalVariables().remove("klisk_trade.started");
        if (returnCode == 0) {
            // player refused to perform trading
            world.getGlobalVariables().put("klisk_trade.result", "refused");
            world.getPlayer().getJournal().addQuestEntries("klisk_trade", "refused");
            npcStation.setCaptain(new NPC(KliskGenerator.loadTradeStationDefaultDialog(world.getFactions().get(KliskGenerator.NAME))));
            return;
        }
        world.getPlayer().getJournal().addQuestEntries("klisk_trade", "traded");
        String result;
        if (flags.containsKey("first_good") && flags.containsKey("second_good") && flags.containsKey("third_good")) {
            result = "perfect";
        } else if (
                (flags.containsKey("first_good") || flags.containsKey("fist_money"))
                        && (flags.containsKey("second_good") || flags.containsKey("second_money"))
                        && (flags.containsKey("third_good") || flags.containsKey("third_money"))
                ) {
            result = "good";
        } else {
            result = "bad";
        }

        world.getGlobalVariables().put("klisk_trade.result", result);

        // reset to default dialog
        npcStation.setCaptain(new NPC(KliskGenerator.loadTradeStationDefaultDialog(world.getFactions().get(KliskGenerator.NAME))));

    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (ss != targetSystem) {
            return false;
        }

        Dialog d = Dialog.loadFromFile("dialogs/klisk/klisk_trade_quest_captain.json");
        d.addListener(this);
        world.addOverlayWindow(d);
        isAlive = false;
        return true;
    }
}
