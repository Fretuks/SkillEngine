package net.fretux.skillengine.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundNodeUnlockedPacket {
    private final ResourceLocation id;
    private final int newSkillPoints;
    private final boolean ability;

    public ClientboundNodeUnlockedPacket(ResourceLocation id, int newSkillPoints, boolean ability) {
        this.id = id;
        this.newSkillPoints = newSkillPoints;
        this.ability = ability;
    }

    public static void encode(ClientboundNodeUnlockedPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.id);
        buf.writeInt(msg.newSkillPoints);
        buf.writeBoolean(msg.ability);
    }

    public static ClientboundNodeUnlockedPacket decode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        int points = buf.readInt();
        boolean ability = buf.readBoolean();
        return new ClientboundNodeUnlockedPacket(id, points, ability);
    }

    public static void handle(ClientboundNodeUnlockedPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT,
                    () -> () -> net.fretux.skillengine.client.ClientSkillEngineBridge.handleNodeUnlocked(
                            msg.id,
                            msg.newSkillPoints,
                            msg.ability
                    )
            );
        });
        ctx.get().setPacketHandled(true);
    }
}

