package net.fretux.skillengine.client;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ClientBootstrap {

    private ClientBootstrap() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(ClientKeybinds::registerKeys);
        MinecraftForge.EVENT_BUS.addListener(ClientEvents::onKeyInput);
        MinecraftForge.EVENT_BUS.addListener(AbilityKeybindHandler::onInput);
        MinecraftForge.EVENT_BUS.addListener(ClientCooldownTicker::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(AbilityHudOverlay::render);
    }
}
