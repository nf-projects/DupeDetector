package me.kicksquare.bldetector.util;

// FOR REFERENCE, THIS IS THE CONFIG STRUCTURE
//# Will scan inventories for the following items
//        # If 'lore' is contained anywhere in the lore (not through lines)
//        # it will be considered a match
//
//        items:
//        - example:
//        name: Legendary Sword
//        material: NETHERITE_SWORD
//        lore: 'Legendary'

import de.leonhard.storage.Config;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemCheckUtil {

    private static List<CustomItem> customItems = new ArrayList<>();

    public static boolean shouldAddIdentifierToItem(Config config, ItemStack item) {
        for (CustomItem configItem : customItems) {
            // Check if the item has the same name, material, and lore as the custom item
            if (!Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals(configItem.getName()) && !Objects.equals(configItem.getName(), "IGNORE")) {
                continue;
            }

            if (!item.getType().toString().equals(configItem.getMaterial()) && !Objects.equals(configItem.getMaterial(), "IGNORE")) {
                continue;
            }

            if (!Objects.requireNonNull(item.getItemMeta().getLore()).contains(configItem.getLore()) && !Objects.equals(configItem.getLore(), "IGNORE")) {
                continue;
            }

            return true;
        }

        return false;
    }

    public static void loadCustomItems(Config config) {
        List<?> items = config.getList("items");
        customItems.clear();
        customItems.addAll(parseCustomItems(items));
    }

    public static List<CustomItem> parseCustomItems(List<?> list) {
        List<CustomItem> result = new ArrayList<>();

        // Make a type-safe list of custom items
        for (Object obj : list) {
            if (obj instanceof Map) {
                Map<String, Object> itemMap = (Map<String, Object>) obj;
//                Bukkit.broadcastMessage(itemMap.toString()); // something like {test={name=Legendary Sword, material=NETHERITE}}  -- we only want the inner map
                // get the first object from the item map (there is only ever 1)
                Map.Entry<String, Object> entry = itemMap.entrySet().iterator().next();
                Map<String, Object> innerMap = (Map<String, Object>) entry.getValue();
                CustomItem customItem = new CustomItem(entry.getKey(), innerMap.get("name").toString(), innerMap.get("material").toString(), innerMap.get("lore").toString());
                result.add(customItem);
            }
        }

        Bukkit.broadcastMessage("Loaded " + result.size() + " custom items from config: ");
        for (CustomItem item : result) {
            Bukkit.broadcastMessage(item.getName() + " " + item.getMaterial() + " " + item.getLore());
        }

        return result;
    }
}