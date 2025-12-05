package net.fretux.skillengine.network;

import net.fretux.skillengine.capability.SkillEngineCapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.fretux.skillengine.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundBindAbilityPacket {

    private final ResourceLocation abilityId;
    private final int slot;

    public ServerboundBindAbilityPacket(ResourceLocation id, int slot) {
        this.abilityId = id;
        this.slot = slot;
    }

    public static void encode(ServerboundBindAbilityPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.abilityId);
        buf.writeInt(msg.slot);
    }

    public static ServerboundBindAbilityPacket decode(FriendlyByteBuf buf) {
        return new ServerboundBindAbilityPacket(buf.readResourceLocation(), buf.readInt());
    }

    public static void handle(ServerboundBindAbilityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(SkillEngineCapabilities.PLAYER_SKILLS).ifPresent(data ->
                data.bindAbility(msg.slot, msg.abilityId)
            );
            // Sync back to client so UI reflects updated bindings
            PacketHandler.syncSkillsTo(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
