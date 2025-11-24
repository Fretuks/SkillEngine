package net.fretux.skillengine.skilltree;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class SkillNodeRegistry {

    private static final Map<ResourceLocation, SkillNode> NODES = new LinkedHashMap<>();

    public static void clear() {
        NODES.clear();
    }

    public static void put(SkillNode node) {
        NODES.put(node.getId(), node);
    }

    public static SkillNode get(ResourceLocation id) {
        return NODES.get(id);
    }

    public static Collection<SkillNode> all() {
        return NODES.values();
    }
}
