package net.fretux.skillengine.api;

import net.fretux.skillengine.SkillEngine;
import net.fretux.skillengine.capability.PlayerSkillData;
import net.fretux.skillengine.capability.SkillEngineCapabilities;
import net.fretux.skillengine.skilltree.SkillNode;
import net.fretux.skillengine.skilltree.SkillNodeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;
public final class SkillEngineAPI {

    private SkillEngineAPI() {}
    public static ResourceLocation id(String path) {
        return new ResourceLocation(SkillEngine.MODID, path);
    }
    public static ResourceLocation rl(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
    public static SkillNode getNode(ResourceLocation id) {
        return SkillNodeRegistry.get(id);
    }
    public static boolean hasNode(ResourceLocation id) {
        return SkillNodeRegistry.get(id) != null;
    }
    public static List<SkillNode> getAllNodes() {
        return List.copyOf(SkillNodeRegistry.all());
    }
    public static PlayerSkillData getPlayerData(Player player) {
        return player.getCapability(SkillEngineCapabilities.PLAYER_SKILLS).orElse(null);
    }
    public static Set<ResourceLocation> getUnlockedNodes(Player player) {
        PlayerSkillData data = getPlayerData(player);
        return data != null ? data.getUnlockedNodes() : Set.of();
    }
    
    public static int getSkillPoints(Player player) {
        PlayerSkillData data = getPlayerData(player);
        return data != null ? data.getSkillPoints() : 0;
    }
    
    public static void addSkillPoints(Player player, int amount) {
        PlayerSkillData data = getPlayerData(player);
        if (data != null) {
            data.addSkillPoints(amount);
        }
    }
    
    public static boolean consumeSkillPoints(Player player, int amount) {
        PlayerSkillData data = getPlayerData(player);
        if (data != null && data.getSkillPoints() >= amount) {
            data.addSkillPoints(-amount);
            return true;
        }
        return false;
    }
    
    public static boolean isUnlocked(Player player, ResourceLocation nodeId) {
        PlayerSkillData data = getPlayerData(player);
        return data != null && data.isUnlocked(nodeId);
    }
    
    public static boolean unlockNode(Player player, ResourceLocation nodeId) {
        PlayerSkillData data = getPlayerData(player);
        if (data == null) return false;

        SkillNode node = SkillNodeRegistry.get(nodeId);
        if (node == null) return false;

        if (!net.fretux.skillengine.skilltree.SkillLogic.canUnlock(data, player, node)) {
            return false;
        }

        data.unlockNode(node);
        return true;
    }
    public static boolean clientHasNode(ResourceLocation nodeId) {
        return net.fretux.skillengine.client.SkilltreeClientState.isUnlocked(nodeId);
    }
}
