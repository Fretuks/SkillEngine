package net.fretux.skillengine.network;

import net.fretux.skillengine.SkillEngine;
import net.fretux.skillengine.capability.SkillEngineCapabilities;
import net.fretux.skillengine.skilltree.SkillLogic;
import net.fretux.skillengine.skilltree.SkillNode;
import net.fretux.skillengine.skilltree.SkillNodeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ServerboundUnlockNodePacket {

    private final ResourceLocation nodeId;

    public ServerboundUnlockNodePacket(ResourceLocation nodeId) {
        this.nodeId = nodeId;
    }

    public static void encode(ServerboundUnlockNodePacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.nodeId);
    }

    public static ServerboundUnlockNodePacket decode(FriendlyByteBuf buf) {
        return new ServerboundUnlockNodePacket(buf.readResourceLocation());
    }

    public static void handle(ServerboundUnlockNodePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                SkillEngine.LOGGER.warn("Unlock packet received but sender was null");
                return;
            }

            SkillNode node = SkillNodeRegistry.get(msg.nodeId);
            if (node == null) {
                SkillEngine.LOGGER.warn("Unlock packet for unknown node {}", msg.nodeId);
                return;
            }

            player.getCapability(SkillEngineCapabilities.PLAYER_SKILLS).ifPresent(data -> {
                SkillEngine.LOGGER.info("Received unlock request for {} from {} (points={})",
                        msg.nodeId, player.getGameProfile().getName(), data.getSkillPoints());

                if (SkillLogic.canUnlock(data, player, node)) {
                    data.unlockNode(node);
                    SkillEngine.LOGGER.info("Node {} unlocked for {}", msg.nodeId, player.getGameProfile().getName());

                    PacketHandler.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new ClientboundNodeUnlockedPacket(node.getId())
                    );
                } else {
                    SkillEngine.LOGGER.info("Cannot unlock node {} for {} (canUnlock=false)",
                            msg.nodeId, player.getGameProfile().getName());
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
