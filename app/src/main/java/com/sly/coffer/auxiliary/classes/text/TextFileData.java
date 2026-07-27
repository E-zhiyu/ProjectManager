package com.sly.coffer.auxiliary.classes.text;

import java.time.LocalDateTime;

public class TextFileData {
    private final String content;               //文本内容
    private final LocalDateTime lastModifyTime; //最后编辑时间的时间戳

    public TextFileData(String content, LocalDateTime lastModifyTime) {
        this.content = content;
        this.lastModifyTime = lastModifyTime;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getLastModifyTime() {
        return lastModifyTime;
    }
}
