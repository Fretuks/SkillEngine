package net.fretux.skillengine.skilltree;

import net.fretux.skillengine.capability.PlayerSkillData;
import net.minecraft.resources.ResourceLocation;

public class SkillLogic {

    public static boolean canUnlock(PlayerSkillData data, SkillNode node) {
        if (data.isUnlocked(node.getId())) return false;
        if (data.getSkillPoints() < node.getCost()) return false;
        if (node.isRoot()) return true;
        for (ResourceLocation neighborId : node.getLinks()) {
            if (data.isUnlocked(neighborId)) return true;
        }
        return false;
    }
}
