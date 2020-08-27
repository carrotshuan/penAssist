package com.example.demo.bean.RuleParse;

import java.util.List;

public class RuleSets {
    private String setName;
    private List<String> ruleNames;

    public RuleSets(String setName, List<String> ruleNames) {
        this.setName = setName;
        this.ruleNames = ruleNames;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public List<String> getRuleNames() {
        return ruleNames;
    }

    public void setRuleNames(List<String> ruleNames) {
        this.ruleNames = ruleNames;
    }
}
