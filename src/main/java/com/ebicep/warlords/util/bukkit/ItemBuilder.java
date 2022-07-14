package com.ebicep.warlords.util.bukkit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ItemBuilder {
    @Nonnull
    private final ItemStack item;
    @Nullable
    private ItemMeta meta = null;

    public ItemBuilder(@Nonnull Material type) {
        item = new ItemStack(type);
    }

    public ItemBuilder(@Nonnull Material type, int amount) {
        item = new ItemStack(type, amount);
    }

    public ItemBuilder(@Nonnull Material type, int amount, short damage) {
        item = new ItemStack(type, amount, damage);
    }

    public ItemBuilder(@Nonnull ItemStack stack) throws IllegalArgumentException {
        item = new ItemStack(stack);
    }

    public ItemBuilder(@Nonnull Potion potion, int amount, boolean splash) {
        potion.setSplash(splash);
        item = potion.toItemStack(amount);
    }

    protected ItemMeta meta() {
        if (meta == null) {
            meta = item.getItemMeta();
            if (meta == null) {
                throw new IllegalStateException("Unable to get item meta for " + item);
            }
        }
        return meta;
    }

    public ItemBuilder name(@Nonnull String name) {
        meta().setDisplayName(name);
        return this;
    }

    public ItemBuilder enchant(@Nonnull Enchantment enchant, int level) {
        meta().addEnchant(enchant, level, true);
        return this;
    }

    public ItemBuilder flags(ItemFlag... ifs) {
        meta().addItemFlags(ifs);
        return this;
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }
    public ItemBuilder lore(Collection<String> lore) {
        for (String row : lore) {
            if (row == null || row.contains("\n")) {
                // Fix for \n and null
                List<String> newLore = new ArrayList<>(Math.max(lore.size() * 2, 16));
                for (String loreRow : lore) {
                    if(loreRow != null) {
                        if (loreRow.contains("\n")) {
                            String chatColor = "";
                            for (String split : loreRow.split("\n")) {
                                String combined = !split.isEmpty() && split.charAt(0) == ChatColor.COLOR_CHAR ? split : chatColor + split;
                                newLore.add(combined);
                                chatColor = ChatColor.getLastColors(combined);
                            }
                        } else {
                            newLore.add(loreRow);
                        }
                    }
                }
                meta().setLore(newLore);
                return this;
            }
        }
        meta().setLore(lore instanceof List<?> ? (List<String>) lore : new ArrayList<>(lore));
        return this;
    }

    public ItemBuilder unbreakable() {
        return unbreakable(true);
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        meta().spigot().setUnbreakable(true);
        return this;
    }

    public ItemStack get() {
        if (this.meta != null) {
            this.item.setItemMeta(meta);
            this.meta = null;
        }
        return this.item;
    }
}