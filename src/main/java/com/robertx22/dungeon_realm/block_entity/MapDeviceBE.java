package com.robertx22.dungeon_realm.block_entity;

import com.robertx22.dungeon_realm.api.CanStartMapEvent;
import com.robertx22.dungeon_realm.api.DungeonExileEvents;
import com.robertx22.dungeon_realm.block.MapDeviceBlock;
import com.robertx22.dungeon_realm.item.DungeonItemNbt;
import com.robertx22.dungeon_realm.item.relic.RelicSlotUtil;
import com.robertx22.dungeon_realm.main.DungeonEntries;
import com.robertx22.dungeon_realm.structure.DungeonMapCapability;
import com.robertx22.library_of_exile.database.relic.stat.RelicStatsContainer;
import com.robertx22.library_of_exile.dimension.device.IMapDeviceBlockEntity;
import com.robertx22.library_of_exile.dimension.device.MapDeviceKind;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MapDeviceBE extends BlockEntity implements ContainerListener, IMapDeviceBlockEntity {

    public boolean gaveMap = false;
    public BlockPos pos = null;

    public String currentWorldUUID = "";

    // slot 0 = the map, 1..4 = relics. see IMapDeviceBlockEntity
    public SimpleContainer deviceInv = new SimpleContainer(SIZE);

    // relics from a device saved before the 4 slot layout that did not fit the new grid. load() runs
    // before the level is assigned, so they are dropped from the block ticker instead.
    public List<ItemStack> pendingSpill = new ArrayList<>();

    private static final String LEGACY_INV_KEY = "inv";
    private static final String INV_KEY = "device_inv";

    public MapDeviceBE(BlockPos pPos, BlockState pBlockState) {
        super(DungeonEntries.MAP_DEVICE_BE.get(), pPos, pBlockState);
        this.deviceInv.addListener(this);
    }

    @Override
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

    // ------------------------------------------------------------------ IMapDeviceBlockEntity

    @Override
    public SimpleContainer getDeviceInventory() {
        return deviceInv;
    }

    @Override
    public MapDeviceKind getDeviceKind() {
        return MapDeviceKind.DUNGEON;
    }

    @Override
    public boolean hasMapSlot(Level level) {
        return true;
    }

    @Override
    public boolean acceptsMapItem(ItemStack stack) {
        return !stack.isEmpty() && DungeonItemNbt.DUNGEON_MAP.has(stack);
    }

    @Override
    public boolean isFreeRunAvailable(Level level) {
        return false;
    }

    @Override
    public boolean startMap(Player player, Supplier<RelicStatsContainer> relicStats) {
        ItemStack stack = deviceInv.getItem(MAP_SLOT);
        if (!acceptsMapItem(stack)) {
            return false;
        }

        // the main mod gates this on level, cooldown and resistances
        var event = new CanStartMapEvent(stack, player);
        DungeonExileEvents.CAN_START_MAP.callEvents(event);
        if (!event.canEnter) {
            return false;
        }

        MapDeviceBlock.startNewMap(player, stack, this, relicStats);
        return true;
    }

    @Override
    public boolean joinMap(Player player) {
        if (!isActivated()) {
            return false;
        }
        // false: the instance is already running, so no spawn grace - see MapDeviceBlock.joinCurrentMap
        return MapDeviceBlock.joinCurrentMap(player, this, false);
    }

    // ------------------------------------------------------------------ nbt

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putBoolean("gave", gaveMap);
        if (pos != null) {
            nbt.putLong("spawnpos", pos.asLong());
        }

        nbt.put(INV_KEY, deviceInv.createTag());
        if (!pendingSpill.isEmpty()) {
            SimpleContainer spill = new SimpleContainer(pendingSpill.size());
            for (int i = 0; i < pendingSpill.size(); i++) {
                spill.setItem(i, pendingSpill.get(i));
            }
            nbt.put("spill", spill.createTag());
        }
        nbt.putString("uid", currentWorldUUID);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.gaveMap = pTag.getBoolean("gave");
        if (pTag.contains("spawnpos")) {
            this.pos = BlockPos.of(pTag.getLong("spawnpos"));
        }
        this.currentWorldUUID = pTag.getString("uid");

        if (pTag.contains(INV_KEY)) {
            deviceInv.fromTag(pTag.getList(INV_KEY, 10));
        } else if (pTag.contains(LEGACY_INV_KEY)) {
            migrateLegacyInventory(pTag);
        }

        if (pTag.contains("spill")) {
            SimpleContainer spill = new SimpleContainer(64);
            spill.fromTag(pTag.getList("spill", 10));
            for (int i = 0; i < spill.getContainerSize(); i++) {
                if (!spill.getItem(i).isEmpty()) {
                    pendingSpill.add(spill.getItem(i));
                }
            }
        }
    }

    /**
     * Devices saved before the GUI rework had a 27 slot relic chest. The first four relics move into the
     * relic slots in their old order, everything else is queued to be dropped on the ground by the ticker
     * so nothing is silently lost.
     */
    private void migrateLegacyInventory(CompoundTag pTag) {
        SimpleContainer legacy = new SimpleContainer(27);
        legacy.fromTag(pTag.getList(LEGACY_INV_KEY, 10));

        int relicSlot = RELIC_SLOT_START;
        for (int i = 0; i < legacy.getContainerSize(); i++) {
            ItemStack stack = legacy.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (relicSlot < RELIC_SLOT_START + RELIC_SLOTS && RelicSlotUtil.isRelic(stack)) {
                deviceInv.setItem(relicSlot++, stack);
            } else {
                pendingSpill.add(stack);
            }
        }
        // the next save writes the new key, so this only ever runs once per device
        setChanged();
    }

    // this i think allows me to make sure the inventory + block entity is dirty easily
    @Override
    public void containerChanged(Container pContainer) {
        this.setChanged();
    }
}
