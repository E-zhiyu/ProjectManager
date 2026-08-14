package com.sly.coffer.data.save.db.services;

import androidx.annotation.NonNull;

import com.sly.coffer.data.save.db.BookkeepingDb;
import com.sly.coffer.data.save.db.daos.AccessibilityRuleDao;
import com.sly.coffer.data.save.db.entities.AccessibilityRuleEntity;
import com.sly.coffer.data.save.db.entities.AccessibilityRuleTransferEntity;
import com.sly.coffer.data.save.db.entities.PickedViewEntity;
import com.sly.coffer.data.save.db.entities.composite.ui.PickedViewUiModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class AccessibilityRuleService {
    /**
     * 添加新无障碍规则
     *
     * @param rule      新规则
     * @param transfer  新规则对应的转账账户数据
     * @param tagIdList 新规则的标签编号数据
     * @param db        数据库实例
     * @return 是否完成
     */
    public static Completable addNewAccessibilityRule(
            AccessibilityRuleEntity rule,
            AccessibilityRuleTransferEntity transfer,
            List<Long> tagIdList,
            BookkeepingDb db
    ) {
        return Completable.defer(() -> {
            db.accessibilityRuleDao().addAccessibilityRule(rule, transfer, tagIdList);
            return Completable.complete();
        });
    }

    /**
     * 修改无障碍规则
     *
     * @param rule      修改后的规则
     * @param transfer  转账账户数据
     * @param tagIdList 标签编号数据
     * @param db        数据库实例
     * @return 是否完成
     */
    public static Completable modifyAccessibilityRule(
            AccessibilityRuleEntity rule,
            AccessibilityRuleTransferEntity transfer,
            List<Long> tagIdList,
            BookkeepingDb db
    ) {
        return Completable.defer(() -> {
            db.accessibilityRuleDao().modifyAccessibilityRule(rule, transfer, tagIdList);
            return Completable.complete();
        });
    }

    /**
     * 添加拾取的视图
     *
     * @param pickedViewEntity 拾取的视图
     * @param db               数据库实例
     * @return 分配的编号
     */
    public static Single<Long> addPickedView(PickedViewEntity pickedViewEntity, BookkeepingDb db) {
        return Single.defer(() -> {
            long id = db.accessibilityRuleDao().addPickedView(pickedViewEntity);
            return Single.just(id);
        });
    }

    /**
     * 获取所有拾取的视图
     *
     * @param db      数据库实例
     * @param keyword 搜索关键词
     * @return 拾取的视图列表，包含应用分隔符
     */
    public static Flowable<List<PickedViewUiModel>> getAllPickedView(@NonNull BookkeepingDb db, String keyword) {
        AccessibilityRuleDao dao = db.accessibilityRuleDao();
        String safeKeyword = "";

        if (keyword != null && !keyword.trim().isEmpty()) {
            safeKeyword = keyword.replace("/", "//")
                    .replace("%", "/%")
                    .replace("_", "/_");
        }

        int isSearchFilter = !safeKeyword.isEmpty() ? 1 : 0;
        return dao.getAllPickedViewFlowable(safeKeyword, isSearchFilter)
                .map(rawList -> {
                    List<PickedViewUiModel> resultList = new ArrayList<>();

                    //判空
                    if (rawList.isEmpty()) {
                        return resultList;
                    }

                    //通过关系远近进行分组
                    Map<String, List<PickedViewEntity>> groupedMap = rawList.stream()
                            .collect(Collectors.groupingBy(
                                    PickedViewEntity::getAppName,
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));

                    //循环插入分隔符和 Item
                    for (Map.Entry<String, List<PickedViewEntity>> entry : groupedMap.entrySet()) {
                        String separatorText = entry.getKey();
                        resultList.add(new PickedViewUiModel.Separator(separatorText));

                        List<PickedViewUiModel.Item> itemList = entry.getValue().stream()
                                .map(PickedViewUiModel.Item::new)
                                .collect(Collectors.toList());
                        resultList.addAll(itemList);
                    }

                    return resultList;
                });
    }
}
