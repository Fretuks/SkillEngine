package net.fretux.skillengine.api;

import net.fretux.skillengine.skilltree.AbilityNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AbilityHandlerRegistry {

    private static final Map<ResourceLocation, AbilityHandler> BY_ID = new LinkedHashMap<>();
    private static final Map<ResourceLocation, AbilityHandler> BY_TAG = new LinkedHashMap<>();

    private AbilityHandlerRegistry() {}

    public static void register(ResourceLocation abilityId, AbilityHandler handler) {
        BY_ID.put(abilityId, handler);
    }

    public static void registerForTag(ResourceLocation tag, AbilityHandler handler) {
        BY_TAG.put(tag, handler);
    }

    public static boolean execute(ServerPlayer player, AbilityNode ability) {
        AbilityHandler direct = BY_ID.get(ability.getId());
        if (direct != null) {
            return direct.execute(player, ability);
        }
        for (ResourceLocation tag : ability.getTags()) {
            AbilityHandler tagged = BY_TAG.get(tag);
            if (tagged != null && tagged.execute(player, ability)) {
                return true;
            }
        }
        return false;
    }
}
