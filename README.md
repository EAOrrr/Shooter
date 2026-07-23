# Java Shooter

A 2D arcade-style shooter built with Java Swing.

Key features:
- 60 FPS game loop with delta-time updates
- Auto-firing player with keyboard movement controls
- Multiple enemy archetypes and multi-phase boss fights
- Reward drops and weapon progression system
- JUnit 5 tests for core gameplay logic

## Requirements

- JDK 17+
- Maven 3.8+
- Linux / macOS / Windows

## Quick Start

1. Compile

```bash
mvn clean compile
```

2. Run the game

```bash
mvn exec:java
```

3. Run tests

```bash
mvn test
```

## Controls

Basic:
- `W/A/S/D` or arrow keys: move the player

Debug shortcuts:
- `F1`: toggle invincibility
- `F2`: upgrade weapon (switch to spread shot)
- `F3`: show/hide hitboxes
- `F4`: force-spawn the boss
- `R`: restart after game over

## Gameplay Mechanics

- Player:
	- Starts with an auto-firing single-shot weapon
	- Rewards can improve fire rate, stream count, and damage

- Enemies and Boss:
	- Regular enemies spawn as different types based on weighted chance
	- A boss appears once the score threshold is reached
	- The boss switches to a stronger attack pattern in low-HP phase

- Reward drops (random):
	- `HEAL`: restores HP
	- `RAPID_FIRE`: increases fire rate (reduces cooldown)
	- `MULTI_SHOT`: increases parallel bullet streams
	- `POWER_SHOT`: increases bullet damage

## Project Structure

```text
src/main/java/
	Main.java            # Entry point
	GamePanel.java       # Main loop, spawning, collision, rendering
	Player.java          # Player entity and weapon upgrades
	Enemy*.java          # Enemy implementations (including boss)
	Weapon*.java         # Weapon implementations and stat model
	RewardItem.java      # Reward drops
	KeyInput.java        # Input and debug hotkeys

src/test/java/
	*Test.java           # Gameplay and logic tests

docs/
	*.md                 # Development tasks and milestone notes
```

## FAQ

- The game window opens, but movement does not work:
	- Click the game window to focus it, then use arrow keys or `W/A/S/D`.

- `mvn exec:java` fails to start:
	- Make sure you are using JDK 17+ and run `mvn -v` to verify Java and Maven versions.

## Possible Next Improvements

- Add bullet-hell enemy patterns and more boss abilities
- Add audio, sprites, and richer animations
- Add a pause menu, difficulty options, and save data
- Add CI (for example, GitHub Actions) to run tests automatically
