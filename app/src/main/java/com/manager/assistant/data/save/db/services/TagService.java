package com.manager.assistant.data.save.db.services;

import androidx.annotation.NonNull;

import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.daos.TagDao;
import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.entities.TagGroupEntity;
import com.manager.assistant.data.save.db.entities.composite.ui.TagGroupUiModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Flowable;

public class TagService {
    /**
     * 获取分组后的标签数据
     *
     * @param db 数据库实例
     * @return 带有分组名称分隔符的标签列表，已按照分组编号分组
     */
    public static Flowable<List<TagGroupUiModel>> getGroupedTagFlowable(@NonNull BookkeepingDb db) {
        TagDao dao = db.tagDao();
        return Flowable.zip(
                dao.getAllTagFlowable(),
                dao.getAllTagGroupFlowable(),
                (tagList, groupList) -> {
                    //将标签数据按照分组编号分组
                    Map<Long, List<TagEntity>> groupedTagMap = tagList.stream()
                            .collect(Collectors.groupingBy(
                                    TagEntity::getGroupId,
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));

                    //获取实际的标签分组数据
                    Map<Long, String> groupNameMap = groupList.stream()
                            .collect(Collectors.toMap(
                                    TagGroupEntity::getGroupId,
                                    TagGroupEntity::getName
                            ));

                    //生成带有分隔符的标签列表
                    List<TagGroupUiModel> resultList = new ArrayList<>();
                    for (Map.Entry<Long, List<TagEntity>> entry : groupedTagMap.entrySet()) {
                        long groupId = entry.getKey();
                        String groupName = groupNameMap.getOrDefault(groupId, "<未知分组>");
                        resultList.add(new TagGroupUiModel.Separator(groupName));

                        resultList.add(new TagGroupUiModel.Item(entry.getValue()));
                    }

                    return resultList;
                }
        );
    }
}
