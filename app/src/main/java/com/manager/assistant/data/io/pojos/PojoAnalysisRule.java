package com.manager.assistant.data.io.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class PojoAnalysisRule {
    private String ruleName;    //规则名称
    private long ruleNo;        //规则编号
    private long tag_no;        //标签编号
    private String type;        //流水种类
    private String packageName; //包名
    private String title;       //通知标题
    private String content;     //通知内容

    public PojoAnalysisRule() {
    }

    public PojoAnalysisRule(String ruleName, long ruleNo,long tag_no, String type, String packageName, String title, String content) {
        this.ruleName = ruleName;
        this.ruleNo = ruleNo;
        this.tag_no = tag_no;
        this.type = type;
        this.packageName = packageName;
        this.title = title;
        this.content = content;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public long getRuleNo() {
        return ruleNo;
    }

    public void setRuleNo(long ruleNo) {
        this.ruleNo = ruleNo;
    }

    public long getTag_no() {
        return tag_no;
    }

    public void setTag_no(long tag_no) {
        this.tag_no = tag_no;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
