package com.robertx22.dungeon_realm.api;

import com.robertx22.library_of_exile.events.base.ExileEventCaller;

public class DungeonExileEvents {

    public static ExileEventCaller<CanStartMapEvent> CAN_START_MAP = new ExileEventCaller<>();
    public static ExileEventCaller<CanEnterMapEvent> CAN_ENTER_MAP = new ExileEventCaller<>();
    public static ExileEventCaller<DungeonMobSpawnedEvent> DUNGEON_MOB_SPAWNED = new ExileEventCaller<>();
    public static ExileEventCaller<PrepareDungeonMobEditsEvent> PREPARE_DUNGEON_MOB_SPAWN = new ExileEventCaller<>();
    public static ExileEventCaller<SpawnUberEvent> ON_SPAWN_UBER_BOSS = new ExileEventCaller<>();
    public static ExileEventCaller<SpawnPinnacleEvent> ON_SPAWN_PINNACLE_BOSS = new ExileEventCaller<>();
    public static ExileEventCaller<OnStartMapEvent> ON_START_NEW_MAP = new ExileEventCaller<>();
    public static ExileEventCaller<OnGenerateNewMapItemEvent> ON_GENERATE_NEW_MAP_ITEM = new ExileEventCaller<>();
    public static ExileEventCaller<OnMapFullyClearedEvent> ON_MAP_FULLY_CLEARED = new ExileEventCaller<>();
    public static ExileEventCaller<GetUnlockedAtlasNodesEvent> GET_UNLOCKED_ATLAS_NODES = new ExileEventCaller<>();
    public static ExileEventCaller<GetPackSizeBonusEvent> GET_PACK_SIZE_BONUS = new ExileEventCaller<>();
    public static ExileEventCaller<GetUberFragmentFindBonusEvent> GET_UBER_FRAGMENT_FIND_BONUS = new ExileEventCaller<>();
    public static ExileEventCaller<GetMapContentWeightBonusEvent> GET_MAP_CONTENT_WEIGHT_BONUS = new ExileEventCaller<>();
    public static ExileEventCaller<OpenAtlasMapEvent> OPEN_ATLAS_MAP = new ExileEventCaller<>();

    public static void init() {

    }
}
