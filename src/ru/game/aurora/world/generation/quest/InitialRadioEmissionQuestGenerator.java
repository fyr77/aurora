/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:10
 */
package ru.game.aurora.world.generation.quest;

import org.newdawn.slick.Color;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.shipai.CombatAI;
import ru.game.aurora.player.earth.EarthResearch;
import ru.game.aurora.player.earth.EarthState;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.engineering.projects.AdvancedLasers;
import ru.game.aurora.player.research.BaseResearchWithFixedProgress;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.player.research.projects.StarResearchProject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.generation.WorldGenerator;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.SpaceHulk;
import ru.game.aurora.world.space.Star;
import ru.game.aurora.world.space.StarSystem;

/**
 * Creates quest chain with initial research for brown dwarf radio emission, that is given to player on game startup
 */
public class InitialRadioEmissionQuestGenerator implements WorldGeneratorPart {

    private static final long serialVersionUID = -4950686913834019746L;

    private static final class EarthEnergyResearch extends EarthResearch {

        private static final long serialVersionUID = 8367244693901465513L;

        private NPCShip construction;

        private int state = 0;

        public EarthEnergyResearch() {
            super(100);
        }

        @Override
        protected void onCompleted(World world) {
            // begin construction of a beacon near the sun
            final EarthState earthState = world.getPlayer().getEarthState();
            if (state == 0) {
                earthState.getMessages().add(new PrivateMessage(
                        "Good work",
                        " Greetings. \n Perhaps you have heard already about the Icarus project? A few more years, and earth energy crisis will be over once and for ever. And this is done thanks to your effort. Good job, captain!" +
                                " \n A. V. Buren, Aurora CEO",
                        "message"
                )
                );
                earthState.getMessages().add(new PrivateMessage(
                        "Free energy tomorrow?"
                        , "The world scientific society is shocked by the discoveries done by analyzing materials retrieved from alien beacon by UNS " + world.getPlayer().getShip().getName() + ", as they break" +
                        " all that we knew about using solar energy before. New discoveries has lead to creation of new cheap ways of extracting power from the Sun. \n " +
                        " As Earth energy reserves are nearly depleted, scientist use this new knowledge to present a solution. An Icarus project, a series of energy consuming space stations surrounding the Sun." +
                        " Construction of the first one has already started."
                        , "news"
                ));


                final AlienRace humanity = world.getRaces().get("Humanity");
                construction = new NPCShip(0, 1, "earth_construction", humanity, null, "Icarus #1");
                construction.setAi(null);
                humanity.getHomeworld().getShips().add(construction);

                completed = false;

                targetTurn += 100;
                state = 1;
            } else if (state == 1) {
                // finish construction
                final AlienRace humanity = world.getRaces().get("Humanity");

                // replace station sprite
                humanity.getHomeworld().getShips().remove(construction);
                construction = new NPCShip(0, 1, "icarus_station", humanity, null, "Icarus #1");
                construction.setAi(null);
                humanity.getHomeworld().getShips().add(construction);

                // add messages

                earthState.getMessages().add(new PrivateMessage(
                        "First Icarus station launch"
                        , "The world holds breath while watching how first GW of energy are transferred from Icarus #1 station to earth. Though the launch of the station was postponed a couple of times, now" +
                        " it is finally launched and is producing power for Earth needs. \n Experts predict lowering of prices for energy by the end of the year by 15%, and cancelling of special power regulation laws" +
                        " in some most populated Earth regions."
                        , "news"
                ));
                completed = false;
                targetTurn += 20;
                state = 2;
            } else if (state == 2) {
                earthState.getMessages().add(new PrivateMessage(
                        "Energy crisis gone, labor crisis coming?"
                        , "For the last months after Icarus power station has reached its full productivity, troubling news are coming from UN power plants and factories. Cheap solar energy has driven" +
                        " these structures obsolete. While ecologists and population praise Icarus project, power tycoons and workers hate it and try to sabotage building of new stations. \n Many people in energy production sector" +
                        " have lost their jobs. Strikes and uprisings happened in Russia, Egypt and some of european countries, which makes future of Icarus project unclear."
                        , "news"
                ));
                earthState.updateTechnologyLevel(200);
            }

        }

        @Override
        public void onStarted(World world) {
            super.onStarted(world);
            world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(
                    "Breakthrough in solar power usage?"
                    , "According to data stated in official Aurora project news, UNS " + world.getPlayer().getShip().getName() + " has brought very valuable data conserning use" +
                    " of star enegry from its expedition. \n Data and technology acquired from an alien beacon-like structure can lead to a creation of much more effective ways of collecting and transfering solar power, which could give" +
                    " humanity so long anticipated 'free energy'. Scientists at Earth laboratories are now carefully studying retrieved material and are preparing official conclusion."
                    , "news"
            ));
        }
    }

    @Override
    public void updateWorld(World world) {
        // initial research projects and their star system
        StarSystem brownStar = WorldGenerator.generateRandomStarSystem(world, 6, 7);
        brownStar.setStar(new Star(6, new Color(128, 0, 0)));
        world.getGalaxyMap().addObjectAndSetTile(brownStar, 6, 7);

        ResearchProjectDesc starInitialResearch = new StarResearchProject(brownStar);
        starInitialResearch.setReport(new ResearchReport("star_research", "This brown dwarf is unusual, as it actively emits radiowaves. Origin of this emission is currently unclear, and it is changing in time in a way that breaks all current theories concerning brown dwarves structure. " +
                "This star is small, and its surface temperature is only about 900K, which makes it look more like a gas giant than like a star. Tracking such stars from Solar system using long-range radio telescopes is very difficult due to their low contrast and great distance." +
                " \n Data collected by expedition can lead to better understanding of processes occurring inside these 'wannabe-stars'. But for better process understanding we should find and observe another brown dwarf with similar emission capacity. The closest one is at [12, 12]"));
        world.getPlayer().getResearchState().addNewAvailableProject(starInitialResearch);

        // add second quest in chain

        brownStar = WorldGenerator.generateRandomStarSystem(world, 12, 12);
        brownStar.setStar(new Star(6, new Color(128, 0, 0)));
        world.getGalaxyMap().addObjectAndSetTile(brownStar, 12, 12);

        NPCShip defenceProbe = new NPCShip(2, 1, "rogues_probe", world.getRaces().get("Rogues"), null, "Defence drone");
        defenceProbe.setAi(new CombatAI(world.getPlayer().getShip()));
        defenceProbe.setWeapons(new StarshipWeapon(ResourceManager.getInstance().getWeapons().getEntity("plasma_cannon"), StarshipWeapon.MOUNT_ALL));
        defenceProbe.setHostile(true);
        defenceProbe.setStationary(true);
        brownStar.getShips().add(defenceProbe);
        brownStar.setFirstEnterDialog(Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/quest/rogue_beacon_found.json")));

        ResearchProjectDesc beaconResearchProject = new BaseResearchWithFixedProgress(
                "Beacon research"
                , "Detailed study for materials and samples taken from enormous alien structure, assigned name 'beacon'"
                , "technology_research"
                , 40
                , 50
        );

        ResearchReport beaconReport = new ResearchReport("rogues_beacon", "This structure is located near the sun, consuming its energy and transforming it into" +
                " emiision of different types of waves, both radio and hyperwave. It is transmitting a small, repeatable pattern, and its power is enormous. We believe it can" +
                " be easily detected even with our low-resolution sensors from more than a thousand light years. Due to these points we believe that this structure is some kind of a beacon, " +
                " perhaps used for navigation, or for marking places of interest for its creators. \n " +
                " We din't fully understand mechanincs of its emission and energy consumption, but it overpowers all technology available to humanity. Detailed study of " +
                " collected data in Earth laboratories can lead to a breakthrough in high-energy and hyperwave physics." +
                " \n We should also be careful, as creators of this beacon may not welcome our violation of their territory.");
        beaconResearchProject.setReport(beaconReport);
        beaconResearchProject.addEarthProgressResearch(new EarthEnergyResearch());
        beaconResearchProject.addEngineeringResult(new AdvancedLasers());

        SpaceHulk beacon = new SpaceHulk(1, 1, "Beacon", "rogues_beacon");
        beacon.setResearchProjectDescs(beaconResearchProject);
        beacon.setOnInteractDialog(Dialog.loadFromFile("dialogs/quest/rogues_beacon_explored.json"));
        brownStar.getShips().add(beacon);

        ResearchProjectDesc secondResearch = new StarResearchProject(brownStar);
        secondResearch.setReport(new ResearchReport("star_research", "We have studied the star, but it is not emitting anything unusual this time. It is a typical brown dwarf of spectral class L, its optical spectrum dominated by absorption bands of FeH, CrH and prominent alkali metal lines." +
                " \n Perhaps, the source of emission is that alien structure near it. But what an immense power should it contain then? We should investigate this as soon as possible"));
        starInitialResearch.addNextResearch(secondResearch);

    }
}
