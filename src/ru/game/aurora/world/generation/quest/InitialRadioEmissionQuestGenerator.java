/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.02.13
 * Time: 15:10
 */
package ru.game.aurora.world.generation.quest;

import org.newdawn.slick.Color;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.npc.Dialog;
import ru.game.aurora.npc.shipai.CombatAI;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.player.research.projects.StarResearchProject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.generation.WorldGenerator;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.SpaceHulk;
import ru.game.aurora.world.space.StarSystem;

/**
 * Creates quest chain with initial research for brown dwarf radio emission, that is given to player on game startup
 */
public class InitialRadioEmissionQuestGenerator implements WorldGeneratorPart
{
    @Override
    public void updateWorld(World world) {
        // initial research projects and their star system
        StarSystem brownStar = WorldGenerator.generateRandomStarSystem(6, 7);
        brownStar.setStar(new StarSystem.Star(6, new Color(128, 0, 0)));
        world.getGalaxyMap().addObjectAndSetTile(brownStar, 6, 7);

        ResearchProjectDesc starInitialResearch = new StarResearchProject(brownStar);
        starInitialResearch.setReport(new ResearchReport("star_research", "This brown dwarf is unusual, as it actively emits radiowaves. Origin of this emission is currently unclear, and it is changing in time in a way that breaks all current theories concerning brown dwarves structure. " +
                "This star is small, and its surface temperature is only about 900K, which makes it look more like a gas giant than like a star. Tracking such stars from Solar system using long-range radio telescopes is very difficult due to their low contrast and great distance." +
                " \n Data collected by expedition can lead to better understanding of processes occurring inside these 'wannabe-stars'. But for better process understanding we should find and observe another brown dwarf with similar emission capacity. The closest one is at [12, 12]"));
        world.getPlayer().getResearchState().getAvailableProjects().add(starInitialResearch);

        // add second quest in chain

        brownStar = WorldGenerator.generateRandomStarSystem(12, 12);
        brownStar.setStar(new StarSystem.Star(6, new Color(128, 0, 0)));
        world.getGalaxyMap().addObjectAndSetTile(brownStar, 12, 12);

        NPCShip defenceProbe = new NPCShip(2, 1, "rogues_probe", world.getRaces().get("Rogues"), null, "Defence drone");
        defenceProbe.setAi(new CombatAI(world.getPlayer().getShip()));
        defenceProbe.setWeapons(new StarshipWeapon(ResourceManager.getInstance().getWeapons().getEntity("plasma_cannon"), StarshipWeapon.MOUNT_ALL));
        defenceProbe.setHostile(true);
        defenceProbe.setStationary(true);
        brownStar.getShips().add(defenceProbe);
        brownStar.setFirstEnterDialog(Dialog.loadFromFile(getClass().getClassLoader().getResourceAsStream("dialogs/quest/rogue_beacon_found.json")));

        SpaceHulk beacon = new SpaceHulk(1, 1, "Beacon", "rogues_beacon");
        brownStar.getShips().add(beacon);

        ResearchProjectDesc secondResearch = new StarResearchProject(brownStar);
        secondResearch.setReport(new ResearchReport("star_research", "We have studied the star, but it is not emitting anything unusual this time. It is a typical brown dwarf of spectral class L, its optical spectrum dominated by absorption bands of FeH, CrH and prominent alkali metal lines." +
                " \n Perhaps, the source of emission is that alien structure near it. But what an immense power should it contain then? We should investigate this as soon as possible"));
        starInitialResearch.addNextResearch(secondResearch);

    }
}
