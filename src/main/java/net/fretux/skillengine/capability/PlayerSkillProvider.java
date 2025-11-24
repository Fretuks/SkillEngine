package net.fretux.skillengine.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerSkillProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

    public static final ResourceLocation ID = new ResourceLocation("skillengine", "player_skill_data");

    private final PlayerSkillData backend = new PlayerSkillData();
    private final LazyOptional<PlayerSkillData> optional = LazyOptional.of(() -> backend);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == SkillEngineCapabilities.PLAYER_SKILLS ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return backend.save();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        backend.load(nbt);
    }
}
