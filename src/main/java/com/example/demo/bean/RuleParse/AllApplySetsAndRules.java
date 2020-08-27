package com.example.demo.bean.RuleParse;

import java.util.List;

/**
 * 包含其他三个类，方便提取规则和操作
 * */
public class AllApplySetsAndRules {

    private ApplyRuleSets applyRuleSets;
    private List<RuleSets> ruleSets;
    private List<DefineRules> defineRules;

    public AllApplySetsAndRules( ApplyRuleSets applyRuleSets, List<RuleSets> ruleSets, List<DefineRules> defineRules ) {
        this.applyRuleSets = applyRuleSets;
        this.ruleSets = ruleSets;
        this.defineRules = defineRules;
    }

    public ApplyRuleSets getApplyRuleSets() {
        return applyRuleSets;
    }

    public void setApplyRuleSets(ApplyRuleSets applyRuleSets) {
        this.applyRuleSets = applyRuleSets;
    }

    public List<RuleSets> getRuleSets() {
        return ruleSets;
    }

    public void setRuleSets(List<RuleSets> ruleSets) {
        this.ruleSets = ruleSets;
    }

    public List<DefineRules> getDefineRules() {
        return defineRules;
    }

    public void setDefineRules(List<DefineRules> defineRules) {
        this.defineRules = defineRules;
    }
}
