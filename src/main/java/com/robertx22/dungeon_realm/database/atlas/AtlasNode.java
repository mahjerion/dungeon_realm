package com.robertx22.dungeon_realm.database.atlas;

import com.robertx22.dungeon_realm.database.DungeonDatabase;
import com.robertx22.library_of_exile.registry.ExileRegistryType;
import com.robertx22.library_of_exile.registry.IAutoGson;
import com.robertx22.library_of_exile.registry.JsonExileRegistry;

// A node in the Atlas map graph. Maps a dungeon layout (by GUID) to a requirement gate.
// Lives in dungeon_realm (rather than library_of_exile) because it's dungeon/map domain data -
// dungeon_realm already owns Dungeon/DungeonMapItem/DungeonDatabase, and depends on
// library_of_exile one-way, so this is the natural home for it.
// Position and connections between nodes are NOT declared here - they live in the main mod's
// AtlasNodeLayout datapack entry (one grid string referencing node ids by this class's `id`,
// same pattern as Perk/TalentTree), so this class stays purely "what is this node" and can't
// drift out of sync with a hand-declared neighbor list.
// See AtlasNodeUtils for lookups.
public class AtlasNode implements JsonExileRegistry<AtlasNode>, IAutoGson<AtlasNode> {

    public static AtlasNode SERIALIZER = new AtlasNode();

    public String id = "";
    public String dungeon = "";
    public boolean starting_node = false;
    public int atlas_points_reward = 1;

    // optional extra completion requirements, evaluated against the actual map run's rolled
    // attributes - lets the same dungeon appear more than once in the tree (see
    // AtlasNodeUtils.byDungeon), with a later appearance demanding a harder run. 0/empty/false
    // means "no extra requirement". Kept as plain primitives since GearRarity (which min_rarity
    // refers to) lives in the main mod, which dungeon_realm has no visibility into - the
    // requirement is actually evaluated where GearRarity is visible.
    public int min_tier = 0;
    public String min_rarity = "";
    public boolean require_uber = false;

    @Override
    public ExileRegistryType getExileRegistryType() {
        return DungeonDatabase.ATLAS_NODE;
    }

    @Override
    public Class<AtlasNode> getClassForSerialization() {
        return AtlasNode.class;
    }

    @Override
    public String GUID() {
        return id;
    }

    @Override
    public int Weight() {
        return 1000;
    }
}
