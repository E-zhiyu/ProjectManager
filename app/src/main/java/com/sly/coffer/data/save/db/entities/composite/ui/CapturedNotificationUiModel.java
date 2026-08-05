package com.sly.coffer.data.save.db.entities.composite.ui;

import com.sly.coffer.data.save.db.entities.CapturedNotificationEntity;

public class CapturedNotificationUiModel {
    public static class Item extends CapturedNotificationUiModel {
        public CapturedNotificationEntity entity;

        public Item(CapturedNotificationEntity entity) {
            this.entity = entity;
        }
    }

    public static class Separator extends CapturedNotificationUiModel {
        public String text;

        public Separator(String text) {
            this.text = text;
        }
    }
}
