package com.robertx22.dungeon_realm.main;

import com.google.common.collect.Lists;
import com.robertx22.dungeon_realm.api.DungeonExileEvents;
import com.robertx22.dungeon_realm.capability.DungeonEntityCapability;
import com.robertx22.dungeon_realm.configs.ObeliskConfig;
import com.robertx22.dungeon_realm.database.DungeonDatabase;
import com.robertx22.dungeon_realm.item.ObeliskMapItem;
import com.robertx22.dungeon_realm.structure.*;
import com.robertx22.library_of_exile.config.map_dimension.MapDimensionConfig;
import com.robertx22.library_of_exile.config.map_dimension.MapDimensionConfigDefaults;
import com.robertx22.library_of_exile.config.map_dimension.MapRegisterBuilder;
import com.robertx22.library_of_exile.dimension.MapChunkGenEvent;
import com.robertx22.library_of_exile.dimension.MapContentType;
import com.robertx22.library_of_exile.dimension.MapDimensionInfo;
import com.robertx22.library_of_exile.dimension.MapDimensions;
import com.robertx22.library_of_exile.events.base.EventConsumer;
import com.robertx22.library_of_exile.events.base.ExileEvents;
import com.robertx22.library_of_exile.main.ApiForgeEvents;
import com.robertx22.library_of_exile.registry.register_info.ModRequiredRegisterInfo;
import com.robertx22.library_of_exile.registry.util.ExileRegistryUtil;
import com.robertx22.library_of_exile.utils.RandomUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

@Mod("dungeon_realm")
public class DungeonMain {
    public static boolean RUN_DEV_TOOLS = true;

    public static String MODID = "dungeon_realm";
    public static String DIMENSION_ID = "dungeon_realm:dungeon";

    public static ResourceLocation DIMENSION_KEY = new ResourceLocation(DIMENSION_ID);
    public static ModRequiredRegisterInfo REGISTER_INFO = new ModRequiredRegisterInfo(MODID);

    public static ResourceLocation id(String id) {
        return new ResourceLocation(MODID, id);
    }

    // other
    public static DungeonMapStructure MAIN_DUNGEON_STRUCTURE = new DungeonMapStructure();

    public static ArenaStructure ARENA = new ArenaStructure();
    public static UberArenaStructure UBER_ARENA = new UberArenaStructure();
    public static RewardStructure REWARD_ROOM = new RewardStructure();

    public static MapDimensionInfo MAP = new MapDimensionInfo(DIMENSION_KEY, MAIN_DUNGEON_STRUCTURE, MapContentType.PRIMARY_CONTENT, Arrays.asList(ARENA, UBER_ARENA, REWARD_ROOM));

    public static MapDimensionConfig getConfig() {
        return MapDimensionConfig.get(DIMENSION_KEY);
    }


    public DungeonMain() {


        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            bus.addListener(this::clientSetup);
        });

        new MapRegisterBuilder(MAP)
                .config(new MapDimensionConfigDefaults(1))
                .chunkGenerator(new EventConsumer<MapChunkGenEvent>() {
                    @Override
                    public void accept(MapChunkGenEvent event) {
                        if (event.mapId.equals("dungeon")) {
                            MAIN_DUNGEON_STRUCTURE.generateInChunk(event.world, event.manager, event.chunk.getPos());
                            ARENA.generateInChunk(event.world, event.manager, event.chunk.getPos());
                            UBER_ARENA.generateInChunk(event.world, event.manager, event.chunk.getPos());
                            REWARD_ROOM.generateInChunk(event.world, event.manager, event.chunk.getPos());
                        }
                    }
                }, id("dungeon_chunk_gen"))
                .build();

        new DungeonModConstructor(MODID, bus);


        if (RUN_DEV_TOOLS) {
            ExileRegistryUtil.setCurrentRegistarMod(DungeonMain.MODID);

            ApiForgeEvents.registerForgeEvent(PlayerEvent.PlayerLoggedInEvent.class, event -> {
                DungeonDatabase.INSTANCE.runDataGen(CachedOutput.NO_CACHE);
            });
        }

        ApiForgeEvents.registerForgeEvent(GatherDataEvent.class, event -> {
            var output = event.getGenerator().getPackOutput();
            var chestsLootTables = new LootTableProvider.SubProviderEntry(DungeonLootTables.ObeliskLootTableProvider::new, LootContextParamSets.CHEST);
            var provider = new LootTableProvider(output, Set.of(), List.of(chestsLootTables));
            event.getGenerator().addProvider(true, provider);

            if (RUN_DEV_TOOLS) {
                // todo this doesnt seem to gen here?   ObeliskDatabase.generateJsons();
            }

            try {
                // .. why does this not work otherwise?
                event.getGenerator().run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ObeliskConfig.SPEC);

        bus.addListener(this::commonSetupEvent);


        DungeonCommands.init();
        ObeliskRewardLogic.init();
        DungeonEvents.init();
        DungeonExileEvents.init();

        ExileEvents.ON_CHEST_LOOTED.register(new EventConsumer<ExileEvents.OnChestLooted>() {
            @Override
            public void accept(ExileEvents.OnChestLooted e) {
                try {

                    float chance = (float) (ObeliskConfig.get().OBELISK_SPAWN_CHANCE_ON_CHEST_LOOT.get() * ObeliskConfig.get().getDimChanceMulti(e.player.level()));
                    if (RandomUtils.roll(chance)) {
                        if (!MapDimensions.isMap(e.player.level())) {
                            var empty = mygetEmptySlotsRandomized(e.inventory, new Random());
                            if (!empty.isEmpty()) {
                                int index = RandomUtils.randomFromList(empty);
                                var map = ObeliskMapItem.blankMap();
                                e.inventory.setItem(index, map);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        DungeonEntries.CREATIVE_TAB.register(MODID, () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 2)
                .icon(() -> DungeonEntries.MAP_DEVICE_ITEM.get().getDefaultInstance())
                .title(DungeonWords.CREATIVE_TAB.get().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .displayItems(new CreativeModeTab.DisplayItemsGenerator() {
                    @Override
                    public void accept(CreativeModeTab.ItemDisplayParameters param, CreativeModeTab.Output output) {
                        for (Item item : ForgeRegistries.ITEMS) {
                            if (ForgeRegistries.ITEMS.getKey(item).getNamespace().equals(DungeonMain.MODID)) {
                                output.accept(item);
                            }
                        }
                    }
                })
                .build());

        System.out.println("Ancient Obelisks loaded.");

    }

    private static List<Integer> mygetEmptySlotsRandomized(Container inventory, Random rand) {
        List<Integer> list = Lists.newArrayList();

        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            if (inventory.getItem(i)
                    .isEmpty()) {
                list.add(i);
            }
        }

        Collections.shuffle(list, rand);
        return list;
    }

    public static Optional<DungeonMapData> ifMapData(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return Optional.empty();
        }
        var map = MapDimensions.getInfo(level);
        if (map != null && map.dimensionId.equals(DIMENSION_KEY)) {
            var mapdata = DungeonMapCapability.get(level).data.data.getData(MAIN_DUNGEON_STRUCTURE, pos);
            if (mapdata != null) {
                return Optional.of(mapdata);
            }
        }
        return Optional.empty();
    }

    public void clientSetup(final FMLClientSetupEvent event) {
        ObeliskClient.init();
    }

    public void commonSetupEvent(FMLCommonSetupEvent event) {


        ComponentInit.reg();

        MinecraftForge.EVENT_BUS.addGenericListener(Level.class, (Consumer<AttachCapabilitiesEvent<Level>>) x -> {
            x.addCapability(DungeonMapCapability.RESOURCE, new DungeonMapCapability(x.getObject()));
        });

        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, (Consumer<AttachCapabilitiesEvent<Entity>>) x -> {
            if (x.getObject() instanceof LivingEntity en) {
                x.addCapability(DungeonEntityCapability.RESOURCE, new DungeonEntityCapability(en));
            }
        });

    }
}
