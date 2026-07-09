package com.manager.assistant.data.save.db.entities.composite.ui;

import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.entities.TagGroupEntity;

public class TagListUiModel {
    public static class Item extends TagListUiModel {
        public final TagEntity entity;

        public Item(TagEntity entity) {
            this.entity = entity;
        }
    }

    public static class Separator extends TagListUiModel {
        public final TagGroupEntity group;

        public Separator(TagGroupEntity group) {
            this.group = group;
        }
    }
}
