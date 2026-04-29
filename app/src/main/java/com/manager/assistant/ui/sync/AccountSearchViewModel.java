package com.manager.assistant.ui.sync;

import androidx.lifecycle.ViewModel;

public class AccountSearchViewModel extends ViewModel {
    private final UnPeekLiveData<String> searchTextData = new UnPeekLiveData<>("");

    /**
     * 获取搜索文本数据
     *
     * @return 搜索文本数据
     */
    public UnPeekLiveData<String> getSearchTextData() {
        return searchTextData;
    }

    /**
     * 更新搜索文本
     *
     * @param searchText 更新后的搜索文本
     */
    public void updateSearchText(String searchText) {
        searchTextData.postValue(searchText);
    }
}
