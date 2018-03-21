package ru.game.aurora.world.quest.act2.warline.war1_explore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.Configuration;
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
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.Star;
import ru.game.aurora.world.space.StarSystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class WarLineDestructionQuest extends GameEventListener implements WorldGeneratorPart{

    private static  final Logger logger = LoggerFactory.getLogger(WarLineDestructionQuest.class);

    private StarSystem targetSystem;

    public WarLineDestructionQuest(){

    }

    private Set<StarSystem> QuestSystems;
    private ArrayList SystemNumber;
    private ArrayList<NPCShip> Navy;
    private ArrayList<GameObject> Ships;

    @Override
    public void updateWorld(final World world){
        this.QuestSystems = new HashSet<StarSystem>();
        this.SystemNumber = new ArrayList();
        this.Navy = new ArrayList<>();
        this.SystemNumber = new ArrayList<>();
        this.Ships = new ArrayList<>();
        int num=1;
        SystemNumber.add(num);
        world.getPlayer().getJournal().addQuestEntries("war1_destruction", "description");
        for (int i = 0; i < Configuration.getIntProperty("war1_explore.star_systems_to_explore"); i++) {
            targetSystem = (StarSystem)world.getGlobalVariables().get("WarLineStation"+Integer.toString(i));
            targetSystem.setQuestLocation(true);
            QuestSystems.add(targetSystem);
        }
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem starSystem){
        logger.info("Entering to quest star starsystem: {}", starSystem.getCoordsString());
        if(QuestSystems.contains(starSystem) && SystemNumber.size()==1){
            setNavy(world);
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/act2/warline/war1_destruction/destruction_first_system.json"));
            for (int i = 0; i < 10; i++) {
                Navy.get(i).setPos(-1+i, 1+i);
                Navy.get(i).setAi(new FollowAI(world.getPlayer().getShip()));
            }
        }
        return  false;
    }

    @Override
    public boolean onTurnEnded(World world){
        if(zorsansChek(world)){
            Dialog manyShipsDialog = Dialog.loadFromFile("dialogs/act2/warline/war1_destruction/destruction_many_ships.json");
            world.addOverlayWindow(manyShipsDialog);
            world.getPlayer().getJournal().addQuestEntries("war1_destruction", "many_ships");
        }
        return false;
    }

    private boolean zorsansChek(final World world){
        Ships.addAll(world.getCurrentStarSystem().getShips());
        for(int i=0; i==Ships.size(); i++){
            if(Ships.get(i).getName().equals("Zorsan station")){
                return false;
            }
        }
        return true;
    }


    private void setNavy(final World world){
        for (int i = 0; i < 10; i++) {
            Navy.add(((AlienRace) world.getFactions().get(HumanityGenerator.NAME)).getDefaultFactory().createShip(world, 0));
        }
    }
}
