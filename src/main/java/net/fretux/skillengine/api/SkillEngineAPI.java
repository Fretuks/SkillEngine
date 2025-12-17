package net.fretux.skillengine.api;

import net.fretux.skillengine.SkillEngine;
import net.fretux.skillengine.capability.PlayerSkillData;
import net.fretux.skillengine.capability.SkillEngineCapabilities;
import net.fretux.skillengine.client.SkilltreeClientState;
import net.fretux.skillengine.skilltree.AbilityNode;
import net.fretux.skillengine.skilltree.AbilityNodeRegistry;
import net.fretux.skillengine.skilltree.SkillLogic;
import net.fretux.skillengine.skilltree.SkillNode;
import net.fretux.skillengine.skilltree.SkillNodeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;

public final class SkillEngineAPI {

    private SkillEngineAPI() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(SkillEngine.MODID, path);
    }

    public static ResourceLocation rl(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static SkillNode getSkillNode(ResourceLocation id) {
        return SkillNodeRegistry.get(id);
    }

    public static AbilityNode getAbilityNode(ResourceLocation id) {
        return AbilityNodeRegistry.get(id);
    }

    public static boolean hasSkillNode(ResourceLocation id) {
        return SkillNodeRegistry.get(id) != null;
    }

    public static boolean hasAbilityNode(ResourceLocation id) {
        return AbilityNodeRegistry.get(id) != null;
    }

    public static List<SkillNode> getAllSkillNodes() {
        return List.copyOf(SkillNodeRegistry.all());
    }

    public static List<AbilityNode> getAllAbilityNodes() {
        return List.copyOf(AbilityNodeRegistry.all());
    }

    public static PlayerSkillData getPlayerData(Player player) {
        return player.getCapability(SkillEngineCapabilities.PLAYER_SKILLS).orElse(null);
    }

    public static int getSkillPoints(Player player) {
        PlayerSkillData data = getPlayerData(player);
        return data != null ? data.getSkillPoints() : 0;
    }

    public static void addSkillPoints(Player player, int amount) {
        PlayerSkillData data = getPlayerData(player);
        if (data != null) data.addSkillPoints(amount);
    }

    public static boolean consumeSkillPoints(Player player, int amount) {
        PlayerSkillData data = getPlayerData(player);
        if (data != null && data.getSkillPoints() >= amount) {
            data.addSkillPoints(-amount);
            return true;
        }
        return false;
    }
    
    public static Set<ResourceLocation> getUnlockedSkillNodes(Player player) {
        PlayerSkillData data = getPlayerData(player);
        return data != null ? data.getUnlockedNodes() : Set.of();
    }

    public static boolean isSkillUnlocked(Player player, ResourceLocation id) {
        PlayerSkillData data = getPlayerData(player);
        return data != null && data.isUnlocked(id);
    }

    public static boolean unlockSkill(Player player, ResourceLocation id) {
        PlayerSkillData data = getPlayerData(player);
        if (data == null) return false;
        SkillNode node = SkillNodeRegistry.get(id);
        if (node == null) return false;
        if (!SkillLogic.canUnlock(data, player, node)) return false;
        data.unlockNode(node);
        return true;
    }

    public static boolean isAbilityUnlocked(Player player, ResourceLocation id) {
        PlayerSkillData data = getPlayerData(player);
        return data != null && data.isAbilityUnlocked(id);
    }

    public static Set<ResourceLocation> getUnlockedAbilities(Player player) {
        PlayerSkillData data = getPlayerData(player);
        return data != null ? data.getUnlockedAbilities() : Set.of();
    }

    public static boolean unlockAbility(Player player, ResourceLocation id) {
        PlayerSkillData data = getPlayerData(player);
        if (data == null) return false;
        AbilityNode ability = AbilityNodeRegistry.get(id);
        if (ability == null) return false;
        data.unlockAbility(ability);
        return true;
    }

    public static void bindAbility(Player player, int slot, ResourceLocation abilityId) {
        PlayerSkillData data = getPlayerData(player);
        if (data == null) return;
        data.bindAbility(slot, abilityId);
    }

    public static ResourceLocation getAbilityInSlot(Player player, int slot) {
        PlayerSkillData data = getPlayerData(player);
        return data != null ? data.getAbilityInSlot(slot) : null;
    }

    public static ResourceLocation[] getAbilitySlots(Player player) {
        PlayerSkillData data = getPlayerData(player);
        return data != null ? data.getAbilitySlots() : new ResourceLocation[3];
    }

    public static boolean clientHasSkill(ResourceLocation id) {
        return SkilltreeClientState.isUnlocked(id);
    }

    public static int clientGetAbilitySlot(ResourceLocation id) {
        return SkilltreeClientState.getSlotOfAbility(id);
    }

    public static ResourceLocation[] clientGetAbilitySlots() {
        return SkilltreeClientState.getAbilitySlots();
    }
}