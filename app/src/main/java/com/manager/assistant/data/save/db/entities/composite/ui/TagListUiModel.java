package com.manager.assistant.data.save.db.entities.composite.ui;

import com.manager.assistant.data.save.db.entities.TagEntity;

public class TagListUiModel {
    public static class Item extends TagListUiModel {
        public final TagEntity entity;

        public Item(TagEntity entity) {
            this.entity = entity;
        }
    }

    public static class Separator extends TagListUiModel {
        public final String text;

        public Separator(String text) {
            this.text = text;
        }
    }
}
