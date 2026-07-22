package com.robertx22.dungeon_realm.capability;

public class DungeonEntityData {

    public boolean isFinalMapBoss = false;
    public boolean isUberBoss = false;
    public boolean isPinnacleBoss = false;
    public boolean isDungeonMob = false;
    public boolean isDungeonEliteMob = false;
    public boolean isMiniBossMob = false;

    public boolean isPackMob = false;

    // ties a mob back to the Strongbox that spawned it (see StrongboxBlock in the main mod's
    // dungeon-realm glue package), so its death can decrement that box's guarded-remaining counter
    // via a LivingDeathEvent hook instead of polling isAlive() by UUID every tick.
    public boolean isStrongboxGuardian = false;
    public long strongboxPos = 0L;
}
