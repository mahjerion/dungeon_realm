package com.robertx22.dungeon_realm.block_entity;

import com.robertx22.dungeon_realm.main.DungeonEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MapDeviceBE extends BlockEntity {


    public boolean gaveMap = false;
    public BlockPos pos = null;

    public boolean isActivated() {
        return pos != null;
    }

    public void setGaveMap() {
        this.gaveMap = true;
        this.setChanged();
    }

    public MapDeviceBE(BlockPos pPos, BlockState pBlockState) {
        super(DungeonEntries.MAP_DEVICE_BE.get(), pPos, pBlockState);

    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putBoolean("gave", gaveMap);
        if (pos != null) {
            nbt.putLong("spawnpos", pos.asLong());
        }

    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.gaveMap = pTag.getBoolean("gave");
        if (pTag.contains("spawnpos")) {
            this.pos = BlockPos.of(pTag.getLong("spawnpos"));
        }
    }

}
