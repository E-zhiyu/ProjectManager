package com.manager.assistant.helpers.appearence;

import android.graphics.Paint;
import android.widget.TextView;

public class TextViewFlagHelper {
    /**
     * 为文本视图设置删除线
     *
     * @param textView 需要设置删除线的文本视图
     * @param withLine 是否有删除线
     */
    public static void setDeleteLine(TextView textView, boolean withLine) {
        int flag;
        if (withLine) {
            flag = textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG;
        } else {
            flag = textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG;
        }

        textView.setPaintFlags(flag);
    }
}
