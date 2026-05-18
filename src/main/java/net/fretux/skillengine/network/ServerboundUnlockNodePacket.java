package net.fretux.skillengine.network;

import net.fretux.skillengine.SkillEngine;
import net.fretux.skillengine.capability.PlayerSkillData;
import net.fretux.skillengine.capability.SkillEngineCapabilities;
import net.fretux.skillengine.skilltree.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
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
            if (player == null) return;
            SkillNode node = SkillNodeRegistry.get(msg.nodeId);
            AbilityNode ability = AbilityNodeRegistry.get(msg.nodeId);
            player.getCapability(SkillEngineCapabilities.PLAYER_SKILLS).ifPresent(data -> {
                if (node != null) {
                    List<SkillNode> unlockPlan = SkillLogic.getUnlockPlan(data, player, node).orElse(null);
                    if (unlockPlan != null) {
                        for (SkillNode plannedNode : unlockPlan) {
                            data.unlockNode(plannedNode);
                        }
                        PacketHandler.syncSkillsTo(player);
                        PacketHandler.CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> player),
                                new ClientboundNodeUnlockedPacket(node.getId(), data.getSkillPoints())
                        );
                    } else {
                        SkillEngine.LOGGER.info("[SKILLENGINE] Rejecting unlock for {}", node.getId());
                    }
                    return;
                }
                if (ability != null) {
                    if (canUnlockAbility(player, data, ability)) {
                        data.unlockAbility(ability);
                        PacketHandler.syncSkillsTo(player);
                        PacketHandler.CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> player),
                                new ClientboundNodeUnlockedPacket(ability.getId(), data.getSkillPoints())
                        );
                    } else {
                        SkillEngine.LOGGER.info("[SKILLENGINE] Rejecting ability unlock for {}", ability.getId());
                    }
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }


    private static boolean canUnlockAbility(ServerPlayer player, PlayerSkillData data, AbilityNode ability) {
        if (data.isAbilityUnlocked(ability.getId())) return false;
        for (ResourceLocation parent : ability.getLinks()) {
            boolean ok = data.isUnlocked(parent) || data.isAbilityUnlocked(parent);
            if (!ok) return false;
        }
        return true;
    }
}
