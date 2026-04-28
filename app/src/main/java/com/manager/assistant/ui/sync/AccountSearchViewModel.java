package com.manager.assistant.ui.sync;

import androidx.lifecycle.ViewModel;

public class AccountSearchViewModel extends ViewModel {
    private final UnPeekLiveData<String> searchTextData = new UnPeekLiveData<>("");
    private String lastSearchText = null;

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
        if (lastSearchText == null || !lastSearchText.equals(searchText)) {
            lastSearchText = searchText;
            searchTextData.postValue(searchText);
        }
    }
}
