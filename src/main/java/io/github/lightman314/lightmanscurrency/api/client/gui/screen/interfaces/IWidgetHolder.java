package io.github.lightman314.lightmanscurrency.api.client.gui.screen.interfaces;

import java.util.ArrayList;
import java.util.List;

public interface IWidgetHolder {

    <T> T addChild(T child);
    void removeChild(Object child);

    void removeAllChildren();

    static IWidgetHolder createChild(IWidgetHolder parent) { return new ChildHolder(parent); }

    final class ChildHolder implements IWidgetHolder {

        private final IWidgetHolder parent;
        private final List<Object> children = new ArrayList<>();
        private ChildHolder(IWidgetHolder parent) { this.parent = parent; }

        @Override
        public <T> T addChild(T child) {
            this.children.add(child);
            return this.parent.addChild(child);
        }

        @Override
        public void removeChild(Object child) {
            this.children.remove(child);
            this.parent.removeChild(child);
        }

        @Override
        public void removeAllChildren() {
            for(Object child : new ArrayList<>(this.children))
                this.parent.removeChild(child);
            this.children.clear();
        }
    }

}