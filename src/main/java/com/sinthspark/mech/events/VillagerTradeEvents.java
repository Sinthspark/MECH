package com.sinthspark.mech.events;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.registration.ItemRegistry;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MECH.MOD_ID)
public class VillagerTradeEvents {

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.FARMER) {
            event.getTrades().get(1).add((trader, random) -> new MerchantOffer(
                    new ItemStack(ItemRegistry.RAPESEED.get(), 20),
                    new ItemStack(Items.EMERALD, 1),
                    16, 5, 1.0f
            ));
            event.getTrades().get(3).add((trader, random) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 1),
                    new ItemStack(ItemRegistry.RAPESEED.get(), 5),
                    16, 2, 1.0f
            ));
        }
    }
}