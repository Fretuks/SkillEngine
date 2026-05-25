package net.fretux.skillengine.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class SkillEngineConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec.IntValue ABILITY_SLOT_COUNT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ABILITY_SLOT_COUNT = builder
                .comment("Number of ability slots available to each player.")
                .defineInRange("abilitySlotCount", 3, 1, 9);
        COMMON_SPEC = builder.build();
    }

    private SkillEngineConfig() {}
}
