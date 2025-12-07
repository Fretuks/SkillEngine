package net.fretux.skillengine.network;

import net.fretux.skillengine.capability.SkillEngineCapabilities;
import net.fretux.skillengine.skilltree.AbilityNode;
import net.fretux.skillengine.skilltree.AbilityNodeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ServerboundActivateAbilityPacket {

    private final int slot;

    public ServerboundActivateAbilityPacket(int slot) {
        this.slot = slot;
    }

    public static void encode(ServerboundActivateAbilityPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slot);
    }

    public static ServerboundActivateAbilityPacket decode(FriendlyByteBuf buf) {
        return new ServerboundActivateAbilityPacket(buf.readInt());
    }

    public static void handle(ServerboundActivateAbilityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {

            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(SkillEngineCapabilities.PLAYER_SKILLS).ifPresent(data -> {

                ResourceLocation abilityId = data.getAbilityInSlot(msg.slot);
                if (abilityId == null) return;

                AbilityNode ability = AbilityNodeRegistry.get(abilityId);
                if (ability == null) return;

                int cd = data.getCooldown(msg.slot);
                if (cd > 0) return;
                ability.execute(player);
                data.setCooldown(msg.slot, ability.getCooldown());
                PacketHandler.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new ClientboundSyncCooldownPacket(msg.slot, ability.getCooldown())
                );
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
