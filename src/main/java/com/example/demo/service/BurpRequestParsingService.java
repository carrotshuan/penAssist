package com.example.demo.service;

import com.example.demo.bean.ParsingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

/**
 * 解析Burp发送过来的URL，并插入数据库，展示解析后的原始URL请求内容
 *
 * */
@Service
public class BurpRequestParsingService {

    @Autowired
    UrlDaoService urlDaoService;     // 数据库中保存所有URL数据

    public BurpRequestParsingService(){
    }

    /**
     * 解析发送过来的Burp内容request，填充到ParsingRequest，并插入数据库
     * @Param request 为在 Burpsuite 中的原始请求内容，包括请求行、请求头、请求体
     * */
    public ParsingRequest ParseBurpRequest(String request){

        if (request == null){
            System.out.println("request is null! check error.");
        }

        ParsingRequest parsingRequest = new ParsingRequest();

        String[] lines = request.split("\n");   // 注意：在最后以\n结尾的多个数据会自动被丢掉

        String endSpaceCharacters = null;   // 提取最终结尾的所有空白符
        if (request.trim().length() != request.length()){
            endSpaceCharacters = request.substring(request.trim().length());
            // Debug
            // System.out.println("空白符为：" + endSpaceCharacters);
        }

        // Debug
        System.out.println("Burp中的原始请求数据:");
        boolean postDatabegin = false;

        for (int i = 0; i< lines.length;i++){

            // Debug
            System.out.println("line["+i+"]"+lines[i]); // 按行显示接收到的原始请求信息

            if (i == 0){    // 解析请求行
                parsingRequest.initRequestLine = lines[i];
                parsingRequest.requestMethod = lines[i].split(" ")[0];  // 请求方法类型，GET、POST等
                parsingRequest.requestUrl = lines[i].split(" ")[1]; // 请求的URL部分
                parsingRequest.requestProtocal = lines[i].split(" ")[2];    // 请求协议
            }
            else if (postDatabegin == false){   // 解析请求头

                if (lines[i].replaceAll("\\s*", "").length() != 0) { // 如果包含字符，则未到数据部分

                    parsingRequest.initRequestHeader += lines[i];
                    // 解析header中的键
                    String headerKey = lines[i].split(":")[0].trim();
                    // 解析header中的值，注意此时存在多个:的情况，将键和替换掉剩余即为值
                    String headerValue = lines[i].replaceAll(headerKey+":","");
                    parsingRequest.requestHeaders.put(headerKey, headerValue);

                    /** 提取其中的请求编码类型参数 */
                    if (headerKey.contains("Content-Type")){
                        parsingRequest.requestContentType = headerValue;
                    }
                }
                else {  // 到达数据部分
                    postDatabegin = true;
                }
            }
            else {  // 处理请求体
                if (i != lines.length -1 )
                    parsingRequest.initRequestBody += lines[i]+"\r\n";    // 换行符刚开始被作为分隔符，但也是数据部分，所以需要补回来
                else {
                    parsingRequest.initRequestBody += lines[i]; // 最后一行不需要增加"\r\n"

                    if (endSpaceCharacters != null) // 原始请求中最终以空白符结尾
                        parsingRequest.initRequestBody += endSpaceCharacters;   // 补齐最终的结尾空白符
                }
            }
        }
        if (endSpaceCharacters != null)
            System.out.println("剩余结尾空格符："+endSpaceCharacters);

        parsingRequest.requestBody = parsingRequest.initRequestBody; // 提取请求体

        // Debug
        // System.out.println("当前请求的Body部分为："+parsingRequest.requestBody);

        /**  URL数据结构解析结束，针对不同的请求方法，解析数据中的参数，作为URL入库时的唯一键 **/
        switch (parsingRequest.requestMethod){

            // 解析 GET 类型的请求，参数在URL中
            case "GET": {
                String[] splitGETData = parsingRequest.requestUrl.split("\\?");
                if (splitGETData.length == 2) {

                    parsingRequest.requestData = splitGETData[1];
                    /* 解析GET数据参数名 */
                    String[] tmpArgsArray = parsingRequest.requestData.split("&");

                    ArrayList<String> tmpParsedArgs = new ArrayList<>();
                    for (int i = 0; i < tmpArgsArray.length; i++) {
                        tmpParsedArgs.add(tmpArgsArray[i].split("=")[0]);
                    }
                    parsingRequest.requestArgs = tmpParsedArgs;
                }
                else {
                    System.out.println("GET请求 URL 数据中不包含参数");
                    parsingRequest.requestData = "";
                }
            }
            break;

            // 解析 POST|PUT|DELETE 类型的请求参数，数据在请求体中
            case "DELETE":
            case "PUT":
            case "POST": {
                parsingRequest.requestData = parsingRequest.initRequestBody;

                String ContentType = parsingRequest.requestContentType.trim();

                if (ContentType.length() != 0) {

                    switch (ContentType) {
                        case "application/x-www-form-urlencoded": {  /** 编码类型为与GET一样的application/x-www-form-urlencoded方式 */
                            String[] tmpArgsArray = parsingRequest.requestData.split("&");

                            ArrayList<String> tmpParsedArgs = new ArrayList<>();
                            for (int i = 0; i < tmpArgsArray.length; i++) {
                                tmpParsedArgs.add(tmpArgsArray[i].split("=")[0]);
                            }
                            parsingRequest.requestArgs = tmpParsedArgs;    // 防止不断add出现重复或时序问题
                        }
                        break;

                        default:
                            System.out.println("未支持的请求数据编码方式：" + parsingRequest.requestContentType.trim() + "，请补充.");
                    }
                    // todo: 测试JSON等Content-type时的解析情况，处理双问号的get异常
                    // todo: 数据库插入唯一键
                }
            }
            break;

            default:
                // todo: 支持其他URL请求类型的参数
                System.out.println("解析数据异常，不支持非 GET|POST|PUT|DELETE 方法请求的参数解析");
        }

        return parsingRequest; // 返回解析后的请求
    }

    /**
     * 将原始请求 request 和解析后的请求 parsedRequest 插入到数据库中
     * @param1 request 原始请求
     * @param2 parsedRequest 解析后的请求
     * */
    public void OriginalAndParsedRequestInsertToDB(String request, ParsingRequest parsedRequest){

        /***   设置需要插入到数据库中的各个信息   ***/
        String methodToInsertToDB = parsedRequest.requestMethod;
        String hostToInsertToDB = parsedRequest.requestHeaders.get("Host") + parsedRequest.requestUrl.split("\\?")[0]; // 当前将主机+请求URL的?前的部分作为全路径的键
        String argsToInsertToDB = "";   // 将数据中的键排序后相连，作为唯一值信息
//        parsedRequest.requestArgs.sort();    // 首先进行排序
        
        if (parsedRequest.requestArgs != null){
            for (int i = 0; i < parsedRequest.requestArgs.size(); i++) {
                argsToInsertToDB += parsedRequest.requestArgs.get(i) + "\n";
            }
        }
        String hashKeyToInsertToDB = hostToInsertToDB + argsToInsertToDB;   // 将host及args组成的字符串的MD5值作为唯一的键，下次插入时首先查询
        String originalUrlToInsertToDB = request;   // 原始请求用于最后显示

        parsedRequest.urlToInsertToDB.setHost(hostToInsertToDB);
        parsedRequest.urlToInsertToDB.setArgs(argsToInsertToDB);
        parsedRequest.urlToInsertToDB.setHashkey(hashKeyToInsertToDB);
        parsedRequest.urlToInsertToDB.setOriginalUrl(originalUrlToInsertToDB);

        // 设置插入的键值对信息，插入到数据库中
        urlDaoService.insertUrl(
                methodToInsertToDB,
                hostToInsertToDB,
                argsToInsertToDB,
                hashKeyToInsertToDB,
                originalUrlToInsertToDB
        );
    }

    /**
     * todo: 将解析后的请求 parsingRequest 插入到数据库中，后续建立双表（原始请求表、解析去重后的表）后实现
     * @param1 request 原始请求
     * @param2 parsingRequest 解析后的请求
     * */
    public void ParsedRequestInsertToDB(ParsingRequest parsingRequest){

        return;
    }
}