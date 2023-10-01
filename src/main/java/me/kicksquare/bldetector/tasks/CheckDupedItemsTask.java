package me.kicksquare.bldetector.tasks;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import me.kicksquare.bldetector.BLDetector;
import me.kicksquare.bldetector.util.ItemCheckUtil;
import me.kicksquare.bldetector.util.NBTUtil;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CheckDupedItemsTask {
    /**
     * Goes through all online inventories and checks for items with the same identifier
     *
     * @param plugin plugin instance
     */
    public static void checkDupedItems(BLDetector plugin) {
        // create a list of known identifiers
        // loop through all online players
        // loop through each player's inventory --> each item
        // check if the item has an dupe identifier
        // if it does, check if the identifier is in the list of known identifiers
        // if it is, then the item is duped
        // if it isn't, add it to the list of known identifiers
        // at the end, clear the list of known identifiers

        List<String> knownIdentifiers = new ArrayList<>();
        List<DupeResult> dupeResults = new ArrayList<>();

        // go through all online player inventories
        if (plugin.getMainConfig().getBoolean("scan-player-inventories")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                Inventory inv = p.getInventory();

                for (int i = 0; i < inv.getSize(); i++) {
                    ItemStack item = inv.getItem(i);

                    if (item != null) {
                        handleItem(plugin, knownIdentifiers, dupeResults, p, item, i, new DupeLocation(LocationType.PLAYER_INVENTORY));
                    }
                }
            }
        }

        // go through player enderchests
        if (plugin.getMainConfig().getBoolean("scan-enderchests")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                Inventory inv = p.getEnderChest();

                for (int i = 0; i < inv.getSize(); i++) {
                    ItemStack item = inv.getItem(i);

                    if (item != null) {
                        handleItem(plugin, knownIdentifiers, dupeResults, p, item, i, new DupeLocation(LocationType.PLAYER_ENDER_CHEST));
                    }
                }
            }
        }

        // go through all chests, barrels, shulkers in loaded chunks
        if (plugin.getMainConfig().getBoolean("scan-containers")) {
            for (World w : Bukkit.getWorlds()) {
                for (Chunk chunk : w.getLoadedChunks()) {
                    for (BlockState blockState : chunk.getTileEntities()) {
                        if (blockState instanceof Container) {
                            Container container = (Container) blockState;
                            for (ItemStack item : container.getInventory().getContents()) {
                                if (item != null) {
                                    handleItem(plugin, knownIdentifiers, dupeResults, null, item, -1, new DupeLocation(container.getLocation()));
                                }
                            }
                        }
                    }
                }
            }
        }

        // send all dupe results via discord webhook
        sendToWebhook(plugin, dupeResults);

        knownIdentifiers.clear();
        dupeResults.clear();
    }

    private static void handleItem(BLDetector plugin, List<String> knownIdentifiers, List<DupeResult> dupeResults, Player p, ItemStack item, int slotIndex, DupeLocation dupeLocation) {
        String identifier = NBTUtil.getNBTString(item, "d_id");

        // ignore if it doesnt have an identifier or if it shouldn't have an identifier
        if (identifier == null || identifier.isEmpty() || !ItemCheckUtil.isDupableItem(item)) {
            return;
        }

        if (knownIdentifiers.contains(identifier)) {
            // duped item(s)
            // if it's a player inventory and they have the bypass permission
            if (plugin.getMainConfig().getBoolean("bypass-permission") &&
                    slotIndex != -1 &&
                    p != null &&
                    p.hasPermission("bldetector.bypass")) {
                if (plugin.getMainConfig().getBoolean("generate-new-dupe-id")) {
                    // generate new dupe IDs for all items in the player's inventory
                    p.getInventory().forEach(item2 -> {
                        if (item2 != null && ItemCheckUtil.isDupableItem(item2)) {
                            NBTUtil.setNBTString(item2, "d_id", java.util.UUID.randomUUID().toString());
                        }
                    });

                    p.sendMessage(ChatColor.GREEN + "Generated new identifiers for all items in your inventory.");
                }

                return;
            }

            
            // if there is already a duperesults entry with this identifier, add this player
            // if necessary and increase the quantity
            // otherwise, create a new duperesults entry

            boolean found = false;
            for (DupeResult dupeResult : dupeResults) {
                if (dupeResult.identifier.equals(identifier)) {
                    if (!dupeResult.dupedPlayers.contains(p)) {
                        dupeResult.dupedPlayers.add(p);
                    }
                    int itemStackQuantity = item.getAmount();

                    // increase quantity by itemstack quantity
                    dupeResult.quantity += itemStackQuantity;

                    found = true;
                    break;
                }
            }

            // still not found - add new entry
            if (!found) {
                List<Player> dupedPlayers = new ArrayList<>();
                dupedPlayers.add(p);
                dupeResults.add(new DupeResult(item, identifier, dupedPlayers, item.getAmount(), dupeLocation));
            }
        } else {
            knownIdentifiers.add(identifier);
        }
    }

    private static void sendToWebhook(BLDetector plugin, List<DupeResult> dupeResults) {
        for (DupeResult dupeResult : dupeResults) {
            // if it's currently on cooldown, continue
            if (plugin.getWebhookCooldown().asMap().containsKey(dupeResult.identifier)) {
                continue;
            }

            // no webhook url set
            if (plugin.getMainConfig().getString("webhook-url").equals("WEBHOOK_URL_HERE")) {
                continue;
            }

            String locationString = "";
            if (dupeResult.dupeLocation.locationType == LocationType.CONTAINER) {
                locationString = "Container at " + dupeResult.dupeLocation.location.toString();
            } else if (dupeResult.dupeLocation.locationType == LocationType.PLAYER_INVENTORY) {
                locationString = "Player Inventory";
            } else if (dupeResult.dupeLocation.locationType == LocationType.PLAYER_ENDER_CHEST) {
                locationString = "Player Ender Chest";
            }

            List<Player> playersInCloseProximity = new ArrayList<>();
            for (Player p : dupeResult.dupedPlayers) {
                // get a list of players in close proximity to the player
                // add to close proximity list if not already in it
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    // only if they are in the same world
                    if (!onlinePlayer.getWorld().equals(p.getWorld())) {
                        continue;
                    }

                    if (onlinePlayer.getLocation().distance(p.getLocation()) <= plugin.getMainConfig().getInt("close-proximity-distance")) {
                        if (!playersInCloseProximity.contains(onlinePlayer)) {
                            playersInCloseProximity.add(onlinePlayer);
                        }
                    }
                }
            }

            // remove the player who triggered the dupe from the list
            playersInCloseProximity.removeIf(player -> player.getName().equals(dupeResult.dupedPlayers.get(0).getName()));

            String playersInCloseProximityString = playersInCloseProximity.size() > 0 ? String.join(", ", playersInCloseProximity.stream().map(Player::getName).toArray(String[]::new)) : "None";

            WebhookClient webhookClient = WebhookClient.withUrl(plugin.getMainConfig().getString("webhook-url"));
            WebhookEmbedBuilder embedBuilder = new WebhookEmbedBuilder();
            embedBuilder.setTitle(new WebhookEmbed.EmbedTitle("Duped Items Detected", null));
            embedBuilder.setDescription(
                    "- **Item:** " + dupeResult.itemStack.getType().name() + "\n" +
                            "- **Item Name:** " + dupeResult.itemStack.getItemMeta().getDisplayName() + "\n" +
                            "- **Location:** " + locationString + "\n" +
                            "- **Identifier:** " + dupeResult.identifier + "\n" +
                            "- **Quantity:** At least " + dupeResult.quantity + "x\n" +
                            "- **# Players:** " + dupeResult.dupedPlayers.size() + "\n" +
                            "- **Duped Player Names:** " + String.join(", ", dupeResult.dupedPlayers.stream().map(Player::getName).toArray(String[]::new)) + "\n " +
                            "- **Players in Close Proximity:** " + playersInCloseProximityString);
            embedBuilder.setColor(0x00FF00);

            // add this identifier to cooldown
            plugin.getWebhookCooldown().put(dupeResult.identifier, System.currentTimeMillis() + plugin.getMainConfig().getInt("webhook-cooldown") * 1000L);

            System.out.println("Sending webhook embed to " + plugin.getMainConfig().getString("webhook-url"));
            webhookClient.send(embedBuilder.build());
            webhookClient.close();
        }
    }

    private enum LocationType {
        PLAYER_INVENTORY,
        PLAYER_ENDER_CHEST,
        CONTAINER
    }

    private static class DupeLocation {
        final LocationType locationType;
        final Location location; // null if locationType is not CONTAINER

        public DupeLocation(LocationType locationType) {
            this.locationType = locationType;
            this.location = null;
        }

        public DupeLocation(Location location) {
            this.locationType = LocationType.CONTAINER;
            this.location = location;
        }
    }

    public static class DupeResult {
        private final ItemStack itemStack;
        private final String identifier;
        private final List<Player> dupedPlayers;
        private final DupeLocation dupeLocation;
        private int quantity;

        public DupeResult(ItemStack itemStack, String identifier, List<Player> dupedPlayers, int quantity, DupeLocation dupeLocation) {
            this.itemStack = itemStack;
            this.identifier = identifier;
            this.dupedPlayers = dupedPlayers;
            this.quantity = quantity;
            this.dupeLocation = dupeLocation;
        }
    }
}
