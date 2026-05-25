package net.fretux.skillengine.skilltree;

import com.google.gson.*;
import net.fretux.skillengine.SkillEngine;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = SkillEngine.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SkillNodeLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    private static final ResourceLocation DEFAULT_TREE =
            ResourceLocation.fromNamespaceAndPath(SkillEngine.MODID, "main");
    private static final Pattern TRANSLATION_KEY =
            Pattern.compile("[a-z0-9_.-]+\\.[a-z0-9_.-]+");

    public SkillNodeLoader() {
        super(GSON, "skillnodes");
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SkillNodeLoader());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons,
                         @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {
        SkillEngine.LOGGER.info("Loading skill nodes...");
        SkillNodeRegistry.clear();
        jsons.forEach((id, element) -> {
            JsonObject obj = element.getAsJsonObject();
            ResourceLocation nodeId = obj.has("id")
                    ? ResourceLocation.parse(obj.get("id").getAsString())
                    : id;
            Component title = readComponent(obj, "title");
            Component description = readComponent(obj, "description");
            int cost = obj.get("cost").getAsInt();
            JsonObject pos = obj.getAsJsonObject("position");
            float x = pos.get("x").getAsFloat();
            float y = pos.get("y").getAsFloat();
            List<ResourceLocation> links = new ArrayList<>();
            obj.getAsJsonArray("links").forEach(e ->
                    links.add(ResourceLocation.parse(e.getAsString())));
            List<ResourceLocation> tags = new ArrayList<>();
            if (obj.has("tags")) {
                obj.getAsJsonArray("tags").forEach(e ->
                        tags.add(ResourceLocation.parse(e.getAsString())));
            }
            ResourceLocation tree = obj.has("tree")
                    ? ResourceLocation.parse(obj.get("tree").getAsString())
                    : ResourceLocation.fromNamespaceAndPath(nodeId.getNamespace(), DEFAULT_TREE.getPath());
            String category = obj.has("category") ? obj.get("category").getAsString() : "";
            String layer = obj.has("layer") ? obj.get("layer").getAsString() : "";
            ResourceLocation icons = ResourceLocation.parse(obj.get("icons").getAsString());
            Map<String, Integer> prereqAttributes = new HashMap<>();
            if (obj.has("prerequisites")) {
                JsonObject prereqObj = obj.getAsJsonObject("prerequisites");
                for (Map.Entry<String, JsonElement> entry : prereqObj.entrySet()) {
                    prereqAttributes.put(entry.getKey(), entry.getValue().getAsInt());
                }
            }
            List<ResourceLocation> exclusiveWith = new ArrayList<>();
            if (obj.has("exclusive_with")) {
                obj.getAsJsonArray("exclusive_with").forEach(e ->
                        exclusiveWith.add(ResourceLocation.parse(e.getAsString())));
            }
            SkillNode node = new SkillNode(
                    nodeId,
                    title,
                    description,
                    cost,
                    x, y,
                    links,
                    tags,
                    tree,
                    category,
                    layer,
                    icons,
                    prereqAttributes,
                    exclusiveWith
            );

            SkillNodeRegistry.put(node);
        });
        SkillEngine.LOGGER.info("Loaded {} skill nodes", SkillNodeRegistry.all().size());
    }

    private static Component readComponent(JsonObject obj, String key) {
        if (!obj.has(key)) {
            return Component.empty();
        }
        JsonElement element = obj.get(key);
        if (element.isJsonObject()) {
            JsonObject text = element.getAsJsonObject();
            if (text.has("translate")) {
                return Component.translatable(text.get("translate").getAsString());
            }
            if (text.has("text")) {
                return Component.literal(text.get("text").getAsString());
            }
        }
        String value = element.getAsString();
        return TRANSLATION_KEY.matcher(value).matches()
                ? Component.translatable(value)
                : Component.literal(value);
    }
}
