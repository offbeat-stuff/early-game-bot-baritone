package net.zenxarch.bot.defense;

import java.util.ArrayList;

public class Settings {
  private final ArrayList<Setting<?>> settings;

  public Settings() { this.settings = new ArrayList<>(); }

  public Setting<?> get(String name) {
    for (Setting<?> setting : settings) {
      if (setting.getName() == name) {
        return setting;
      }
    }
    return null;
  }

  public ArrayList<String> getSettings() {
    var result = new ArrayList<String>();
    for (var setting : settings) {
      result.add(setting.getName());
    }
    return result;
  }

  public void addSetting(Setting<?> setting) { this.settings.add(setting); }

  public static class Setting<T> {
    private String name;
    private T value;

    public Setting(String name, T defaultValue) {
      this.name = name;
      this.value = defaultValue;
    }

    public String getName() { return this.name; }

    public T get() { return this.value; }

    public void set(T value) { this.value = value; }
  }

  public static class BooleanSetting extends Settings.Setting<Boolean> {
    public BooleanSetting(String name, boolean defaultValue) {
      super(name, Boolean.valueOf(defaultValue));
    }
  }
}
