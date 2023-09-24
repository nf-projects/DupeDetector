package me.kicksquare.bldetector.commands;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.kicksquare.bldetector.BLDetector;
import me.kicksquare.bldetector.util.ItemCheckUtil;
import me.kicksquare.bldetector.util.NBTUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TestNBTCommand implements CommandExecutor {
    private final BLDetector plugin;

    public TestNBTCommand(BLDetector plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        // only players can use this command
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only players can use this command!");
            return true;
        }

        // permission check
        if (!commandSender.hasPermission("bldetector.testnbt")) {
            commandSender.sendMessage("You do not have permission to use this command!");
            return true;
        }

        if(args.length == 3 && args[0].equalsIgnoreCase("set")) {
            Player p = (Player) commandSender;
            String key = args[1];
            String value = args[2];

            ItemStack item = p.getInventory().getItemInMainHand();
            NBTItem nbtItem = new NBTItem(item);

            ItemStack updatedItem = NBTUtil.setNBTString(item, key, value);
            p.getInventory().setItemInMainHand(updatedItem);

            return true;
        }

        if(args.length == 1 && args[0].equalsIgnoreCase("checkitem")) {
            Player p = (Player) commandSender;
            ItemStack item = p.getInventory().getItemInMainHand();

            if(ItemCheckUtil.shouldAddIdentifierToItem(plugin.getMainConfig(), item)) {
                p.sendMessage("Item should have identifier added");
            } else {
                p.sendMessage("Item should not have identifier added");
            }

            return true;
        }

        // gets the player's item in hand
        Player p = (Player) commandSender;
        ItemStack item = p.getInventory().getItemInMainHand();
        NBTItem nbtItem = new NBTItem(item);

        p.sendMessage("Item in hand: " + item.getType().toString());
        p.sendMessage("Item in hand has NBT: " + nbtItem.hasNBTData());
        p.sendMessage("NBT of item in hand: " + nbtItem.toString());
        p.sendMessage("Item in hand has NBT key 'test': " + nbtItem.hasKey("test"));
        p.sendMessage("Item in hand value of NBT key 'test': " + nbtItem.getString("test"));

        // set the value of test to a random num ber between 0 and 100
        nbtItem.setString("test", String.valueOf(Math.random() * 100));
        // update the item in hand
        nbtItem.applyNBT(item);
        p.getInventory().setItemInMainHand(item);

        p.sendMessage("updated!");
        p.sendMessage("Item in hand value of NBT key 'test': " + nbtItem.getString("test"));

        return true;
    }
}
