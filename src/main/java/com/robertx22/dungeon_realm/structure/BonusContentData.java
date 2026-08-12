package com.robertx22.dungeon_realm.structure;

public class BonusContentData {
    public static BonusContentData EMPTY = new BonusContentData(0);
    public int remainingSpawns = 0;

    // how many content slots this mechanic won on this map - a mechanic can win several now.
    public int rolled = 0;

    public BonusContentData(int remainingSpawns) {
        this.remainingSpawns = remainingSpawns;
    }

    // always read the count through this, never the field directly. There's no no-arg constructor, so
    // gson builds this class through Unsafe and field initializers never run - every deserialized
    // instance comes back with rolled = 0, not just maps saved before duplicates were possible.
    public int rolledCount() {
        return Math.max(1, rolled);
    }
}
