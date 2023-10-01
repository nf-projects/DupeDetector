package me.kicksquare.bldetector.listener;

import me.kicksquare.bldetector.BLDetector;
import me.kicksquare.bldetector.util.ItemCheckUtil;
import me.kicksquare.bldetector.util.NBTUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;

public class ItemStackSplitListener implements Listener {
    private final BLDetector plugin;

    public ItemStackSplitListener(BLDetector blDetector) {
        this.plugin = blDetector;
    }

    /*
    The problem with the item UUID system to track dupes is that when itemstacks where the
    quantity is > 1 (e.g. 64x diamonds) are split, such as being dropped, the new stack
    will count as being duped. This class listens for any events where this could happen
    and prevents it from happening by generating new identifiers for the new stacks.
     */

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack itemCopy = e.getItemDrop().getItemStack();
        NBTUtil.setNBTString(itemCopy, "d_id", java.util.UUID.randomUUID().toString());
        // cancel the event and drop the new item
        e.setCancelled(true);
        p.getWorld().dropItem(p.getLocation(), itemCopy);

        Bukkit.getLogger().info("ItemStackSplitListener: " + p.getName() + " dropped a stack of " + itemCopy.getType() + " with quantity " + itemCopy.getAmount() + " and identifier " + NBTUtil.getNBTString(itemCopy, "d_id"));
    }


    @EventHandler
    public void onInventorySplit(InventoryClickEvent e) {
        // there's too many edge cases here (right click, triple left click, etc.)
        // so just assign new UUIDs to all items in the inventory
        Player p = (Player) e.getWhoClicked();
        p.getInventory().forEach(item -> {
            if (item != null && ItemCheckUtil.isDupableItem(item)) {
                NBTUtil.setNBTString(item, "d_id", java.util.UUID.randomUUID().toString());
            }
        });

        Bukkit.getLogger().info("ItemStackSplitListener: " + p.getName() + " split an inventory");
    }

    @EventHandler
    public void onItemDispense(BlockDispenseEvent e) {
        ItemStack itemCopy = e.getItem();
        NBTUtil.setNBTString(itemCopy, "d_id", java.util.UUID.randomUUID().toString());
        e.setItem(itemCopy);

        Bukkit.getLogger().info("ItemStackSplitListener: Dispensed item " + itemCopy.getType() + " with quantity " + itemCopy.getAmount() + " and identifier " + NBTUtil.getNBTString(itemCopy, "d_id"));
    }

    @EventHandler
    public void onItemTransferChest(InventoryMoveItemEvent e) {
        ItemStack itemCopy = e.getItem();
        NBTUtil.setNBTString(itemCopy, "d_id", java.util.UUID.randomUUID().toString());
        e.setItem(itemCopy);

        Bukkit.getLogger().info("ItemStackSplitListener: Transferred item " + itemCopy.getType() + " with quantity " + itemCopy.getAmount() + " and identifier " + NBTUtil.getNBTString(itemCopy, "d_id"));
    }
}
