package com.sly.coffer.data.save.db.entities.composite.ui;

import com.sly.coffer.data.save.db.entities.PickedViewEntity;

public class PickedViewListUiModel {
    public static class Item extends PickedViewListUiModel {
        public final PickedViewEntity entity;

        public Item(PickedViewEntity entity) {
            this.entity = entity;
        }
    }

    public static class Separator extends PickedViewListUiModel {
        public final String text;

        public Separator(String text) {
            this.text = text;
        }
    }
}
