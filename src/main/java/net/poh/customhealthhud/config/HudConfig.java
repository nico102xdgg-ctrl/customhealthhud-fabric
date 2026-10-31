package net.poh.customhealthhud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class HudConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static HudConfig INSTANCE = new HudConfig();

    public int heartX = 0;
    public int heartY = -39;
    public float heartScale = 1.0f;
    public int heartSpacing = 13;
    public boolean showHotbar = true;
    public boolean showExpBar = true;
    public boolean showExpNumber = true;
    public boolean showFood = true;
    public boolean showArmor = true;
    public boolean showMountHealth = true;
    public boolean showAir = true;
    public boolean showPlayerHealth = true;
    public boolean showVanillaPlayerHealth = false;
    public int expBarX = 0;
    public int expBarY = -29;
    public int expNumberX = 0;
    public int expNumberY = -31;
    public float expNumberScale = 1.0f;
    public int heartColorR = 255;
    public int heartColorG = 255;
    public int heartColorB = 255;
    public int hotbarColorR = 255;
    public int hotbarColorG = 255;
    public int hotbarColorB = 255;
    public int expBarColorR = 130;
    public int expBarColorG = 217;
    public int expBarColorB = 130;
    public int expNumberColorR = 128;
    public int expNumberColorG = 255;
    public int expNumberColorB = 128;

    public static HudConfig getInstance() {
        return INSTANCE;
    }

    // Mismo archivo que la versión Forge: <carpeta del juego>/config/customhud.json
    private static File configFile() {
        return FabricLoader.getInstance().getGameDir().resolve("config").resolve("customhud.json").toFile();
    }

    public void save() {
        File file = configFile();
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        File file = configFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                HudConfig loaded = GSON.fromJson(reader, HudConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void copyFrom(HudConfig o) {
        this.heartX = o.heartX;
        this.heartY = o.heartY;
        this.heartScale = o.heartScale;
        this.heartSpacing = o.heartSpacing;
        this.showHotbar = o.showHotbar;
        this.showExpBar = o.showExpBar;
        this.showExpNumber = o.showExpNumber;
        this.showFood = o.showFood;
        this.showArmor = o.showArmor;
        this.showMountHealth = o.showMountHealth;
        this.showAir = o.showAir;
        this.showPlayerHealth = o.showPlayerHealth;
        this.showVanillaPlayerHealth = o.showVanillaPlayerHealth;
        this.expBarX = o.expBarX;
        this.expBarY = o.expBarY;
        this.expNumberX = o.expNumberX;
        this.expNumberY = o.expNumberY;
        this.expNumberScale = o.expNumberScale;
        this.heartColorR = o.heartColorR;
        this.heartColorG = o.heartColorG;
        this.heartColorB = o.heartColorB;
        this.hotbarColorR = o.hotbarColorR;
        this.hotbarColorG = o.hotbarColorG;
        this.hotbarColorB = o.hotbarColorB;
        this.expBarColorR = o.expBarColorR;
        this.expBarColorG = o.expBarColorG;
        this.expBarColorB = o.expBarColorB;
        this.expNumberColorR = o.expNumberColorR;
        this.expNumberColorG = o.expNumberColorG;
        this.expNumberColorB = o.expNumberColorB;
    }

    public void resetToDefaults() {
        copyFrom(new HudConfig());
    }
}
