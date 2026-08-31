package com.sly.coffer.data.save.db.entities.composite.ui;

import com.sly.coffer.data.save.db.entities.PickedPageEntity;

public class PickedPageListUiModel {
    public static class Item extends PickedPageListUiModel {
        public final PickedPageEntity entity;

        public Item(PickedPageEntity entity) {
            this.entity = entity;
        }
    }

    public static class Separator extends PickedPageListUiModel {
        public final String text;

        public Separator(String text) {
            this.text = text;
        }
    }
}
