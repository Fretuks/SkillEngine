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

    public ClientboundSyncSkillsPacket(Set<ResourceLocation> unlocked) {
        this.unlocked = unlocked;
    }

    public static void encode(ClientboundSyncSkillsPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.unlocked.size());
        for (ResourceLocation id : msg.unlocked) {
            buf.writeResourceLocation(id);
        }
    }

    public static ClientboundSyncSkillsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<ResourceLocation> result = new HashSet<>();
        for (int i = 0; i < size; i++) {
            result.add(buf.readResourceLocation());
        }
        return new ClientboundSyncSkillsPacket(result);
    }

    public static void handle(ClientboundSyncSkillsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            SkilltreeClientState.setUnlocked(msg.unlocked);
        });
        ctx.get().setPacketHandled(true);
    }
}
