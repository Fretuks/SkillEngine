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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = SkillEngine.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SkillNodeLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();

    public SkillNodeLoader() {
        super(GSON, "skillnodes");
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SkillNodeLoader());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        SkillEngine.LOGGER.info("Loading skill nodes...");
        SkillNodeRegistry.clear();
        jsons.forEach((id, element) -> {
            JsonObject obj = element.getAsJsonObject();
            String title = obj.get("title").getAsString();
            String description = obj.get("description").getAsString();
            int cost = obj.get("cost").getAsInt();
            JsonObject pos = obj.getAsJsonObject("position");
            float x = pos.get("x").getAsFloat();
            float y = pos.get("y").getAsFloat();
            List<ResourceLocation> links = new ArrayList<>();
            obj.getAsJsonArray("links").forEach(e ->
                    links.add(new ResourceLocation(e.getAsString())));
            List<ResourceLocation> tags = new ArrayList<>();
            obj.getAsJsonArray("tags").forEach(e ->
                    tags.add(new ResourceLocation(e.getAsString())));
            ResourceLocation icons = new ResourceLocation(obj.get("icons").getAsString());
            Map<String, Integer> prereqAttributes = new HashMap<>();
            if (obj.has("prerequisites")) {
                JsonObject prereqObj = obj.getAsJsonObject("prerequisites");
                for (Map.Entry<String, JsonElement> entry : prereqObj.entrySet()) {
                    prereqAttributes.put(entry.getKey(), entry.getValue().getAsInt());
                }
            }
            SkillNode node = new SkillNode(
                    id,
                    Component.literal(title),
                    Component.literal(description),
                    cost,
                    x, y,
                    links,
                    tags,
                    icons,
                    prereqAttributes
            );
            SkillNodeRegistry.put(node);
        });
        SkillEngine.LOGGER.info("Loaded {} skill nodes", SkillNodeRegistry.all().size());
    }
}
