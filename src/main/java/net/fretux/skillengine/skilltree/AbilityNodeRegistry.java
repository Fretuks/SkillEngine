package net.fretux.skillengine.skilltree;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class AbilityNodeRegistry {
    private static final Map<ResourceLocation, AbilityNode> ABILITIES = new LinkedHashMap<>();
    public static void clear() {
        ABILITIES.clear();
    }
    public static void put(AbilityNode node) {
        ABILITIES.put(node.getId(), node);
    }
    public static AbilityNode get(ResourceLocation id) {
        return ABILITIES.get(id);
    }
    public static Collection<AbilityNode> all() {
        return ABILITIES.values();
    }
}
