package net.fretux.skillengine.network;

import net.fretux.skillengine.client.SkilltreeClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundSyncCooldownPacket {

    private final int slot;
    private final int cooldown;

    public ClientboundSyncCooldownPacket(int slot, int cooldown) {
        this.slot = slot;
        this.cooldown = cooldown;
    }

    public static void encode(ClientboundSyncCooldownPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slot);
        buf.writeInt(msg.cooldown);
    }

    public static ClientboundSyncCooldownPacket decode(FriendlyByteBuf buf) {
        return new ClientboundSyncCooldownPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(ClientboundSyncCooldownPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> SkilltreeClientState.updateCooldown(msg.slot, msg.cooldown));
        ctx.get().setPacketHandled(true);
    }
}
