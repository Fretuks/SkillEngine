package net.fretux.skillengine.capability;

import net.fretux.skillengine.network.PacketHandler;
import net.fretux.skillengine.skilltree.AbilityNode;
import net.fretux.skillengine.skilltree.SkillNode;
import net.fretux.skillengine.skilltree.SkillNodeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

public class PlayerSkillData {
    private int skillPoints;
    private final Set<ResourceLocation> unlockedNodes = new HashSet<>();
    private final Set<ResourceLocation> unlockedAbilities = new HashSet<>();
    private final Set<ResourceLocation> activeTags = new HashSet<>();
    private final ResourceLocation[] abilitySlots = new ResourceLocation[3];
    private final int[] abilityCooldowns = new int[3];

    public int getSkillPoints() {
        return skillPoints;
    }

    public void addSkillPoints(int amount) {
        skillPoints += amount;
    }

    public boolean isUnlocked(ResourceLocation nodeId) {
        return unlockedNodes.contains(nodeId);
    }

    public void unlockNode(SkillNode node) {
        if (node == null) return;
        if (unlockedNodes.add(node.getId())) {
            activeTags.addAll(node.getTags());
            skillPoints -= node.getCost();
        }
    }

    public Set<ResourceLocation> getUnlockedNodes() {
        return unlockedNodes;
    }

    public boolean hasTag(ResourceLocation tag) {
        return activeTags.contains(tag);
    }

    public Set<ResourceLocation> getActiveTags() {
        return activeTags;
    }

    public boolean isAbilityUnlocked(ResourceLocation id) {
        return unlockedAbilities.contains(id);
    }

    public void unlockAbility(AbilityNode ability) {
        if (ability == null) return;
        unlockedAbilities.add(ability.getId());
    }

    public Set<ResourceLocation> getUnlockedAbilities() {
        return unlockedAbilities;
    }

    public void bindAbility(int slot, ResourceLocation abilityId) {
        bindHelper(slot, abilityId, abilitySlots);
    }

    public static void bindHelper(int slot, ResourceLocation abilityId, ResourceLocation[] slots) {
        int index = slot - 1;
        if (index < 0 || index >= slots.length) return;
        if (abilityId != null) {
            for (int i = 0; i < slots.length; i++) {
                if (abilityId.equals(slots[i])) {
                    slots[i] = null;
                }
            }
        }
        slots[index] = abilityId;
    }

    public ResourceLocation getAbilityInSlot(int slot) {
        int index = slot - 1;
        if (index < 0 || index >= abilitySlots.length) return null;
        return abilitySlots[index];
    }

    public ResourceLocation[] getAbilitySlots() {
        return abilitySlots.clone();
    }

    public void setCooldown(int slot, int ticks) {
        int idx = slot - 1;
        if (idx < 0 || idx >= abilityCooldowns.length) return;
        abilityCooldowns[idx] = ticks;
    }

    public int getCooldown(int slot) {
        int idx = slot - 1;
        if (idx < 0 || idx >= abilityCooldowns.length) return 0;
        return abilityCooldowns[idx];
    }

    public void tickCooldowns() {
        for (int i = 0; i < abilityCooldowns.length; i++) {
            if (abilityCooldowns[i] > 0) {
                abilityCooldowns[i]--;
            }
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("SkillPoints", skillPoints);
        ListTag unlockedList = new ListTag();
        for (ResourceLocation id : unlockedNodes) {
            CompoundTag t = new CompoundTag();
            t.putString("Id", id.toString());
            unlockedList.add(t);
        }
        tag.put("UnlockedNodes", unlockedList);
        ListTag tagsList = new ListTag();
        for (ResourceLocation id : activeTags) {
            CompoundTag t = new CompoundTag();
            t.putString("Tag", id.toString());
            tagsList.add(t);
        }
        tag.put("ActiveTags", tagsList);
        ListTag slotList = new ListTag();
        for (ResourceLocation slot : abilitySlots) {
            CompoundTag t = new CompoundTag();
            t.putString("Id", slot == null ? "" : slot.toString());
            slotList.add(t);
        }
        tag.put("AbilitySlots", slotList);
        ListTag abilityList = new ListTag();
        for (ResourceLocation id : unlockedAbilities) {
            CompoundTag t = new CompoundTag();
            t.putString("Id", id.toString());
            abilityList.add(t);
        }
        tag.put("UnlockedAbilities", abilityList);
        ListTag cdList = new ListTag();
        for (int cd : abilityCooldowns) {
            CompoundTag t = new CompoundTag();
            t.putInt("CD", cd);
            cdList.add(t);
        }
        tag.put("AbilityCooldowns", cdList);
        return tag;
    }

    public void load(CompoundTag tag) {
        skillPoints = tag.getInt("SkillPoints");
        unlockedNodes.clear();
        unlockedAbilities.clear();
        activeTags.clear();
        ListTag nodeList = tag.getList("UnlockedNodes", Tag.TAG_COMPOUND);
        for (Tag t : nodeList) {
            ResourceLocation id = new ResourceLocation(((CompoundTag) t).getString("Id"));
            unlockedNodes.add(id);
            SkillNode node = SkillNodeRegistry.get(id);
            if (node != null) activeTags.addAll(node.getTags());
        }
        ListTag tagList = tag.getList("ActiveTags", Tag.TAG_COMPOUND);
        for (Tag t : tagList) {
            activeTags.add(new ResourceLocation(((CompoundTag) t).getString("Tag")));
        }
        ListTag slots = tag.getList("AbilitySlots", Tag.TAG_COMPOUND);
        for (int i = 0; i < 3 && i < slots.size(); i++) {
            String id = slots.getCompound(i).getString("Id");
            abilitySlots[i] = id.isEmpty() ? null : new ResourceLocation(id);
        }
        ListTag abilityList = tag.getList("UnlockedAbilities", Tag.TAG_COMPOUND);
        for (Tag t : abilityList) {
            unlockedAbilities.add(new ResourceLocation(((CompoundTag) t).getString("Id")));
        }
        ListTag cdList = tag.getList("AbilityCooldowns", Tag.TAG_COMPOUND);
        for (int i = 0; i < abilityCooldowns.length && i < cdList.size(); i++) {
            CompoundTag t = cdList.getCompound(i);
            abilityCooldowns[i] = t.getInt("CD");
        }
    }

    public int getTotalSkillCost() {
        int total = 0;
        for (ResourceLocation id : unlockedNodes) {
            SkillNode node = SkillNodeRegistry.get(id);
            if (node != null) total += node.getCost();
        }
        return total;
    }

    public void clearAllNodes() {
        unlockedNodes.clear();
        activeTags.clear();
        unlockedAbilities.clear();
    }

    public void sync(ServerPlayer player) {
        PacketHandler.syncSkillsTo(player);
    }
}