package com.manager.assistant.data.save.db.entity;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "medias",
        foreignKeys = @ForeignKey(
                entity = RunningAccountEntity.class,
                parentColumns = "accountId",
                childColumns = "accountId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "accountId")
        }
)
public class MediaEntity {
    private long mediaId = 0;   //主键（默认为0，只有当确认保存时才写入数据库并分配编号）
    private Uri fileUri;        //文件 Uri
    private long accountId;     //所属流水记录的编号

    public MediaEntity(Uri fileUri, long accountId) {
        this.fileUri = fileUri;
        this.accountId = accountId;
    }

    public long getMediaId() {
        return mediaId;
    }

    public void setMediaId(long mediaId) {
        this.mediaId = mediaId;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }
}
