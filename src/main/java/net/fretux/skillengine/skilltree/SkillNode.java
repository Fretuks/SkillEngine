package net.fretux.skillengine.skilltree;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public class SkillNode {

    private final ResourceLocation id;
    private final Component title;
    private final Component description;
    private final int cost;
    private final float x;
    private final float y;
    private final List<ResourceLocation> links;
    private final List<ResourceLocation> tags;
    private final ResourceLocation icons;
    private final Map<String, Integer> prereqAttributes;

    public SkillNode(ResourceLocation id,
                     Component title,
                     Component description,
                     int cost,
                     float x, float y,
                     List<ResourceLocation> links,
                     List<ResourceLocation> tags,
                     ResourceLocation icons,
                     Map<String, Integer> prereqAttributes) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.cost = cost;
        this.x = x;
        this.y = y;
        this.links = List.copyOf(links);
        this.tags = List.copyOf(tags);
        this.icons = icons;
        this.prereqAttributes = Map.copyOf(prereqAttributes);
    }

    public ResourceLocation getId() {
        return id;
    }

    public Component getTitle() {
        return title;
    }

    public Component getDescription() {
        return description;
    }

    public int getCost() {
        return cost;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public List<ResourceLocation> getLinks() {
        return links;
    }

    public List<ResourceLocation> getTags() {
        return tags;
    }

    public ResourceLocation getIcons() {
        return icons;
    }

    public Map<String, Integer> getPrereqAttributes() {
        return prereqAttributes;
    }

    public boolean isRoot() {
        return links.isEmpty();
    }
}