package net.zenxarch.bot.defense;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Settings {
  private static final Map<String, Setting<?>> settingsMap = new HashMap<>();

  private static final ArrayList<String> modules = new ArrayList<>();

  public static void registerModule(String module) {
    modules.add(module.toLowerCase());
  }

  public static void registerSetting(String module, String setting, Type type) {
    if (modules.contains(module.toLowerCase())) {
      settingsMap.put(module.toLowerCase() + "." + setting.toLowerCase(),
                      new BoolSetting());
    }
  }

  public static boolean getBoolean(String identifier) {
    if (settingsMap.containsKey(identifier) &&
        settingsMap.get(identifier).type == Type.Bool) {
      return ((BoolSetting)settingsMap.get(identifier)).get();
    }
    return false;
  }

  public static void execute(String s) {
    var a = s.indexOf('.');
    var b = s.indexOf('=');
    if (a == -1 || b < a)
      return;
    var moduleName = toWord(s.substring(0, a));
    var settingName = toWord(s.substring(a + 1, b));
    var settingValue = s.substring(b + 1);
    var instance = settingsMap.get(moduleName + "." + settingName);
    if (instance == null)
      return;
    instance.accept(settingValue);
  }

  public static CompletableFuture<Suggestions>
  suggest(SuggestionsBuilder builder) {
    var string = builder.getRemaining();
    var a = string.indexOf('a');
    var b = string.indexOf('b');
    if (a == -1) {
      var word = toWord(string);
      for (var mod : modules) {
        if (mod.startsWith(word))
          builder.suggest(mod);
      }
    } else if (b == -1) {
      var mod = toWord(string.substring(0, a));
      var set = toWord(string.substring(a + 1));
      for (var s : settingsMap.keySet()) {
        if (s.startsWith(mod + "." + set))
          builder.suggest(s);
      }
    } else if (a < b) {
      var mod = toWord(string.substring(0, a));
      var set = toWord(string.substring(a + 1, b));
      var val = string.substring(b);
      var instance = settingsMap.get(mod + "." + set);
      if (instance != null) {
        for (var v : instance.suggest(val)) {
          builder.suggest(mod + "." + set + "=" + v);
        };
      }
    }
    return builder.buildFuture();
  }

  private static String toWord(String s) {
    String result = "";
    for (int i = 0; i < s.length(); i++) {
      var ch = s.charAt(i);
      if (ch >= 'a' && ch <= 'z') {
        result += ch;
      } else if (ch >= 'A' && ch <= 'Z') {
        result += ch - ('A' - 'a');
      }
    }
    return result;
  }

  public static abstract class Setting<T> {
    final Type type;
    private T value;
    public Setting(Type type) { this.type = type; }

    public T get() { return value; }

    public void set(T value) { this.value = value; }

    public abstract boolean accept(String s);
    public abstract List<String> suggest(String input);
  }

  public static class BoolSetting extends Setting<Boolean> {

    public BoolSetting() { super(Type.Bool); }

    @Override
    public boolean accept(String s) {
      switch (s) {
      case "true":
        this.set(true);
        break;
      case "false":
        this.set(false);
      case "toggle":
        this.set(!this.get());
      default:
        return false;
      }
      return true;
    }

    @Override
    public List<String> suggest(String input) {
      var list = List.of("true", "false", "toggle");
      list.removeIf((s) -> !s.startsWith(input));
      return list;
    }
  }

  public enum Type { Bool }
}
