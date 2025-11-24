package net.fretux.skillengine.capability;

import net.fretux.skillengine.skilltree.SkillNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class PlayerSkillData {

    private int skillPoints;
    private final Set<ResourceLocation> unlockedNodes = new HashSet<>();
    private final Set<ResourceLocation> activeTags = new HashSet<>();

    public int getSkillPoints() { return skillPoints; }
    public void addSkillPoints(int amount) { skillPoints += amount; }

    public boolean isUnlocked(ResourceLocation nodeId) {
        return unlockedNodes.contains(nodeId);
    }

    public boolean hasTag(ResourceLocation tag) {
        return activeTags.contains(tag);
    }

    public Set<ResourceLocation> getActiveTags() {
        return activeTags;
    }

    public void unlockNode(SkillNode node) {
        if (unlockedNodes.add(node.getId())) {
            activeTags.addAll(node.getTags());
            skillPoints -= node.getCost();
        }
    }

    /* ---------- NBT ---------- */

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

        return tag;
    }

    public void load(CompoundTag tag) {
        skillPoints = tag.getInt("SkillPoints");
        unlockedNodes.clear();
        activeTags.clear();

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
    }

    public Set<ResourceLocation> getUnlockedNodes() {
        return unlockedNodes;
    }
}