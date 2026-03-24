package com.manager.assistant.data.classes;

import android.net.Uri;

/**
 * 流水记录相关图片
 */
public class Picture {
    private final Uri pictureUri;   //图片文件Uri
    private final long rno;         //所属标签编号
    private long pno = 0;           //图片编号（默认为0，只有当确认保存时才写入数据库并分配编号）

    public Picture(Uri pictureUri, long rno) {
        this.pictureUri = pictureUri;
        this.rno = rno;
    }

    public long getPno() {
        return pno;
    }

    public void setPno(long pno) {
        this.pno = pno;
    }

    public long getRno() {
        return rno;
    }

    public Uri getPictureUri() {
        return pictureUri;
    }
}
