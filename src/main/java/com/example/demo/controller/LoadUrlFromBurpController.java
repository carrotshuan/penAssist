package com.example.demo.controller;

import com.example.demo.bean.ParsingRequest;
import com.example.demo.service.BurpRequestParsingService;
import com.example.demo.service.HttpSendService;
import com.example.demo.service.ModifyRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Controller
public class LoadUrlFromBurpController {

    @Autowired
    BurpRequestParsingService burpRequestParsingService;  // 使用单独类解析原始数据，得到请求中的各项信息

    @Autowired
    HttpSendService httpSendService; // 用于发送变换后的Http请求

    @Autowired
    ModifyRequestService modifyRequestService;  // 用于变换要发送出去的请求

    // Burp发送过来的请求使用该字符串进行包装
    private final String headerAndTailSplitCharacter = "@HEADTAILCHAR@";
    // Burp每一行使用该字符作为起始，防止一个换行符被替换过滤的情况
    private final String middleSplitCharacter = "@MIDDLECHAR@";
    // Burp的换行符使用该字符替换
    private final String burpSplitCharacter = "=&";
    // 将Burp发送数据部分中，使用的默认换行符转换为该字符串，接收后再转换回去
    private final String burpEncodeSplitChar = "@BURPENCODESPLITCHAR@";
    // 等号会被Burp过滤掉，发送前进行转码
    private final String burpEncodeEqualChar = "@BURPEQUALCHAR@";
    // 与号会被Burp作为换行符，发送前进行转码
    private final String burpEncodeAndChar = "@BURPANDCHAR@";

    /**
     * 将自定义Burp插件接收到的URL导入到本服务器中
     * 自定义Burp插件将所有的代理到的请求，发送到配置的127.0.0.1:9090端口对应的本URL上，接收后还原、解析和插入数据库用于前台查询
     * */
    @RequestMapping("/recUrl")
    @ResponseBody
    public String ReceiveURLFromBurp(HttpServletRequest request){

        try {
            InputStream is = request.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String line = null;
            String requestContents = "";

            // 按行读取Burp发送过来的数据，转换为字符串
            while ((line = br.readLine()) != null) {
                requestContents += line;
            }

            // 对Burp发送的数据解码，还原原始请求
            String OriginRequest = decodeRequest(requestContents);

            //解析原始请求中的数据：请求行、请求头、请求参数等，并插入数据库
            ParsingRequest parsedRequest = parseOriginalRequestInsertToDB(OriginRequest);

            // 变换请求，并发送出去
            ModifyRequestAndSendOut(parsedRequest);

            //todo:自动填充session字段

        }catch (Exception e){
            System.out.println("Read Data from buffer ERROR!\n");
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 解码由Burp包装原始请求后，按照编码格式发送过来的数据，解码得到原始请求
     * @param burpRequest 来自Burp中的原始请求
     */
    private String decodeRequest(String burpRequest){
        // Debug
        // System.out.println("Burp中未解码前的数据为：\n"+burpRequest);

        if (!burpRequest.contains(headerAndTailSplitCharacter) || burpRequest.split(headerAndTailSplitCharacter).length != 3){
            System.out.println("Original URL from Burp formal ERROR! Please check, original request is:\n"+ burpRequest);
            return null;
        }
        // 接收数据部分确认
        String validContents = burpRequest.split(headerAndTailSplitCharacter)[1].trim();
        // Debug
        // System.out.println("去除起始和结束符的中间请求内容为:\n" + validContents);

        /**
         *  处理编码部分，得到原始数据发送内容
         */
        String preProcessContents = validContents.replace(burpSplitCharacter,"\n").trim();  // 解码Burp发送时转换的换行符，空白行存在毛刺
        // Debug
//        System.out.println("解码换行符后的请求内容为：\n"+preProcessContents);
        preProcessContents = preProcessContents.replace(middleSplitCharacter,"");   // 解码毛刺修正在每行添加的行结束符
        // Debug
//        System.out.println("替换middleSplitCharacter后：\n"+preProcessContents);
        preProcessContents = preProcessContents.replace(burpEncodeSplitChar, burpSplitCharacter);    //解码原始数据中的=&，防止Burp作为换行符
        // Debug
//        System.out.println("替换burpEncodeSplitChar后：\n"+preProcessContents);
        preProcessContents = preProcessContents.replace(burpEncodeEqualChar,"=");   // 解码等号字符
        preProcessContents = preProcessContents.replace(burpEncodeAndChar, "&");    // 解码与号字符
//        System.out.println("最终还原数据为:\n" + preProcessContents);

        return preProcessContents;
    }

    /**
     * 解析原始请求，并插入到数据库中
     * */
    public ParsingRequest parseOriginalRequestInsertToDB(String originalRequest){

        ParsingRequest parsedRequest = null;

        try {
            // 解析发送过来的Burp请求
            parsedRequest = burpRequestParsingService.ParseBurpRequest(originalRequest);

            // 将原始请求 和 解析后的请求 一并插入到数据库中
            burpRequestParsingService.OriginalAndParsedRequestInsertToDB(originalRequest, parsedRequest);

            System.out.println("解析Burp请求并插入到数据库完成.\n");
        }
        catch (Exception e){
            System.out.println("解析或插入数据发生错误，错误详情：");
            e.printStackTrace();
        }

        return parsedRequest;
    }

    /**
     * 按照自定义规则更改请求，并发送出去，todo:应该放在main中，位置待更改
     * */
    public void ModifyRequestAndSendOut(ParsingRequest parsedRequest){

        // 对更改请求进行变换，得到多个请求数组
        ParsingRequest[] parsingRequests = modifyRequestService.ModifyRequestByRulesReturnMultiRequests(parsedRequest);
        if (parsingRequests == null){
            System.out.println("未定义任何变化规则，请求无需进行更改");
            return ;
        }
        System.out.println("请求个数为：" + parsingRequests.length + "，开始发送变换后的请求......");

        for (int i = 0; i < parsingRequests.length; i++){

            // 将变换后的请求，按照不同的请求方法发送出去
            switch (parsingRequests[i].requestMethod){

                case "GET":{
                    httpSendService.sendParsedHttpGetRequest(parsingRequests[i]);
                }break;

                case "POST":{
                    httpSendService.sendParsedHttpPostRequest(parsingRequests[i]);
                }break;

                case "PUT":{
                    httpSendService.sendParsedHttpPutRequest(parsingRequests[i]);
                }break;

                case "DELETE":{
                    httpSendService.sendParsedHttpDeleteRequest(parsingRequests[i]);

                }break;

                default:{
                    System.out.println("不支持HTTP请求类型的发送请求！");
                }
            }
        }
    }

}
