package com.project.manager.ui.pages.setting.data_io.pojo;

public class PojoPicture {
    private long pno;   //图片编号
    private long rno;   //所属流水编号
    private String uri; //图片Uri

    public PojoPicture(long pno, long rno, String uri) {
        this.pno = pno;
        this.rno = rno;
        this.uri = uri;
    }

    public PojoPicture() {
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

    public void setRno(long rno) {
        this.rno = rno;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
