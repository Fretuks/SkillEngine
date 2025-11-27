package net.fretux.skillengine.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.fretux.skillengine.client.SkilltreeClientState;

import java.util.function.Supplier;

public class ClientboundNodeUnlockedPacket {

    private final ResourceLocation id;
    private final int newSkillPoints;

    public ClientboundNodeUnlockedPacket(ResourceLocation id, int newSkillPoints) {
        this.id = id;
        this.newSkillPoints = newSkillPoints;
    }

    public static void encode(ClientboundNodeUnlockedPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.id);
        buf.writeInt(msg.newSkillPoints);
    }

    public static ClientboundNodeUnlockedPacket decode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        int points = buf.readInt();
        return new ClientboundNodeUnlockedPacket(id, points);
    }

    public static void handle(ClientboundNodeUnlockedPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Update client-side unlocked set and points immediately so UI can reflect changes this frame
            SkilltreeClientState.unlockNode(msg.id);
            SkilltreeClientState.setCurrentSkillPoints(msg.newSkillPoints);
        });
        ctx.get().setPacketHandled(true);
    }
}

