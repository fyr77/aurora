package ru.game.aurora.world.quest.act2.unity;

import org.slf4j.LoggerFactory;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.music.MusicDialogListener;
import ru.game.aurora.music.Playlist;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;
import ru.game.aurora.world.space.StarSystemListFilter;

import java.util.Map;

/**
 * Created by User on 08.01.2017.
 * Unity quest
 */
public class UnityQuest extends GameEventListener implements WorldGeneratorPart {

    private static final long serialVersionUID = 8713537599537481751L;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UnityQuest.class);

    private StarSystem targetSystem;

    public UnityQuest(){}

    @Override
    public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject) {
        if (galaxyMapObject == targetSystem) {
            return Localization.getText("journal", "unity.title");
        }
        return null;
    }

    @Override
    public void updateWorld(World world) {
        addUnityStation(world);
        addKomsky(world); // as crew
        world.getPlayer().getJournal().addQuestEntries("unity", "start");
        world.addListener(this);
    }

    private void addUnityStation(World world) {
        StarSystem solarSystem = (StarSystem)world.getGlobalVariables().get("solar_system");
        if(solarSystem == null){
            throw new NullPointerException("Solar system does not exist");
        }

        StarSystem unityStarSystem = world.getGalaxyMap().getRandomNonQuestStarsystemInRange(solarSystem.getX(), solarSystem.getY(), 70, null);

        if(unityStarSystem != null){
            logger.info("Unity station founded in " + unityStarSystem.getCoordsString());

            NPCShip spaceStation = new NPCShip(0, 0, "rogues_beacon", world.getFactions().get(RoguesGenerator.NAME), null, "Unity station", 90);
            setStationPosition(unityStarSystem.getPlanets(), spaceStation, unityStarSystem.getRadius()/2);
            spaceStation.setStationary(true);
            spaceStation.setAi(null);

            NPC capitan = new NPC(getUnityStationDialog(spaceStation));
            capitan.setCustomPlaylist("unity_dialog");

            spaceStation.setCaptain(capitan);

            unityStarSystem.getShips().add(spaceStation);
            unityStarSystem.setQuestLocation(true);

            world.getGlobalVariables().put("unity_station_system", unityStarSystem);
            this.targetSystem = unityStarSystem; // for display quest mark on galaxy map
        }
        else{
            logger.error("Unity station cant founded");
        }
    }

    private void setStationPosition(BasePlanet[] planets, NPCShip spaceStation, final int radius) {
        int y = CommonRandom.getRandom().nextInt(2 * radius) - radius;
        int x = (int) (Math.sqrt(radius * radius - y * y) * (CommonRandom.getRandom().nextBoolean() ? -1 : 1));

        // Remove planet overlap
        if (planets != null && planets.length > 0) {
            for (int i = 0; i < planets.length; ++i) {
                if (planets[i].getX() == x && planets[i].getY() == y) {
                    x += 2;
                    y += 2;
                }
            }
        }

        spaceStation.setPos(y, x);
    }

    private Dialog getUnityStationDialog(final NPCShip spaceStation) {
        final Dialog startDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_docking.json");
        final Dialog insideDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_inside.json");
        final Dialog meettingDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_meeting.json");
        final Dialog intermissionDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_intermission.json");
        final Dialog embassyChoiceDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_embassy_choice.json");
        final Dialog kliskEmbassyDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_embassy_klisk.json");
        final Dialog borkEmbassyDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_embassy_bork.json");
        final Dialog roguesEmbassyDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_embassy_rogues.json");
        final Dialog endDialog = Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_after_dialogue_with_ambassadors.json");

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

                world.getGlobalVariables().put("unity_done", true); // end the Unity quest branch

                // set default station dialogues and music after visit
                spaceStation.setCaptain(new NPC(Dialog.loadFromFile("dialogs/act2/quest_union/unity_station_docking_visited.json")));

                if(flags.containsKey("unity_zorsan_ambassador_visited")){
                    world.getGlobalVariables().put("unity_zorsan_ambassador_visited", true);
                }
            }
        });

        endDialog.addListener(new MusicDialogListener(Playlist.getCurrentPlaylist().getId())); // end dialogs chain music

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

    private void addKomsky(World world) {
        final Dialog startDialog = Dialog.loadFromFile("dialogs/act2/unity_human_ambassador_onboard_1.json");
        final Dialog secondDialog = Dialog.loadFromFile("dialogs/act2/unity_human_ambassador_onboard_2.json");
        final Dialog busyDialog = Dialog.loadFromFile("dialogs/act2/unity_human_ambassador_onboard_3.json");

        final CrewMember komsky = new CrewMember("komsky", "earth_diplomat_dialog", startDialog);
        world.getPlayer().getShip().addCrewMember(world, komsky);

        startDialog.addListener(new DialogListener() {
            // Set Komsky dialogue to  'unity_human_ambassador_onboard_3' and await 10 turns to switch to 'unity_human_ambassador_onboard_2'
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

    private void createBuildersArtifacts() {
        // todo: add 4 quest star systems (quest "On the trail of the Builders")
    }
}