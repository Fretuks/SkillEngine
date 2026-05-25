package net.fretux.skillengine.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class SkillPointsChangedEvent extends Event {

    public enum Reason {
        GAINED,
        SPENT,
        REFUNDED
    }

    private final Player player;
    private final int amount;
    private final Reason reason;
    private final ResourceLocation nodeId;

    public SkillPointsChangedEvent(Player player, int amount, Reason reason, ResourceLocation nodeId) {
        this.player = player;
        this.amount = amount;
        this.reason = reason;
        this.nodeId = nodeId;
    }

    public Player getPlayer() {
        return player;
    }

    public int getAmount() {
        return amount;
    }

    public Reason getReason() {
        return reason;
    }

    public ResourceLocation getNodeId() {
        return nodeId;
    }
}
