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
    private final int skillPoints;

    public ClientboundSyncSkillsPacket(Set<ResourceLocation> unlocked, int skillPoints) {
        this.unlocked = unlocked;
        this.skillPoints = skillPoints;
    }

    public static void encode(ClientboundSyncSkillsPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.unlocked.size());
        for (ResourceLocation id : msg.unlocked) {
            buf.writeResourceLocation(id);
        }
        buf.writeInt(msg.skillPoints);
    }

    public static ClientboundSyncSkillsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<ResourceLocation> result = new HashSet<>();
        for (int i = 0; i < size; i++) {
            result.add(buf.readResourceLocation());
        }
        int points = buf.readInt();
        return new ClientboundSyncSkillsPacket(result, points);
    }
    
    public static void handle(ClientboundSyncSkillsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            SkilltreeClientState.setUnlocked(msg.unlocked);
            SkilltreeClientState.setCurrentSkillPoints(msg.skillPoints);
        });
        ctx.get().setPacketHandled(true);
    }
}