package com.robertx22.dungeon_realm.structure;


import com.robertx22.dungeon_realm.api.DungeonExileEvents;
import com.robertx22.dungeon_realm.api.GetBonusContentChanceEvent;
import com.robertx22.dungeon_realm.api.GetMapContentWeightBonusEvent;
import com.robertx22.dungeon_realm.database.holders.DungeonBonusContents;
import com.robertx22.dungeon_realm.database.holders.DungeonRelicStats;
import com.robertx22.dungeon_realm.item.DungeonItemNbt;
import com.robertx22.library_of_exile.components.LibMapData;
import com.robertx22.library_of_exile.database.extra_map_content.MapContent;
import com.robertx22.library_of_exile.database.init.LibDatabase;
import com.robertx22.library_of_exile.database.relic.stat.ContentWeightRS;
import com.robertx22.library_of_exile.database.relic.stat.ExtraContentRS;
import com.robertx22.library_of_exile.database.relic.stat.GuaranteeContentRS;
import com.robertx22.library_of_exile.database.relic.stat.RelicStat;
import com.robertx22.library_of_exile.dimension.structure.MapStructure;
import com.robertx22.library_of_exile.util.PointData;
import com.robertx22.library_of_exile.util.Weighted;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapBonusContentsData {

    HashMap<String, BonusContentData> map = new HashMap<>();

    public int totalGenDungeonChunks = 0;
    public int processedChunks = 0;

    // chunks mechanics were tried to spawn or succeded in (limit 1 per chunk, both attempts and successes)
    public Set<PointData> mechsChunks = new HashSet<>();

    // calc chance in such a way that if a mechanic should spawn 5 times, and the dungeon has 20 chunks left, it scatters the spawn chunks but never accidentally not generates
    public float calcSpawnChance(BlockPos pos) {

        var cp = new ChunkPos(pos);
        var point = new PointData(cp.x, cp.z);

        if (mechsChunks.contains(point)) {
            return 0;
        }

        int remaining = getTotalSpawnsRemainingFromAllContents();
        int chunksLeft = totalGenDungeonChunks - processedChunks;

        if (chunksLeft < remaining) {
            return 100;
        }

        float chance = remaining / (float) chunksLeft * 100F;

        return chance;

    }


    public int getTotalSpawnsRemainingFromAllContents() {
        return (int) map.values().stream().mapToInt(x -> x.remainingSpawns).sum();
    }


    void addContent(MapContent c, LibMapData libdata) {
        int amount = RandomUtils.RandomRange(c.min_blocks, c.max_blocks);

        for (RelicStat stat : LibDatabase.RelicStats().getList()) {
            if (stat instanceof ExtraContentRS extra && extra.data.type() == ExtraContentRS.Type.ADDITION) {
                if (extra.data.map_content_id().equals(c.GUID())) {
                    if (RandomUtils.roll(libdata.relicStats.get(stat))) {
                        amount += extra.data.extra();
                    }
                }
            }
        }
        for (RelicStat stat : LibDatabase.RelicStats().getList()) {
            if (stat instanceof ExtraContentRS extra && extra.data.type() == ExtraContentRS.Type.MULTIPLY) {
                if (extra.data.map_content_id().equals(c.GUID())) {
                    if (RandomUtils.roll(libdata.relicStats.get(stat))) {
                        amount *= extra.data.extra();
                    }
                }
            }
        }

        // a mechanic can win more than one content slot, so stack the block budget instead of
        // replacing it - winning Harvest twice means twice as many Harvest blocks in the map. The
        // ExtraContentRS rolls above run once per slot won, which is intended: more instances of the
        // mechanic means more chances at its bonus blocks.
        var existing = this.map.get(c.GUID());
        if (existing != null) {
            existing.remainingSpawns += amount;
            existing.rolled = existing.rolledCount() + 1;
        } else {
            var data = new BonusContentData(amount);
            data.rolled = 1;
            this.map.put(c.GUID(), data);
        }
    }

    public void setupOnMapStart(ItemStack stack, LibMapData libdata, Player p) {

        var map = DungeonItemNbt.DUNGEON_MAP.loadFrom(stack);

        for (MapContent c : LibDatabase.MapContent().getFiltered(x -> x.always_spawn)) {
            addContent(c, libdata);
        }

        int bonus = map.bonus_contents;

        // One shared pool rather than a separate coin flip per source: the BONUS_CONTENT_CHANCE relic
        // stat plus the main mod's contribution (map tier scaling + the Atlas double_event_chance
        // node). Every full 100% in the pool is one guaranteed extra content and the leftover is
        // rolled once, so the pool can grant more than +1 - a 250% pool is +2 and a 50% roll for a 3rd.
        float pool = libdata.relicStats.get(DungeonRelicStats.INSTANCE.BONUS_CONTENT_CHANCE);
        pool += DungeonExileEvents.GET_BONUS_CONTENT_CHANCE.callEvents(
                new GetBonusContentChanceEvent(p, stack)).bonusPercent;

        if (pool > 0) {
            bonus += (int) (pool / 100F);
            if (RandomUtils.roll(pool % 100F)) {
                bonus++;
            }
        }

        List<Weighted<MapContent>> possible = new ArrayList<>();
        for (MapContent e : LibDatabase.MapContent().getFiltered(x -> !x.always_spawn && x.Weight() > 0)) {

            // player-stat parallel to ContentWeightRS: the starter's Atlas "event chance" node for this
            // league raises its weight, so it's more likely to be among the bonus contents picked below.
            // Also where the main mod gates a league mechanic below its configured min map level - a
            // blocked content is dropped entirely rather than zero-weighted (see GetMapContentWeightBonusEvent).
            var weightEvent = DungeonExileEvents.GET_MAP_CONTENT_WEIGHT_BONUS.callEvents(
                    new GetMapContentWeightBonusEvent(List.of(p), e.GUID()));

            if (weightEvent.blocked) {
                continue;
            }

            float weight = e.weight;
            for (RelicStat stat : LibDatabase.RelicStats().getList()) {
                if (stat instanceof ContentWeightRS cw && cw.map_content_id.equals(e.GUID())) {
                    weight *= 1F + libdata.relicStats.get(cw) / 100F;
                }
            }
            weight *= 1F + weightEvent.bonusPercent / 100F;
            possible.add(new Weighted<>(e, (int) weight));
        }

        // Relic guarantees (the implicit affix on relics) claim bonus slots before the random picks.
        // A guaranteed mechanic deliberately stays in `possible` afterwards, so the chance stats for
        // that same mechanic can still win it more slots - guaranteeing Harvest secures one and a
        // stacked +Harvest build can add more on top, rather than the guarantee making those stats
        // dead. Only draws from `possible`, so a guarantee for content that's blocked by its min
        // level config - or whose mod isn't installed - is a no-op and that slot stays random.
        List<MapContent> guaranteed = new ArrayList<>();
        for (RelicStat stat : LibDatabase.RelicStats().getList()) {
            if (stat instanceof GuaranteeContentRS g && RandomUtils.roll(libdata.relicStats.get(g))) {
                possible.stream()
                        .filter(x -> x.obj.GUID().equals(g.map_content_id))
                        .findFirst()
                        .ifPresent(w -> guaranteed.add(w.obj));
            }
        }
        Collections.shuffle(guaranteed); // fair pick when there are more guarantees than slots
        for (MapContent c : guaranteed) {
            if (bonus <= 0) {
                break;
            }
            addContent(c, libdata);
            bonus--;
        }

        // every slot is an independent weighted roll - the same mechanic can win several, which is
        // what keeps the per-league chance stats worth stacking once a guarantee has already claimed
        // one. The empty check is required rather than defensive: there used to be a clamp of bonus
        // to possible.size() above, and without it a map whose mechanics are all level-gated would
        // reach weightedRandom with an empty list.
        for (int i = 0; i < bonus; i++) {
            if (possible.isEmpty()) {
                break;
            }
            var c = RandomUtils.weightedRandom(possible).obj;
            addContent(c, libdata);
        }

        // pinnacle maps reuse the exact same arena/altar content as uber maps - only what the
        // altar actually spawns differs, read from this same map's own `pinnacle` flag
        if (map.uber || map.pinnacle) {
            addContent(DungeonBonusContents.INSTANCE.UBER_BOSS.get(), libdata);
        }
    }

    public BonusContentData get(MapStructure m) {
        return map.getOrDefault(m.guid(), BonusContentData.EMPTY);
    }

    // ids of league content rolled for this map instance, excluding uber_boss (that's shown
    // separately via DungeonStatsStore.isMapUber()). Sorted for a stable display order. A mechanic
    // that won several slots is listed once per slot, so the map screen shows it repeated without
    // needing to know anything about counts.
    public List<String> getRolledLeagueContentIds() {
        List<String> list = new ArrayList<>();
        map.entrySet().stream()
                .filter(en -> !en.getKey().equals(DungeonBonusContents.INSTANCE.UBER_BOSS.get().GUID()))
                .sorted(Map.Entry.comparingByKey())
                .forEach(en -> {
                    for (int i = 0; i < en.getValue().rolledCount(); i++) {
                        list.add(en.getKey());
                    }
                });
        return list;
    }

}
