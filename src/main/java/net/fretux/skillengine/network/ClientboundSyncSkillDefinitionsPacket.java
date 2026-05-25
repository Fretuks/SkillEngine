package net.fretux.skillengine.network;

import net.fretux.skillengine.skilltree.AbilityNode;
import net.fretux.skillengine.skilltree.AbilityNodeRegistry;
import net.fretux.skillengine.skilltree.SkillNode;
import net.fretux.skillengine.skilltree.SkillNodeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ClientboundSyncSkillDefinitionsPacket {

    private final List<SkillNode> skillNodes;
    private final List<AbilityNode> abilityNodes;

    public ClientboundSyncSkillDefinitionsPacket(Collection<SkillNode> skillNodes,
                                                Collection<AbilityNode> abilityNodes) {
        this.skillNodes = List.copyOf(skillNodes);
        this.abilityNodes = List.copyOf(abilityNodes);
    }

    public static void encode(ClientboundSyncSkillDefinitionsPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.skillNodes.size());
        for (SkillNode node : msg.skillNodes) {
            writeSkillNode(buf, node);
        }

        buf.writeInt(msg.abilityNodes.size());
        for (AbilityNode node : msg.abilityNodes) {
            writeAbilityNode(buf, node);
        }
    }

    public static ClientboundSyncSkillDefinitionsPacket decode(FriendlyByteBuf buf) {
        int skillNodeCount = buf.readInt();
        List<SkillNode> skillNodes = new ArrayList<>(skillNodeCount);
        for (int i = 0; i < skillNodeCount; i++) {
            skillNodes.add(readSkillNode(buf));
        }

        int abilityNodeCount = buf.readInt();
        List<AbilityNode> abilityNodes = new ArrayList<>(abilityNodeCount);
        for (int i = 0; i < abilityNodeCount; i++) {
            abilityNodes.add(readAbilityNode(buf));
        }

        return new ClientboundSyncSkillDefinitionsPacket(skillNodes, abilityNodes);
    }

    public static void handle(ClientboundSyncSkillDefinitionsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> net.fretux.skillengine.client.ClientSkillEngineBridge.handleSkillDefinitionsSync(
                        msg.skillNodes,
                        msg.abilityNodes
                )
        ));
        ctx.get().setPacketHandled(true);
    }

    private static void writeSkillNode(FriendlyByteBuf buf, SkillNode node) {
        buf.writeResourceLocation(node.getId());
        buf.writeComponent(node.getTitle());
        buf.writeComponent(node.getDescription());
        buf.writeInt(node.getCost());
        buf.writeFloat(node.getX());
        buf.writeFloat(node.getY());
        writeResourceLocationList(buf, node.getLinks());
        writeResourceLocationList(buf, node.getTags());
        buf.writeResourceLocation(node.getTree());
        buf.writeUtf(node.getCategory());
        buf.writeUtf(node.getLayer());
        buf.writeResourceLocation(node.getIcons());
        writePrerequisites(buf, node.getPrereqAttributes());
        writeResourceLocationList(buf, node.getExclusiveWith());
    }

    private static SkillNode readSkillNode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Component title = buf.readComponent();
        Component description = buf.readComponent();
        int cost = buf.readInt();
        float x = buf.readFloat();
        float y = buf.readFloat();
        List<ResourceLocation> links = readResourceLocationList(buf);
        List<ResourceLocation> tags = readResourceLocationList(buf);
        ResourceLocation tree = buf.readResourceLocation();
        String category = buf.readUtf();
        String layer = buf.readUtf();
        ResourceLocation icon = buf.readResourceLocation();
        Map<String, Integer> prerequisites = readPrerequisites(buf);
        List<ResourceLocation> exclusiveWith = readResourceLocationList(buf);
        return new SkillNode(id, title, description, cost, x, y, links, tags, tree, category, layer, icon, prerequisites, exclusiveWith);
    }

    private static void writeAbilityNode(FriendlyByteBuf buf, AbilityNode node) {
        buf.writeResourceLocation(node.getId());
        buf.writeComponent(node.getTitle());
        buf.writeComponent(node.getDescription());
        buf.writeFloat(node.getX());
        buf.writeFloat(node.getY());
        writeResourceLocationList(buf, node.getLinks());
        writeResourceLocationList(buf, node.getTags());
        buf.writeResourceLocation(node.getIcon());
        buf.writeInt(node.getCooldown());
    }

    private static AbilityNode readAbilityNode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Component title = buf.readComponent();
        Component description = buf.readComponent();
        float x = buf.readFloat();
        float y = buf.readFloat();
        List<ResourceLocation> links = readResourceLocationList(buf);
        List<ResourceLocation> tags = readResourceLocationList(buf);
        ResourceLocation icon = buf.readResourceLocation();
        int cooldown = buf.readInt();
        return new AbilityNode(id, title, description, x, y, links, tags, icon, cooldown);
    }

    private static void writeResourceLocationList(FriendlyByteBuf buf, List<ResourceLocation> ids) {
        buf.writeInt(ids.size());
        for (ResourceLocation id : ids) {
            buf.writeResourceLocation(id);
        }
    }

    private static List<ResourceLocation> readResourceLocationList(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<ResourceLocation> ids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(buf.readResourceLocation());
        }
        return ids;
    }

    private static void writePrerequisites(FriendlyByteBuf buf, Map<String, Integer> prerequisites) {
        buf.writeInt(prerequisites.size());
        for (Map.Entry<String, Integer> entry : prerequisites.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    private static Map<String, Integer> readPrerequisites(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<String, Integer> prerequisites = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            prerequisites.put(buf.readUtf(), buf.readInt());
        }
        return prerequisites;
    }
}
