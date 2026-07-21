package com.robertx22.dungeon_realm.packets;

import com.robertx22.dungeon_realm.client.DungeonStatsStore;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.main.MyPacket;
import com.robertx22.library_of_exile.packets.ExilePacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DungeonStatsPacket  extends MyPacket<DungeonStatsPacket>  {
    public int killCompletionPercent;
    public int lootCompletionPercent;
    public String mapRarityId;
    public boolean bossTeleportUnlocked;
    public String mapDungeon = "";
    public boolean mapUber;
    public int rarityProgressPercent;
    // snapshot of the started map's item, sent only on map entry (empty on the frequent event packets)
    public ItemStack snapshotStack = ItemStack.EMPTY;

    public DungeonStatsPacket() {}

    public DungeonStatsPacket(int killCompletionPercent, int lootCompletionPercent, String mapRarity) {
        this.killCompletionPercent = killCompletionPercent;
        this.lootCompletionPercent = lootCompletionPercent;
        this.mapRarityId = mapRarity;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return new ResourceLocation(DungeonMain.MODID, "dungeon_stats");
    }

    @Override
    public void loadFromData(FriendlyByteBuf friendlyByteBuf) {
        this.killCompletionPercent = friendlyByteBuf.readInt();
        this.lootCompletionPercent = friendlyByteBuf.readInt();
        this.mapRarityId = friendlyByteBuf.readUtf();
        this.bossTeleportUnlocked = friendlyByteBuf.readBoolean();
        this.mapDungeon = friendlyByteBuf.readUtf();
        this.mapUber = friendlyByteBuf.readBoolean();
        this.rarityProgressPercent = friendlyByteBuf.readInt();
        this.snapshotStack = friendlyByteBuf.readItem();
    }

    @Override
    public void saveToData(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(killCompletionPercent);
        friendlyByteBuf.writeInt(lootCompletionPercent);
        friendlyByteBuf.writeUtf(mapRarityId);
        friendlyByteBuf.writeBoolean(bossTeleportUnlocked);
        friendlyByteBuf.writeUtf(mapDungeon == null ? "" : mapDungeon);
        friendlyByteBuf.writeBoolean(mapUber);
        friendlyByteBuf.writeInt(rarityProgressPercent);
        friendlyByteBuf.writeItem(snapshotStack);
    }

    @Override
    public void onReceived(ExilePacketContext exilePacketContext) {
        DungeonStatsStore.setKillCompletionPercent(killCompletionPercent);
        DungeonStatsStore.setLootCompletionPercent(lootCompletionPercent);
        DungeonStatsStore.setMapRarityId(mapRarityId);
        DungeonStatsStore.setBossTeleportUnlocked(bossTeleportUnlocked);
        DungeonStatsStore.setMapDungeon(mapDungeon);
        DungeonStatsStore.setMapUber(mapUber);
        DungeonStatsStore.setRarityProgressPercent(rarityProgressPercent);
        // only the entry packet carries the snapshot; ignore the empty item on event packets so it
        // doesn't wipe the stored snapshot
        if (!snapshotStack.isEmpty()) {
            DungeonStatsStore.setMapItem(snapshotStack);
        }
    }

    @Override
    public MyPacket<DungeonStatsPacket> newInstance() {
        return new DungeonStatsPacket();
    }
}
