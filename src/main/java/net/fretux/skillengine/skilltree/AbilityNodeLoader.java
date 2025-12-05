package net.fretux.skillengine.skilltree;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = SkillEngine.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AbilityNodeLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();

    public AbilityNodeLoader() {
        super(GSON, "abilitynodes");
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new AbilityNodeLoader());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons,
                         @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {
        SkillEngine.LOGGER.info("Loading ability nodes...");
        AbilityNodeRegistry.clear();
        jsons.forEach((id, json) -> {
            JsonObject obj = json.getAsJsonObject();
            Component title = Component.literal(obj.get("title").getAsString());
            Component description = Component.literal(obj.get("description").getAsString());
            JsonObject pos = obj.getAsJsonObject("position");
            float x = pos.get("x").getAsFloat();
            float y = pos.get("y").getAsFloat();
            List<ResourceLocation> links = new ArrayList<>();
            obj.getAsJsonArray("links").forEach(e ->
                    links.add(new ResourceLocation(e.getAsString())));
            List<ResourceLocation> tags = new ArrayList<>();
            obj.getAsJsonArray("tags").forEach(e ->
                    tags.add(new ResourceLocation(e.getAsString())));
            ResourceLocation icon = new ResourceLocation(obj.get("icon").getAsString());
            int cooldown = obj.has("cooldown") ? obj.get("cooldown").getAsInt() : 0;
            AbilityNode node = new AbilityNode(id, title, description, x, y, links, tags, icon, cooldown);
            AbilityNodeRegistry.put(node);
        });
        SkillEngine.LOGGER.info("Loaded {} active ability nodes", AbilityNodeRegistry.all().size());
    }
}
