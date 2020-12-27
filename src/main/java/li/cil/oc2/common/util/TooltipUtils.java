package li.cil.oc2.common.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

import static li.cil.oc2.Constants.BLOCK_ENTITY_INVENTORY_TAG_NAME;
import static li.cil.oc2.Constants.BLOCK_ENTITY_TAG_NAME_IN_ITEM;

public final class TooltipUtils {
    private static final ThreadLocal<List<ItemStack>> ITEM_STACKS = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<IntList> ITEM_STACKS_SIZES = ThreadLocal.withInitial(IntArrayList::new);

    public static void addInventoryInformation(final ItemStack stack, final List<ITextComponent> tooltip) {
        final CompoundNBT tileEntityNbt = stack.getChildTag(BLOCK_ENTITY_TAG_NAME_IN_ITEM);
        if (tileEntityNbt != null && tileEntityNbt.contains(BLOCK_ENTITY_INVENTORY_TAG_NAME, NBTTagIds.TAG_COMPOUND)) {
            final CompoundNBT itemHandlerNbt = tileEntityNbt.getCompound(BLOCK_ENTITY_INVENTORY_TAG_NAME);
            final ListNBT itemsNbt = itemHandlerNbt.getList("Items", NBTTagIds.TAG_COMPOUND);

            final List<ItemStack> itemStacks = ITEM_STACKS.get();
            itemStacks.clear();
            final IntList itemStackSizes = ITEM_STACKS_SIZES.get();
            itemStackSizes.clear();

            for (int i = 0; i < itemsNbt.size(); i++) {
                final CompoundNBT itemNbt = itemsNbt.getCompound(i);
                final ItemStack itemStack = ItemStack.read(itemNbt);

                boolean didMerge = false;
                for (int j = 0; j < itemStacks.size(); j++) {
                    final ItemStack existingStack = itemStacks.get(j);
                    if (ItemStack.areItemsEqual(existingStack, itemStack) &&
                        ItemStack.areItemStackTagsEqual(existingStack, itemStack)) {
                        final int existingCount = itemStackSizes.getInt(j);
                        itemStackSizes.set(j, existingCount + itemStack.getCount());
                        didMerge = true;
                        break;
                    }
                }

                if (!didMerge) {
                    itemStacks.add(itemStack);
                    itemStackSizes.add(itemStack.getCount());
                }
            }

            for (int i = 0; i < itemStacks.size(); i++) {
                final ItemStack itemStack = itemStacks.get(i);
                tooltip.add(new StringTextComponent("")
                        .append(itemStack.getDisplayName())
                        .modifyStyle(style -> style.setColor(Color.fromTextFormatting(TextFormatting.GRAY)))
                        .append(new StringTextComponent(" x")
                                .appendString(String.valueOf(itemStackSizes.getInt(i)))
                                .modifyStyle(style -> style.setColor(Color.fromTextFormatting(TextFormatting.DARK_GRAY))))
                );
            }
        }
    }
}