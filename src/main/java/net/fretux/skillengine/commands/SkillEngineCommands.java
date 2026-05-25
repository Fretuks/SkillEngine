package net.fretux.skillengine.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fretux.skillengine.SkillEngine;
import net.fretux.skillengine.skilltree.SkillTreeValidator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = SkillEngine.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SkillEngineCommands {

    private SkillEngineCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("skillengine")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("validate")
                        .executes(context -> validate(context.getSource()))));
    }

    private static int validate(CommandSourceStack source) {
        List<String> issues = SkillTreeValidator.validate();
        if (issues.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Skill Engine validation passed."), false);
            return 1;
        }
        source.sendFailure(Component.literal("Skill Engine validation found " + issues.size() + " issue(s):"));
        for (String issue : issues) {
            SkillEngine.LOGGER.warn("[SkillEngine validate] {}", issue);
            source.sendFailure(Component.literal("- " + issue));
        }
        return 0;
    }
}
