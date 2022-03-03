package net.zenxarch.bot.task;

import java.util.ArrayList;

public class MultiTask extends Task {
  private ArrayList<Task> subtasks;
  private boolean running;

  public MultiTask(String name, ArrayList<Task> subtasks) {
    super(name);
    this.subtasks = subtasks;
    this.running = true;
  }

  @Override
  public void onTick() {
    if (subtasks.size() == 0) {
      running = false;
      return;
    }
    subtasks.get(0).onTick();
    if (!subtasks.get(0).isRunning()) {
      subtasks.remove(0);
    }
  }

  public void addTask(Task t) { this.subtasks.add(t); }

  @Override
  public boolean isRunning() {
    return running;
  }
}
