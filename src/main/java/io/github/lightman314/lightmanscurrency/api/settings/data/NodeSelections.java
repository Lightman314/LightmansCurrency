package io.github.lightman314.lightmanscurrency.api.settings.data;

import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.*;

public final class NodeSelections {

    Map<String,Set<String>> data = new HashMap<>();

    public NodeSelections() { }

    public boolean nodeSelected(String node) { return this.data.containsKey(node); }

    public boolean subNodeSelected(String node, String subNode) {
        Set<String> subNodes = this.data.getOrDefault(node,new HashSet<>(0));
        return subNodes.contains(subNode);
    }

    public void toggleNode(String node) { this.setNodeSelected(node,!this.nodeSelected(node)); }

    public void toggleSubNode(String node, String subNode)
    {
        if(this.subNodeSelected(node,subNode))
            this.removeSubNode(node,subNode);
        else
            this.addSubNode(node,subNode);
    }

    public void setNodeSelected(String node, boolean selected) {
        if(selected == this.nodeSelected(node))
            return;
        if(selected)
            this.data.put(node,new HashSet<>());
        else
            this.data.remove(node);
    }

    public void addSubNode(String node, String subNode)
    {
        Set<String> set = this.data.getOrDefault(node,new HashSet<>(1));
        set.add(subNode);
        this.data.put(node,set);
    }

    public void removeSubNode(String node, String subNode)
    {
        Set<String> set = this.data.getOrDefault(node,null);
        if(set == null)
            return;
        set.remove(subNode);
        this.data.put(node,set);
    }

    public void setSubNodes(String node, Collection<String> subNodes)
    {
        Set<String> set = new HashSet<>(subNodes.size());
        set.addAll(subNodes);
        this.data.put(node,set);
    }

    public CompoundTag write()
    {
        CompoundTag tag = new CompoundTag();
        this.data.forEach((node,subNodes) -> {
            List<String> list = new ArrayList<>(subNodes);
            tag.put(node,TagUtil.writeStringList(list));
        });
        return tag;
    }

    public static NodeSelections read(CompoundTag tag)
    {
        NodeSelections selections = new NodeSelections();
        for(String node : tag.getAllKeys())
        {
            if(tag.getTagType(node) == Tag.TAG_LIST)
            {
                selections.setNodeSelected(node,true);
                selections.setSubNodes(node,TagUtil.loadStringList(tag.getList(node,Tag.TAG_STRING)));
            }
        }
        return selections;
    }

}