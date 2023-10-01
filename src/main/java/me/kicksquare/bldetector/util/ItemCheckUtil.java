package me.kicksquare.bldetector.util;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ItemCheckUtil {

    public static boolean isDupableItem(ItemStack item) {
        // returns true if the item has ANY (or multiple) of the following:
        // - custom lore
        // - custom model data
        // - custom nbt

        if (item.hasItemMeta()) {
            // check if there is lore
            if (Objects.requireNonNull(item.getItemMeta()).getLore() != null && item.getItemMeta().getLore().size() > 0) {
                return true;
            }

            if (item.getItemMeta().hasCustomModelData()) {
                return true;
            }
        }

        NBTItem nbtItem = new NBTItem(item);
        if (nbtItem.getKeys().size() > 0) {
            return true;
        }

        return false;
    }
}