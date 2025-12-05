package net.fretux.skillengine.network;

import net.fretux.skillengine.client.SkilltreeClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ClientboundSyncSkillsPacket {

    private final Set<ResourceLocation> unlocked;
    private final Set<ResourceLocation> unlockedAbilities;
    private final int skillPoints;
    private final ResourceLocation[] abilitySlots;

    public ClientboundSyncSkillsPacket(Set<ResourceLocation> unlocked,
                                       Set<ResourceLocation> unlockedAbilities,
                                       int skillPoints,
                                       ResourceLocation[] abilitySlots) {
        this.unlocked = unlocked;
        this.unlockedAbilities = unlockedAbilities;
        this.skillPoints = skillPoints;
        this.abilitySlots = abilitySlots;
    }

    public static void encode(ClientboundSyncSkillsPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.unlocked.size());
        for (ResourceLocation id : msg.unlocked) buf.writeResourceLocation(id);
        buf.writeInt(msg.unlockedAbilities.size());
        for (ResourceLocation id : msg.unlockedAbilities) buf.writeResourceLocation(id);
        buf.writeInt(msg.skillPoints);
        // ability slots (nullable entries)
        int count = msg.abilitySlots != null ? msg.abilitySlots.length : 0;
        buf.writeInt(count);
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                ResourceLocation id = msg.abilitySlots[i];
                buf.writeBoolean(id != null);
                if (id != null) buf.writeResourceLocation(id);
            }
        }
    }

    public static ClientboundSyncSkillsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<ResourceLocation> unlocked = new HashSet<>();
        for (int i = 0; i < size; i++) unlocked.add(buf.readResourceLocation());
        int abilitySize = buf.readInt();
        Set<ResourceLocation> unlockedAbilities = new HashSet<>();
        for (int i = 0; i < abilitySize; i++) unlockedAbilities.add(buf.readResourceLocation());
        int points = buf.readInt();
        int slotsCount = buf.readInt();
        ResourceLocation[] slots = new ResourceLocation[Math.max(slotsCount, 0)];
        for (int i = 0; i < slots.length; i++) {
            boolean present = buf.readBoolean();
            slots[i] = present ? buf.readResourceLocation() : null;
        }
        return new ClientboundSyncSkillsPacket(unlocked, unlockedAbilities, points, slots);
    }

    public static void handle(ClientboundSyncSkillsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            SkilltreeClientState.setUnlocked(msg.unlocked);
            SkilltreeClientState.setUnlockedAbilities(msg.unlockedAbilities);
            SkilltreeClientState.setCurrentSkillPoints(msg.skillPoints);
            SkilltreeClientState.setAbilitySlots(msg.abilitySlots);
        });
        ctx.get().setPacketHandled(true);
    }
}