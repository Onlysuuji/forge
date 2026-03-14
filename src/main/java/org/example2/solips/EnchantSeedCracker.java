package org.example2.solips;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.List;

public final class EnchantSeedCracker {
    private static Thread worker;

    private EnchantSeedCracker() {}

    public static void startCrack() {
        if (SeedCrackState.isRunning()) return;
        if (!ObservedEnchantState.isValid()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        RegistryAccess registryAccess = mc.level.registryAccess();
        SeedCrackState.start();

        worker = new Thread(() -> runCrack(registryAccess), "EnchantSeedCracker");
        worker.setDaemon(true);
        worker.start();
    }

    private static void runCrack(RegistryAccess registryAccess) {
        try {
            var observedItem = ObservedEnchantState.getItem();
            var observedCosts = ObservedEnchantState.getCosts();
            var observedClueIds = ObservedEnchantState.getClueIds();
            var observedClueLevels = ObservedEnchantState.getClueLevels();
            int bookshelves = ObservedEnchantState.getBookshelves();

            ItemStack stack = new ItemStack(observedItem);
            HolderLookup.RegistryLookup<Enchantment> lookup =
                    registryAccess.lookupOrThrow(Registries.ENCHANTMENT);

            // 最初は軽めの範囲だけ。あとで拡張。
            int maxSeed = 0x00FFFFFF;

            for (int seed = 0; seed <= maxSeed; seed++) {
                if ((seed & 0x3FFF) == 0) {
                    SeedCrackState.setChecked(seed);
                }

                if (!matchesCosts(stack, bookshelves, seed, observedCosts)) {
                    continue;
                }

                if (!matchesClues(stack, seed, observedCosts, observedClueIds, observedClueLevels, lookup)) {
                    continue;
                }

                SeedCrackState.addCandidate(seed);
            }
        } finally {
            SeedCrackState.finish();
        }
    }

    private static boolean matchesCosts(ItemStack stack, int bookshelves, int seed, int[] observedCosts) {
        RandomSource random = RandomSource.create(seed);

        for (int slot = 0; slot < 3; slot++) {
            int cost = EnchantmentHelper.getEnchantmentCost(random, slot, bookshelves, stack);
            if (cost < slot + 1) cost = 0;
            if (cost != observedCosts[slot]) return false;
        }
        return true;
    }

    private static boolean matchesClues(
            ItemStack stack,
            int seed,
            int[] observedCosts,
            int[] observedClueIds,
            int[] observedClueLevels,
            HolderLookup.RegistryLookup<Enchantment> lookup
    ) {
        for (int slot = 0; slot < 3; slot++) {
            if (observedCosts[slot] <= 0) {
                continue;
            }

            RandomSource enchantRandom = RandomSource.create((long) seed + slot);
            List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(
                    enchantRandom,
                    stack,
                    observedCosts[slot],
                    lookup.listElements().map(holder -> (Holder<Enchantment>) holder)
            );

            if (list.isEmpty()) return false;

            EnchantmentInstance first = list.get(0);

            int firstId = lookup.listElements()
                    .map(holder -> (Holder<Enchantment>) holder)
                    .toList()
                    .indexOf(first.enchantment);

            int firstLevel = first.level;

            if (observedClueIds[slot] >= 0 && firstId != observedClueIds[slot]) {
                return false;
            }
            if (observedClueLevels[slot] > 0 && firstLevel != observedClueLevels[slot]) {
                return false;
            }
        }
        return true;
    }
}