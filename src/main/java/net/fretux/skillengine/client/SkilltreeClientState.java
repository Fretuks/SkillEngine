package net.fretux.skillengine.client;

import net.fretux.skillengine.SkillEngine;
import net.fretux.skillengine.capability.PlayerSkillData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SkilltreeClientState {
    private static final Set<ResourceLocation> unlocked = new HashSet<>();
    private static final Set<ResourceLocation> unlockedAbilities = new HashSet<>();
    private static int currentSkillPoints = 0;
    private static final ResourceLocation[] abilitySlots = new ResourceLocation[3];
    private static final int[] clientCooldowns = new int[3];

    public static void updateCooldown(int slot, int cd) {
        clientCooldowns[slot - 1] = cd;
    }

    public static int getClientCooldown(int slot) {
        return clientCooldowns[slot - 1];
    }

    public static void unlockNode(ResourceLocation id) {
        SkillEngine.LOGGER.info("[CLIENT] Unlocking node: {}", id);
        unlocked.add(id);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
        }
        mc.getToasts().addToast(
                SystemToast.multiline(
                        mc,
                        SystemToast.SystemToastIds.TUTORIAL_HINT,
                        Component.literal("Node Unlocked"),
                        Component.literal(id.toString())
                )
        );
    }

    public static boolean isUnlocked(ResourceLocation id) {
        return unlocked.contains(id);
    }

    public static void setUnlocked(Set<ResourceLocation> ids) {
        unlocked.clear();
        unlocked.addAll(ids);
    }

    public static void setUnlockedAbilities(Set<ResourceLocation> abilities) {
        unlockedAbilities.clear();
        unlockedAbilities.addAll(abilities);
    }

    public static void setCurrentSkillPoints(int points) {
        currentSkillPoints = points;
    }

    public static int getCurrentSkillPoints() {
        return currentSkillPoints;
    }

    public static boolean isAbilityUnlocked(ResourceLocation id) {
        return unlockedAbilities.contains(id);
    }


    public static void unlockAbility(ResourceLocation id) {
        unlockedAbilities.add(id);
    }

    public static void bindAbilityLocal(int slot, ResourceLocation abilityId) {
        PlayerSkillData.bindHelper(slot, abilityId, abilitySlots);
    }

    public static void setAbilitySlots(ResourceLocation[] slots) {
        for (int i = 0; i < abilitySlots.length; i++) {
            abilitySlots[i] = (slots != null && i < slots.length) ? slots[i] : null;
        }
    }

    public static ResourceLocation getAbilityInSlot(int slot) {
        int index = slot - 1;
        if (index < 0 || index >= abilitySlots.length) return null;
        return abilitySlots[index];
    }

    public static int getSlotOfAbility(ResourceLocation abilityId) {
        if (abilityId == null) return -1;
        for (int i = 0; i < abilitySlots.length; i++) {
            if (abilityId.equals(abilitySlots[i])) return i + 1;
        }
        return -1;
    }

    public static ResourceLocation[] getAbilitySlots() {
        return Arrays.copyOf(abilitySlots, abilitySlots.length);
    }
}