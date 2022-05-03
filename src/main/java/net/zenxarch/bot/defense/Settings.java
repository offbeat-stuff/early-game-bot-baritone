package net.zenxarch.bot.defense;

import java.util.ArrayList;

public class Settings {
  private final ArrayList<Setting> settings;

  public Settings() {
    this.settings = new ArrayList<>();
    this.settings.add(new BooleanSetting("enabled", true));
  }

  public Setting get(String name) {
    for (var setting : settings) {
      if (setting.getName() == name) {
        return setting;
      }
      return null;
    }
  }

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

  public static class BooleanSetting extends Setting<Boolean> {}
}
