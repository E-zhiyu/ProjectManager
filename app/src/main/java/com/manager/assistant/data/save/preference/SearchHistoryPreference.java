package com.manager.assistant.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryPreference {
    private static final String PREF_NAME = "SearchHistoryPreference";
    public static final String KEY_ACCOUNT_REMARK = "account_remark";   //流水记录备注搜索历史
    public static final String KEY_APP_NAME = "app_name";               //应用名称搜索历史

    /**
     * 读取搜索历史
     *
     * @param key     需要读取的历史所对应的键值，具体可见{@link SearchHistoryPreference}的静态成员变量
     * @param context 上下文
     * @return 包含已保存的搜索记录的字符串列表
     */
    public static List<String> getHistory(String key, @NonNull Context context) {
        //读取原始JSON字符串
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(key, "[]");

        //将JSON转换为列表
        ObjectMapper mapper = new ObjectMapper();
        List<String> historyList;
        try {
            historyList = mapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            historyList = new ArrayList<>();
        }

        return historyList;
    }

    /**
     * 覆写搜索历史记录
     *
     * @param key         需要读取的历史所对应的键值，具体可见{@link SearchHistoryPreference}的静态成员变量
     * @param historyList 修改后的历史记录列表
     * @param context     上下文
     */
    public static void setHistory(String key, List<String> historyList, Context context) {
        //将列表转换为JSON
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(historyList);
        } catch (JsonProcessingException e) {
            json = "[]";
        }

        //写入Preference
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(key, json).apply();
    }
}
