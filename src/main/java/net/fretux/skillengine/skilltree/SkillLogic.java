package net.fretux.skillengine.skilltree;

import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.skillengine.capability.PlayerSkillData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SkillLogic {

    public static boolean canUnlock(PlayerSkillData data, Player player, SkillNode node) {
        return getUnlockPlan(data, player, node).isPresent();
    }

    public static Optional<List<SkillNode>> getUnlockPlan(PlayerSkillData data, Player player, SkillNode node) {
        if (data == null || player == null || node == null || data.isUnlocked(node.getId())) {
            return Optional.empty();
        }

        List<SkillNode> plan = new ArrayList<>();
        Set<ResourceLocation> planned = new HashSet<>();
        Set<ResourceLocation> visiting = new HashSet<>();
        if (!collectUnlockPlan(data, player, node, plan, planned, visiting)) {
            return Optional.empty();
        }

        int totalCost = 0;
        for (SkillNode plannedNode : plan) {
            totalCost += plannedNode.getCost();
        }
        if (data.getSkillPoints() < totalCost) {
            return Optional.empty();
        }

        return Optional.of(List.copyOf(plan));
    }

    public static boolean meetsAscendPrerequisites(Player player, SkillNode node) {
        if (node.getPrereqAttributes().isEmpty()) return true;
        return player.getCapability(PlayerStatsProvider.PLAYER_STATS).map(stats -> {
            for (Map.Entry<String, Integer> req : node.getPrereqAttributes().entrySet()) {
                int current = stats.getAttributeLevel(req.getKey());
                if (current < req.getValue()) return false;
            }
            return true;
        }).orElse(false);
    }

    private static boolean collectUnlockPlan(PlayerSkillData data,
                                             Player player,
                                             SkillNode node,
                                             List<SkillNode> plan,
                                             Set<ResourceLocation> planned,
                                             Set<ResourceLocation> visiting) {
        ResourceLocation id = node.getId();
        if (data.isUnlocked(id) || planned.contains(id)) {
            return true;
        }
        if (!visiting.add(id)) {
            return false;
        }
        if (!meetsAscendPrerequisites(player, node) || conflictsWithUnlocked(data, node)) {
            visiting.remove(id);
            return false;
        }

        for (ResourceLocation parentId : node.getLinks()) {
            if (data.isUnlocked(parentId)) {
                continue;
            }
            SkillNode parent = SkillNodeRegistry.get(parentId);
            if (parent == null || !collectUnlockPlan(data, player, parent, plan, planned, visiting)) {
                visiting.remove(id);
                return false;
            }
        }

        if (conflictsWithPlanned(node, planned)) {
            visiting.remove(id);
            return false;
        }

        visiting.remove(id);
        planned.add(id);
        plan.add(node);
        return true;
    }

    private static boolean conflictsWithUnlocked(PlayerSkillData data, SkillNode node) {
        for (ResourceLocation excl : node.getExclusiveWith()) {
            if (data.isUnlocked(excl)) {
                return true;
            }
        }
        return false;
    }

    private static boolean conflictsWithPlanned(SkillNode node, Set<ResourceLocation> planned) {
        for (ResourceLocation excl : node.getExclusiveWith()) {
            if (planned.contains(excl)) {
                return true;
            }
        }
        return false;
    }
}
