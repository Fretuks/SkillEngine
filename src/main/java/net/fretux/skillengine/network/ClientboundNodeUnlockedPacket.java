package net.fretux.skillengine.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.fretux.skillengine.client.SkilltreeClientState;

import java.util.function.Supplier;

public class ClientboundNodeUnlockedPacket {

    private final ResourceLocation id;

    public ClientboundNodeUnlockedPacket(ResourceLocation id) {
        this.id = id;
    }

    public static void encode(ClientboundNodeUnlockedPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.id);
    }

    public static ClientboundNodeUnlockedPacket decode(FriendlyByteBuf buf) {
        return new ClientboundNodeUnlockedPacket(buf.readResourceLocation());
    }

    public static void handle(ClientboundNodeUnlockedPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            SkilltreeClientState.unlockNode(msg.id);
        });
        ctx.get().setPacketHandled(true);
    }
}

