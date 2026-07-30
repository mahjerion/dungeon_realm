package com.robertx22.dungeon_realm.item;

public class DungeonMapGenSettings {

    // whether this map is allowed to inherit a tier floor from the map the player is currently in.
    // Defaults to OFF - the position of the player is not enough to decide this. Crafting a map with
    // the Map Creator or identifying a creative/give map also happens at the player's position inside
    // a map, and neither should buy you tier. Only actual loot drops opt in.
    public boolean inheritMapTier = false;

    // map boss drops use a stricter band: never below the run's tier, with a small chance to rise.
    // This is the intended ascent vector, so it only applies where the map was earned by a boss kill.
    public boolean fromMapBoss = false;

    public static DungeonMapGenSettings ofMapBoss() {
        DungeonMapGenSettings settings = new DungeonMapGenSettings();
        settings.inheritMapTier = true;
        settings.fromMapBoss = true;
        return settings;
    }
}
