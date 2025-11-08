package com.robertx22.dungeon_realm.client;

public class DungeonStatsStore {
    private static int killCompletionPercent;
    private static int lootCompletionPercent;
    private static String mapRarityId;

    public static void setKillCompletionPercent(int killCompletionPercent) {
        DungeonStatsStore.killCompletionPercent = killCompletionPercent;
    }

    public static void setLootCompletionPercent(int lootCompletionPercent) {
        DungeonStatsStore.lootCompletionPercent = lootCompletionPercent;
    }

    public static void setMapRarityId(String mapRarityId) {
        DungeonStatsStore.mapRarityId = mapRarityId;
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
}
