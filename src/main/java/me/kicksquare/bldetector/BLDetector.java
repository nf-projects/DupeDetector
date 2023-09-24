package me.kicksquare.bldetector;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.leonhard.storage.Config;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import me.kicksquare.bldetector.commands.MainCommand;
import me.kicksquare.bldetector.commands.TestNBTCommand;
import me.kicksquare.bldetector.tasks.ApplyItemIdentifiersTask;
import me.kicksquare.bldetector.tasks.CheckDupedItemsTask;
import me.kicksquare.bldetector.util.ItemCheckUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.TimeUnit;

public final class BLDetector extends JavaPlugin {
//    TODO
//    - add a command to create an item and add it to config automatically
//    - hide plugin name from command tab
// - add a mode that automatically detects any items with custom lore or name
// -playervaultsx support

    private Config mainConfig;

    /*
    webhookCooldown is a cache that stores the last time a webhook was sent for a dupe identifier
    to prevent spamming the webhook
     */
    private Cache<String, Long> webhookCooldown;

    @Override
    public void onEnable() {
        getCommand("testnbt").setExecutor(new TestNBTCommand(this));
        getCommand("bldetector").setExecutor(new MainCommand(this));
        getCommand("dd").setExecutor(new MainCommand(this));

        mainConfig = SimplixBuilder
                .fromFile(new File(getDataFolder(), "config.yml"))
                .addInputStreamFromResource("config.yml")
                .setDataType(DataType.SORTED)
                .setReloadSettings(ReloadSettings.MANUALLY)
                .createConfig();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            ApplyItemIdentifiersTask.applyItemIdentifiers(this);
            CheckDupedItemsTask.checkDupedItems(this);
        }, 0L, mainConfig.getInt("scheduler-interval-seconds") * 20L);

        webhookCooldown = CacheBuilder.newBuilder().expireAfterWrite(mainConfig.getInt("webhook-cooldown-seconds"), TimeUnit.SECONDS).build();

        ItemCheckUtil.loadCustomItems(mainConfig);
    }

    public Config getMainConfig() {
        return mainConfig;
    }

    public Cache<String, Long> getWebhookCooldown() {
        return webhookCooldown;
    }

    public void setWebhookCooldown(Cache<String, Long> webhookCooldown) {
        this.webhookCooldown = webhookCooldown;
    }
}
