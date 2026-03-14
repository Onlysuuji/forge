package org.example2.solips;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Solips.MODID, value = Dist.CLIENT)
public class EnchantScreenObserver {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (!(mc.screen instanceof EnchantmentScreen)) {
            ObservedEnchantState.clear();
            return;
        }

        if (mc.player == null || !(mc.player.containerMenu instanceof EnchantmentMenu menu)) {
            ObservedEnchantState.clear();
            return;
        }

        ItemStack stack = menu.getSlot(0).getItem();
        if (stack.isEmpty()) {
            ObservedEnchantState.clear();
            return;
        }

        int[] costs = new int[3];
        int[] clueIds = new int[3];
        int[] clueLevels = new int[3];

        for (int i = 0; i < 3; i++) {
            costs[i] = menu.costs[i];
            clueIds[i] = menu.enchantClue[i];
            clueLevels[i] = menu.levelClue[i];
        }

        // 本棚数は今は固定入力でも可。あとで自動推定にしてよい。
        int bookshelves = 15;

        ObservedEnchantState.set(stack.getItem(), bookshelves, costs, clueIds, clueLevels);
    }
}