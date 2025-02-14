package com.robertx22.dungeon_realm.item;

import com.robertx22.dungeon_realm.main.DungeonWords;
import com.robertx22.library_of_exile.components.PlayerDataCapability;
import com.robertx22.library_of_exile.dimension.MapDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class TeleportBackItem extends Item {
    public TeleportBackItem() {
        super(new Properties().stacksTo(64));

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        var stack = pPlayer.getItemInHand(pUsedHand);

        if (!pLevel.isClientSide) {
            if (pPlayer instanceof ServerPlayer p) {
                if (!MapDimensions.isMap(pLevel)) {
                    p.sendSystemMessage(DungeonWords.USABLE_ONLY_IN_DUNGEON_REALM.get().withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.pass(stack);
                }
                PlayerDataCapability.get(p).mapTeleports.teleportHome(p);
                stack.shrink(1);

            }
        }
        return InteractionResultHolder.success(stack);
    }


    // todo this had BUGS, idk why but finish using is casted AGAIN after the player is teleported

    /*
    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 20 * 2;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level pLevel, LivingEntity en) {
        if (!pLevel.isClientSide) {
            if (en instanceof ServerPlayer p) {
                if (!MapDimensions.isMap(pLevel)) {
                    p.sendSystemMessage(DungeonWords.USABLE_ONLY_IN_DUNGEON_REALM.get().withStyle(ChatFormatting.RED));
                    return stack;
                }
                PlayerDataCapability.get(p).mapTeleports.teleportHome(p);
                stack.shrink(1);

            }
        }
        return stack;
    }

     */

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(DungeonWords.HOME_PEARL_DESC.get().withStyle(ChatFormatting.BLUE));
    }

}
