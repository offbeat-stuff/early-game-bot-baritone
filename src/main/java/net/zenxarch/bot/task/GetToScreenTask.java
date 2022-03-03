package net.zenxarch.bot.task;

import baritone.api.BaritoneAPI;
import baritone.api.process.IGetToBlockProcess;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class GetToScreenTask extends Task {
  private boolean running;
  private final ArrayList<Block> supportedBlocks =
      new ArrayList<Block>() {
        {
          add(Blocks.CRAFTING_TABLE);
          add(Blocks.FURNACE);
          add(Blocks.BLAST_FURNACE);
          add(Blocks.SMOKER);
          add(Blocks.ENCHANTING_TABLE);
          add(Blocks.STONECUTTER);
          add(Blocks.SMITHING_TABLE);
        }
      };

  private Block goal;
  private boolean init;
  public GetToScreenTask(Block b) {
    super("GetToScreenTask");
    if (supportedBlocks.contains(b)) {
      this.running = true;
      this.goal = b;
      this.init = true;
    }
  }

  @Override
  public void onTick() {
    IGetToBlockProcess p = BaritoneAPI.getProvider()
                               .getPrimaryBaritone()
                               .getGetToBlockProcess();
    if (init && !p.isActive()) {
      BaritoneAPI.getSettings().enterPortal.value = true;
      p.getToBlock(goal);
      init = false;
      return;
    }

    if (!p.isActive()) {
      this.running = false;
      return;
    }
  }
}
