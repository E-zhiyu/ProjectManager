package com.sly.coffer.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.sly.coffer.data.save.db.entities.composite.AccessibilityRuleWithDetailModel;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface AccessibilityRuleDao {
    @Transaction
    @Query("SELECT * FROM accessibilityRules WHERE enabled = 1")
    Flowable<List<AccessibilityRuleWithDetailModel>> getOpenedAccessibilityRuleWithDetailFlowable();
}
