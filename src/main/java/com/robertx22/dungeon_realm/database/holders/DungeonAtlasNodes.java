package com.robertx22.dungeon_realm.database.holders;

import com.robertx22.dungeon_realm.database.atlas.AtlasNode;
import com.robertx22.dungeon_realm.main.DungeonMain;
import com.robertx22.library_of_exile.registry.helpers.ExileKey;
import com.robertx22.library_of_exile.registry.helpers.ExileKeyHolder;
import com.robertx22.library_of_exile.registry.helpers.KeyInfo;
import com.robertx22.library_of_exile.registry.register_info.ModRequiredRegisterInfo;

// Java-driven registration for the Atlas unlock map's nodes, replacing what used to be 21
// hand-authored datapack JSON files - every time AtlasNode gained a field, every one of those
// files needed hand-patching or it'd log a "datapack check failed" warning. This class is now
// the source of truth; it auto-generates the equivalent JSON into src/generated/resources (via
// the normal ExileKeyHolder/JsonExileRegistry datagen pipeline - see DungeonUberBosses for the
// same pattern), so nothing about datapack support is lost - a modpack author can still add or
// override AtlasNode entries with their own hand-authored JSON exactly as before.
public class DungeonAtlasNodes extends ExileKeyHolder<AtlasNode> {

    public static DungeonAtlasNodes INSTANCE = new DungeonAtlasNodes(DungeonMain.REGISTER_INFO);

    public DungeonAtlasNodes(ModRequiredRegisterInfo modRegisterInfo) {
        super(modRegisterInfo);
    }

    // the 17 base dungeon nodes - unchanged from before
    public ExileKey<AtlasNode, KeyInfo> CEMENT = ExileKey.ofId(this, "cement", x -> AtlasNode.of(x.GUID(), "cement", 1).startingNode());
    public ExileKey<AtlasNode, KeyInfo> WARPED = ExileKey.ofId(this, "warped", x -> AtlasNode.of(x.GUID(), "warped", 1).startingNode());
    public ExileKey<AtlasNode, KeyInfo> SANDSTONE = ExileKey.ofId(this, "sandstone", x -> AtlasNode.of(x.GUID(), "sandstone", 1).startingNode());
    public ExileKey<AtlasNode, KeyInfo> WN = ExileKey.ofId(this, "wn", x -> AtlasNode.of(x.GUID(), "wn", 1));
    public ExileKey<AtlasNode, KeyInfo> BASTION = ExileKey.ofId(this, "bastion", x -> AtlasNode.of(x.GUID(), "bastion", 1));
    public ExileKey<AtlasNode, KeyInfo> IT = ExileKey.ofId(this, "it", x -> AtlasNode.of(x.GUID(), "it", 1));
    public ExileKey<AtlasNode, KeyInfo> NATURE = ExileKey.ofId(this, "nature", x -> AtlasNode.of(x.GUID(), "nature", 1));
    public ExileKey<AtlasNode, KeyInfo> SEWER2 = ExileKey.ofId(this, "sewer2", x -> AtlasNode.of(x.GUID(), "sewer2", 1));
    public ExileKey<AtlasNode, KeyInfo> MOSSY_BRICK = ExileKey.ofId(this, "mossy_brick", x -> AtlasNode.of(x.GUID(), "mossy_brick", 1));
    public ExileKey<AtlasNode, KeyInfo> MINE = ExileKey.ofId(this, "mine", x -> AtlasNode.of(x.GUID(), "mine", 1));
    public ExileKey<AtlasNode, KeyInfo> STONE_BRICK = ExileKey.ofId(this, "stone_brick", x -> AtlasNode.of(x.GUID(), "stone_brick", 1));
    public ExileKey<AtlasNode, KeyInfo> SEWERS = ExileKey.ofId(this, "sewers", x -> AtlasNode.of(x.GUID(), "sewers", 1));
    public ExileKey<AtlasNode, KeyInfo> NETHER = ExileKey.ofId(this, "nether", x -> AtlasNode.of(x.GUID(), "nether", 1));
    public ExileKey<AtlasNode, KeyInfo> SPRUCE_MANSION = ExileKey.ofId(this, "spruce_mansion", x -> AtlasNode.of(x.GUID(), "spruce_mansion", 1));
    public ExileKey<AtlasNode, KeyInfo> TENT = ExileKey.ofId(this, "tent", x -> AtlasNode.of(x.GUID(), "tent", 1));
    public ExileKey<AtlasNode, KeyInfo> STEAMPUNK = ExileKey.ofId(this, "steampunk", x -> AtlasNode.of(x.GUID(), "steampunk", 1));
    public ExileKey<AtlasNode, KeyInfo> NIGHT_TERROR = ExileKey.ofId(this, "night_terror", x -> AtlasNode.of(x.GUID(), "night_terror", 1));

    // the one special capstone that gates the Pinnacle unlock - kept as its own named entry,
    // separate from the systematic per-dungeon generation below. The only node allowed a reward
    // above 1 - see loadClass()'s comment for why.
    public ExileKey<AtlasNode, KeyInfo> NIGHT_TERROR_APEX = ExileKey.ofId(this, "night_terror_apex",
            x -> AtlasNode.of(x.GUID(), "night_terror", 3).minTier(15).requireUber().pinnacleUnlock());

    // every dungeon that has a base AtlasNode above - this list drives the systematic generation
    // in loadClass() below. Replaces the old one-off examples (mine_uber, nether_tier,
    // steampunk_rare) with a full, consistent set per dungeon instead of a handful of samples.
    private static final String[] DUNGEONS = {
            "cement", "warped", "sandstone", "wn", "bastion", "it", "nature", "sewer2",
            "mossy_brick", "mine", "stone_brick", "sewers", "nether", "spruce_mansion",
            "tent", "steampunk", "night_terror"
    };

    @Override
    public void loadClass() {
        // for every dungeon: a repeat node every 10 map tiers (10 through 100 - 100 is the actual
        // ceiling, see GearRarity's map_tiers ranges: common 0-10 ... mythic 80-100), plus one
        // require_uber repeat node. Rarity-gated repeats are skipped per direction - tier and
        // rarity are already tied together for maps (each GearRarity owns a map_tiers range), so
        // a separate rarity axis wouldn't add anything a tier threshold doesn't already cover.
        // Reward stays flat at 1 regardless of tier/uber - only the Pinnacle-unlock capstone
        // (night_terror_apex, above) is allowed to reward more (3), since that's the one node
        // meant to feel like a distinct milestone rather than routine progression.
        for (String dungeon : DUNGEONS) {
            for (int tier = 10; tier <= 100; tier += 10) {
                final int t = tier;
                ExileKey.ofId(this, dungeon + "_tier" + t, x -> AtlasNode.of(x.GUID(), dungeon, 1).minTier(t));
            }
            ExileKey.ofId(this, dungeon + "_uber", x -> AtlasNode.of(x.GUID(), dungeon, 1).requireUber());
        }
    }
}
