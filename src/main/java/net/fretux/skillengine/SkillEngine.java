package net.fretux.skillengine;

import com.mojang.logging.LogUtils;
import net.fretux.skillengine.capability.SkillEngineCapabilities;
import net.fretux.skillengine.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SkillEngine.MODID)
public class SkillEngine {

    public static final String MODID = "skillengine";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SkillEngine() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        PacketHandler.register();
    }
}
