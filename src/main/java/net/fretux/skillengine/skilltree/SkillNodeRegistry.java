package net.fretux.skillengine.skilltree;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class SkillNodeRegistry {

    private static final Map<ResourceLocation, SkillNode> NODES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Map<ResourceLocation, SkillNode>> NODES_BY_TREE = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Integer> DUPLICATE_COUNTS = new LinkedHashMap<>();

    public static void clear() {
        NODES.clear();
        NODES_BY_TREE.clear();
        DUPLICATE_COUNTS.clear();
    }

    public static void put(SkillNode node) {
        SkillNode previous = NODES.put(node.getId(), node);
        if (previous != null) {
            DUPLICATE_COUNTS.merge(node.getId(), 1, Integer::sum);
            Map<ResourceLocation, SkillNode> previousTree = NODES_BY_TREE.get(previous.getTree());
            if (previousTree != null) {
                previousTree.remove(previous.getId());
                if (previousTree.isEmpty()) {
                    NODES_BY_TREE.remove(previous.getTree());
                }
            }
        }
        NODES_BY_TREE.computeIfAbsent(node.getTree(), ignored -> new LinkedHashMap<>())
                .put(node.getId(), node);
    }

    public static SkillNode get(ResourceLocation id) {
        return NODES.get(id);
    }

    public static Collection<SkillNode> all() {
        return NODES.values();
    }

    public static List<ResourceLocation> trees() {
        return List.copyOf(NODES_BY_TREE.keySet());
    }

    public static Collection<SkillNode> byTree(ResourceLocation tree) {
        Map<ResourceLocation, SkillNode> nodes = NODES_BY_TREE.get(tree);
        return nodes != null ? nodes.values() : List.of();
    }

    public static Map<ResourceLocation, Integer> duplicateCounts() {
        return Map.copyOf(DUPLICATE_COUNTS);
    }
}
