package me.roundaround.inventorymanagement.client.gui.widget.button;

import me.roundaround.inventorymanagement.api.ButtonContext;
import me.roundaround.inventorymanagement.api.PositioningFunction;
import me.roundaround.inventorymanagement.network.SortInventoryPacket;
import me.roundaround.roundalib.config.value.Position;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

public class SortInventoryButton<H extends ScreenHandler, S extends HandledScreen<H>>
    extends ButtonBase<H, S> {
  public SortInventoryButton(
      Position offset, PositioningFunction<H, S> positioningFunction, ButtonContext<H, S> context) {
    super(positioningFunction.apply(context),
        offset,
        new Position(0, 0),
        positioningFunction,
        context,
        (button) -> SortInventoryPacket.sendToServer(context.isPlayerInventory()),
        getTooltip(context.isPlayerInventory()));
  }

  private static Text getTooltip(boolean isPlayerInventory) {
    String key = isPlayerInventory
        ? "inventorymanagement.button.sort_player"
        : "inventorymanagement.button.sort_container";
    return Text.translatable(key);
  }
}
