package com.robertx22.dungeon_realm.client;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DungeonStatsStore {
    private static int killCompletionPercent;
    private static int lootCompletionPercent;
    private static String mapRarityId;
    private static boolean bossTeleportUnlocked;
    private static String mapDungeon;
    private static boolean mapUber;
    private static int rarityProgressPercent;
    private static ItemStack mapItem = ItemStack.EMPTY;
    private static List<String> bonusContentIds = new ArrayList<>();

    public static void setKillCompletionPercent(int killCompletionPercent) {
        DungeonStatsStore.killCompletionPercent = killCompletionPercent;
    }

    public static void setLootCompletionPercent(int lootCompletionPercent) {
        DungeonStatsStore.lootCompletionPercent = lootCompletionPercent;
    }

    public static void setMapRarityId(String mapRarityId) {
        DungeonStatsStore.mapRarityId = mapRarityId;
    }

    public static void setBossTeleportUnlocked(boolean bossTeleportUnlocked) {
        DungeonStatsStore.bossTeleportUnlocked = bossTeleportUnlocked;
    }

    public static void setMapDungeon(String mapDungeon) {
        DungeonStatsStore.mapDungeon = mapDungeon;
    }

    public static void setMapUber(boolean mapUber) {
        DungeonStatsStore.mapUber = mapUber;
    }

    public static void setRarityProgressPercent(int rarityProgressPercent) {
        DungeonStatsStore.rarityProgressPercent = rarityProgressPercent;
    }

    public static int getKillCompletionPercent() {
        return DungeonStatsStore.killCompletionPercent;
    }

    public static int getLootCompletionPercent() {
        return DungeonStatsStore.lootCompletionPercent;
    }

    public static String getMapRarityId() {
        return DungeonStatsStore.mapRarityId;
    }

    public static boolean isBossTeleportUnlocked() {
        return DungeonStatsStore.bossTeleportUnlocked;
    }

    public static String getMapDungeon() {
        return DungeonStatsStore.mapDungeon;
    }

    public static boolean isMapUber() {
        return DungeonStatsStore.mapUber;
    }

    public static int getRarityProgressPercent() {
        return DungeonStatsStore.rarityProgressPercent;
    }

    public static void setMapItem(ItemStack mapItem) {
        DungeonStatsStore.mapItem = mapItem;
    }

    public static ItemStack getMapItem() {
        return DungeonStatsStore.mapItem;
    }

    public static void setBonusContentIds(List<String> bonusContentIds) {
        DungeonStatsStore.bonusContentIds = bonusContentIds != null ? bonusContentIds : new ArrayList<>();
    }

    public static List<String> getBonusContentIds() {
        return DungeonStatsStore.bonusContentIds;
    }
}
