package ru.game.aurora.world.generation.quest.asteroidbelt;

import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.tools.ObjectPool;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.gui.FailScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.world.ITileMap;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

import java.util.*;

class AsteroidBeltEncounter implements Room {

    private static final long serialVersionUID = -8114836767522499104L;

    private final StarSystem currentStarSystem;

    // encounter data
    private EncounterParallaxBackground background;
    private Camera camera;
    private PriorityQueue<AsteroidExplosionEffect> effects;

    private int encounterLength;
    private int encounterSpeed;

    private int screenWidth;
    private int screenHeight;

    private int minBoundX;
    private int maxBoundX;
    private int minBoundY;
    private int maxBoundY;

    // encounter ship data
    private EncounterShip encounterShip;
    private int shipShiftX;
    private int shipShiftY;
    private int shipShiftSpeed;

    // encounter objects data
    private final int asteroidsPoolSize;
    private List<Asteroid> asteroidsActiveList;
    private Set<Asteroid> asteroidsFreeSet;
    private ObjectPool<Asteroid> asteroidsPool;

    private final int shootsPoolSize;
    private List<ShootEffect> shootsActiveList;
    private Set<ShootEffect> shootsFreeSet;
    private ObjectPool<ShootEffect> shootsPool;

    private final int lootPoolSize = 3;
    private float lootDropChance;
    private List<LootObject> lootActiveList;
    private Set<LootObject> lootFreeSet;
    private ObjectPool<LootObject> lootPool;

    public AsteroidBeltEncounter(StarSystem currentStarSystem){
        this.currentStarSystem = currentStarSystem;
        encounterSpeed = Configuration.getIntProperty("encounter.asteroid_belt.ship_forced_speed");
        shipShiftSpeed = Configuration.getIntProperty("encounter.asteroid_belt.ship_movement_speed");

        final int lenMin = Configuration.getIntProperty("encounter.asteroid_belt.time_sec_min") * encounterSpeed * 60; // 60 fps
        final int lenMax = Configuration.getIntProperty("encounter.asteroid_belt.time_sec_max") * encounterSpeed * 60;
        encounterLength = CommonRandom.nextInt(lenMin, lenMax);

        encounterShip = new EncounterShip();
        encounterShip.scaleCollider(0.5);

        effects = new PriorityQueue<AsteroidExplosionEffect>();

        asteroidsPoolSize = Configuration.getIntProperty("encounter.asteroid_belt.asteroids_count");
        asteroidsActiveList = new ArrayList<Asteroid>(asteroidsPoolSize);
        asteroidsFreeSet = new HashSet<Asteroid>();
        asteroidsPool = new ObjectPool<Asteroid>(asteroidsPoolSize, new ObjectPool.Factory<Asteroid>() {
            @Override
            public Asteroid createNew() {
                return new Asteroid(getTexId());
            }

            private String getTexId() {
                String texId = "asteroid_encounter_obj_1";
                switch (CommonRandom.nextInt(0, 2)){
                    case 0: texId = "asteroid_encounter_obj_1"; break;
                    case 1: texId = "asteroid_encounter_obj_2"; break;
                    case 2: texId = "asteroid_encounter_obj_3"; break;
                }

                return  texId;
            }
        });

        shootsPoolSize = Configuration.getIntProperty("encounter.asteroid_belt.shoot_ammo_count");
        shootsActiveList = new ArrayList<ShootEffect>(shootsPoolSize);
        shootsFreeSet = new HashSet<ShootEffect>();
        shootsPool = new ObjectPool<ShootEffect>(shootsPoolSize, new ObjectPool.Factory<ShootEffect>() {
            @Override
            public ShootEffect createNew() {
                return new ShootEffect(encounterSpeed + 2*shipShiftSpeed); // shoots always faster than ship
            }
        });

        lootDropChance = (float)Configuration.getDoubleProperty("encounter.asteroid_belt.ru_drop_chance");
        lootActiveList = new ArrayList<LootObject>(lootPoolSize);
        lootFreeSet = new HashSet<LootObject>();
        lootPool = new ObjectPool<LootObject>(lootPoolSize, new ObjectPool.Factory<LootObject>() {
            @Override
            public LootObject createNew() {
                return new LootObject();
            }
        });
    }
    
    @Override
    public ITileMap getMap() { return null; }
    @Override
    public double getTurnToDayRelation() { return 0; }

    @Override
    public void enter(World world) {
        guiHideElements(false);
        this.camera = world.getCamera();
        screenWidth = (int)camera.getTileWidth() * camera.getViewportTilesX();
        screenHeight = (int)camera.getTileHeight() * camera.getViewportTilesY();

        if (background == null) {
            background = new EncounterParallaxBackground(encounterLength, screenHeight, 0, 0, 3);
        }

        // encounter bounds
        minBoundX = 20;
        maxBoundX = screenWidth - 65;
        minBoundY = 80;
        maxBoundY = screenHeight - 60;

        // start position
        encounterShip.setPos(minBoundX, maxBoundY/2);
        shipShiftX = encounterShip.getX();
        shipShiftY = encounterShip.getY();
    }

    private void gameOver() {
        GUI.getInstance().getNifty().gotoScreen("fail_screen");
        FailScreenController controller = (FailScreenController) GUI.getInstance().getNifty().findScreenController(FailScreenController.class.getCanonicalName());
        controller.set("ship_destroyed_gameover", "ship_destroyed");
    }

    private void endEncounter(World world) {
        guiHideElements(true);
        world.setCurrentRoom(currentStarSystem);
        world.getCamera().resetViewPort();
        GameLogger.getInstance().logMessage(Localization.getText("journal", "asteroids.completed"));
        world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/asteroids_end.json"));
    }

    @Override
    public void returnTo(World world) {

    }

    private void guiHideElements(boolean value) {
        Element element = null;

        element = GUI.getInstance().getNifty().getCurrentScreen().findElementByName("right_menu");
        if(element != null){
            element.setVisible(value);
        }
        element = GUI.getInstance().getNifty().getCurrentScreen().findElementByName("resources_panel");
        if(element != null){
            element.setVisible(value);
        }
        element = GUI.getInstance().getNifty().getCurrentScreen().findElementByName("bottom_menu");
        if(element != null) {
            element.setVisible(value);
        }
    }

    @Override
    public void update(GameContainer container, World world) {
        if(world.getPlayer().getShip().getHull() <= 0) {
            gameOver();
            return;
        }

        if(camera.getViewportX() + screenWidth > encounterLength){
            endEncounter(world);
            return;
        }

        updateMovement(container.getInput(), world);
        updateShoot(container);
        updateEnvironment(world);
        updateEffects(container, world);
        reuseAsteroids();
    }

    private void updateMovement(Input input, World world) {
        int dx = 0;
        int dy = 0;

        // movement
        if(input.isKeyDown(InputBinding.keyBinding.get(InputBinding.Action.UP)) || input.isKeyDown(InputBinding.keyBinding.get(InputBinding.Action.UP_SECONDARY))){
            dy -= shipShiftSpeed;
        }
        else if(input.isKeyDown(InputBinding.keyBinding.get(InputBinding.Action.DOWN)) || input.isKeyDown(InputBinding.keyBinding.get(InputBinding.Action.DOWN_SECONDARY))){
            dy += shipShiftSpeed;
        }

        if(input.isKeyDown(InputBinding.keyBinding.get(InputBinding.Action.LEFT)) || input.isKeyDown(InputBinding.keyBinding.get(InputBinding.Action.LEFT_SECONDARY))){
            dx -= shipShiftSpeed * 2;
        }
        else if(input.isKeyDown(InputBinding.keyBinding.get(InputBinding.Action.RIGHT)) || input.isKeyDown(InputBinding.keyBinding.get(InputBinding.Action.RIGHT_SECONDARY))){
            dx += shipShiftSpeed;
        }

        // bounds check
        if(Math.abs(dx) > 0) {
            if(shipShiftX + dx < minBoundX){
                dx = 0;
            }

            if(shipShiftX + dx > maxBoundX){
                dx = 0;
            }
        }
        if(Math.abs(dy) > 0) {
            if(shipShiftY + dy < minBoundY){
                dy = 0;
            }

            if(shipShiftY + dy > maxBoundY){
                dy = 0;
            }
        }

        shipShiftX += dx;
        shipShiftY += dy;

        encounterShip.setPos(encounterShip.getX() + dx + encounterSpeed, encounterShip.getY() + dy);
        camera.setViewportX(camera.getViewportX() + encounterSpeed);
    }

    private void updateShoot(GameContainer container) {
        // check cooldown
        if(!encounterShip.readyToShoot(container)){
            return;
        }

        Input input = container.getInput();
        if(input.isKeyDown(InputBinding.keyBinding.get(InputBinding.Action.INTERACT)) || input.isKeyDown(InputBinding.keyBinding.get(InputBinding.Action.INTERACT_SECONDARY))){
            if(shootsActiveList.size() < shootsPoolSize){
                encounterShip.shootSetCooldown(container);

                final int x = encounterShip.getX() + encounterShip.getImage().getWidth();
                final int y = encounterShip.getY() + encounterShip.getImage().getHeight() / 2;

                ShootEffect laser = shootsPool.allocate();
                laser.setPos(x, y);
                shootsActiveList.add(laser);
            }
        }
    }

    private void updateEnvironment(World world) {
        // update laser beams
        if(!shootsActiveList.isEmpty()) {
            for (ShootEffect shoot : shootsActiveList) {
                shoot.updateMovement();

                if (shoot.getX() > camera.getViewportX() + screenWidth) {
                    shootsFreeSet.add(shoot);
                }
            }
        }

        // update asteroids pos
        for(int i = 0; i < asteroidsActiveList.size(); ++i){
            Asteroid asteroid = asteroidsActiveList.get(i);
            asteroid.updateMovement();

            // check collision
            if(asteroid.collision(encounterShip)){
                world.getPlayer().getShip().onAttack(world, null, 1); // deal damage
                asteroidsFreeSet.add(asteroid);
            }
            else if(asteroid.getX() < camera.getViewportX() - asteroid.getImage().getWidth()){
                asteroidsFreeSet.add(asteroid);
            }
            else{
                // check asteroid destroy by shoot
                if(shootsActiveList.size() > 0) {
                    for (ShootEffect shoot : shootsActiveList) {
                        if (shoot.collision(asteroid)) {
                            shootsFreeSet.add(shoot);
                            asteroidsFreeSet.add(asteroid);
                            dropLoot(asteroid);
                            effects.add(new AsteroidExplosionEffect(asteroid.getX(), asteroid.getY(), "rocket_explosion"));
                        }
                    }
                }
            }
        }

        // update loot objects
        if(!lootActiveList.isEmpty()){
            for(int i = 0; i < lootActiveList.size(); ++i){
                LootObject loot = lootActiveList.get(i);

                if(loot.collision(encounterShip)){
                    lootFreeSet.add(loot);
                    pickLoot(world);
                }
                else if(loot.getX() < camera.getViewportX() - loot.getImage().getWidth()){
                    lootFreeSet.add(loot);
                }
            }
        }

        freeObjects();
    }

    private void updateEffects(GameContainer container, World world) {
        if(!effects.isEmpty()){
            List<AsteroidExplosionEffect> freeList = new ArrayList<AsteroidExplosionEffect>(effects.size());

            for(AsteroidExplosionEffect effect: effects){
                if(effect.isOver()){
                    freeList.add(effect);
                }
                else if(effect.isAlive()){
                    effect.update(container, world);
                }
            }

            if(!freeList.isEmpty()){
                effects.removeAll(freeList);
            }
        }
    }

    private void freeObjects() {
        if(!asteroidsFreeSet.isEmpty()){
            for(Asteroid asteroid: asteroidsFreeSet){
                asteroidsActiveList.remove(asteroid);
                asteroidsPool.free(asteroid);
            }
            asteroidsFreeSet.clear();
        }

        if(!shootsFreeSet.isEmpty()){
            for(ShootEffect shoot: shootsFreeSet){
                shootsActiveList.remove(shoot);
                shootsPool.free(shoot);
            }
            shootsFreeSet.clear();
        }

        if(!lootFreeSet.isEmpty()){
            for(LootObject lootObj: lootFreeSet) {
                lootActiveList.remove(lootObj);
                lootPool.free(lootObj);
            }
            lootFreeSet.clear();
        }
    }

    private void reuseAsteroids() {
        if(asteroidsActiveList.size() < asteroidsPoolSize){
            // check encounter ending
            if(camera.getViewportX() < encounterLength - screenWidth *2){
                if(CommonRandom.getRandom().nextFloat() < 0.1f){ // 10% chance to launch - check every frame
                    Asteroid asteroid = asteroidsPool.allocate();
                    asteroid.resetParams(camera.getViewportX(), screenWidth, minBoundY, maxBoundY);
                    asteroidsActiveList.add(asteroid);
                }
            }
        }
    }

    private void dropLoot(Asteroid asteroid) {
        if(CommonRandom.getRandom().nextFloat() < lootDropChance){
            if(lootActiveList.size() < lootPoolSize){
                LootObject loot = lootPool.allocate();
                loot.setPos(asteroid.getX(), asteroid.getY());
                lootActiveList.add(loot);
            }
        }
    }

    private void pickLoot(World world) {
        world.getPlayer().setResourceUnits(world.getPlayer().getResourceUnits() + 1);
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
        background.draw(graphics, camera);

        for(Asteroid asteroid: asteroidsActiveList){
            asteroid.draw(container, graphics, camera, world);
        }

        if(!lootActiveList.isEmpty()) {
            for (LootObject loot : lootActiveList) {
                loot.draw(container, graphics, camera, world);
            }
        }

        if(!effects.isEmpty()){
            for(AsteroidExplosionEffect effect: effects){
                effect.draw(container, graphics, camera, world);
            }
        }

        if(!shootsActiveList.isEmpty()) {
            for (ShootEffect laser : shootsActiveList) {
                laser.draw(container, graphics, camera, world);
            }
        }

        encounterShip.draw(container, graphics, camera, world);
    }
}