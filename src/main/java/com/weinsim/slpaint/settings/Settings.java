package com.weinsim.slpaint.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.weinsim.slpaint.main.Loader;
import com.weinsim.sutil.json.JSONParser;
import com.weinsim.sutil.json.JSONSerializer;
import com.weinsim.sutil.json.values.JSONObject;
import com.weinsim.sutil.json.values.JSONValue;

/**
 * This class manages global (user) settings, such as the color theme (base
 * color + dark mode) and the shape of the HueSatField.
 * 
 * Potential futre change: allow settings to not only exist at top-level, but
 * nested inside of JSON objects and arrays (see ArraySetting and
 * ObjectSetting). For the time being though, this is not neccessary.
 */
public class Settings {

    private static final String DEFAULT_SETTINGS_FILE = "settings/defaultSettings.json";
    private static final String SETTINGS_FILE;

    private static JSONObject defaultSettings;
    private static JSONObject currentSettings;

    private static HashMap<String, Setting<?>> allSettings;

    private static SettingsSaveThread saveThread;

    static {
        Path configPath = Path.of(System.getProperty("user.home"), ".slpaint");
        try {
            Files.createDirectories(configPath);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create config directory", e);
        }
        SETTINGS_FILE = configPath.resolve("settings.json").toString();

        allSettings = new HashMap<>();
        try {
            defaultSettings = JSONParser.parseObject(Loader.getString(DEFAULT_SETTINGS_FILE));
        } catch (IOException e) {
            final String message = String.format("Unable to load default settings file (%s)", DEFAULT_SETTINGS_FILE);
            throw new RuntimeException(message, e);
        }
        loadUserSettings();

        if (saveThread == null) {
            saveThread = new SettingsSaveThread();
            saveThread.start();
        }
    }

    private Settings() {
    }

    public static void addSetting(String identifier, Setting<?> setting) {
        if (allSettings.get(identifier) != null) {
            throw new RuntimeException(String.format("Duplicate setting %s", identifier));
        }
        allSettings.put(identifier, setting);

        updateSetting(identifier, setting);
    }

    private static void updateSetting(String identifier, Setting<?> setting) {
        JSONValue jsonValue = currentSettings.get(identifier);
        if (jsonValue == null) {
            jsonValue = defaultSettings.get(identifier);
            if (jsonValue == null) {
                throw new RuntimeException(String.format("Setting \"%s\" does not exist", identifier));
            }
        }
        setting.setJSONValue(jsonValue);
    }

    public static void setDefaultSettings() {
        currentSettings = new JSONObject(defaultSettings);

        for (Entry<String, Setting<?>> entry : allSettings.entrySet()) {
            updateSetting(entry.getKey(), entry.getValue());
        }
    }

    private static void loadUserSettings() {
        String buffer = "";
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(SETTINGS_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    buffer += line + "\n";
                }
            }
            currentSettings = JSONParser.parseObject(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to load settings. Using default settings instead.");
            currentSettings = new JSONObject(defaultSettings);
        }
    }

    public static void finish() {
        if (saveThread != null) {
            saveThread.shouldStop = true;
            saveThread.interrupt();
        }
    }

    private static String generateJSONString() {
        LinkedHashMap<String, JSONValue> jsonValues = new LinkedHashMap<>();
        for (String identifier : allSettings.keySet()) {
            Setting<?> setting = allSettings.get(identifier);
            jsonValues.put(identifier, setting.getJSONValue());
        }

        JSONObject json = new JSONObject(jsonValues);
        String jsonString = JSONSerializer.serialize(json);
        return jsonString;
    }

    private static class SettingsSaveThread extends Thread {

        private static final long SLEEP_TIME = 10_000;

        private String lastJSONString = null;

        private boolean shouldStop = false;

        public SettingsSaveThread() {
            super("Settings Thread");
        }

        @Override
        public void run() {
            while (!shouldStop) {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }

                String newJSONString = generateJSONString();

                if (newJSONString.equals(lastJSONString)) {
                    continue;
                }

                save(newJSONString);

                lastJSONString = newJSONString;
            }
        }

        synchronized void save(String json) {
            File file = new File(SETTINGS_FILE);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}