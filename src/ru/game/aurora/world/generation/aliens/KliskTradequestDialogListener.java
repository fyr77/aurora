package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.NextDialogListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.SpaceObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 * Common listener for Klisk initial trade quest
 */
public class KliskTradequestDialogListener implements DialogListener {
    private static final long serialVersionUID = -1387556231542083021L;

    private StarSystem targetSystem;

    public KliskTradequestDialogListener(StarSystem targetSystem) {
        this.targetSystem = targetSystem;
    }

    private void processArrival(World world, int returnCode) {
        if (returnCode == 0) {
            // player refused to perform trading
            world.getGlobalVariables().put("klisk_trade.result", "refused");
            return;
        }

        // now put player into target star system

        world.setCurrentRoom(targetSystem);
        targetSystem.enter(world);

        SpaceObject npcStation = targetSystem.getShips().get(0); // TODO: what if it is destroyed?
        world.getPlayer().getShip().setPos(npcStation.getX() + 1, npcStation.getY());

        Dialog tradeDialog = Dialog.loadFromFile("dialogs/klisk/klisk_trade_quest_trade.json");
        tradeDialog.setListener(this);

        world.addOverlayWindow(tradeDialog);
    }

    private void processPartyInvitation(World world, int returnCode) {
        Dialog arrivalDialog = Dialog.loadFromFile("dialogs/klisk/klisk_trade_quest_arrival.json");
        arrivalDialog.setListener(this);

        if (returnCode == 1) {
            Dialog nextDialog = Dialog.loadFromFile("dialogs/klisk/klisk_trade_quest_party.json");
            nextDialog.setListener(new NextDialogListener(arrivalDialog));
            world.addOverlayWindow(nextDialog);
        } else {
            world.addOverlayWindow(arrivalDialog);
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
        if (returnCode == 0) {
            // player refused to perform trading
            world.getGlobalVariables().put("klisk_trade.result", "refused");
            return;
        }

        String result;
        if (flags.containsKey("fist_good") && flags.containsKey("second_good") && flags.containsKey("third_good")) {
            result = "perfect";
        } else if (
                (flags.containsKey("fist_good") || flags.containsKey("fist_money"))
                        && (flags.containsKey("second_good") || flags.containsKey("second_money"))
                        && (flags.containsKey("third_good") || flags.containsKey("third_money"))
                ) {
            result = "good";
        } else {
            result = "bad";
        }

        world.getGlobalVariables().put("klisk_trade.result", result);

    }
}
