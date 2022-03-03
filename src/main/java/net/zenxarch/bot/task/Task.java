package net.zenxarch.bot.task;

public class Task {
  private boolean running;
  private String name;

  public Task(String name) { this.name = name; }

  public void onTick() {
    if (!this.running)
      return;
  };

  public boolean isRunning() { return this.running; }

  public String getName() { return this.name; }
}
