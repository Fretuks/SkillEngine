package net.fretux.skillengine.client;

import net.minecraft.resources.ResourceLocation;

public final class ClientSkillEngineBridge {

    private ClientSkillEngineBridge() {}

    public static boolean clientHasSkill(ResourceLocation id) {
        return SkilltreeClientState.isUnlocked(id);
    }

    public static int clientGetAbilitySlot(ResourceLocation id) {
        return SkilltreeClientState.getSlotOfAbility(id);
    }

    public static ResourceLocation[] clientGetAbilitySlots() {
        return SkilltreeClientState.getAbilitySlots();
    }

    public static void handleNodeUnlocked(ResourceLocation id, int newSkillPoints, boolean isAbility) {
        if (isAbility) {
            SkilltreeClientState.unlockAbility(id);
        } else {
            SkilltreeClientState.unlockNode(id);
        }
        SkilltreeClientState.setCurrentSkillPoints(newSkillPoints);
    }

    public static void handleSkillsSync(Iterable<ResourceLocation> unlocked,
                                        Iterable<ResourceLocation> unlockedAbilities,
                                        int skillPoints,
                                        ResourceLocation[] abilitySlots) {
        SkilltreeClientState.setUnlocked(unlocked);
        SkilltreeClientState.setUnlockedAbilities(unlockedAbilities);
        SkilltreeClientState.setCurrentSkillPoints(skillPoints);
        SkilltreeClientState.setAbilitySlots(abilitySlots);
    }

    public static void handleCooldownSync(int slot, int cooldown) {
        SkilltreeClientState.updateCooldown(slot, cooldown);
    }
}
