package me.kicksquare.dupedetector.util;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

public class NBTUtil {
    public static ItemStack setNBTString(ItemStack item, String key, String value) {
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString(key, value);
        nbtItem.applyNBT(item);

        return item;
    }

    public static String getNBTString(ItemStack item, String key) {
        NBTItem nbtItem = new NBTItem(item);
        String s =  nbtItem.getString(key);

        if (s == null || s.isEmpty()) {
            return null;
        }

        return s;
    }
}
