package com.manager.assistant.data.save.db.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.converters.DateTimeConverter;
import com.manager.assistant.data.save.db.daos.AccountDao;
import com.manager.assistant.data.save.db.entities.AccountEntity;
import com.manager.assistant.data.save.db.entities.composite.ui.AccountUiModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Flowable;

public class AccountService {
    /**
     * 加载流水记录列表数据
     *
     * @param filterTagSet  标签白名单的 ID 列表
     * @param filterTypeSet 种类白名单的编号列表
     * @param start          起始日期（包含）
     * @param end            结束日期（包含）
     * @param keyword        搜索关键词
     * @param db             数据库实例
     * @return 符合过滤条件的流水记录数据，支持响应式更新
     */
    public static Flowable<List<AccountUiModel>> loadAccountListDataFlowable(
            @NonNull Set<Long> filterTagSet,
            @NonNull Set<Integer> filterTypeSet,
            @Nullable LocalDate start,
            @Nullable LocalDate end,
            @Nullable String keyword,
            @NonNull BookkeepingDb db
    ) {
        //判断需要使用哪些过滤
        boolean useTypeFilter = !filterTypeSet.isEmpty();
        boolean useTagFilter = !filterTagSet.isEmpty();
        boolean useTimeFilter = start != null && end != null;
        boolean useSearchFilter = keyword != null && !keyword.isEmpty();

        //生成基础 SQL 语句
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM accounts " +
                        "WHERE 1=1"
        );
        List<Object> args = new ArrayList<>();

        //种类过滤
        if (useTypeFilter) {
            sql.append(" AND ");
            sql.append("type IN (");

            int i = 0;
            for (Integer type : filterTypeSet) {
                sql.append("?");
                args.add(type);
                if (i < filterTagSet.size() - 1) sql.append(",");
                i++;
            }

            sql.append(")");
        }

        //标签过滤
        if (useTagFilter) {
            sql.append(" AND ");
            sql.append("accountId IN (SELECT accountId FROM accountTagRef WHERE tagId IN (");

            int i = 0;
            for (Long tagId : filterTagSet) {
                sql.append("?");
                args.add(tagId);
                if (i < filterTagSet.size() - 1) sql.append(",");
                i++;
            }

            sql.append("))");
        }

        //日期过滤
        if (useTimeFilter) {
            sql.append(" AND ");
            sql.append("dateTime >= ? AND dateTime < ?");
            args.add(DateTimeConverter.fromLocalDate(start));
            args.add(DateTimeConverter.fromLocalDate(end.plusDays(1)));
        }

        //搜索过滤
        if (useSearchFilter) {
            String safeKeyword = keyword.replace("/", "//").replace("%", "/%").replace("_", "/_");
            sql.append(" AND ");
            sql.append("remark LIKE ? ESCAPE '/'");
            args.add("%" + safeKeyword + "%");
        }

        //补上排序规则
        sql.append(" ORDER BY dateTime DESC");

        //执行查询并返回结果
        SimpleSQLiteQuery rawQuery = new SimpleSQLiteQuery(sql.toString(), args.toArray());
        AccountDao dao = db.accountDao();
        return dao.getAccountWithFilter(rawQuery)
                .map(accountList -> {
                    //按照日期分组
                    Map<LocalDate, List<AccountEntity>> dateGroupedMap = accountList.stream()
                            .collect(Collectors.groupingBy(
                                    accountEntity -> accountEntity.getDateTime().toLocalDate(),
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));

                    //转换为 UiModel
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    List<AccountUiModel> resultList = new ArrayList<>();
                    for (Map.Entry<LocalDate, List<AccountEntity>> entry : dateGroupedMap.entrySet()) {
                        LocalDate date = entry.getKey();
                        String dateStr = date.format(formatter);
                        resultList.add(new AccountUiModel.Separator(dateStr));

                        List<AccountUiModel.Item> itemList = entry.getValue().stream()
                                .map(AccountUiModel.Item::new)
                                .collect(Collectors.toList());
                        resultList.addAll(itemList);
                    }

                    return resultList;
                });
    }
}
