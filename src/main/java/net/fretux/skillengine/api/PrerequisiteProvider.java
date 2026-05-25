package net.fretux.skillengine.api;

import net.fretux.skillengine.skilltree.SkillNode;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface PrerequisiteProvider {
    boolean meets(ServerPlayer player, SkillNode node, String key, int requiredValue);
}
