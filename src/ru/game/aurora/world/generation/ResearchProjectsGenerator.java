package ru.game.aurora.world.generation;

import ru.game.aurora.world.World;
import ru.game.aurora.world.research.AlienAlloysResearch;

/**
 * Creates some general research projects
 */
public class ResearchProjectsGenerator implements WorldGeneratorPart
{
    @Override
    public void updateWorld(World world) {
        world.getResearchAndDevelopmentProjects().getResearchProjects().put("loot.materials", new AlienAlloysResearch());
    }
}
