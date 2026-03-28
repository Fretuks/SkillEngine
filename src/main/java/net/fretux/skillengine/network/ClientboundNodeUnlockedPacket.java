package net.fretux.skillengine.network;

import net.fretux.skillengine.skilltree.AbilityNodeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

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
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.handle(msg));
        });
        ctx.get().setPacketHandled(true);
    }

    private static final class ClientHandler {
        private static void handle(ClientboundNodeUnlockedPacket msg) {
            if (AbilityNodeRegistry.get(msg.id) != null) {
                net.fretux.skillengine.client.SkilltreeClientState.unlockAbility(msg.id);
            } else {
                net.fretux.skillengine.client.SkilltreeClientState.unlockNode(msg.id);
            }
            net.fretux.skillengine.client.SkilltreeClientState.setCurrentSkillPoints(msg.newSkillPoints);
        }
    }
}

