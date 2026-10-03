package net.xiaohuige_hhy.winefoxfaction.mixin;

import com.solegendary.reignofnether.building.production.ProductionItem;
import com.solegendary.reignofnether.building.production.ProductionItems;

import net.xiaohuige_hhy.winefoxfaction.register.ModProductionItems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(ProductionItems.class)
public class ProductionItemsMixin {

	@Redirect(method = "<clinit>", at = @At(value = "INVOKE",
			target = "Ljava/util/List;of([Ljava/lang/Object;)Ljava/util/List;"))
	private static List<ProductionItem> appendModProductionItems(Object[] elements) {
		List<ProductionItem> list = new ArrayList<>(elements.length + ModProductionItems.ALL.size());
		for (Object element : elements) {
			list.add((ProductionItem) element);
		}
		list.addAll(ModProductionItems.ALL);
		return list;
	}
}
