package me.kicksquare.bldetector.tasks;

import me.kicksquare.bldetector.BLDetector;
import me.kicksquare.bldetector.util.ItemCheckUtil;
import me.kicksquare.bldetector.util.NBTUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ApplyItemIdentifiersTask {
    public static void applyItemIdentifiers(BLDetector plugin) {
        // get all online players
        // loop through all online players
        // get their inventory
        // loop through their inventory
        // check if it should be checked
        // if yes and it does not have an identifier, give it one

        for (Player p : Bukkit.getOnlinePlayers()) {
            Inventory inv = p.getInventory();

            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);

                if (item != null) {
                    if (ItemCheckUtil.isDupableItem(item)) {
                        if (NBTUtil.getNBTString(item, "d_id") != null) {
                            // already has an identifier
                            continue;
                        }

                        // add identifier
                        ItemStack updatedItem = NBTUtil.setNBTString(item, "d_id", UUID.randomUUID().toString());
                        inv.setItem(i, updatedItem);
                    }
                }
            }
        }
    }
}
