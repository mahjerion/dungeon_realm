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

    // same idea for the Imprisoned Monster encounter (see ImprisonedMonsterBlock): ties a caged mob
    // back to the block that released it so its death decrements that block's remaining counter.
    // Polling level.getEntity(uuid) instead would read "dead" for a monster that merely unloaded,
    // handing out the guaranteed reward for free.
    public boolean isImprisonedMonster = false;
    public long imprisonedMonsterPos = 0L;
}
