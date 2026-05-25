package net.fretux.skillengine.api;

import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.skillengine.skilltree.SkillNode;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PrerequisiteProviderRegistry {

    private static final Map<String, PrerequisiteProvider> PROVIDERS = new LinkedHashMap<>();

    static {
        register("ascend", (player, node, key, requiredValue) ->
                player.getCapability(PlayerStatsProvider.PLAYER_STATS)
                        .map(stats -> stats.getAttributeLevel(key) >= requiredValue)
                        .orElse(false));
    }

    private PrerequisiteProviderRegistry() {}

    public static void register(String namespace, PrerequisiteProvider provider) {
        PROVIDERS.put(namespace, provider);
    }

    public static boolean meets(ServerPlayer player, SkillNode node, String key, int requiredValue) {
        int separator = key.indexOf(':');
        String namespace = separator > 0 ? key.substring(0, separator) : "ascend";
        String localKey = separator > 0 ? key.substring(separator + 1) : key;
        PrerequisiteProvider provider = PROVIDERS.get(namespace);
        return provider != null && provider.meets(player, node, localKey, requiredValue);
    }

    public static boolean hasProvider(String namespace) {
        return PROVIDERS.containsKey(namespace);
    }
}
