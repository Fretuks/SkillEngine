package net.fretux.skillengine.skilltree;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class AbilityNode {

    private final ResourceLocation id;
    private final Component title;
    private final Component description;
    private final float x, y;
    private final List<ResourceLocation> links;
    private final List<ResourceLocation> tags;
    private final ResourceLocation icon;
    private final int cooldown;

    public AbilityNode(ResourceLocation id,
                       Component title,
                       Component description,
                       float x, float y,
                       List<ResourceLocation> links,
                       List<ResourceLocation> tags,
                       ResourceLocation icon,
                       int cooldown) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.x = x;
        this.y = y;
        this.links = List.copyOf(links);
        this.tags = List.copyOf(tags);
        this.icon = icon;
        this.cooldown = cooldown;
    }

    public ResourceLocation getId() { return id; }
    public Component getTitle() { return title; }
    public Component getDescription() { return description; }
    public float getX() { return x; }
    public float getY() { return y; }
    public List<ResourceLocation> getLinks() { return links; }
    public List<ResourceLocation> getTags() { return tags; }
    public ResourceLocation getIcon() { return icon; }
    public int getCooldown() { return cooldown; }

    public void execute(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("Activated ability: " + id));
    }
}
