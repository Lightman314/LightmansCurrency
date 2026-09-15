package io.github.lightman314.lightmanscurrency.api.helpers;

import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;

import java.util.List;

public record ExtractionResults<T extends Resource>(List<ResourceStack<T>> extracted, int totalCount) {
    public ExtractionResults(List<ResourceStack<T>> list) { this(list,ResourceHelper.getTotalResourceCount(list)); }
}
