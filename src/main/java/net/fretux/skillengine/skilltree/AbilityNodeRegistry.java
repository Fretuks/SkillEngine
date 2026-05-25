package net.fretux.skillengine.skilltree;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class AbilityNodeRegistry {
    private static final Map<ResourceLocation, AbilityNode> ABILITIES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Integer> DUPLICATE_COUNTS = new LinkedHashMap<>();

    public static void clear() {
        ABILITIES.clear();
        DUPLICATE_COUNTS.clear();
    }

    public static void put(AbilityNode node) {
        if (ABILITIES.put(node.getId(), node) != null) {
            DUPLICATE_COUNTS.merge(node.getId(), 1, Integer::sum);
        }
    }

    public static AbilityNode get(ResourceLocation id) {
        return ABILITIES.get(id);
    }

    public static Collection<AbilityNode> all() {
        return ABILITIES.values();
    }

    public static Map<ResourceLocation, Integer> duplicateCounts() {
        return Map.copyOf(DUPLICATE_COUNTS);
    }
}
