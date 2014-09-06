package ru.game.aurora.npc.ai;

import org.newdawn.slick.util.pathfinding.Path;
import ru.game.aurora.world.*;

public class MoveToTargetTask extends AITask {
    private static final long serialVersionUID = 8222614054985915774L;

    private Positionable target;

    private Path path;

    private int pathIndex = -1;

    protected MoveToTargetTask(int priority) {
        super(priority);
    }

    @Override
    public void perform(World world, GameObject myObject) {
        if (BasePositionable.getDistance(myObject, target) == 0) {
            completed = true;
            return;
        }

        final ITileMap map = world.getCurrentRoom().getMap();
        int newX, newY;

        if (path == null) {
            path = map.getPathFinder().findPath(null, myObject.getX(), myObject.getY(), target.getX(), target.getY());
        }

        if (path != null && path.getLength() > 1) {
            newX = path.getX(pathIndex);
            newY = path.getY(pathIndex);
            pathIndex++;
            if (pathIndex >= (path.getLength() - 1)) {
                path = null;
            }
        } else {
            return;
        }

        if (map.isTilePassable(newX, newY)) {
            map.setTilePassable(myObject.getX(), myObject.getY(), true);
            if (newX > myObject.getX()) {
                myObject.moveRight();
            } else if (newX < myObject.getX()) {
                myObject.moveLeft();
            } else if (newY < myObject.getY()) {
                myObject.moveUp();
            } else if (newY > myObject.getY()) {
                myObject.moveDown();
            }
        }
    }
}
