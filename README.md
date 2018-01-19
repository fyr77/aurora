# README #

Aurora - a space exploration roguelike game, inspired by Star Control 2, Prospector and Space Rangers.
Take command of a large scout spaceship, with dozens of scientists, engineers and marines on board, and start your journey in outer space. Collect everything valuable and bring back to Earth, for humanity sake.
Game is available in Steam: http://store.steampowered.com/app/437890/AuroraRL/ 

### License ###

The game source is open-source and distributed under Apache license.
The game resources (music, sounds, art etc) can not be used outside of this project.

### Contribution ###

If you want to contribute, you can create a fork and then send me a pull request.
There are no specific code conventions here, however code should be clean and commented where necessary.

#### Prerequisites ####

Aurora requires Java 7 to run and develop.
Gradle build system is used. Use gradle assemble task to build a full distribution zip file with all the core, resources and scripts.

#### Main Libraries ####

* Slick2D game engine http://slick.ninjacave.com - wrapper for LWJGL engine, no longer maintained.
* NiftyGUI library for building hud and menus
* Frankenstein library for creating monster images from parts https://bitbucket.org/e_smirnov/frankenstein/wiki/Home

#### Entry point ####

Check AuroraGame.main() method. You will need to pass a -noSteam argument to it in order to run game from your IDE without Steam.

#### Basic code structure ####

Main object is a World class which contains all game related information. Saved games are just serialized (with standard
java serialization) World objects.

World contains information about Player (ship status, journal with quests, state of Earth and humanity research)

Game locations are represented as Room subclasses. Room knows how to draw itself and update. There is one current room that is stored in World (the one seen on screen),
but rooms can form a stack-like structure. E.g. a Galaxy Map is a room, where every star leads to a StarSystem room, where each planet is a Planet room, and on the 
surface there may be entrances to Dungeon rooms. 

Rooms are highly extensible, actually you may implement any arbitrary mini-game as a Room. You may see AsteroidBeltEncounter which is a simple side-scroller, or a ParallelWorld where
enemies move in real-time instead of waiting for their turns.

Most game logic is written as GameEventListeners. Listeners are stored in the World and listen to specific game events like 'player entered room' or 'player ship was attacked'.
E.g. a typical random encounter listens for 'enter star system' event and with some probability spawns a quest spaceship in that star system.

Game is configured via game.properties that contains both general game settings (like galaxy size) and individual quest parameters (like quest npc spawn chances).

Dialog system is pretty complicated. XLSX files from 'dialog sources' dir are processed by BatchDialogConverter utility into sets of .json files with dialog structure and 
 .properties files with localized texts.

### Where to start ###

- Check our issue tracker https://bitbucket.org/e_smirnov/aurora/issues?status=new&status=open
- Feel free to add any improvements and you think will make the game better
- Write to aurorateam@auroraroguelike.com and we will advise on how to help the project 

### Special thanks to ###

[JProfiler java profiler](http://www.ej-technologies.com/products/jprofiler/overview.html) and
[Install4j, a multi-platform installer builder](http://www.ej-technologies.com/products/install4j/overview.html) for providing a free open-source licenses for our project.