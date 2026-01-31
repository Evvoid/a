# PVP Arena System - MVP Requirements

## Core Features

### 1. Queue System
- **Compass Item**: Players receive a compass that opens a GUI when clicked
- **GUI Menu**: Displays available game modes for selection
- **Matchmaking**: When two players select the same game mode, they are matched together
- **Queue Management**: Players wait in queue until a match is found

### 2. Arena Management
- **Dynamic Arena Creation**: Arenas spawn in the "pvp" world when a match starts
- **Arena Spacing**: Each arena is at least 1000 blocks apart from others
- **Arena Cleanup**: Arenas are deleted after the match ends
- **Coordinate Reuse**: Arena coordinates can be reused 30 seconds after deletion
- **Spawn Points**: Each arena has 2 spawn points for the competing players

### 3. Match Flow
1. Players queue via compass GUI
2. When matched, both players are teleported to the pvp world
3. Arena spawns at available coordinates
4. Players are teleported to opposite spawn points in the arena
5. Kit items are given to both players
6. 3-second countdown timer
7. Match begins
8. When one player dies, match ends
9. Both players teleport back to main world at coordinates (0, 0, 0)
10. Arena is deleted after 30 seconds (coordinates become available for reuse)

### 4. World Isolation
- **Chat Isolation**: Players only see chat messages from players in the same world
- **Thread Safety**: No concurrent operations on the same resources

### 5. Configuration
- Configurable game modes
- Configurable kits per game mode
- Configurable arena structures
- Configurable spawn points
- Configurable timers and delays
- Configurable arena spacing

## Technical Architecture

### Plugin Structure
```
PVPArenaPlugin/
├── src/main/java/com/pvparena/
│   ├── PVPArenaPlugin.java (Main plugin class)
│   ├── managers/
│   │   ├── QueueManager.java (Queue system)
│   │   ├── ArenaManager.java (Arena lifecycle)
│   │   ├── MatchManager.java (Match flow)
│   │   └── ConfigManager.java (Configuration)
│   ├── models/
│   │   ├── GameMode.java
│   │   ├── Arena.java
│   │   ├── Match.java
│   │   └── Kit.java
│   ├── listeners/
│   │   ├── CompassClickListener.java
│   │   ├── PlayerDeathListener.java
│   │   └── ChatListener.java
│   └── gui/
│       └── GameModeGUI.java
├── src/main/resources/
│   ├── plugin.yml
│   ├── config.yml
│   └── arenas.yml
└── pom.xml
```

### Key Components

#### Queue Manager
- Manages player queues for each game mode
- Matches players when two are in the same queue
- Thread-safe queue operations

#### Arena Manager
- Tracks available arena coordinates
- Creates arenas dynamically
- Manages arena lifecycle (spawn, active, cleanup, cooldown)
- Ensures 1000+ block spacing
- Handles coordinate reuse after 30-second cooldown

#### Match Manager
- Controls match state (waiting, countdown, active, ended)
- Handles player teleportation
- Gives kits to players
- Manages countdown timer
- Detects match end conditions
- Coordinates cleanup

#### Config Manager
- Loads and manages plugin configuration
- Provides access to game modes, kits, and arena settings

## Configuration Files

### config.yml
```yaml
# Main configuration
arena-spacing: 1000
arena-cleanup-delay: 30
countdown-duration: 3
pvp-world-name: "pvp"
main-world-spawn:
  x: 0
  y: 64
  z: 0

# Game modes
game-modes:
  no-debuff:
    display-name: "&cNo Debuff"
    material: DIAMOND_SWORD
    kit: "nodebuff"
  uhc:
    display-name: "&6UHC"
    material: GOLDEN_APPLE
    kit: "uhc"
  combo:
    display-name: "&eCombo"
    material: IRON_SWORD
    kit: "combo"

# Kits
kits:
  nodebuff:
    armor:
      - DIAMOND_HELMET
      - DIAMOND_CHESTPLATE
      - DIAMOND_LEGGINGS
      - DIAMOND_BOOTS
    items:
      - type: DIAMOND_SWORD
        amount: 1
      - type: ENDER_PEARL
        amount: 16
      - type: SPLASH_POTION
        amount: 36
        potion-type: INSTANT_HEAL_II
  uhc:
    armor:
      - DIAMOND_HELMET
      - DIAMOND_CHESTPLATE
      - DIAMOND_LEGGINGS
      - DIAMOND_BOOTS
    items:
      - type: DIAMOND_SWORD
        amount: 1
      - type: BOW
        amount: 1
      - type: ARROW
        amount: 64
      - type: GOLDEN_APPLE
        amount: 8
  combo:
    armor:
      - IRON_HELMET
      - IRON_CHESTPLATE
      - IRON_LEGGINGS
      - IRON_BOOTS
    items:
      - type: IRON_SWORD
        amount: 1
```

### arenas.yml
```yaml
# Arena structure configuration
arenas:
  basic:
    size: 50
    spawn-points:
      - x: 10
        y: 64
        z: 10
      - x: -10
        y: 64
        z: -10
    blocks:
      - material: STONE
        positions:
          - x: 0, y: 63, z: 0, width: 50, length: 50
      - material: BARRIER
        positions:
          - x: -25, y: 64, z: -25, height: 10, type: wall
```

## Deliverables
1. Complete plugin source code
2. Configuration files with examples
3. README with setup instructions
4. Build instructions
