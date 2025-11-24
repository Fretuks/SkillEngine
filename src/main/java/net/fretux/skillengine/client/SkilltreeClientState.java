package net.fretux.skillengine.client;

import net.fretux.skillengine.SkillEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.HashSet;
import java.util.Set;

public class SkilltreeClientState {
    private static final Set<ResourceLocation> unlocked = new HashSet<>();

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
}