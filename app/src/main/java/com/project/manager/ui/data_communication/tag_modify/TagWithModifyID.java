package com.project.manager.ui.data_communication.tag_modify;

public class TagWithModifyID {
    private final String tag_name;
    private final long tag_no;
    private final AccountTagModifyID modifyID;

    public TagWithModifyID(String tag_name, long tag_no, AccountTagModifyID modifyID) {
        this.tag_name = tag_name;
        this.tag_no = tag_no;
        this.modifyID = modifyID;
    }

    public String getTag_name() {
        return tag_name;
    }

    public long getTag_no() {
        return tag_no;
    }

    public AccountTagModifyID getModifyID() {
        return modifyID;
    }
}
