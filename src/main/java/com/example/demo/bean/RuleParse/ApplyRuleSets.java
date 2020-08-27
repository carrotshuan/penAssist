package com.example.demo.bean.RuleParse;

import java.util.List;

public class ApplyRuleSets {
    private List<String> ruleSetsNames;

    public ApplyRuleSets(List<String> ruleSetsNames) {
        this.ruleSetsNames = ruleSetsNames;
    }

    public List<String> getRuleSetsNames() {
        return ruleSetsNames;
    }

    public void setRuleSetsNames(List<String> ruleSetsNames) {
        this.ruleSetsNames = ruleSetsNames;
    }
}
