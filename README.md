# CooldownCommands  
A mod used to manage cooldowns on specific commands.  
  
## Usage  
Adding a command to the config makes the mod watch for that command.  
```yaml
cooldowns:
  # The command to watch for.
  levelup:
    # The default cooldown.
    cooldown-seconds: 600
    # The message given when the player is on cooldown.
    cooldown-message: "&c<bold>(!)</bold> This command is on cooldown for another {time-formatted}."
    # Whether or not the cooldown applies on use of the command, or only via /placecooldown.
    applies-on-use: true
```
This config will look for any usage of the `/levelup` command provided by Cobblemon.  
  
You can add further arguments like `levelup 1` to force a specific cooldown on that specific argument, like so:  
```yaml
cooldowns:
  "levelup 1":
    ...
```
  
For rank or player specific cooldowns, set the meta `cooldowncommands.[key].cooldown` with the amount of seconds.  
Example: `/lp user Neovitalism meta set cooldowncommands.levelup.cooldown 30`  
  
To bypass cooldowns, give the permission `cooldowncommands.[key].bypass`.  
Example: `/lp user Neovitalism permission set cooldowncommands.levelup.bypass`  
  
To force a cooldown, use `/placecooldown [player] [command] <seconds>`. Permission: `cooldowncommands.placecooldown`.  
Without the <seconds> arg, it'll use the default cooldown suited for that player.  
Example: `/placecooldown Neovitalism levelup 60`  
  
To remove a cooldown, use `/removecooldown [player] [command]`. Permission: `cooldowncommands.removecooldown`.  
Example: `/removecooldown Neovitalism levelup`
