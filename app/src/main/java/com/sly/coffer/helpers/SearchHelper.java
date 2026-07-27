package com.sly.coffer.helpers;

import android.content.Context;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.sly.coffer.auxiliary.interfaces.search.SearchBarMenuListener;
import com.sly.coffer.auxiliary.interfaces.search.SearchExecuter;
import com.sly.coffer.data.save.preference.SearchHistoryPreference;
import com.sly.coffer.helpers.appearence.VisibilityHelper;
import com.sly.coffer.ui.others.adapters.SearchHistoryAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchHelper {
    /**
     * 初始化搜索视图
     *
     * @param searchBar                 {@link SearchBar}对象
     * @param searchView                {@link SearchView}对象
     * @param searchHistoryRecyclerView 显示搜索历史的{@link RecyclerView}对象
     * @param clearBtn                  清空搜索历史的按钮
     * @param searchKey                 搜索历史结果保存的 Key
     * @param executer                  执行搜索的执行器
     * @param menuListener              searchBar 的菜单项的点击监听
     */
    public static void initSearchComponents(
            @NonNull SearchBar searchBar,
            @NonNull SearchView searchView,
            @NonNull RecyclerView searchHistoryRecyclerView,
            @NonNull MaterialButton clearBtn,
            String searchKey,
            @NonNull SearchExecuter executer,
            SearchBarMenuListener menuListener
    ) {
        searchView.setupWithSearchBar(searchBar);

        Context context = searchBar.getContext();

        //搜索历史显示
        SearchHistoryAdapter historyAdapter = new SearchHistoryAdapter(
                searchKey,
                (keyword, anchor) -> {
                    searchView.hide();
                    searchBar.setText(keyword.trim());

                    //执行搜索
                    executer.executeSearch(keyword.trim());
                },
                (keywordList, adapter) -> {
                    boolean isVisible = !keywordList.isEmpty();
                    VisibilityHelper.toggleViewExpansion(
                            searchView,
                            isVisible,
                            () -> adapter.submitList(new ArrayList<>(keywordList)),
                            searchHistoryRecyclerView
                    );
                }
        );
        List<String> initList = SearchHistoryPreference.getHistory(
                searchKey,
                context
        );
        historyAdapter.submitList(new ArrayList<>(initList));
        searchHistoryRecyclerView.setAdapter(historyAdapter);

        //设置清除搜索历史按钮点击监听
        clearBtn.setOnClickListener(v -> {
            SearchHistoryPreference.clearHistory(
                    searchKey,
                    context
            );
            VisibilityHelper.toggleViewExpansion(
                    searchView,
                    false,
                    () -> historyAdapter.submitList(new ArrayList<>()),
                    searchHistoryRecyclerView
            );
        });

        //设置搜索监听
        searchView.getEditText().setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                //收起搜索视图
                String keyword = String.valueOf(searchView.getText());
                searchView.hide();

                //设置搜索文本到 SearchBar上
                searchBar.setText(keyword.trim());

                //触发搜索执行器
                executer.executeSearch(keyword.trim());

                //保存搜索历史
                List<String> historyList = SearchHistoryPreference.addKeyword(
                        keyword,
                        searchKey,
                        context
                );
                historyAdapter.submitListWithAnimation(new ArrayList<>(historyList));

                return true;
            } else {
                return false;
            }
        });

        //设置 SearchBar 的菜单按钮点击监听
        if (menuListener != null) {
            searchBar.setOnMenuItemClickListener(menuListener::onItemClicked);
        }
    }
}
