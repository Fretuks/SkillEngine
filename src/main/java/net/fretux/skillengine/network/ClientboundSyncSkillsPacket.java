package net.fretux.skillengine.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ClientboundSyncSkillsPacket {

    private static final int MAX_SYNCED_NODES = 4096;
    private static final int MAX_ABILITY_SLOTS = 64;

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
        int size = readBoundedCount(buf, MAX_SYNCED_NODES, "unlocked skill nodes");
        Set<ResourceLocation> unlocked = new HashSet<>();
        for (int i = 0; i < size; i++) unlocked.add(buf.readResourceLocation());
        int abilitySize = readBoundedCount(buf, MAX_SYNCED_NODES, "unlocked abilities");
        Set<ResourceLocation> unlockedAbilities = new HashSet<>();
        for (int i = 0; i < abilitySize; i++) unlockedAbilities.add(buf.readResourceLocation());
        int points = buf.readInt();
        int slotsCount = readBoundedCount(buf, MAX_ABILITY_SLOTS, "ability slots");
        ResourceLocation[] slots = new ResourceLocation[slotsCount];
        for (int i = 0; i < slots.length; i++) {
            boolean present = buf.readBoolean();
            slots[i] = present ? buf.readResourceLocation() : null;
        }
        return new ClientboundSyncSkillsPacket(unlocked, unlockedAbilities, points, slots);
    }

    public static void handle(ClientboundSyncSkillsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT,
                    () -> () -> net.fretux.skillengine.client.ClientSkillEngineBridge.handleSkillsSync(
                            msg.unlocked,
                            msg.unlockedAbilities,
                            msg.skillPoints,
                            msg.abilitySlots
                    )
            );
        });
        ctx.get().setPacketHandled(true);
    }

    private static int readBoundedCount(FriendlyByteBuf buf, int max, String fieldName) {
        int count = buf.readInt();
        if (count < 0 || count > max) {
            throw new IllegalArgumentException("Invalid " + fieldName + " count: " + count);
        }
        return count;
    }
}
