package ru.game.aurora.world.quest.act2.metropole;

import de.lessvoid.nifty.controls.Label;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Condition;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.Statement;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.password_input.PasswordInputController;
import ru.game.aurora.gui.password_input.PasswordInputEventsController;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.space.AlienHomeworld;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by di Grigio on 28.02.2017.
 */
public class MetropoleBurdenQuest extends GameEventListener implements WorldGeneratorPart {

    private static final long serialVersionUID = -8129152492083887705L;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MetropoleBurdenQuest.class);

    private StarSystem colonySystem;
    private AlienHomeworld colonyPlanet;

    @Override
    public String getLocalizedMessageForStarSystem(final World world, final GalaxyMapObject galaxyMapObject) {
        if (galaxyMapObject == colonySystem) {
            return Localization.getText("journal", "metropole_burden.title");
        }
        return null;
    }

    @Override
    public void updateWorld(final World world) {
        ColonyFounder.foundColony(world);
        getColonyData(world);

        colonyPlanet.setDialog(getColonyDialog(world));
        addStartJornalEntries(world);
        addLeaderFamily(world);
        addAliensStations(world);
        world.addListener(this);
    }

    private void getColonyData(final World world) {
        if(world.getGlobalVariables().containsKey("colony_search.coords")){
            Object obj = world.getGlobalVariables().get("colony_search.coords");

            if(obj != null && obj instanceof AlienHomeworld){
                colonyPlanet = (AlienHomeworld)obj;
                colonySystem = colonyPlanet.getOwner();
                colonySystem.setQuestLocation(true);
            }
            else{
                logger.error("Colony star system not defined");
            }
        }
    }

    private void addAliensStations(final World world) {
        addKliskStation(world);
        addBorkStation(world);
        addRoguesStation(world);
    }
    private void addKliskStation(final World world) {
        Dialog dialog = Dialog.loadFromFile("dialogs/act2/metropole_burden/stations/metropole_burden_klisk_station.json");
        dialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 7356902391168484873L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if(!world.getGlobalVariables().containsKey("metropole_burden.klisk_station_visited")){
                    world.getGlobalVariables().put("metropole_burden.klisk_station_visited", true);
                }

                if(flags.containsKey("metropole_burden.trade_weapon")){
                    world.getGlobalVariables().put("metropole_burden.trade_weapon", true);
                }
                if(flags.containsKey("metropole_burden.trade_augmentation")){
                    world.getGlobalVariables().put("metropole_burden.trade_augmentation", true);
                }
            }
        });

        addStation(dialog, "klisk_station",  world.getFactions().get(KliskGenerator.NAME), "Klisk trade station", 1, -1);
    }

    private void addBorkStation(final World world) {
        Dialog dialog = Dialog.loadFromFile("dialogs/act2/metropole_burden/stations/metropole_burden_bork_station.json");
        dialog.addListener(new DialogListener() {
            private static final long serialVersionUID = -4050295078694394215L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if(!world.getGlobalVariables().containsKey("metropole_burden.bork_station_visited")){
                    world.getGlobalVariables().put("metropole_burden.bork_station_visited", true);
                }
            }
        });
        addStation(dialog, "bork_ship",  world.getFactions().get(BorkGenerator.NAME), "Bork trade station", -1, 1);
    }

    private void addRoguesStation(final World world) {
        Dialog dialog = Dialog.loadFromFile("dialogs/act2/metropole_burden/stations/metropole_burden_rogues_station.json");
        dialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 7331782200454824692L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if(!world.getGlobalVariables().containsKey("metropole_burden.rogue_station_visited")){
                    world.getGlobalVariables().put("metropole_burden.rogue_station_visited", true);
                }
            }
        });

        addStation(dialog, "rogues_beacon",  world.getFactions().get(RoguesGenerator.NAME), "Rogues trade station", 1, 1);
    }

    private void addStation(Dialog dialog, String sprite, Faction faction, String name, int dx, int dy){
        NPCShip station = new NPCShip(0, 0, sprite, faction, new NPC(dialog), name, 25);
        station.setStationary(true);
        station.setAi(null);
        station.setPos(colonyPlanet.getX() + dx, colonyPlanet.getY() + dy);
        colonySystem.getShips().add(station);
    }

    private void addStartJornalEntries(final World world) {
        // JournalScreenController.onItemClicked() modifed
        // Player must see this log entries to discover some answers in quest dialogues
        world.getPlayer().getJournal().addQuestEntries("metropole_burden", "start");
        world.getPlayer().getJournal().addQuestEntries("metropole_burden", "document_introduction");
        world.getPlayer().getJournal().addQuestEntries("metropole_burden", "document_taxes");
        world.getPlayer().getJournal().addQuestEntries("metropole_burden", "document_dues");
        world.getPlayer().getJournal().addQuestEntries("metropole_burden", "document_embargo");
    }

    private Dialog getColonyDialog(final World world) {
        final Dialog landing = Dialog.loadFromFile("dialogs/act2/metropole_burden/planet/metropole_burden_landing.json");
        final Dialog afterLanding = Dialog.loadFromFile("dialogs/act2/metropole_burden/planet/metropole_burden_leader_after_landing.json");
        final Dialog discussionBeginNormal = Dialog.loadFromFile("dialogs/act2/metropole_burden/planet/metropole_burden_discussion_begin_normal.json");
        final Dialog discussionBeginInstant = Dialog.loadFromFile("dialogs/act2/metropole_burden/planet/metropole_burden_discussion_begin_instant.json");
        final Dialog discussionMain = Dialog.loadFromFile("dialogs/act2/metropole_burden/planet/metropole_burden_discussion.json");
        final Dialog embargoSigning = Dialog.loadFromFile("dialogs/act2/metropole_burden/planet/metropole_burden_embargo_signing.json");
        final Dialog discussionEnd = Dialog.loadFromFile("dialogs/act2/metropole_burden/planet/metropole_burden_discussion_end.json");
        final Dialog pressConference = Dialog.loadFromFile("dialogs/act2/metropole_burden/planet/metropole_burden_press_conference.json");

        landing.addListener(new DialogListener() {
            private static final long serialVersionUID = 7356902391168484873L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                world.getPlayer().getShip().removeCrewMember(world, "colony_leader_family");
                logger.info("Sin and Jodie Donahue removed from Aurora crew");

                world.getPlayer().getJournal().addQuestEntries("metropole_burden", "colony_landing");
                world.getPlayer().getJournal().addQuestEntries("metropole_burden", "colony_after_landing");

                world.addOverlayWindow(afterLanding, flags);
            }
        });

        afterLanding.addListener(new DialogListener() {
            private static final long serialVersionUID = -4050295078694394215L;
            @Override
            public void onDialogEnded(final World world, Dialog dialog, int returnCode, final Map<String, String> flags) {
                if(returnCode == 1){
                    world.addOverlayWindow(discussionBeginInstant, flags);
                }
                else if(returnCode == 2){
                    logger.info("Show login window");
                    PasswordInputController controller = (PasswordInputController)GUI.getInstance().getNifty().findScreenController(PasswordInputController.class.getCanonicalName());
                    if(controller != null){
                        controller.setLoginField(Localization.getText("gui", "password_input.login_name"));
                        controller.setLoginFieldEnabled(false);

                        controller.setEventObserver(new PasswordInputEventsController() {
                            private int errorCounter = 0;

                            @Override
                            public void onPasswordChanged(String passwordInputed, Label errorLabel) {
                                if(passwordInputed != null){
                                    if(passwordInputed.length() < 5){
                                        errorLabel.setText(Localization.getText("gui", "password_input.err_short_password"));
                                    }
                                    else{
                                        errorLabel.setText("");
                                    }
                                }
                            }

                            @Override
                            public void onLogin(String passwordInputed, Label errorLabel) {
                                if(!passwordInputed.toLowerCase().equals(Localization.getText("gui", "password_input.password").toLowerCase())){
                                    ++errorCounter;
                                    if(errorCounter < 3){
                                        errorLabel.setText(Localization.getText("gui", "password_input.err_wrong_password"));
                                    }
                                    else{
                                        errorLabel.setText(Localization.getText("gui", "password_input.password_hint"));
                                    }
                                }
                                else{
                                    logger.info("Password is correct");
                                    GUI.getInstance().popAndSetScreen(); // close password input window
                                    world.addOverlayWindow(afterLanding, flags, 46); // return to dialogue from login complete (statementId 100)
                                }
                            }

                            @Override
                            public void onClose() {
                                logger.info("Login canceled");
                                world.addOverlayWindow(afterLanding, flags, 37); // return to dialogue from login begin (statementId 37)
                            }
                        });

                        GUI.getInstance().pushCurrentScreen();
                        GUI.getInstance().getNifty().gotoScreen("password_input");
                    }
                    else {
                        throw new NullPointerException(PasswordInputController.class.getCanonicalName() + " not registered in Nifty screen controllers");
                    }
                }
                else {
                    world.addOverlayWindow(discussionBeginNormal, flags);
                }
            }
        });

        discussionBeginNormal.addListener(new DialogListener() {
            private static final long serialVersionUID = 7331782200454824692L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if(returnCode == 1){
                    logger.info("Discussion start from statement 1");
                    world.addOverlayWindow(discussionMain, flags, 1);
                }
                else if(returnCode == 2){
                    logger.info("Discussion start from statement 2");
                    world.addOverlayWindow(discussionMain, flags, 2);
                }
                else if(returnCode == 3){
                    logger.info("Discussion start from statement 0");
                    world.addOverlayWindow(discussionMain, flags);
                }
            }
        });

        discussionBeginInstant.addListener(new DialogListener() {
            private static final long serialVersionUID = -2480466612114908957L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if(returnCode == 1){
                    world.addOverlayWindow(discussionMain, flags, 1);
                }
                else if(returnCode == 2){
                    world.addOverlayWindow(discussionMain, flags, 2);
                }
                else if(returnCode == 3){
                    world.getPlayer().getJournal().addQuestEntries("metropole_burden", "colony_failed");
                    endQuest(world, flags);
                }
            }
        });

        discussionMain.addListener(new DialogListener() {
            private static final long serialVersionUID = -3349206094163021925L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if(returnCode == 1){
                    // setup dialogue conditions
                    Map<String, Serializable> globalFlags = world.getGlobalVariables();
                    boolean colonyHelp = false;
                    boolean stationsVisited = false;
                    boolean terminalUsed = false;

                    // player assist in one or more colony quest
                    if((globalFlags.containsKey("colony.hunt_quest.completed") && globalFlags.get("colony.hunt_quest.completed").equals("1"))
                    || (globalFlags.containsKey("colony.lost_group_quest.completed") && globalFlags.get("colony.lost_group_quest.completed").equals("1"))
                    || (globalFlags.containsKey("colony.dungeon_quest.completed") && globalFlags.get("colony.dungeon_quest.completed").equals("1"))) {
                        colonyHelp = true;

                        // combine condition variants into one flag
                        flags.put("colony_help", "true");
                    }

                    // player visit all trade stations near colony planet
                    if(globalFlags.containsKey("metropole_burden.klisk_station_visited")
                    && globalFlags.containsKey("metropole_burden.rogue_station_visited")
                    && globalFlags.containsKey("metropole_burden.bork_station_visited")){
                        stationsVisited = true;
                    }

                    // player use trade reports in terminal
                    if(flags.containsKey("metropole_burden.documents_found") && flags.containsKey("metropole_burden.matthew_surname_reply")){
                        terminalUsed = true;
                    }

                    // load clear source
                    final Dialog embargo = Dialog.loadFromFile("dialogs/act2/metropole_burden/planet/metropole_burden_embargo_discussion.json");
                    // change flags
                    setEmbargoDialogConditions(embargo, colonyHelp, stationsVisited, terminalUsed);
                    embargo.addListener(new DialogListener() {
                        private static final long serialVersionUID = -1453971074210478453L;
                        @Override
                        public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                            int result = 0;

                            if(flags.containsKey("colony_help_used")){
                                ++result;
                            }

                            if(flags.containsKey("station_used")){
                                ++result;
                            }

                            if(flags.containsKey("terminal_used")){
                                if(flags.containsKey("metropole_burden.matthew_surname_reply")){
                                    --result;
                                }
                                else{
                                    ++result;
                                }
                            }

                            if(flags.containsKey("disagreement_plus")){
                                ++result;
                            }
                            else if(flags.containsKey("disagreement_minus")){
                                --result;
                            }

                            if(flags.containsKey("pressure_used")){
                                --result;
                            }

                            if(result <= 0){
                                // negative result
                                world.addOverlayWindow(embargoSigning, flags);
                            }
                            else{
                                // positive result
                                world.addOverlayWindow(embargoSigning, flags, 1);
                            }
                        }
                    });
                    world.addOverlayWindow(embargo, flags);
                }
                else{
                    int result = 0;
                    if(flags.containsKey("metropole_burden.embargo_signed")){
                        if(flags.get("metropole_burden.embargo_signed").equals("true")){
                            ++result;
                        }
                    }
                    else{
                        throw new NullPointerException("Flag 'metropole_burden.embargo_signed' not found");
                    }

                    if(flags.containsKey("metropole_burden.taxes_signed")){
                        if(flags.get("metropole_burden.taxes_signed").equals("true")){
                            ++result;
                        }
                    }
                    else{
                        throw new NullPointerException("Flag 'metropole_burden.taxes_signed' not found");
                    }

                    if(flags.containsKey("metropole_burden.dues_signed")){
                        if(flags.get("metropole_burden.dues_signed").equals("true")){
                            --result;
                        }
                    }
                    else{
                        throw new NullPointerException("Flag 'metropole_burden.dues_signed' not found");
                    }

                    if(flags.containsKey("metropole_burden.bring_colony_requests")){
                        if(flags.get("metropole_burden.bring_colony_requests").equals("true")){
                            ++result;
                        }
                        else if(flags.get("metropole_burden.bring_colony_requests").equals("false")){
                            --result;
                        }
                        else{
                            throw new IllegalArgumentException("Flag 'metropole_burden.bring_colony_requests' value must be 'true' or 'false'");
                        }
                    }
                    else{
                        throw new NullPointerException("Flag 'metropole_burden.bring_colony_requests' not found");
                    }

                    if(result > 0){
                        // good result
                        world.addOverlayWindow(discussionEnd, flags, 0);
                    }
                    else if(result == 0){
                        // neutral result
                        world.addOverlayWindow(discussionEnd, flags, 1);
                    }
                    else{
                        // negative result
                        world.addOverlayWindow(discussionEnd, flags, 2);
                    }
                }
            }
        });

        embargoSigning.addListener(new DialogListener() {
            private static final long serialVersionUID = 927972884883844574L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                world.addOverlayWindow(discussionMain, flags, 3); // back to main discussion
            }
        });

        discussionEnd.addListener(new DialogListener() {
            private static final long serialVersionUID = 1400548520770065711L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                flags.put("result", "0"); // initialization arithmetic flag
                world.addOverlayWindow(pressConference, flags);
            }
        });

        pressConference.addListener(new DialogListener() {
            private static final long serialVersionUID = 14728585031076478L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if(flags.containsKey("result")){
                    if(flags.get("result").equals("0")){
                        world.getGlobalVariables().put("metropole_burden.agressive_end", true);
                        logger.info("Press conference result is negative");
                        // negative result
                        if(flags.containsKey("metropole_burden.instant")){
                            world.getPlayer().getJournal().addQuestEntries("metropole_burden", "press_conference_instant_agressive");
                        }
                        else{
                            world.getPlayer().getJournal().addQuestEntries("metropole_burden", "press_conference_normal_agressive");
                        }
                    }
                    else{
                        logger.info("Press conference result is positive");
                        // positive result
                        if(flags.containsKey("metropole_burden.instant")){
                            world.getPlayer().getJournal().addQuestEntries("metro pole_burden", "press_conference_instant");
                        }
                        else{
                            world.getPlayer().getJournal().addQuestEntries("metropole_burden", "press_conference_normal");
                        }
                    }
                }
                else{
                    throw new NullPointerException("Flag 'result' not found");
                }

                if(flags.containsKey("metropole_burden.documents_found")){
                    world.getGlobalVariables().put("metropole_burden.documents_found", true);
                }

                endQuest(world, flags);
            }
        });

        return landing;
    }

    private void endQuest(World world, Map<String, String> flags) {
        logger.info("Quest metropole_burden complete");

        world.getGlobalVariables().put("metropole_burden_done", true); // end the 'The burden of the metropolis' quest branch
        recordFlags(world, flags, "metropole_burden.taxes_signed");
        recordFlags(world, flags, "metropole_burden.dues_signed");
        recordFlags(world, flags, "metropole_burden.embargo_signed");
        recordFlags(world, flags, "metropole_burden.embargo_signed");

        // todo: rebuild default dialogue to DialogListener methods(?)
        colonyPlanet.setDialog(Dialog.loadFromFile("dialogs/act2/metropole_burden/planet/metropole_burden_after_discussion.json"));
    }

    private void recordFlags(World world, Map<String, String> flags, String key){
        if(flags.containsKey(key)){
            world.getGlobalVariables().put(key, Boolean.parseBoolean(flags.get(key)));
        }
        else{
            world.getGlobalVariables().put(key, false);
        }
    }

    private void setEmbargoDialogConditions(Dialog dialog, boolean colonyHelp, boolean stationsVisited, boolean terminalUsed) {
        // find all "replace_condition" and set custom condition
        Map<Integer, Statement> statements = dialog.getStatements();
        for(Statement statement: statements.values()){
            for(int i = 0; i < statement.replies.length; ++i){
                Condition [] conditions = statement.replies[i].getReplyConditions();

                if(conditions != null && conditions.length > 0){
                    for(int j = 0; j < conditions.length; ++j){

                        if(conditions[j].name.equals("replace_condition")){
                            // replace this condition
                            statement.replies[i].setReplyConditions(buildReplyConditions(colonyHelp, stationsVisited, terminalUsed));
                        }
                    }
                }
            }
        }
    }

    private Condition[] buildReplyConditions(boolean colonyHelp, boolean stationsVisited, boolean terminalUsed) {
        //[colony_help_used] && [station_used] && [terminal_used] && disagreement_used && pressure_used
        List<Condition> conditions = new ArrayList<Condition>();
        if(colonyHelp){
            conditions.add(new Condition("colony_help_used", null, Condition.ConditionType.SET));
        }
        if(stationsVisited){
            conditions.add(new Condition("station_used", null, Condition.ConditionType.SET));
        }
        if(terminalUsed){
            conditions.add(new Condition("terminal_used", null, Condition.ConditionType.SET));
        }
        conditions.add(new Condition("disagreement_used", null, Condition.ConditionType.SET));
        conditions.add(new Condition("pressure_used", null, Condition.ConditionType.SET));
        return conditions.toArray(new Condition[conditions.size()]);
    }

    private void addLeaderFamily(World world) {
        Dialog startDialog = Dialog.loadFromFile("dialogs/act2/metropole_burden/family/metropole_burden_leader_family.json");
        final Dialog dialogBusy = Dialog.loadFromFile("dialogs/act2/metropole_burden/family/metropole_burden_leader_family_busy.json");

        // todo: set custom avatar
        final CrewMember family = new CrewMember("colony_leader_family", "colonist_dialog", startDialog);
        world.getPlayer().getShip().addCrewMember(world, family);

        startDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 4121884818510810548L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                family.setDialog(dialogBusy);
            }
        });
    }
}
