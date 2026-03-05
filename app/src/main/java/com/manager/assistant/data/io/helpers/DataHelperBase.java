package com.manager.assistant.data.io.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 数据帮助器基类
 *
 * @param <H> 数据库的帮助器
 * @param <M> 数据字典类型
 */
abstract public class DataHelperBase<H extends SQLiteOpenHelper, M> {
    protected H dbHelper;              //数据库帮助器
    protected Class<M> mapClass;        //数据字典类型

    public DataHelperBase(Context context) {
        dbHelper = createHelper(context);
        mapClass = getMapClass();
    }

    protected abstract Class<M> getMapClass();

    protected abstract H createHelper(Context context); //子类需要实现的生成helper类的方法

    protected abstract M getAllDataInMap();             //获取数据字典的方法

    protected abstract void saveDataInMapToDb(M map);   //将map中的数据保存至数据库的方法

    /**
     * 将JSON中的数据保存到数据库
     *
     * @param json JSON数据字符串
     * @return 是否导入成功
     */
    public final boolean saveJsonDataToDb(String json) {
        try {
            //得到数据字典实例
            ObjectMapper mapper = new ObjectMapper();
            M dataMap = mapper.readValue(json, mapClass);

            //将对应的数据写入数据库
            saveDataInMapToDb(dataMap);

            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 将数据库中的数据转换为JSON字符串
     *
     * @return 转换得到的JSON字符串
     * @throws JsonProcessingException JSON解析失败引发的异常
     */
    public String getDataInJSON() throws JsonProcessingException {
        M dataMap = getAllDataInMap();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(dataMap);
    }
}
