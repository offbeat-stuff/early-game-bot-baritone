package net.zenxarch.bot.defense;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class Settings {
  private static final Map<String, Setting<?>> settingsMap = new HashMap<>();

  private static final ArrayList<String> modules = new ArrayList<>();

  public static void registerModule(String module) {
    modules.add(cleanup(module));
  }

  public static void registerSetting(String module, String setting, Type type) {
    if (modules.contains(cleanup(module))) {
      settingsMap.put(cleanup(module) + "." + cleanup(setting),
                      new BoolSetting());
    }
  }

  public static boolean getBoolean(String identifier) {
    var s = parse(identifier);
    var n = s[0] + "." + s[1];
    if (settingsMap.containsKey(n) && settingsMap.get(n).type == Type.Bool) {
      return ((BoolSetting)settingsMap.get(n)).get();
    }
    return false;
  }

  public static void execute(String str) {
    var s = parse(str);
    if (settingsMap.containsKey(s[0] + "." + s[1])) {
      settingsMap.get(s[0] + "." + s[1]).accept(s[2]);
    }
  }

  public static List<String> exec(String str) {
    var s = parse(str);
    var result = new ArrayList<String>();
    if (s[0] == "") {
      settingsMap.forEach((k, v) -> { result.add(k + " = " + v.get()); });
    } else if (s[1] == "") {
      settingsMap.forEach((k, v) -> {
        if (k.startsWith(s[0])) {
          result.add(k + " = " + v.get());
        }
      });
    } else {
      var n = s[0] + "." + s[1];
      if (s[2] != "") {
        execute(str);
      }
      if (settingsMap.containsKey(n)) {
        result.add(n + " = " + settingsMap.get(n).get());
      }
    }
    return result;
  }

  public static CompletableFuture<Suggestions>
  suggest(SuggestionsBuilder builder) {
    var strings = parse(builder.getRemaining());
    var mod = strings[0];
    var set = strings[1];
    var val = strings[2];

    var perfectMatch = false;

    if (modules.contains(mod)) {
      if (set == "") {
        settingsMap.keySet().forEach(ss -> {
          if (ss.startsWith(mod + ".")) {
            builder.suggest(ss);
          }
        });
        perfectMatch = true;
      } else if (settingsMap.keySet().contains(mod + "." + set)) {
        settingsMap.get(mod + "." + set).suggest(val).forEach(s -> {
          builder.suggest(mod + "." + set + "." + s);
        });
        perfectMatch = true;
      }
    }

    if (!perfectMatch) {
      if (set == "") {
        modules.forEach(m -> {
          if (m.startsWith(mod)) {
            builder.suggest(mod);
            builder.suggest(mod + ".");
          }
        });
      } else {
        settingsMap.keySet().forEach(s -> {
          if (s.startsWith(mod + "." + set)) {
            builder.suggest(s);
            builder.suggest(s + ".");
          }
        });
      }
    }
    return builder.buildFuture();
  }

  private static String cleanup(String s, Predicate<Character> test) {
    var result = "";
    for (int i = 0; i < s.length(); i++) {
      if (test.test(s.charAt(i)))
        result = result + Character.toLowerCase(s.charAt(i));
    }
    return result;
  }

  private static String cleanup(String s) {
    return cleanup(s, Character::isLetter);
  }

  private static String[] parse(String s) {
    var a = split(s, '.');
    var b = split(a[1], '.');
    return new String[] {cleanup(a[0]), cleanup(b[0]),
                         cleanup(b[1], Character::isLetterOrDigit)};
  }

  private static String[] split(String s, char delimiter) {
    var a = s.indexOf(delimiter);
    return a == -1 ? new String[] {s, ""}
                   : new String[] {s.substring(0, a), s.substring(a + 1)};
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

    public BoolSetting() {
      super(Type.Bool);
      this.set(false);
    }

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

  public enum Type { Bool,Int }
}
