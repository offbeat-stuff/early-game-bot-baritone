package net.zenxarch.bot.defense;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.util.math.MathHelper;

public class Settings {
  private static final Map<String, Setting<?>> settingsMap = new HashMap<>();

  private static final ArrayList<String> modules = new ArrayList<>();

  public static void registerModule(String module) {
    modules.add(cleanup(module));
  }

  public static void registerBoolSetting(String module, String setting,
                                         boolean value) {
    if (!modules.contains(cleanup(module)))
      return;
    var n = cleanup(module) + "." + cleanup(setting);
    settingsMap.put(n, new BoolSetting(value));
  }

  public static void registerDoubleSetting(String module, String setting,
                                           double value, double min, double max,
                                           List<Double> suggestions) {
    if (!modules.contains(cleanup(module)))
      return;
    var n = cleanup(module) + "." + cleanup(setting);
    settingsMap.put(n, new DoubleSetting(value, min, max, suggestions));
  }

  public static boolean getBoolean(String identifier) {
    var s = parse(identifier);
    var n = s[0] + "." + s[1];
    if (settingsMap.containsKey(n) && settingsMap.get(n).type == Type.Bool) {
      return ((BoolSetting)settingsMap.get(n)).get();
    }
    return false;
  }

  public static double getDouble(String identifier) {
    var s = parse(identifier);
    var n = s[0] + "." + s[1];
    if (settingsMap.containsKey(n) && settingsMap.get(n).type == Type.Double) {
      return ((DoubleSetting)settingsMap.get(n)).get();
    }
    return 0;
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
    if (s[0].isEmpty()) {
      settingsMap.forEach((k, v) -> { result.add(k + " = " + v.get()); });
    } else if (s[1].isEmpty()) {
      settingsMap.forEach((k, v) -> {
        if (k.startsWith(s[0])) {
          result.add(k + " = " + v.get());
        }
      });
    } else {
      var n = s[0] + "." + s[1];
      if (!s[2].isEmpty()) {
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
      if (set.isEmpty()) {
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
      if (set.isEmpty()) {
        modules.forEach(m -> {
          if (m.startsWith(mod)) {
            builder.suggest(m);
            builder.suggest(m + ".");
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

    public BoolSetting(boolean value) {
      super(Type.Bool);
      this.set(value);
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
      return List.of("true", "false", "toggle")
          .stream()
          .filter(s -> s.startsWith(input))
          .toList();
    }
  }

  public static class DoubleSetting extends Setting<Double> {
    private final ArrayList<String> suggestions = new ArrayList<>();
    private double min, max;

    public DoubleSetting(double value, double min, double max,
                         List<Double> suggestions) {
      super(Type.Double);
      this.set(value);
      this.min = min;
      this.max = max;
      suggestions.forEach(i -> { this.suggestions.add(String.valueOf(i)); });
    }

    @Override
    public boolean accept(String s) {
      try {
        var i = Double.parseDouble(s);
        this.set(MathHelper.clamp(i, min, max));
        return true;
      } catch (Exception e) {
        return false;
      }
    }

    @Override
    public List<String> suggest(String input) {
      return suggestions.stream().filter(s -> s.startsWith(input)).toList();
    }
  }

  public enum Type { Bool, Double }
}
