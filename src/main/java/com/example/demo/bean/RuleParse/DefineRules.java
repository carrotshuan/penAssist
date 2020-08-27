package com.example.demo.bean.RuleParse;

public class DefineRules {

    private String name;
    private String type;
    private String action;
    private String target;
    private String value;

    public DefineRules(String name, String type, String action, String target, String value) {
        this.name = name;
        this.type = type;
        this.action = action;
        this.target = target;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
