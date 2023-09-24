package me.kicksquare.bldetector.commands;

import me.kicksquare.bldetector.BLDetector;
import me.kicksquare.bldetector.tasks.CheckDupedItemsTask;
import me.kicksquare.bldetector.util.NBTUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class MainCommand implements CommandExecutor {
    private BLDetector plugin;

    public MainCommand(BLDetector plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player && !sender.hasPermission("bldetector.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 1) {
            switch (args[0]) {
                case "reload":
                    plugin.reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
                    return true;
                case "createitem":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
                        return true;
                    }

                    Player p = (Player) sender;
                    ItemStack item = p.getInventory().getItemInMainHand();

                    if (item.getType().isAir()) {
                        sender.sendMessage(ChatColor.RED + "You must be holding an item to use this command.");
                        return true;
                    }

                    String itemName;
                    // if the item has a custom name
                    if (Objects.requireNonNull(item.getItemMeta()).hasDisplayName()) {
                        itemName = item.getItemMeta().getDisplayName();
                    } else {
                        itemName = "IGNORE";
                        sender.sendMessage(ChatColor.YELLOW + "The item you are holding does not have a custom name. The name will be ignored.");
                    }

                    String materialName = item.getType().name();
                    String lore;
                    // if the item has lore
                    if (item.getItemMeta().hasLore()) {
                        lore = item.getItemMeta().getLore().toString();
                    } else {
                        lore = "IGNORE";
                        sender.sendMessage(ChatColor.YELLOW + "The item you are holding does not have lore. The lore will be ignored.");
                    }

                    List<?> items = plugin.getMainConfig().getList("items");
                    plugin.getMainConfig().set("items", items);

                    return true;
                case "run":
                    CheckDupedItemsTask.checkDupedItems(plugin);
                    sender.sendMessage(ChatColor.GREEN + "Dupe check task running now.");
                    return true;
                case "generate":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
                        return true;
                    }

                    Player p2 = (Player) sender;

                    p2.getInventory().forEach(item2 -> {
                        if (item2 != null) {
                            NBTUtil.setNBTString(item2, "d_id", java.util.UUID.randomUUID().toString());
                        }
                    });

                    sender.sendMessage(ChatColor.GREEN + "Generated new identifiers for all items in your inventory.");
                    return true;
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Running BLDetector v" + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GREEN + "Commands:");
        sender.sendMessage(ChatColor.AQUA + "/dd reload - Reloads the config");
        sender.sendMessage(ChatColor.AQUA + "/dd createitem - Creates an item based on the item in your hand and adds it to config");
        sender.sendMessage(ChatColor.AQUA + "/dd run - Manually runs the dupe check task");
        sender.sendMessage(ChatColor.AQUA + "/dd generate - Generates new identifiers for all items in your inventory. Useful when working on kits, crates, etc.");


        return true;
    }
}
