package com.manager.assistant.data.save.db.entities.composite.ui;

import com.manager.assistant.data.save.db.entities.AccountEntity;

public class AccountUiModel {
    public static class Item extends AccountUiModel {
        public final AccountEntity entity;

        public Item(AccountEntity entity) {
            this.entity = entity;
        }
    }

    public static class Separator extends AccountUiModel {
        public final String text;

        public Separator(String text) {
            this.text = text;
        }
    }
}
