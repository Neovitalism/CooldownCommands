package me.neovitalism.cooldowncommands.cooldowns;

import me.neovitalism.neoapi.config.Configuration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CooldownManager {
    private static final Map<String, CooldownCommand> COMMAND_COOLDOWNS = new HashMap<>();

    public static void reload(Configuration config) {
        CooldownManager.COMMAND_COOLDOWNS.clear();
        Configuration cooldownSection = config.getSection("cooldowns");
        if (cooldownSection == null) return;
        for (String key : cooldownSection.getKeys()) {
            String lowercase = key.toLowerCase(Locale.ENGLISH);
            Configuration section = cooldownSection.getSection(key);
            if (section == null) continue;
            CooldownManager.COMMAND_COOLDOWNS.put(lowercase, new CooldownCommand(lowercase, section));
        }
    }

    public static CooldownCommand getCooldownCommand(String usedCommand) {
        String lowercase = usedCommand.toLowerCase(Locale.ENGLISH);
        for (String key : CooldownManager.COMMAND_COOLDOWNS.keySet()) {
            if (lowercase.startsWith(key)) return CooldownManager.COMMAND_COOLDOWNS.get(key);
        }
        return null;
    }
}
