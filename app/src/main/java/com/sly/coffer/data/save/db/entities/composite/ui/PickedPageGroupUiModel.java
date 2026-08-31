package com.sly.coffer.data.save.db.entities.composite.ui;

import com.sly.coffer.data.save.db.entities.PickedPageEntity;

import java.util.List;

public class PickedPageGroupUiModel {
    public static class Item extends PickedPageGroupUiModel {
        public List<PickedPageEntity> viewList;

        public Item(List<PickedPageEntity> viewList) {
            this.viewList = viewList;
        }
    }

    public static class Separator extends PickedPageGroupUiModel {
        public String text;

        public Separator(String text) {
            this.text = text;
        }
    }
}
