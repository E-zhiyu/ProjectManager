package com.sly.coffer.data.save.db.entities.composite.ui;

import com.sly.coffer.data.save.db.entities.PickedViewEntity;

import java.util.List;

public class PickedViewGroupUiModel {
    public static class Item extends PickedViewGroupUiModel {
        public List<PickedViewEntity> viewList;

        public Item(List<PickedViewEntity> viewList) {
            this.viewList = viewList;
        }
    }

    public static class Separator extends PickedViewGroupUiModel {
        public String text;

        public Separator(String text) {
            this.text = text;
        }
    }
}
