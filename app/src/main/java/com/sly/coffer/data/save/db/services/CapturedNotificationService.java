package com.sly.coffer.data.save.db.services;

import com.sly.coffer.auxiliary.classes.CustomDateTimeFormatter;
import com.sly.coffer.data.save.db.BookkeepingDb;
import com.sly.coffer.data.save.db.daos.CapturedNotificationDao;
import com.sly.coffer.data.save.db.entities.CapturedNotificationEntity;
import com.sly.coffer.data.save.db.entities.composite.ui.CapturedNotificationUiModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Flowable;

public class CapturedNotificationService {
    /**
     * 获取所有捕获的通知
     *
     * @param db      数据库实例
     * @param keyword 搜索关键词
     * @return 捕获的通知列表，包含日期分隔符
     */
    public static Flowable<List<CapturedNotificationUiModel>> getAllCapturedNotification(BookkeepingDb db, String keyword) {
        CapturedNotificationDao notificationDao = db.capturedNotificationDao();
        String safeKeyword = "";

        if (keyword != null && !keyword.trim().isEmpty()) {
            safeKeyword = keyword.replace("/", "//")
                    .replace("%", "/%")
                    .replace("_", "/_");
        }

        int isSearchFilter = !safeKeyword.isEmpty() ? 1 : 0;
        return notificationDao.getAllCapturedNotificationFlowable(safeKeyword, isSearchFilter)
                .map(rawList -> {
                    List<CapturedNotificationUiModel> resultList = new ArrayList<>();

                    //判空
                    if (rawList.isEmpty()) {
                        return resultList;
                    }

                    //通过关系远近进行分组
                    Map<LocalDate, List<CapturedNotificationEntity>> groupedMap = rawList.stream()
                            .collect(Collectors.groupingBy(
                                    entity -> entity.getTime().toLocalDate(),
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));

                    //循环插入分隔符和 Item
                    for (Map.Entry<LocalDate, List<CapturedNotificationEntity>> entry : groupedMap.entrySet()) {
                        String separatorText = entry.getKey().format(CustomDateTimeFormatter.DATE);
                        resultList.add(new CapturedNotificationUiModel.Separator(separatorText));

                        List<CapturedNotificationUiModel.Item> itemList = entry.getValue().stream()
                                .map(CapturedNotificationUiModel.Item::new)
                                .collect(Collectors.toList());
                        resultList.addAll(itemList);
                    }

                    return resultList;
                });
    }
}
