package com.robertx22.dungeon_realm.capability;

import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.registry.IAutoGson;
import com.robertx22.library_of_exile.utils.LoadSave;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DungeonEntityCapability implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public LivingEntity en;

    public static final ResourceLocation RESOURCE = new ResourceLocation(DungeonMain.MODID, "entity");
    public static Capability<DungeonEntityCapability> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    transient final LazyOptional<DungeonEntityCapability> supp = LazyOptional.of(() -> this);

    public DungeonEntityCapability(LivingEntity en) {
        this.en = en;
    }

    public static DungeonEntityCapability get(LivingEntity entity) {
        return entity.getCapability(INSTANCE).orElse(new DungeonEntityCapability(entity));
    }

    public DungeonEntityData data = new DungeonEntityData();

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == INSTANCE) {
            return supp.cast();
        }
        return LazyOptional.empty();

    }

    @Override
    public CompoundTag serializeNBT() {
        var nbt = new CompoundTag();

        try {
            nbt.putString("data", IAutoGson.GSON.toJson(data));
        } catch (Exception e) {
            e.printStackTrace();
        }


        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

        try {
            this.data = LoadSave.loadOrBlank(DungeonEntityData.class, new DungeonEntityData(), nbt, "data", new DungeonEntityData());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
