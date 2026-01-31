# PVP Arena Plugin

A fully-featured Minecraft PVP matchmaking system with dynamic arenas, queue system, and customizable game modes.

## Features

- **Compass GUI System**: Players receive a compass item that opens a GUI for game mode selection
- **Matchmaking Queue**: Automatic matching when two players queue for the same game mode
- **Dynamic Arenas**: Arenas spawn automatically in the PVP world with configurable spacing
- **Multiple Game Modes**: Support for unlimited custom game modes (NoDebuff, UHC, Combo, etc.)
- **Customizable Kits**: Configure armor and items for each game mode
- **Match Flow**: Automatic teleportation, countdown timer, and match management
- **Arena Cleanup**: Arenas are automatically deleted after matches with coordinate cooldown
- **World Isolation**: Chat is isolated per world - players only see messages from their world
- **Thread-Safe**: No concurrent operations on shared resources

## Requirements

- Spigot/Paper 1.20.4 or higher
- Java 17 or higher
- A world named "pvp" (or configure a different name)

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins/` folder
3. Create or ensure you have a world named "pvp" (or configure in config.yml)
4. Restart your server
5. Configure `config.yml` to your preferences
6. Use `/pvparena give` to get the arena compass

## Building from Source

```bash
# Clone the repository
git clone <repository-url>
cd PVPArenaPlugin

# Build with Maven
mvn clean package

# The JAR file will be in target/PVPArenaPlugin-1.0-SNAPSHOT.jar
```

## Configuration

### Main Settings (config.yml)

```yaml
arena-spacing: 1000           # Blocks between arenas
arena-cleanup-delay: 30       # Seconds before arena deletion
countdown-duration: 3         # Match countdown in seconds
pvp-world-name: "pvp"        # Name of PVP world
```

### Adding Game Modes

```yaml
game-modes:
  your-mode:
    display-name: "&aYour Mode"
    material: DIAMOND_SWORD
    slot: 0
    kit: "your-kit-name"
```

### Creating Kits

```yaml
kits:
  your-kit-name:
    armor:
      helmet: DIAMOND_HELMET
      chestplate: DIAMOND_CHESTPLATE
      leggings: DIAMOND_LEGGINGS
      boots: DIAMOND_BOOTS
    items:
      - type: DIAMOND_SWORD
        amount: 1
      - type: ENDER_PEARL
        amount: 16
```

### Arena Structure

```yaml
arena:
  size: 50                    # Arena size (50x50)
  floor-y: 64                 # Floor Y level
  spawn-points:
    point1:
      x: 15
      y: 65
      z: 0
    point2:
      x: -15
      y: 65
      z: 0
  blocks:
    floor:
      material: STONE
      y: 64
    barrier:
      material: BARRIER
      height: 10
```

## Commands

- `/pvparena` - Show command help
- `/pvparena reload` - Reload configuration
- `/pvparena debug` - Show debug information (queues, matches, arenas)
- `/pvparena give` - Get the arena compass item

## Permissions

- `pvparena.admin` - Access to admin commands (default: op)
- `pvparena.use` - Use the arena system (default: true)

## How It Works

### Player Flow

1. Player receives/gets compass item
2. Right-clicks compass to open game mode GUI
3. Selects a game mode to join queue
4. When another player joins the same queue, match is created
5. Both players are teleported to dynamically spawned arena
6. 3-second countdown begins
7. Match starts, players fight
8. When one player dies, match ends
9. Both players teleport back to spawn (0, 0, 0)
10. Arena is deleted after 30 seconds

### Arena Management

- Arenas spawn at least 1000 blocks apart (configurable)
- Each arena has barriers and a floor
- Coordinates are tracked to prevent overlap
- After deletion, coordinates enter 30-second cooldown before reuse
- Multiple matches can run simultaneously without interference

### Chat Isolation

- Players only see chat from others in the same world
- Prevents cross-world communication
- Maintains immersion during matches

## Troubleshooting

### "PVP world does not exist" warning

Create a world named "pvp" or change the world name in config.yml:
```yaml
pvp-world-name: "your-world-name"
```

### Arenas not spawning

1. Check that the PVP world exists and is loaded
2. Verify arena spacing isn't too large
3. Check console for errors
4. Use `/pvparena debug` to see available coordinates

### Players not being matched

1. Verify both players selected the same game mode
2. Check that game modes are properly configured
3. Ensure no errors in console
4. Use `/pvparena debug` to see queue status

### Kits not working

1. Verify kit names match between game mode and kit configuration
2. Check material names are valid (must be uppercase, e.g., DIAMOND_SWORD)
3. For potions, ensure potion-type is correct (e.g., INSTANT_HEAL)

## API Usage

### Checking if Player is in Match

```java
PVPArenaPlugin plugin = PVPArenaPlugin.getInstance();
if (plugin.getMatchManager().isInMatch(player)) {
    // Player is currently in a match
}
```

### Getting Player's Match

```java
Match match = plugin.getMatchManager().getMatch(player.getUniqueId());
if (match != null) {
    // Access match data
    Arena arena = match.getArena();
    String gameMode = match.getGameMode();
}
```

## Support

For issues, questions, or suggestions, please create an issue on the project repository.

## License

This project is provided as-is for use in Minecraft servers.

## Credits

Developed for PVP server matchmaking systems.
