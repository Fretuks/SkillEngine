package net.fretux.skillengine.api;

import net.fretux.skillengine.skilltree.AbilityNode;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface AbilityHandler {
    boolean execute(ServerPlayer player, AbilityNode ability);
}
