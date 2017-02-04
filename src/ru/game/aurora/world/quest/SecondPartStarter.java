package ru.game.aurora.world.quest;

import org.slf4j.LoggerFactory;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.dialog.IntroDialog;
import ru.game.aurora.gui.FadeOutScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.IntroDialogController;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.IStateChangeListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.space.AlienHomeworld;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.StarSystemListFilter;
import ru.game.aurora.world.space.earth.Earth;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by User on 07.01.2017.
 * This code starts second part of a global plot, after Obliterator visits Solar system
 */
public class SecondPartStarter implements WorldGeneratorPart {

    private static final long serialVersionUID = 3401155966034871085L;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecondPartStarter.class);

    public void start(final World world) {
        world.getGlobalVariables().put("2nd_part", true);
        logger.info("Starting 2nd story part");

        // fade out screen, show new intro and dialogs after that
        FadeOutScreenController.makeFade(new IStateChangeListener() {
            private static final long serialVersionUID = 33172965260750148L;

            @Override
            public void stateChanged(Object param) {
                showIntro(world);
            }
        });
    }

    private void showIntro(World world) {
        IntroDialog dialog;
        if (world.getGlobalVariables().containsKey("colony_established")) {
            dialog = IntroDialog.load("story/intro_2_with_colony.json");
        }
        else {
            dialog = IntroDialog.load("story/intro_2_no_colony.json");
        }

        IntroDialogController introDialogController = (IntroDialogController) GUI.getInstance().getNifty().findScreenController(IntroDialogController.class.getName());
        introDialogController.pushDialog(dialog);
        introDialogController.setEndListener(new IStateChangeListener<World>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void stateChanged(World world) {
                updateWorld(world);
            }
        });
        GUI.getInstance().getNifty().gotoScreen("intro_dialog");
    }

    public void updateWorld(final World world) {
        world.getGlobalVariables().put("autosave_disabled", true);

        StarSystem solarSystem = (StarSystem)world.getGlobalVariables().get("solar_system");
        if(solarSystem == null){
            logger.error("Solar system not found");
            return;
        }

        // Apply Act II world changes
        updateYear(world);
        removeObliteratorBackground(world);
        updateColony(world);
        addUnityStation(world, solarSystem);
        movePlayerShipToEarth(world, solarSystem);

        //
        world.getGlobalVariables().remove("autosave_disabled");

        startUnityQuest(world);
    }

    private void updateYear(final World world) {
        // Add 5 years after Act I ended
        world.addDays(365 * 5);
    }

    private void removeObliteratorBackground(final World world) {
        // todo: remove Obliterator background from cloned star system (MainQuestGenerator)
    }

    private void updateColony(final World world) {
        // Replaces colony planet with a dialog planet like Earth (which can not be landed)
        if(world.getGlobalVariables().containsKey("colony_established")){
            colonyReplace(world);
        }
        else{
            // Create colony as AlienHomeworld
            colonyFound(world);
        }
    }

    private void colonyReplace(World world) {
        // Change colony planet type to AlienHomeworld
        if(!world.getGlobalVariables().containsKey("colony_search.coords")) {
            logger.error("Colony data not found, but global variable 'colony_established' exist");
            return;
        }

        Object obj = world.getGlobalVariables().get("colony_search.coords");
        if(obj == null){
            logger.error("Colony Planet data is not null");
            return;
        }

        if(obj instanceof Planet){
            Planet colonyPlanet = (Planet)obj;
            AlienHomeworld newColonyPlanet = buildColonyPlanet(world, colonyPlanet);

            // replace old colony planet
            BasePlanet[] planets = colonyPlanet.getOwner().getPlanets();
            for(int i = 0; i < planets.length; ++i){
                if(planets[i] == colonyPlanet){
                    logger.info("Colony type changed");
                    planets[i] = newColonyPlanet;
                    break;
                }
            }
        }
        else{
            logger.error("Colony Planet data is not instance of class " + Planet.class.getCanonicalName());
            return;
        }
    }

    private void colonyFound(World world) {
        int x = 0;
        int y = 0;

        Matcher m = Pattern.compile("\\d+").matcher((String)world.getGlobalVariables().get("colony_search.klisk_coords"));
        try {
            if (m.find()) {
                x = Integer.parseInt(m.group());

                if(m.find()) {
                    y = Integer.parseInt(m.group());
                }
            }
            else {
                logger.error("Cant parse 'colony_search.klisk_coords' - no coordinates data");
            }
        }
        catch (NumberFormatException e){
            logger.error("Cant parse 'colony_search.klisk_coords' coordinates.");
        }

        Object obj = world.getGalaxyMap().getObjectAt(x, y);
        if(obj != null && obj instanceof StarSystem){
            StarSystem colonyStarSystem = (StarSystem)obj;

            BasePlanet[] planets = colonyStarSystem.getPlanets();
            for(int i = 0; i < planets.length; ++i){
                if(planets[i] != null && planets[i].getSize() == 3 && planets[i].getAtmosphere() == PlanetAtmosphere.BREATHABLE_ATMOSPHERE){
                    // create colony here
                    AlienHomeworld newColonyPlanet = buildColonyPlanet(world, planets[i]);
                    planets[i] = buildColonyPlanet(world, planets[i]);
                    world.getGlobalVariables().put("colony_established", true);
                    world.getGlobalVariables().put("colony_search.coords", newColonyPlanet);
                    logger.info("Colony found in star system " + colonyStarSystem.getCoordsString());
                    break;
                }
            }
        }
        else{
            logger.error("Colony founding fail. Object [" + x + "," + y + "] is not star system.");
        }
    }

    private AlienHomeworld buildColonyPlanet(World world, BasePlanet sourcePlanet){
        return new AlienHomeworld(
                null,
                ((AlienRace) world.getFactions().get("Humanity")),
                loadColonyDialog(),
                sourcePlanet.getSize(),
                sourcePlanet.getY(),
                sourcePlanet.getOwner(),
                PlanetAtmosphere.BREATHABLE_ATMOSPHERE,
                sourcePlanet.getX(),
                PlanetCategory.PLANET_ROCK);
    }

    private Dialog loadColonyDialog() {
        // todo: build full colony dialog (quest "The burden of the metropolis")
        return Dialog.loadFromFile("dialogs/act2/colony_line/colony_dialog_before_landing.json");
    }

    private void addUnityStation(World world, StarSystem solarSystem) {
        int range = 50;
        StarSystem unityStarSystem = world.getGalaxyMap().getRandomNonQuestStarsystemInRange(solarSystem.getX(), solarSystem.getY(), range, null);

        if(unityStarSystem != null){
            logger.info("Unity station founded in " + unityStarSystem.getCoordsString());

            NPC capitan = new NPC(getUnityStationDialog());
            NPCShip spaceStation = new NPCShip(0, 0, "rogues_beacon", world.getFactions().get(RoguesGenerator.NAME), capitan, "Unity station", 90);
            setStationPosition(unityStarSystem.getPlanets(), spaceStation, unityStarSystem.getRadius()/2);
            spaceStation.setStationary(true);
            spaceStation.setAi(null);

            unityStarSystem.getShips().add(spaceStation);
            unityStarSystem.setQuestLocation(true);

            world.getGlobalVariables().put("unity_station_system", unityStarSystem);
        }
        else{
            logger.error("Unity station cant founded");
        }
    }

    private void setStationPosition(BasePlanet[] planets, NPCShip spaceStation, final int radius) {
        int y = CommonRandom.getRandom().nextInt(2 * radius) - radius;
        int x = (int) (Math.sqrt(radius * radius - y * y) * (CommonRandom.getRandom().nextBoolean() ? -1 : 1));

        // Remove planet overlap
        if(planets != null && planets.length > 0){
            for(int i = 0; i < planets.length; ++i){
                if(planets[i].getX() == x && planets[i].getY() == y){
                    x += 2;
                    y += 2;
                }
            }
        }

        spaceStation.setPos(y, x);
    }

    private Dialog getUnityStationDialog() {
        final Dialog startDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_docking.json");
        final Dialog insideDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_inside.json");
        final Dialog meettingDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_meeting.json");
        final Dialog intermissionDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_intermission.json");
        final Dialog embassyChoiceDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_embassy_choice.json");
        final Dialog kliskEmbassyDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_embassy_klisk.json");
        final Dialog borkEmbassyDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_embassy_bork.json");
        final Dialog roguesEmbassyDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_embassy_rogues.json");
        final Dialog endDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_after_dialogue_with_ambassadors.json");

        // docking
        startDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = -2371433038571672351L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                world.getPlayer().getJournal().addQuestEntries("unity", "inside");
                world.addOverlayWindow(insideDialog, flags);
                world.getPlayer().getShip().removeCrewMember(world, "komsky");
                logger.info("Joseph Komsky removed from Aurora crew");
            }
        });

        // inside
        insideDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 8780263868110509203L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if(world.getGlobalVariables().containsKey("zorsan_escape_victims")){
                    String victims = (String) world.getGlobalVariables().get("zorsan_escape_victims");
                    flags.put("zorsan_escape_victims", victims);
                    logger.info("flag 'zorsan_escape_victims'=" + victims);
                }

                world.addOverlayWindow(meettingDialog, flags);
            }
        });

        // meeting
        meettingDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 8780263868110509203L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                world.addOverlayWindow(intermissionDialog, flags);
            }
        });

        // intermission
        intermissionDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 2670006195502711335L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                createKliskPirateBase();
                world.addOverlayWindow(embassyChoiceDialog, flags);
            }
        });

        // embassy choise
        embassyChoiceDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 3854381839777885936L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                switch (returnCode){
                    case 1:
                        world.addOverlayWindow(kliskEmbassyDialog, flags);
                        break;

                    case 2:
                        world.addOverlayWindow(borkEmbassyDialog, flags);
                        break;

                    case 3:
                        world.addOverlayWindow(roguesEmbassyDialog, flags);
                        break;

                    default:
                        throw new IllegalStateException("Unknown 'dialogs/act2/quest_union/unity_station_embassy_choice.json' dialog returnCode " + returnCode);
                }
            }
        });

        // klisk embassy
        kliskEmbassyDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 5531196549355238685L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if(flags.containsKey("klisk_pirate_quest_accept")){
                    // dialog flag: klisk_pirate_quest_accept
                    logger.info("Quest Klisk Pirate accepted");
                    world.getGlobalVariables().put("klisk_pirate.started", true);
                }
                else {
                    // dialog flag: klisk_pirate_quest_reject
                    logger.info("Quest Klisk Pirate rejected");
                    world.getGlobalVariables().put("klisk_pirate.quest_result", "rejected");
                }

                if(flags.containsKey("unity_bork_ambassador_visited")
                && flags.containsKey("unity_rogues_ambassador_visited")){
                    world.addOverlayWindow(endDialog, flags);
                }
                else{
                    world.addOverlayWindow(embassyChoiceDialog, flags);
                }
            }
        });

        // bork embassy
        borkEmbassyDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 8123031492187245360L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if(flags.containsKey("unity_klisk_ambassador_visited")
                && flags.containsKey("unity_rogues_ambassador_visited")){
                    world.addOverlayWindow(endDialog, flags);
                }
                else{
                    world.addOverlayWindow(embassyChoiceDialog, flags);
                }
            }
        });

        // rogues embassy
        roguesEmbassyDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = -5792100234277240314L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                createBuildersArtifacts();

                if(flags.containsKey("unity_bork_ambassador_visited")
                && flags.containsKey("unity_klisk_ambassador_visited")){
                    world.addOverlayWindow(endDialog, flags);
                }
                else{
                    world.addOverlayWindow(embassyChoiceDialog, flags);
                }
            }
        });

        // end dialog
        endDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 5531196549355238685L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if(world.getGlobalVariables().containsKey("klisk_pirate.started")){
                    world.getPlayer().getJournal().questCompleted("unity", "klisk_quest_start");
                }
                else if(world.getGlobalVariables().containsKey("klisk_pirate.quest_result") && world.getGlobalVariables().get("klisk_pirate.quest_result").equals("rejected")){
                    world.getPlayer().getJournal().questCompleted("unity", "klisk_quest_reject");
                }

                // todo: add default 'Unity station dialog'
                // todo: start quest "The burden of the metropolis"
                // todo: after quest "The burden of the metropolis" start quest "On the trail of the Builders" and "Klisk pirate"
            }
        });

        return startDialog;
    }

    private void createKliskPirateBase() {
        World world = World.getWorld();

        final int x = world.getCurrentStarSystem().getX();
        final int y = world.getCurrentStarSystem().getY();
        StarSystem questStarSystem = world.getGalaxyMap().getRandomNonQuestStarsystemInRange(x, y, 100, new StarSystemListFilter() {
            @Override
            public boolean filter(StarSystem system) {
                return  system.getPlanets().length > 1;
            }
        });

        BasePlanet questPlanet = questStarSystem.getPlanets()[0];
        // todo: place pirate base on planet

        world.getGlobalVariables().put("klisk_pirate.planet", questPlanet);
        world.getGlobalVariables().put("klisk_pirate.coords", questStarSystem.getCoordsString());
        logger.info("Klisk pirate base placed in " + questStarSystem.getCoordsString());
    }

    private void createBuildersArtifacts() {
        // todo: add 4 quest star systems (quest "On the trail of the Builders")
    }

    private void movePlayerShipToEarth(final World world, final StarSystem solarSystem) {
        // Replace Aurora to Earth orbit
        BasePlanet earth = null;
        BasePlanet [] planets = solarSystem.getPlanets();
        for(int i = 0; i < planets.length; ++i){
            if(planets[i] != null && planets[i] instanceof Earth){
                earth = planets[i];
                break;
            }
        }

        if(earth == null){
            logger.error("Earth not found");
            return;
        }

        logger.info("Move player ship to Earth " + solarSystem.getCoordsString());
        world.setCurrentRoom(solarSystem);
        solarSystem.enter(world);
        world.getPlayer().getShip().setPos(earth.getX(), earth.getY());
    }

    private void startUnityQuest(final World world) {
        Dialog startDialog = Dialog.loadFromFile("dialogs/act2/quest_union/act_2_begin_martan.json");
        startDialog.addListener(new DialogListener() {
            private static final long serialVersionUID = 4488508909100895730L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                addKomsky(world);
            }
        });

        world.addOverlayWindow(startDialog);
        world.addListener(new UnityQuest(world));
        world.getPlayer().getJournal().addQuestEntries("unity", "start");
    }

    private void addKomsky(World world) {
        final Dialog startDialog = Dialog.loadFromFile("dialogs/act2/unity_human_ambassador_onboard_1.json");
        final Dialog secondDialog = Dialog.loadFromFile("dialogs/act2/unity_human_ambassador_onboard_2.json");
        final Dialog busyDialog = Dialog.loadFromFile("dialogs/act2/unity_human_ambassador_onboard_3.json");

        final CrewMember komsky = new CrewMember("komsky", "europe_leader", startDialog);
        world.getPlayer().getShip().addCrewMember(world, komsky);

        startDialog.addListener(new DialogListener() {
            // Set komsky dialogue to  'unity_human_ambassador_onboard_3' and await 10 turns to switch to 'unity_human_ambassador_onboard_2'
            private static final long serialVersionUID = 5578057943354971146L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                logger.info("flag 'unity_human_ambassador_onboard.talk_turn'=" + world.getDayCount());
                world.getGlobalVariables().put("unity_human_ambassador_onboard.talk_turn", world.getDayCount());
                komsky.setDialog(busyDialog);
                addSecondDialogWorldListener(world, komsky, secondDialog);
            }
        });

        secondDialog.addListener(new DialogListener() {
            // Set Komsky dialogue to 'unity_human_ambassador_onboard_3' - this is end of Komksy talking on Aurora board

            private static final long serialVersionUID = 4446555067154195647L;
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                logger.info("Joseph Komsky dialog line ended");
                world.getGlobalVariables().put("unity_human_ambassador_onboard.talked", true);
                komsky.setDialog(busyDialog);
            }
        });
    }

    private void addSecondDialogWorldListener(final World world, final CrewMember komsky, final Dialog secondDialog) {
        // Set Komsky dialogue after 10 turns to 'unity_human_ambassador_onboard_2'
        world.addListener(new GameEventListener() {
            private static final long serialVersionUID = -2752824071525087767L;
            @Override
            public boolean onTurnEnded(World world) {
                if(world.getGlobalVariables().containsKey("unity_human_ambassador_onboard.talk_turn")){
                    int lastDay = (int)world.getGlobalVariables().get("unity_human_ambassador_onboard.talk_turn");

                    if(world.getDayCount() > lastDay + 10){
                        logger.info("Joseph Komsky second dialog added on turn " + world.getDayCount());
                        komsky.setDialog(secondDialog);
                        this.isAlive = false;
                        world.getListeners().remove(this);
                    }
                }
                return super.onTurnEnded(world);
            }
        });
    }
}