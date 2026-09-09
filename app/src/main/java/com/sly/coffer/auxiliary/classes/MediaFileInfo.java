package com.sly.coffer.auxiliary.classes;

import android.net.Uri;

public class MediaFileInfo {
    private final Uri uri;      //文件 Uri
    private final long size;    //大小(B)
    private final String name;  //文件名
    private final long createdTimeStamp;    //创建时间的时间戳

    public MediaFileInfo(Uri uri, long size, String name, long createdTimeStamp) {
        this.uri = uri;
        this.size = size;
        this.name = name;
        this.createdTimeStamp = createdTimeStamp;
    }

    public Uri getUri() {
        return uri;
    }

    public long getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public long getCreatedTimeStamp() {
        return createdTimeStamp;
    }
}
