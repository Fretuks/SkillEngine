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
            boolean isAbility = AbilityNodeRegistry.get(msg.id) != null;
            DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT,
                    () -> () -> net.fretux.skillengine.client.ClientSkillEngineBridge.handleNodeUnlocked(
                            msg.id,
                            msg.newSkillPoints,
                            isAbility
                    )
            );
        });
        ctx.get().setPacketHandled(true);
    }
}

