package net.fretux.skillengine.capability;

import net.fretux.skillengine.skilltree.AbilityNode;
import net.fretux.skillengine.skilltree.SkillNode;
import net.fretux.skillengine.skilltree.SkillNodeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class PlayerSkillData {

    private int skillPoints;
    private final Set<ResourceLocation> unlockedNodes = new HashSet<>();
    private final Set<ResourceLocation> unlockedAbilities = new HashSet<>();
    private final Set<ResourceLocation> activeTags = new HashSet<>();

    public int getSkillPoints() { return skillPoints; }
    public void addSkillPoints(int amount) { skillPoints += amount; }
    public boolean isUnlocked(ResourceLocation nodeId) {
        return unlockedNodes.contains(nodeId);
    }
    private final ResourceLocation[] abilitySlots = new ResourceLocation[3];


    public void unlockNode(SkillNode node) {
        if (unlockedNodes.add(node.getId())) {
            activeTags.addAll(node.getTags());
            skillPoints -= node.getCost();
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
        tag.put("Unlocked", unlockedList);
        ListTag tagList = new ListTag();
        for (ResourceLocation id : activeTags) {
            CompoundTag t = new CompoundTag();
            t.putString("Tag", id.toString());
            tagList.add(t);
        }
        tag.put("Tags", tagList);
        ListTag abilityList = new ListTag();
        for (ResourceLocation abilitySlot : abilitySlots) {
            CompoundTag t = new CompoundTag();
            if (abilitySlot != null) {
                t.putString("Id", abilitySlot.toString());
            } else {
                t.putString("Id", "");
            }
            abilityList.add(t);
        }
        tag.put("AbilitySlots", abilityList);
        ListTag unlockedAbilitiesList = new ListTag();
        for (ResourceLocation id : unlockedAbilities) {
            CompoundTag t = new CompoundTag();
            t.putString("Id", id.toString());
            unlockedAbilitiesList.add(t);
        }
        tag.put("UnlockedAbilities", unlockedAbilitiesList);
        return tag;
    }

    public void load(CompoundTag tag) {
        skillPoints = tag.getInt("SkillPoints");
        unlockedNodes.clear();
        activeTags.clear();
        unlockedAbilities.clear();
        ListTag unlockedList = tag.getList("Unlocked", Tag.TAG_COMPOUND);
        for (Tag t : unlockedList) {
            CompoundTag c = (CompoundTag) t;
            unlockedNodes.add(new ResourceLocation(c.getString("Id")));
        }
        ListTag tagList = tag.getList("Tags", Tag.TAG_COMPOUND);
        for (Tag t : tagList) {
            CompoundTag c = (CompoundTag) t;
            activeTags.add(new ResourceLocation(c.getString("Tag")));
        }
        ListTag abilityList = tag.getList("AbilitySlots", Tag.TAG_COMPOUND);
        for (int i = 0; i < abilitySlots.length && i < abilityList.size(); i++) {
            CompoundTag t = abilityList.getCompound(i);
            String id = t.getString("Id");
            if (!id.isEmpty()) {
                abilitySlots[i] = new ResourceLocation(id);
            } else {
                abilitySlots[i] = null;
            }
        }
        ListTag unlockedAbilitiesList = tag.getList("UnlockedAbilities", Tag.TAG_COMPOUND);
        for (Tag t : unlockedAbilitiesList) {
            CompoundTag c = (CompoundTag) t;
            unlockedAbilities.add(new ResourceLocation(c.getString("Id")));
        }
    }

    public Set<ResourceLocation> getUnlockedNodes() {
        return unlockedNodes;
    }

    public int getTotalSkillCost() {
        int total = 0;
        for (ResourceLocation id : unlockedNodes) {
            SkillNode node = SkillNodeRegistry.get(id);
            if (node != null) {
                total += node.getCost();
            }
        }
        return total;
    }

    public void clearAllNodes() {
        unlockedNodes.clear();
        activeTags.clear();
    }

    public void bindAbility(int slot, ResourceLocation abilityId) {
        bindHelperFunc(slot, abilityId, abilitySlots);
    }

    public static void bindHelperFunc(int slot, ResourceLocation abilityId, ResourceLocation[] abilitySlots) {
        int index = slot - 1;
        if (index < 0 || index >= abilitySlots.length) return;
        if (abilityId != null) {
            for (int i = 0; i < abilitySlots.length; i++) {
                if (abilityId.equals(abilitySlots[i])) {
                    abilitySlots[i] = null;
                }
            }
        }
        abilitySlots[index] = abilityId;
    }

    public ResourceLocation[] getAbilitySlots() {
        return abilitySlots.clone();
    }

    public boolean isAbilityUnlocked(ResourceLocation id) {
        return unlockedAbilities.contains(id);
    }

    public void unlockAbility(AbilityNode node) {
        unlockedAbilities.add(node.getId());
    }
    
    public Set<ResourceLocation> getUnlockedAbilities() {
        return unlockedAbilities;
    }
}