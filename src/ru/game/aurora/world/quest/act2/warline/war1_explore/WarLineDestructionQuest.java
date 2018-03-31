package ru.game.aurora.world.quest.act2.warline.war1_explore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.npc.shipai.FollowAI;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.Star;
import ru.game.aurora.world.space.StarSystem;

import javax.print.attribute.standard.MediaSize;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class WarLineDestructionQuest extends GameEventListener implements WorldGeneratorPart{

    private static  final Logger logger = LoggerFactory.getLogger(WarLineDestructionQuest.class);

    private StarSystem targetSystem;

    public WarLineDestructionQuest(){

    }

    private ArrayList<StarSystem> questSystems;
    private ArrayList systemNumber;
    private ArrayList<NPCShip> navy;
    private ArrayList<GameObject> ships;
    private ArrayList<GameObject> humanityShips;
    private ArrayList<GameObject> zorsanStations;

    @Override
    public void updateWorld(final World world){
        this.questSystems = new ArrayList<>();
        this.humanityShips = new ArrayList<>();
        this.navy = new ArrayList<>();
        this.systemNumber = new ArrayList<>();
        this.zorsanStations = new ArrayList<>();
        this.ships = new ArrayList<>();
        int num=1;
        systemNumber.add(num);
        world.getPlayer().getJournal().addQuestEntries("war1_destruction", "description");
        for (int i = 0; i < Configuration.getIntProperty("war1_explore.star_systems_to_explore"); i++) {
            targetSystem = (StarSystem) world.getGlobalVariables().get("WarLineStation" + Integer.toString(i));
            questSystems.add(targetSystem);
        }
        for(StarSystem starSystem: questSystems){
            SetNav(starSystem);
        }
    }

    @Override
    public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject){
        for(int i=0; i < Configuration.getIntProperty("war1_explore.star_systems_to_explore"); i++) {
            if (galaxyMapObject == questSystems.get(i)) {
                return Localization.getText("journal", "war1_destruction.title");
            }
        }
        return null;
    }

    private void SetNav(final StarSystem starSystem){
        starSystem.setQuestLocation(true);
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem starSystem){
        if(questSystems.contains(starSystem) && systemNumber.size()==1){
            logger.info("Entering to quest star starsystem: {}", starSystem.getCoordsString());
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/act2/warline/war1_destruction/destruction_first_system.json"));
            setNavy(world);
            for(int i=0; i==navy.size(); i++){
                starSystem.setRandomEmptyPosition(navy.get(i));
                navy.get(i).setAi(new FollowAI(world.getPlayer().getShip()));
                starSystem.getShips().add(navy.get(i));
            }
        }
        return  false;
    }

    @Override
    public boolean onTurnEnded(World world){
        if (questSystems.contains(world.getCurrentStarSystem())){
            if(zorsansChek(world)){
                for(int i=1; i==ships.size(); i++){
                    if(ships.get(i).getName().equals("Humanity ship")){
                        humanityShips.add(ships.get(i));
                    };
                    if(humanityShips.size()>7){
                        world.addOverlayWindow(Dialog.loadFromFile("dialogs/act2/warline/war1_destruction/destruction_many_ships.json"));
                        systemNumber.add(2);
                    }
                    if(humanityShips.size()<=7){
                        world.addOverlayWindow(Dialog.loadFromFile("dialogs/act2/warline/war1_destruction/destruction_few_ships.json"));
                        systemNumber.add(2);
                    }
                }
            };
        }
        return false;
    }


    private boolean zorsansChek(final World world){
        ships.clear();
        ships.addAll(world.getCurrentStarSystem().getShips());
        for(int i=0; i==ships.size(); i++){
            if(ships.get(i).getName().equals("Zorsan station")){
                zorsanStations.add(ships.get(i));
            }
            if(zorsanStations.size()==1){
                return true;
            }
        }
        return false;
    }

    private void setNavy(World world){
        for (int i = 0; i < 15; i++) {
            navy.add(((AlienRace) world.getFactions().get(HumanityGenerator.NAME)).getDefaultFactory().createShip(world, 0));
            navy.get(i).setAi(new FollowAI(world.getPlayer().getShip()));
            navy.get(i).setWeapons(ResourceManager.getInstance().getWeapons().getEntity("plasma_cannon"), ResourceManager.getInstance().getWeapons().getEntity("long_range_plasma_cannon"));
        }
    }
}
