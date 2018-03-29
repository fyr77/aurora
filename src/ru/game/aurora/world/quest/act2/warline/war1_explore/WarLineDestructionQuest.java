package ru.game.aurora.world.quest.act2.warline.war1_explore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.Localization;
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

    private ArrayList<StarSystem> QuestSystems;
    private ArrayList SystemNumber;
    private ArrayList<NPCShip> Navy;
    private ArrayList<GameObject> Ships;
    private ArrayList<GameObject> ZorsanStations;
    private ArrayList<GameObject> HumanityShips;

    @Override
    public void updateWorld(final World world){
        this.QuestSystems = new ArrayList<>();
        this.HumanityShips = new ArrayList<>();
        this.SystemNumber = new ArrayList();
        this.Navy = new ArrayList<>();
        this.SystemNumber = new ArrayList<>();
        this.Ships = new ArrayList<>();
        this.ZorsanStations = new ArrayList<>();
        int num=1;
        SystemNumber.add(num);
        world.getPlayer().getJournal().addQuestEntries("war1_destruction", "description");
        for (int i = 0; i < Configuration.getIntProperty("war1_explore.star_systems_to_explore"); i++) {
            targetSystem = (StarSystem) world.getGlobalVariables().get("WarLineStation" + Integer.toString(i));
            QuestSystems.add(targetSystem);
        }
        for(StarSystem starSystem: QuestSystems){
            SetNav(starSystem);
        }
    }

    @Override
    public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject){
        for(int i=0; i < Configuration.getIntProperty("war1_explore.star_systems_to_explore"); i++) {
            if (galaxyMapObject == QuestSystems.get(i)) {
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
        if(QuestSystems.contains(starSystem) && SystemNumber.size()==1){
            logger.info("Entering to quest star starsystem: {}", starSystem.getCoordsString());
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/act2/warline/war1_destruction/destruction_first_system.json"));
            setNavy(world);
//            for(int i=0; i==Navy.size(); i++){
//                starSystem.setRandomEmptyPosition(Navy.get(i));
//                starSystem.getShips().add(Navy.get(i));
//            }
                starSystem.setRandomEmptyPosition(Navy.get(0));
                starSystem.setRandomEmptyPosition(Navy.get(1));
                starSystem.setRandomEmptyPosition(Navy.get(2));
                starSystem.setRandomEmptyPosition(Navy.get(3));
                starSystem.setRandomEmptyPosition(Navy.get(4));
                starSystem.setRandomEmptyPosition(Navy.get(5));
                starSystem.setRandomEmptyPosition(Navy.get(6));
                starSystem.setRandomEmptyPosition(Navy.get(7));
                starSystem.setRandomEmptyPosition(Navy.get(8));
                starSystem.setRandomEmptyPosition(Navy.get(9));
                starSystem.setRandomEmptyPosition(Navy.get(10));
                starSystem.setRandomEmptyPosition(Navy.get(11));
                starSystem.setRandomEmptyPosition(Navy.get(12));
                starSystem.setRandomEmptyPosition(Navy.get(13));
                starSystem.setRandomEmptyPosition(Navy.get(14));
                starSystem.getShips().add(Navy.get(0));
                starSystem.getShips().add(Navy.get(1));
                starSystem.getShips().add(Navy.get(2));
                starSystem.getShips().add(Navy.get(3));
                starSystem.getShips().add(Navy.get(4));
                starSystem.getShips().add(Navy.get(5));
                starSystem.getShips().add(Navy.get(6));
                starSystem.getShips().add(Navy.get(7));
                starSystem.getShips().add(Navy.get(8));
                starSystem.getShips().add(Navy.get(9));
                starSystem.getShips().add(Navy.get(10));
                starSystem.getShips().add(Navy.get(11));
                starSystem.getShips().add(Navy.get(12));
                starSystem.getShips().add(Navy.get(13));
                starSystem.getShips().add(Navy.get(14));
        }
        return  false;
    }

    @Override
    public boolean onTurnEnded(World world){
        if (QuestSystems.contains(world.getCurrentStarSystem())){
            if(zorsansChek(world)){
                for(int i=1; i==Ships.size(); i++){
                    if(Ships.get(i).getName().equals("Humanity ship")){
                        HumanityShips.add(Ships.get(i));
                    };
                    if(HumanityShips.size()>7){
                        world.addOverlayWindow(Dialog.loadFromFile("dialogs/act2/warline/war1_destruction/destruction_many_ships.json"));
                    }
                    if(HumanityShips.size()<=7){
                        world.addOverlayWindow(Dialog.loadFromFile("dialogs/act2/warline/war1_destruction/destruction_few_ships.json"));
                    }
                }
            };
        }
        return false;
    }


    private boolean zorsansChek(final World world){
        Ships.clear();
        Ships.addAll(world.getCurrentStarSystem().getShips());
        for(int i=0; i==Ships.size(); i++){
            if(Ships.get(i).getName().equals("Zorsan station") && Ships.size()==1){
                return true;
            }
        }
        return false;
    }

    private void setNavy(World world){
        for (int i = 0; i < 15; i++) {
            Navy.add(((AlienRace) world.getFactions().get(HumanityGenerator.NAME)).getDefaultFactory().createShip(world, 0));
            Navy.get(i).setAi(new FollowAI(world.getPlayer().getShip()));
        }
    }
}
