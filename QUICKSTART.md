# Quick Start Guide - PVP Arena Plugin

## Installation Steps

### 1. Build the Plugin
```bash
cd PVPArenaPlugin
./build.sh
```

Or manually with Maven:
```bash
mvn clean package
```

### 2. Install on Server
1. Copy `target/PVPArenaPlugin-1.0-SNAPSHOT.jar` to your server's `plugins/` folder
2. Create a world named "pvp" (or configure a different name in config.yml)
3. Restart your server

### 3. Create PVP World (if needed)

**Using Multiverse:**
```
/mv create pvp normal
```

**Using default Bukkit:**
- Create a folder named `pvp` in your server directory
- Server will generate it on restart

### 4. Configure the Plugin

Edit `plugins/PVPArenaPlugin/config.yml`:

```yaml
pvp-world-name: "pvp"          # Name of your PVP world
arena-spacing: 1000            # Distance between arenas
arena-cleanup-delay: 30        # Seconds before arena deletion
countdown-duration: 3          # Match countdown
```

### 5. Give Players the Compass

```
/pvparena give
```

Or use a join event to auto-give on player join.

## Player Usage

1. **Right-click the compass** to open the game mode menu
2. **Click a game mode** to join the queue
3. **Wait for a match** - you'll be notified when one is found
4. **Fight!** - Winner stays, loser leaves
5. **Automatic cleanup** - Arena deletes after match ends

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/pvparena` | Show help | `pvparena.admin` |
| `/pvparena reload` | Reload config | `pvparena.admin` |
| `/pvparena debug` | Show debug info | `pvparena.admin` |
| `/pvparena give` | Get compass | `pvparena.admin` |

## Testing the Setup

1. Give yourself the compass: `/pvparena give`
2. Right-click the compass
3. Select a game mode
4. Have another player (or use a second account) do the same
5. You should both be teleported to an arena
6. After 3 seconds, the match starts
7. When one player dies, both return to spawn and arena deletes

## Common Issues

### "PVP world does not exist"
- Create the world using a world management plugin
- Or change `pvp-world-name` in config.yml to an existing world

### Players not being matched
- Verify both players selected the SAME game mode
- Check `/pvparena debug` to see queue status
- Ensure no errors in console

### Arena not spawning
- Verify the PVP world exists and is loaded
- Check that `arena-spacing` isn't too large (default: 1000)
- Look for errors in server console

### Kit items not appearing
- Verify kit names match between `game-modes` and `kits` sections
- Check material names are valid (UPPERCASE, e.g., DIAMOND_SWORD)
- For potions, ensure potion-type is correct

## Adding Custom Game Modes

Edit `config.yml`:

```yaml
game-modes:
  your-mode:
    display-name: "&dYour Custom Mode"
    material: NETHERITE_SWORD
    slot: 3
    kit: "your-kit"

kits:
  your-kit:
    armor:
      helmet: NETHERITE_HELMET
      chestplate: NETHERITE_CHESTPLATE
      leggings: NETHERITE_LEGGINGS
      boots: NETHERITE_BOOTS
    items:
      - type: NETHERITE_SWORD
        amount: 1
      - type: ENCHANTED_GOLDEN_APPLE
        amount: 5
```

Then reload: `/pvparena reload`

## Performance Tips

- Use a void world for the PVP world to improve performance
- Adjust `arena-spacing` based on your server capacity (lower = more reuse)
- Monitor with `/pvparena debug` to see arena usage
- Consider pre-generating the PVP world to avoid lag

## Support

For issues or questions:
1. Check the README.md for detailed documentation
2. Review server console for error messages
3. Use `/pvparena debug` to check system status
4. Verify configuration syntax in config.yml
