package com.robertx22.dungeon_realm.block_entity;

import com.robertx22.dungeon_realm.item.DungeonItemNbt;
import com.robertx22.dungeon_realm.item.relic.RelicAffixData;
import com.robertx22.dungeon_realm.item.relic.RelicItemData;
import com.robertx22.dungeon_realm.main.DungeonEntries;
import com.robertx22.dungeon_realm.structure.DungeonMapCapability;
import com.robertx22.library_of_exile.database.relic.stat.ExactRelicStat;
import com.robertx22.library_of_exile.database.relic.stat.RelicMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapDeviceBE extends BlockEntity implements ContainerListener {


    public boolean gaveMap = false;
    public BlockPos pos = null;

    public String currentWorldUUID = "";

    public boolean isActivated() {
        if (currentWorldUUID.isEmpty() || !currentWorldUUID.equals(DungeonMapCapability.getFromServer().data.data.uuid)) {
            return false;
        }

        return pos != null;
    }

    public void setGaveMap() {
        this.gaveMap = true;
        this.setChanged();
    }

    public MapDeviceBE(BlockPos pPos, BlockState pBlockState) {
        super(DungeonEntries.MAP_DEVICE_BE.get(), pPos, pBlockState);

        this.inv.addListener(this);

    }

    public SimpleContainer inv = new SimpleContainer(27);


    public List<ExactRelicStat> getAllValidRelicStats() {
        HashMap<String, Integer> map = new HashMap<>();

        List<RelicItemData> all = new ArrayList<>();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);


            try {
                if (!stack.isEmpty() && DungeonItemNbt.RELIC.has(stack)) {
                    var data = DungeonItemNbt.RELIC.loadFrom(stack);
                    int cur = map.getOrDefault(data.type, 0) + 1;
                    map.put(data.type, cur);
                    if (cur <= data.getType().max_equipped) {
                        all.add(data);
                    } else {
                        int a = 5;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<ExactRelicStat> ex = new ArrayList<>();

        for (RelicItemData data : all) {
            for (RelicAffixData affix : data.affixes) {
                for (RelicMod mod : affix.get().mods) {
                    ex.add(mod.toExact(affix.p));
                }
            }
        }
        return ex;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putBoolean("gave", gaveMap);
        if (pos != null) {
            nbt.putLong("spawnpos", pos.asLong());
        }

        nbt.put("inv", inv.createTag());
        nbt.putString("uid", currentWorldUUID);

    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.gaveMap = pTag.getBoolean("gave");
        if (pTag.contains("spawnpos")) {
            this.pos = BlockPos.of(pTag.getLong("spawnpos"));
        }
        inv.fromTag(pTag.getList("inv", 10)); // todo care when porting
        this.currentWorldUUID = pTag.getString("uid");
    }

    // this i think allows me to make sure the inventory + block entity is dirty easily
    @Override
    public void containerChanged(Container pContainer) {
        this.setChanged();
    }
}
