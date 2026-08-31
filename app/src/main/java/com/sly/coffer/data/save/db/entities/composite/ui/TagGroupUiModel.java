package com.sly.coffer.data.save.db.entities.composite.ui;

import com.sly.coffer.data.save.db.entities.TagEntity;

import java.util.List;

public class TagGroupUiModel {
    public static class Item extends TagGroupUiModel {
        public List<TagEntity> tagList;

        public Item(List<TagEntity> tagList) {
            this.tagList = tagList;
        }
    }

    public static class Separator extends TagGroupUiModel {
        public String text;

        public Separator(String text) {
            this.text = text;
        }
    }
}
