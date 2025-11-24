package net.fretux.skillengine.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.fretux.skillengine.SkillEngine;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(SkillEngine.MODID, "network"))
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .networkProtocolVersion(() -> PROTOCOL)
            .simpleChannel();

    private static int id = 0;

    public static void register() {
        CHANNEL.messageBuilder(ServerboundUnlockNodePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundUnlockNodePacket::encode)
                .decoder(ServerboundUnlockNodePacket::decode)
                .consumerMainThread(ServerboundUnlockNodePacket::handle)
                .add();
        CHANNEL.messageBuilder(ClientboundNodeUnlockedPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundNodeUnlockedPacket::encode)
                .decoder(ClientboundNodeUnlockedPacket::decode)
                .consumerMainThread(ClientboundNodeUnlockedPacket::handle)
                .add();
        CHANNEL.messageBuilder(ClientboundSyncSkillsPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundSyncSkillsPacket::encode)
                .decoder(ClientboundSyncSkillsPacket::decode)
                .consumerMainThread(ClientboundSyncSkillsPacket::handle)
                .add();
    }
}
