package com.robertx22.dungeon_realm.packets;

import com.robertx22.dungeon_realm.client.DungeonStatsStore;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.main.MyPacket;
import com.robertx22.library_of_exile.packets.ExilePacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class DungeonStatsPacket  extends MyPacket<DungeonStatsPacket>  {
    public int killCompletionPercent;
    public int lootCompletionPercent;
    public String mapRarityId;

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
    }

    @Override
    public void saveToData(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(killCompletionPercent);
        friendlyByteBuf.writeInt(lootCompletionPercent);
        friendlyByteBuf.writeUtf(mapRarityId);
    }

    @Override
    public void onReceived(ExilePacketContext exilePacketContext) {
        DungeonStatsStore.setKillCompletionPercent(killCompletionPercent);
        DungeonStatsStore.setLootCompletionPercent(lootCompletionPercent);
        DungeonStatsStore.setMapRarityId(mapRarityId);
    }

    @Override
    public MyPacket<DungeonStatsPacket> newInstance() {
        return new DungeonStatsPacket();
    }
}
