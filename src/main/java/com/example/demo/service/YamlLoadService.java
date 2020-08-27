package com.example.demo.service;

import com.example.demo.bean.RuleParse.AllApplySetsAndRules;
import com.example.demo.bean.RuleParse.ApplyRuleSets;
import com.example.demo.bean.RuleParse.DefineRules;
import com.example.demo.bean.RuleParse.RuleSets;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

@Service
public class YamlLoadService {

    private Yaml yaml;
    private InputStream inputStream;
    private ApplyRuleSets applyRuleSets;
    private List<RuleSets> ruleSets;
    private List<DefineRules> defineRules;

    public YamlLoadService(){
    }

    /**
     * get yaml property
     *
     * @param number 数组第N个
     * @param keyName 要查询的键名
     * @return
     */
    public Object getRuleByNumAndKeyName(int number, String keyName) {

        yaml = new Yaml();
        inputStream = this.getClass().getClassLoader().getResourceAsStream("rules.yml");

        Map<String, List<Map<String, Object>>> allDefineRules = yaml.load(inputStream);

        return allDefineRules.get("DefineRules").get(number).get(keyName);

    }

    public AllApplySetsAndRules getRuleSetByFileName(String yamlFileName){

        yaml = new Yaml();
        defineRules = new ArrayList<>();

        /** 解析出所有的规则，放入对象中 **/
        inputStream = this.getClass().getClassLoader().getResourceAsStream(yamlFileName);
        Map<String, List< Map<String, Object> >> allDefineRulesInYaml = yaml.load(inputStream);

        int defineRuleSize = allDefineRulesInYaml.get("DefineRules").size();    // 自定义规则条数

        for (int i = 0; i<defineRuleSize; i++){

            Map<String,Object> message =  allDefineRulesInYaml.get("DefineRules").get(i);

            defineRules.add(new DefineRules(
                    (String) message.get("name"),
                    (String) message.get("type"),
                    (String) message.get("action"),
                    (String) message.get("target"),
                    (String) message.get("value")
            ));
        }


        /** 解析出规则集，放入对象中 **/
        ruleSets = new ArrayList<>();

        inputStream = this.getClass().getClassLoader().getResourceAsStream(yamlFileName);
        Map<String, List< Map< String, List<String> > > > ruleSetsMessage = yaml.load(inputStream);

        int ruleSetsSize = ruleSetsMessage.get("RuleSets").size();

        for (int i = 0; i< ruleSetsSize; i++){

            String ruleSetName = ruleSetsMessage.get("RuleSets").get(i).get("setName").get(0);
            List ruleNames = ruleSetsMessage.get("RuleSets").get(i).get("ruleNames");

            ruleSets.add(new RuleSets(
                    ruleSetName,
                    ruleNames
            ));
        }

        /** 解析，放入对象中 **/
        inputStream = this.getClass().getClassLoader().getResourceAsStream(yamlFileName);
        Map<String, Map<String, List<String> > > ruleSetsNames = yaml.load(inputStream);

        applyRuleSets = new ApplyRuleSets(ruleSetsNames.get("ApplyRuleSets").get("ruleSetsName"));

        AllApplySetsAndRules allApplySetsAndRules = new AllApplySetsAndRules(applyRuleSets,ruleSets,defineRules);


        return allApplySetsAndRules;
    }

}
