package com.manager.assistant.data.save.db.services;

import androidx.annotation.NonNull;

import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.daos.TagDao;
import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.entities.TagGroupEntity;
import com.manager.assistant.data.save.db.entities.composite.ui.TagGroupUiModel;
import com.manager.assistant.data.save.db.entities.composite.ui.TagListUiModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

public class TagService {
    /**
     * 获取分组后的标签数据
     *
     * @param db  数据库实例
     * @param pow 标签作用域标识符，传递0以获取所有作用域内的标签
     * @return 带有分组名称分隔符的标签列表，已按照分组编号分组
     */
    public static Flowable<List<TagGroupUiModel>> getGroupedTagFlowable(@NonNull BookkeepingDb db, int pow) {
        TagDao dao = db.tagDao();
        return Flowable.combineLatest(
                dao.getAllTagFlowable(pow),
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

    /**
     * 获取标签列表数据
     *
     * @param db 数据库实例
     * @return 带有分隔符的标签列表数据，支持响应式更新
     */
    public static Flowable<List<TagListUiModel>> getTagListFlowable(@NonNull BookkeepingDb db) {
        TagDao dao = db.tagDao();
        return Flowable.combineLatest(
                dao.getAllTagFlowable(0),
                dao.getAllTagGroupFlowable(),
                (tagList, groupList) -> {
                    //将标签数据按照分组编号分组
                    Map<Long, List<TagEntity>> groupedTagMap = tagList.stream()
                            .collect(Collectors.groupingBy(
                                    TagEntity::getGroupId,
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));

                    //生成带有分隔符的标签列表
                    List<TagListUiModel> resultList = new ArrayList<>();
                    for (TagGroupEntity group : groupList) {
                        resultList.add(new TagListUiModel.Group(group));

                        List<TagEntity> tagInGroupList = groupedTagMap.get(group.getGroupId());
                        if (tagInGroupList != null) {
                            List<TagListUiModel.Item> itemList = tagInGroupList.stream()
                                    .map(TagListUiModel.Item::new)
                                    .collect(Collectors.toList());
                            resultList.addAll(itemList);
                        }
                    }

                    return resultList;
                }
        );
    }

    /**
     * 添加标签
     *
     * @param tag       新添加的标签实体
     * @param groupName 用户输入的分组名称
     * @param db        数据库实例
     * @return 是否完成
     */
    public static Completable addTag(TagEntity tag, String groupName, BookkeepingDb db) {
        return Completable.defer(() -> {
            db.tagDao().addTag(tag, groupName);
            return Completable.complete();
        });
    }

    /**
     * 修改标签
     *
     * @param tag       修改后的标签实体
     * @param groupName 用户输入的分组名称
     * @param db        数据库实例
     * @return 是否完成
     */
    public static Completable modifyTag(TagEntity tag, String groupName, BookkeepingDb db) {
        return Completable.defer(() -> {
            db.tagDao().modifyTag(tag, groupName);
            return Completable.complete();
        });
    }

    /**
     * 合并标签分组
     * @param mergedGroup 被合并的标签分组
     * @param targetGroup 合并到的标签分组
     * @param db 数据库实例
     * @return 是否完成
     */
    public static Completable mergeTagGroup(TagGroupEntity mergedGroup,TagGroupEntity targetGroup,BookkeepingDb db) {
        return Completable.defer(()->{
            db.tagDao().mergeGroup(mergedGroup,targetGroup);
            return Completable.complete();
        });
    }
}
