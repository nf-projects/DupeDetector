package me.kicksquare.bldetector.util;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

public class ItemCheckUtil {

    public static boolean shouldAddIdentifierToItem(ItemStack item) {
        // returns true if the item has ANY (or multiple) of the following:
        // - custom name & lore
        // - custom model data
        // - custom nbt

        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName() || item.getItemMeta().hasLore()) {
                return true;
            }
        }

        if (item.hasItemMeta()) {
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