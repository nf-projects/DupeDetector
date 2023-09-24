package me.kicksquare.bldetector.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class CustomItem {
    private final String name;
    private final String material;
    private final String lore;

    public CustomItem(String name, String material, String lore) {
        this.name = name;
        this.material = material;
        this.lore = lore;
    }

    public String getName() {
        return name;
    }

    public String getMaterial() {
        return material;
    }

    public String getLore() {
        return lore;
    }

    public static void addItemToConfigItemsList(CustomItem customItem, File config) throws IOException {
        // Use LinkedHashMap to maintain the order of keys
        Yaml yaml = new Yaml(new Constructor(LinkedHashMap.class), new Representer(), new DumperOptions());

        // Read the YAML config
        FileInputStream inputStream = new FileInputStream(config);
        LinkedHashMap<String, Object> yamlData = yaml.load(inputStream);
        inputStream.close();

        // Get the list of items
        ArrayList<Object> items = (ArrayList<Object>) yamlData.get("items");

        // Create a new item
        LinkedHashMap<String, Object> newItem = new LinkedHashMap<>();
        LinkedHashMap<String, Object> itemData = new LinkedHashMap<>();
        itemData.put("name", customItem.getName());
        itemData.put("material", customItem.getMaterial());
        itemData.put("lore", customItem.getLore());
        newItem.put("example", itemData);

        // Add the new item to the list
        items.add(newItem);

        // Write the modified YAML data back to the config
        FileWriter writer = new FileWriter(config);
        yaml.dump(yamlData, writer);
        writer.close();
    }
}