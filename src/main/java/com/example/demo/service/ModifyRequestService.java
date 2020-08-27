package com.example.demo.service;

import com.example.demo.bean.ParsingRequest;
import com.example.demo.bean.RuleParse.AllApplySetsAndRules;
import com.example.demo.bean.RuleParse.DefineRules;
import com.example.demo.bean.RuleParse.RuleSets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.json.JSONObject;

import java.util.*;

@Component
public class ModifyRequestService {

    @Autowired
    YamlLoadService yamlLoader;

    private Map<String, List<DefineRules>> usedSetsAndRules;  // {规则集名: [变换规则1, 变换规则2, ...]}

    public ModifyRequestService() {
    }

    /**
     * 从配置文件中加载用户自定义变换规则集和对应规则定义
     */
    public AllApplySetsAndRules loadDefinedRules() {

        String yamlFileName = "rules.yml";
        return (yamlLoader.getRuleSetByFileName(yamlFileName));
    }

    /**
     * 提取本次要进行变化的所有自定义规则
     */
    public List<DefineRules> extractUseRules(AllApplySetsAndRules allApplySetsAndRules) {

        List<String> currentUseSets = allApplySetsAndRules.getApplyRuleSets().getRuleSetsNames();  // 提取当前要进行变化的所有规则集名称
        List<RuleSets> allRuleSets = allApplySetsAndRules.getRuleSets();   // 所有定义的规则集名称
        List<DefineRules> allDefineRules = allApplySetsAndRules.getDefineRules();  // 所有已定义的规则

        List<String> currentUseRuleSetsNames = new ArrayList<>();  // 保存所有当前要进行变化的所有规则名
        List<DefineRules> currentUseRules = new ArrayList<>();  // 保存最终当前要进行变换的规则集

        System.out.println("要使用的规则集：" + currentUseSets);

        for (int i = 0; i < currentUseSets.size(); i++) {

            for (int j = 0; j < allRuleSets.size(); j++) {

                RuleSets currentRuleSet = allRuleSets.get(j);

                // RuleSet中指定的规则集名称等于当前的规则集名称时提取出该规则集中的所有规则
                if (currentRuleSet.getSetName().equals(currentUseSets.get(i))) {

                    for (int k = 0; k < currentRuleSet.getRuleNames().size(); k++) {

                        String currentRuleSetName = currentRuleSet.getRuleNames().get(k);
                        // 取最小集
                        if (!currentUseRuleSetsNames.contains(currentRuleSetName))
                            currentUseRuleSetsNames.add(currentRuleSetName);
                    }
                }
            }
        }

        System.out.println("规则集并集：" + currentUseRuleSetsNames);

        // 提取每条规则集对应的规则细节
        for (int i = 0; i < currentUseRuleSetsNames.size(); i++) {

            for (int j = 0; j < allDefineRules.size(); j++) {

                DefineRules defineRules = allDefineRules.get(j);

                if (currentUseRuleSetsNames.get(i).equals(defineRules.getName())) {

                    currentUseRules.add(defineRules);
                }
            }
        }

        System.out.println("最终使用的规则大小：" + currentUseRules.size() + "，规则名：");
        for (int i = 0; i < currentUseRules.size(); i++)
            System.out.println("\t" + currentUseRules.get(i).getName());

        return currentUseRules;
    }


    /**
     * 提取本次要进行变化的所有自定义规则
     *
     * @param ：包含所有规则的规则类
     * @return ：map[ 变换规则名1: 对应规则集1，变换规则名2: 对应规则集2，....]
     */
    public Map<String, List<DefineRules>> extractUseSetsAndRules(AllApplySetsAndRules allApplySetsAndRules) {

        // 提取过程：规则集 -> 规则名 -> 规则

        List<String> currentUseSets = allApplySetsAndRules.getApplyRuleSets().getRuleSetsNames();  // 提取当前要进行变化的所有规则集名称
        List<RuleSets> allRuleSets = allApplySetsAndRules.getRuleSets();   // 所有定义的规则集名称
        List<DefineRules> allDefineRules = allApplySetsAndRules.getDefineRules();  // 所有定义的规则


        Map<String, List<String>> currentUseRuleNames = new HashMap<>(); // 每个规则集对应的规则名组成Map对象
        Map<String, List<DefineRules>> currentUseRulesMap = new HashMap<>(); // 最终由规则名+规则对象组成的Map对象

        System.out.println("要使用的规则集：" + currentUseSets);
        if (currentUseSets == null){
            System.out.println("待解析yaml文件中未定义任何规则.");
            return null;
        }
        // 提取要使用的规则集中的规则名
        for (int i = 0; i < currentUseSets.size(); i++) {
            // Debug
            // System.out.println("当前规则集名："+currentUseSets.get(i));

            // 保存每个规则集名对应的规则名
            List<String> ruleNames = new ArrayList<>();

            // 遍历所有已定义的规则集
            for (int j = 0; j < allRuleSets.size(); j++) {

                RuleSets currentRuleSet = allRuleSets.get(j);

                // RuleSet中指定的规则集名称等于当前的规则集名称时
                if (currentRuleSet.getSetName().equals(currentUseSets.get(i))) {

                    // 将当前规则集对应的所有规则名放入数组
                    for (int k = 0; k < currentRuleSet.getRuleNames().size(); k++)
                        ruleNames.add(currentRuleSet.getRuleNames().get(k));
                }
            }
            currentUseRuleNames.put(currentUseSets.get(i), ruleNames);
        }

        // Debug
         System.out.println("{ 规则集名:[规则集规则名] }:" + currentUseRuleNames);

        // 提取每条规则集对应的规则细节
        Iterator iterator = currentUseRuleNames.keySet().iterator();
        while (iterator.hasNext()) {

            String key = iterator.next().toString();

            List<String> value = currentUseRuleNames.get(key);

            List<DefineRules> currentDefineRules = new ArrayList<>();

            for (int i = 0; i < value.size(); i++) {

                // Debug
//                System.out.println("当前规则集规则名："+value.get(i));

                for (int j = 0; j < allDefineRules.size(); j++) {

                    DefineRules defineRules = allDefineRules.get(j);
                    // Debug
//                    System.out.println("当前比较的规则名："+defineRules.getName());

                    if (value.get(i).equals(defineRules.getName())) {

//                        System.out.println("与 " + defineRules.getName() +" 规则名相等");

                        boolean contains = false;
                        // 去重
                        for (int k = 0; k < currentDefineRules.size(); k++) {
                            if (currentDefineRules.get(k).getName().equals(value.get(i))) {
                                System.out.println("发现重复项，跳过");
                                contains = true;
                            }
                        }

                        if (!contains) {
                            // Debug
//                            System.out.println("非重复项，添加");
                            currentDefineRules.add(defineRules);
                        }
                    }
                }
            }
            currentUseRulesMap.put(key, currentDefineRules);
        }

        // Debug
//        Iterator iterator1 = currentUseRulesMap.keySet().iterator();
//        while (iterator1.hasNext()){
//            String key = iterator1.next().toString();
//            System.out.println("最终key："+ key);
//            System.out.println("List对象大小："+currentUseRulesMap.get(key).size());
//            System.out.println("对应规则名字：");
//
//            for (int i = 0; i<currentUseRulesMap.get(key).size(); i++){
//                System.out.println("\t"+currentUseRulesMap.get(key).get(i).getName());
//            }
//        }

        return currentUseRulesMap;
    }


    /**
     * 功能：
     *      按照配置文件中定义的规则，更改原来的请求数据，一条规则可能对应多条变换后的URL数据返回
     */
    public ParsingRequest ModifyRequestByGivenRules(ParsingRequest parsedRequest, List<DefineRules> defineRules) {

        ParsingRequest parsingRequestLocal = new ParsingRequest(parsedRequest); // 使用深复制重新创建对象，不使用原来的对象，避免操作出错
        List<ParsingRequest> to_send_modified_urls_by_rule = new ArrayList<>(); // 保存按照当前规则变换后的所有待发送请求

        // 遍历所有规则，对请求进行变换
        int ruleCount = defineRules.size();

        for (int i = 0; i < ruleCount; i++) {

            DefineRules currentUseRule = defineRules.get(i);    // 当前变换规则

            System.out.println("应用当前的变换规则：" + currentUseRule.getName());

            switch (currentUseRule.getType()) {
                case "lineRule":
                    modifyRequestLineByRule(parsingRequestLocal, currentUseRule);
                    break;
                case "headerRule":
                    modifyRequestHeaderByRule(parsingRequestLocal, currentUseRule);
                    break;
                case "cookieRule":
                    modifyRequestCookieByRule(parsingRequestLocal, currentUseRule);
                    break;
                case "bodyRule":
                    modifyRequestBodyByRule(parsingRequestLocal, currentUseRule, to_send_modified_urls_by_rule);
                    break;
                default:
                    System.out.println("Unrecognized modify type:" + currentUseRule.getType());
            }
        }

        // todo: 变为返回的是 to_send_modified_urls_by_rule 数组去发送变换后的 URL 数据
//        System.out.println("确认更改：");
//        System.out.println(parsingRequestLocal.requestData);
        return parsingRequestLocal;
    }

    /**
     * 按照配置文件中定义的规则，更改原来的请求数据，返回多条更改后待发送的变换请求
     */
    public ParsingRequest[] ModifyRequestByRulesReturnMultiRequests(ParsingRequest parsedRequest) {

        if (usedSetsAndRules == null) {
            System.out.println("第一次使用，开始解析自定义规则");
            // 从配置文件中加载自定义的规则
            AllApplySetsAndRules allApplySetsAndRules = loadDefinedRules();

            // 根据配置文件，提取出要进行的变化所有规则细节
            usedSetsAndRules = extractUseSetsAndRules(allApplySetsAndRules);
            // 规则为空时返回空
            if (usedSetsAndRules == null){
                return null;
            }
        } else
            System.out.println("非第一次使用，无需解析请求变换规则");

        // 遍历所有规则，根据原始请求，生成变换后的多个请求
        int usedSetsCount = usedSetsAndRules.keySet().size();
        System.out.println("要变换规则的个数为："+usedSetsCount);

        Iterator iterator = usedSetsAndRules.keySet().iterator();
        ParsingRequest[] parsingRequests = new ParsingRequest[usedSetsCount];

        int iteratorNumber = 0;

        // 针对每个变换规则，分别对原始请求进行变换，得到不同的变化数组
        while (iterator.hasNext()) {

            String key = iterator.next().toString();

            System.out.println("开始对 " + key + " 进行变换......");

            parsingRequests[iteratorNumber] = new ParsingRequest();

            // 按照给定变换规则，变换原始请求
            parsingRequests[iteratorNumber] = ModifyRequestByGivenRules(parsedRequest, usedSetsAndRules.get(key));
            iteratorNumber++;
        }

        return parsingRequests;
    }

    /**
     * 对 请求行 进行变换，包括增删替换，增加代表仅对URL进行增加
     */
    public void modifyRequestLineByRule(ParsingRequest parsedRequest, DefineRules defineRule) {

        // Debug
         System.out.println("进入 请求头 变换规则，开始更改line值......");

        String targetKey = defineRule.getTarget();  // 待替换目标的键
        String targetValue = defineRule.getValue(); // 待替换目标的值

        System.out.println("目标键："+targetKey);
        System.out.println("目标值："+targetValue);


        System.out.println("当前URL为："+parsedRequest.requestUrl);
        System.out.println("URL参数为："+parsedRequest.requestData);

        switch (defineRule.getAction()) {
            case "add":

                break;
            case "delete":

                break;
            case "replace":
                if (targetKey.equals("method")){
                    parsedRequest.requestMethod = targetValue;
                }
                break;
            default:
                System.out.println("Unrecognized modify action in modify line:" + defineRule.getAction());
        }
    }

    /**
     * 对 header 进行变换，包括增删替换
     */
    public void modifyRequestHeaderByRule(ParsingRequest parsedRequest, DefineRules defineRule) {

        switch (defineRule.getAction()) {
            case "add":
                parsedRequest.requestHeaders.put(defineRule.getTarget(), defineRule.getValue());
                break;
            case "delete":
                Set<String> allHeaders = parsedRequest.requestHeaders.keySet();
                Iterator<String> headerIterator = allHeaders.iterator();

                while (headerIterator.hasNext()) {
                    String currentHeader = headerIterator.next();

                    if (currentHeader.equals(defineRule.getTarget())) {
                        headerIterator.remove();
                    }
                }
                break;
            case "replace":
                for (String header : parsedRequest.requestHeaders.keySet()) {

                    if (header.equals(defineRule.getTarget())) {

                        // 直接put一个同名的新值就可以替换原来的头
                        parsedRequest.requestHeaders.put(header, defineRule.getValue());
                    }
                }
                break;
            default:
                System.out.println("Unrecognized modify action in modify header:" + defineRule.getAction());
        }
    }

    /**
     * 对 Cookie 进行变换，包括增删替换
     */
    public void modifyRequestCookieByRule(ParsingRequest parsedRequest, DefineRules defineRule) {

        // Debug
        // System.out.println("进入Cookie变换规则，开始更改Cookie值......");

        String targetKey = defineRule.getTarget();  // 待替换目标Cookie的键
        String targetValue = defineRule.getValue(); // 待替换目标Cookie的值

        // Debug
        // System.out.println("当前Cookie值："+parsedRequest.requestHeaders.get("Cookie"));

        String cookieString = parsedRequest.requestHeaders.get("Cookie");
        String[] cookieStringArray = null;

        if ( cookieString == null ){

            if (! defineRule.getAction().equals("add")){
                System.out.println("当前请求中没有Cookie，仅可进行增加操作，当前动作为："+defineRule.getAction());
                return ;
            }
        }
        else
            cookieStringArray = parsedRequest.requestHeaders.get("Cookie").split(";");

        String newCookie = "";

        switch (defineRule.getAction()) {
            case "add":
                // 新增Cookie键值对放在第一个
                if (cookieString == null)
                    parsedRequest.requestHeaders.put("Cookie",( targetKey+"="+targetValue+"; " ));
                else
                    parsedRequest.requestHeaders.put("Cookie",( targetKey+"="+targetValue+"; " ) + cookieString.trim() );
                break;
            case "delete":
                for (int i = 0;i<cookieStringArray.length; i++){

                    String key = cookieStringArray[i].substring(0,cookieStringArray[i].indexOf("=")).trim();
                    String value = cookieStringArray[i].substring(cookieStringArray[i].indexOf("=")+1).trim();

                    if (key.equals(targetKey))
                        continue;
                    else
                        newCookie += ( key+"="+value+"; " );
                }

                parsedRequest.requestHeaders.put("Cookie",newCookie.trim());
                break;

            case "replace":

                for (int i = 0;i<cookieStringArray.length; i++){

                    String key = cookieStringArray[i].substring(0,cookieStringArray[i].indexOf("=")).trim();
                    String value = cookieStringArray[i].substring(cookieStringArray[i].indexOf("=")+1).trim();

                    if (key.equals(targetKey))
                        newCookie += ( key+"="+targetValue+"; " );
                    else
                        newCookie += ( key+"="+value+"; " );
                }
                parsedRequest.requestHeaders.put("Cookie",newCookie.trim());

                break;
            default:
                System.out.println("Unrecognized modify action in modify cookie:" + defineRule.getAction());
        }
    }

    /**
     * 对 Body 进行变换，包括增删替换，放在 to_send_modified_urls_by_rule 表中
     */
    public void modifyRequestBodyByRule(ParsingRequest parsedRequest,
                                        DefineRules defineRule,
                                        List<ParsingRequest> to_send_modified_urls_by_rule) {

        // 提取请求方法
        String requestMethod = parsedRequest.requestMethod;
//        System.out.println("To change request method: " + requestMethod);

        // 提取数据类型
        String dataType = "";
        if (parsedRequest.requestHeaders.containsKey("Content-Type") ){
            dataType = parsedRequest.requestHeaders.get("Content-Type").trim();
        }
        else {
            dataType = "application/x-www-form-urlencoded";
        }
//        System.out.println("To change request data type: " + dataType);

        // 根据规则进行 URL 数据变换，得到多个变换后待发送的 URL 存放在 to_send_modified_urls_by_rule 中
        switch (defineRule.getAction()) {
            case "add":
                if (requestMethod.equals("POST")){
                    if (dataType.equals("application/x-www-form-urlencoded")){

                        // 仅在数据最开始处增加新的参数即可
                        parsedRequest.requestData = defineRule.getTarget()+"="+defineRule.getValue()+"&" + parsedRequest.requestData;
//                        System.out.println("变换后的 POST 请求数据部分为："+parsedRequest.requestData);
                    }
                }
                break;
            case "delete":

                break;
            case "replaceEach":
                if (requestMethod.equals("POST")){
                    // 数据格式为www-form
                    if (dataType.contains("application/x-www-form-urlencoded")){
                        parsedRequest.requestData = parsedRequest.requestData.replace(defineRule.getTarget(),defineRule.getValue());
//                        System.out.println("变换后的 POST 请求数据部分为："+ parsedRequest.requestData );

                    }
                    // 数据格式为json
                    else if (dataType.contains("application/json")){
                        // 加载定义的替换 payloads
                        List<String> payloads = new ArrayList<>();
                        payloads.add(defineRule.getValue());

                        for (int i = 0; i< payloads.size(); i++){
                            ParsingRequest parsedRequestToModify = new ParsingRequest(parsedRequest); // 进行深复制

                            if (defineRule.getTarget().equals("key")){
                                // 替换 json 数据中的键
                                replaceEachKey(parsedRequestToModify.requestData, payloads.get(i));

                            }
                            else{
                                // 替换 json 中的值



                            }
                            to_send_modified_urls_by_rule.add(parsedRequestToModify);
                        }
                    }
                }
                break;
            default:
                System.out.println("Unrecognized modify action in modify body:" + defineRule.getAction());
        }
    }

    /*
    * 功能：
    *       给定一个待替换的 json 字符和一个替换的 key，返回替换后的字符串数组
    * */
    public List<String> replaceEachKey(String originalJsonData, String key){

        System.out.println("replaceEachKey 待替换字符串为：\n"+ originalJsonData +"\n替换的 key 为："+ key);

        List<String> replacedJsonData = new ArrayList<>();

        if (originalJsonData.length() == 0){
            System.out.println("待替换 JSON 字符串为空！");
        }

        // 单层遍历JSON
//        JSONObject jsonObject = new JSONObject(originalJsonData);
//        Iterator<String> sIterator = jsonObject.keys();
//        while(sIterator.hasNext()){
//            // 获得key
//            String jsonKey = sIterator.next();
//            // 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
//            String jsonValue = jsonObject.get(jsonKey).toString();
//            System.out.println("key: "+ jsonKey +",value: " + jsonValue );
//        }

        // 提取所有出现 ": 的位置，作为键值对分离的地方
        int start = 0;
        List splitPosition = new ArrayList<>();

        while(originalJsonData.indexOf("\":",start + 2 )  != -1 ){ // todo:增加去除 \"这种情况
            start = originalJsonData.indexOf("\":",start + 2);
            splitPosition.add(start);
        }
        System.out.println(splitPosition.toString());

        // 保留所有键出现的位置
        List keyStartPosition = new ArrayList();
        Iterator<Integer> position = splitPosition.iterator();
        while (position.hasNext()){

            int currentPosition = position.next();
            keyStartPosition.add(originalJsonData.lastIndexOf("\"", currentPosition - 1));
        }
        System.out.println(keyStartPosition.toString());

        // 获取键的起止位置
        for(int i = 0;i< splitPosition.size();i++){

            int keyStart = (int)keyStartPosition.get(i); // 键开始的位置
            int keyEnd = (int) splitPosition.get(i) + 1; // 键结束的位置

            System.out.println( originalJsonData.substring( keyStart, keyEnd ) );
        }

        // 获取值的起止位置
        for (int i = 0;i< splitPosition.size(); i++){

            int valueStart = (int) splitPosition.get(i) + 1 + 1;    // 从下一个开始
            int valueEnd = 0;

            char correspondingChar = originalJsonData.charAt(valueStart);
//            System.out.println(correspondingChar);

            if ( correspondingChar == '"' ){
                System.out.println("string is \"");

                int findend = valueStart+1;
                while ( originalJsonData.charAt(findend) != '"' ||
                        (originalJsonData.charAt(findend) == '"' && originalJsonData.charAt(findend - 1 ) =='\\')){

                    findend++;
                }
                valueEnd = findend;
            }
            else if ( correspondingChar == '['){
                System.out.println("string is [");

                int findend = valueStart+1;
                int innerArrNum = 1;

                while ( true ){
                    if (originalJsonData.charAt(findend) == '[')
                        innerArrNum++;
                    else if (originalJsonData.charAt(findend) == ']')
                        innerArrNum--;

                    if (originalJsonData.charAt(findend) == ']' && originalJsonData.charAt(findend - 1 ) != '\\' && innerArrNum == 0)
                        break;
                    findend++;
                }
                valueEnd = findend;

                System.out.println( valueEnd );
                System.out.println(originalJsonData.charAt( valueEnd ));
            }
            else if ( correspondingChar == '{'){
                System.out.println("string is {");

                int findend = valueStart + 1;
                int innerArrNum = 1;

                while ( true ){
                    if (originalJsonData.charAt(findend) == '{')
                        innerArrNum++;
                    else if (originalJsonData.charAt(findend) == '}')
                        innerArrNum--;

                    if (originalJsonData.charAt(findend) == '}' && originalJsonData.charAt(findend - 1 ) != '\\' && innerArrNum == 0)
                        break;
                    findend++;
                }
                valueEnd = findend;

//                System.out.println( valueEnd );
//                System.out.println(originalJsonData.charAt( valueEnd ));

            }
            else {
                System.out.println("end is number, null ...");
            }
        }


        return replacedJsonData;
    }
}


