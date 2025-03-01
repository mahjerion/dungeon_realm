package com.robertx22.dungeon_realm.main;

import com.mojang.brigadier.CommandDispatcher;
import com.robertx22.dungeon_realm.item.relic.RelicGenerator;
import com.robertx22.dungeon_realm.structure.DungeonMapCapability;
import com.robertx22.dungeon_realm.structure.DungeonWorldData;
import com.robertx22.library_of_exile.command_wrapper.CommandBuilder;
import com.robertx22.library_of_exile.command_wrapper.PermWrapper;
import com.robertx22.library_of_exile.command_wrapper.PlayerWrapper;
import com.robertx22.library_of_exile.command_wrapper.RegistryWrapper;
import com.robertx22.library_of_exile.database.init.LibDatabase;
import com.robertx22.library_of_exile.database.relic.relic_rarity.RelicRarity;
import com.robertx22.library_of_exile.database.relic.relic_type.RelicType;
import com.robertx22.library_of_exile.utils.PlayerUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class DungeonCommands {


    public static void init(CommandDispatcher d) {
        CommandBuilder.of(DungeonMain.MODID, d, x -> {

            x.addLiteral("wipe_world_data", PermWrapper.OP);

            x.action(e -> {
                var world = e.getSource().getLevel();

                DungeonMapCapability.get(world).data = new DungeonWorldData();

                e.getSource().getPlayer().sendSystemMessage(Component.literal(
                        "Dungeon realm World data wiped, you should only do this when wiping the dimension's folder too! The dimension folder is in: savefolder\\dimensions\\dungeon_realm").withStyle(ChatFormatting.GREEN));
            });

        }, "Applies an item modification to the item in player's hand.");
        

        CommandBuilder.of(DungeonMain.MODID, d, x -> {
            PlayerWrapper PLAYER = new PlayerWrapper();
            var RARITY = new RegistryWrapper<RelicRarity>(LibDatabase.RELIC_RARITY);
            var TYPE = new RegistryWrapper<RelicType>(LibDatabase.RELIC_TYPE);

            x.addLiteral("give", PermWrapper.OP);
            x.addLiteral("relic", PermWrapper.OP);

            x.addArg(PLAYER);
            x.addArg(RARITY);
            x.addArg(TYPE);

            x.action(e -> {
                var p = PLAYER.get(e);
                var rar = RARITY.get(e);
                var type = TYPE.get(e);

                var set = new RelicGenerator.Settings();

                set.type = Optional.of(LibDatabase.RelicTypes().get(type));
                set.rar = Optional.of(LibDatabase.RelicRarities().get(rar));

                var stack = RelicGenerator.randomRelicItem(Optional.of(p), set);
                PlayerUtil.giveItem(stack, p);
            });

        }, "");
    }


}
