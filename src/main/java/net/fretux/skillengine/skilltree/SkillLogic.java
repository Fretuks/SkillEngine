package net.fretux.skillengine.skilltree;

import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.skillengine.capability.PlayerSkillData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class SkillLogic {

    public static boolean canUnlock(PlayerSkillData data, Player player, SkillNode node) {
        if (data.isUnlocked(node.getId())) return false;
        if (data.getSkillPoints() < node.getCost()) return false;
        if (node.isRoot()) return true;
        if (!meetsAscendPrerequisites(player, node)) return false;
        for (ResourceLocation neighborId : node.getLinks()) {
            if (data.isUnlocked(neighborId)) return true;
        }
        for (ResourceLocation excl : node.getExclusiveWith()) {
            if (data.isUnlocked(excl)) {
                return false;
            }
        }
        return false;
    }
    public static boolean meetsAscendPrerequisites(Player player, SkillNode node) {
        return player.getCapability(PlayerStatsProvider.PLAYER_STATS).map(stats -> {
            for (Map.Entry<String, Integer> req : node.getPrereqAttributes().entrySet()) {
                int current = stats.getAttributeLevel(req.getKey());
                if (current < req.getValue()) return false;
            }
            return true;
        }).orElse(false);
    }

}
