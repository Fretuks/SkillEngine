package net.fretux.skillengine.skilltree;

import net.fretux.skillengine.api.PrerequisiteProviderRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SkillTreeValidator {

    private SkillTreeValidator() {}

    public static List<String> validate() {
        List<String> issues = new ArrayList<>();
        SkillNodeRegistry.duplicateCounts().forEach((id, count) ->
                issues.add(id + " was loaded " + (count + 1) + " times; node ids must be unique"));
        AbilityNodeRegistry.duplicateCounts().forEach((id, count) ->
                issues.add(id + " was loaded " + (count + 1) + " times; ability ids must be unique"));
        for (SkillNode node : SkillNodeRegistry.all()) {
            validateNode(node, issues);
        }
        for (AbilityNode ability : AbilityNodeRegistry.all()) {
            validateAbility(ability, issues);
        }
        detectSkillCycles(issues);
        detectAbilityCycles(issues);
        return issues;
    }

    private static void validateNode(SkillNode node, List<String> issues) {
        if (!isPngTexture(node.getIcons())) {
            issues.add(node.getId() + " has invalid icon path " + node.getIcons() + " (expected a .png texture)");
        }
        for (ResourceLocation parent : node.getLinks()) {
            if (SkillNodeRegistry.get(parent) == null) {
                issues.add(node.getId() + " links to missing skill node " + parent);
            }
        }
        for (ResourceLocation exclusive : node.getExclusiveWith()) {
            SkillNode other = SkillNodeRegistry.get(exclusive);
            if (other == null) {
                issues.add(node.getId() + " is exclusive with missing node " + exclusive);
            } else if (exclusive.equals(node.getId())) {
                issues.add(node.getId() + " is exclusive with itself");
            } else if (!other.getExclusiveWith().contains(node.getId())) {
                issues.add(node.getId() + " is exclusive with " + exclusive + " but the reverse entry is missing");
            }
        }
        Set<ResourceLocation> requiredSkillNodes = collectRequiredSkillNodes(node);
        for (ResourceLocation exclusive : node.getExclusiveWith()) {
            if (requiredSkillNodes.contains(exclusive)) {
                issues.add(node.getId() + " is impossible to unlock because required ancestor " + exclusive + " is mutually exclusive");
            }
        }
        List<ResourceLocation> requiredList = new ArrayList<>(requiredSkillNodes);
        for (int i = 0; i < requiredList.size(); i++) {
            SkillNode first = SkillNodeRegistry.get(requiredList.get(i));
            if (first == null) continue;
            for (int j = i + 1; j < requiredList.size(); j++) {
                ResourceLocation second = requiredList.get(j);
                if (first.getExclusiveWith().contains(second)) {
                    issues.add(node.getId() + " is impossible to unlock because required nodes "
                            + first.getId() + " and " + second + " are mutually exclusive");
                }
            }
        }
        for (String key : node.getPrereqAttributes().keySet()) {
            int separator = key.indexOf(':');
            if (separator >= 0 && (separator == 0 || separator == key.length() - 1)) {
                issues.add(node.getId() + " has malformed prerequisite key " + key);
                continue;
            }
            String namespace = separator > 0 ? key.substring(0, separator) : "ascend";
            if (!PrerequisiteProviderRegistry.hasProvider(namespace)) {
                issues.add(node.getId() + " requires " + key + " but no prerequisite provider is registered for " + namespace);
            }
        }
    }

    private static void validateAbility(AbilityNode ability, List<String> issues) {
        if (!isPngTexture(ability.getIcon())) {
            issues.add(ability.getId() + " has invalid icon path " + ability.getIcon() + " (expected a .png texture)");
        }
        for (ResourceLocation parent : ability.getLinks()) {
            if (SkillNodeRegistry.get(parent) == null && AbilityNodeRegistry.get(parent) == null) {
                issues.add(ability.getId() + " links to missing node or ability " + parent);
            }
        }
    }

    private static boolean isPngTexture(ResourceLocation id) {
        return id != null && id.getPath().endsWith(".png");
    }

    private static void detectSkillCycles(List<String> issues) {
        Set<ResourceLocation> visited = new HashSet<>();
        Set<ResourceLocation> visiting = new HashSet<>();
        for (SkillNode node : SkillNodeRegistry.all()) {
            visit(node, visited, visiting, issues);
        }
    }

    private static void detectAbilityCycles(List<String> issues) {
        Set<ResourceLocation> visited = new HashSet<>();
        Set<ResourceLocation> visiting = new HashSet<>();
        for (AbilityNode ability : AbilityNodeRegistry.all()) {
            visitAbility(ability, visited, visiting, issues);
        }
    }

    private static void visit(SkillNode node,
                              Set<ResourceLocation> visited,
                              Set<ResourceLocation> visiting,
                              List<String> issues) {
        ResourceLocation id = node.getId();
        if (visited.contains(id)) return;
        if (!visiting.add(id)) {
            issues.add("Cycle detected at skill node " + id);
            return;
        }
        for (ResourceLocation parentId : node.getLinks()) {
            SkillNode parent = SkillNodeRegistry.get(parentId);
            if (parent != null) {
                visit(parent, visited, visiting, issues);
            }
        }
        visiting.remove(id);
        visited.add(id);
    }

    private static void visitAbility(AbilityNode ability,
                                     Set<ResourceLocation> visited,
                                     Set<ResourceLocation> visiting,
                                     List<String> issues) {
        ResourceLocation id = ability.getId();
        if (visited.contains(id)) return;
        if (!visiting.add(id)) {
            issues.add("Cycle detected at ability node " + id);
            return;
        }
        for (ResourceLocation parentId : ability.getLinks()) {
            AbilityNode parent = AbilityNodeRegistry.get(parentId);
            if (parent != null) {
                visitAbility(parent, visited, visiting, issues);
            }
        }
        visiting.remove(id);
        visited.add(id);
    }

    private static Set<ResourceLocation> collectRequiredSkillNodes(SkillNode node) {
        Set<ResourceLocation> required = new HashSet<>();
        collectRequiredSkillNodes(node, required, new HashSet<>());
        required.remove(node.getId());
        return required;
    }

    private static void collectRequiredSkillNodes(SkillNode node,
                                                  Set<ResourceLocation> required,
                                                  Set<ResourceLocation> visiting) {
        if (!visiting.add(node.getId())) return;
        for (ResourceLocation parentId : node.getLinks()) {
            SkillNode parent = SkillNodeRegistry.get(parentId);
            if (parent != null && required.add(parentId)) {
                collectRequiredSkillNodes(parent, required, visiting);
            }
        }
        visiting.remove(node.getId());
    }
}
