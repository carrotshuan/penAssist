package com.example.demo.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  原始请求类，用于解析和保存从Burp发送过来的请求各部分：请求行、请求头、请求体等
 * */
public class ParsingRequest implements Cloneable{

    // 原始数据
    public String initRequestLine = "";    // 原始请求行
    public String initRequestHeader = "";  // 原始请求头
    public String initRequestBody = "";    // 原始请求体

    // 提取其中部分数据
    public String requestMethod = "";  // 原始请求方法
    public String requestUrl = "";     // 原始URL部分
    public String requestProtocal = "";// 原始协议

    public Map<String,String> requestHeaders;   // 解析成功的请求头数组
    public String requestBody;   // 解析成功的请求体，与原始请求体保持一致

    // 按照类型提取不同类型数据中的键值对，将键作为插入数据库中的唯一键，方便后续查询
    public String requestContentType = ""; // 数据编码类型，单独提取用于不同编码类型的数据处理
    public String requestData = "";     // 解析成功的POST或GET的数据部分，GET请求的数据在URL中，POST等其他类型的请求位于请求体中 todo:改为两者一起作为数据
    public List<String> requestArgs = null;    // 解析成功的数据部分包含的参数名，用于作为数据库中存储时的唯一键



    public Url urlToInsertToDB; // 主要用于后续数据显示

    public ParsingRequest(){
        this.requestHeaders = new HashMap<>();
        this.urlToInsertToDB = new Url();
    }

    public ParsingRequest(ParsingRequest parsingRequest){

        this.requestHeaders = new HashMap<>();
//        this.requestArgs = new ArrayList<>();
        this.urlToInsertToDB = new Url();

        this.initRequestLine = parsingRequest.initRequestLine;
        this.initRequestHeader = parsingRequest.initRequestHeader;
        this.initRequestBody = parsingRequest.initRequestBody;

        this.requestMethod = parsingRequest.requestMethod;
        this.requestUrl = parsingRequest.requestUrl;
        this.requestProtocal = parsingRequest.requestProtocal;

        this.requestHeaders.putAll(parsingRequest.requestHeaders); // 深复制
        this.requestBody = parsingRequest.requestBody;

        this.requestContentType = parsingRequest.requestContentType;
        this.requestData = parsingRequest.requestData;

//        this.requestArgs.addAll(parsingRequest.requestArgs);    // 深复制

        this.urlToInsertToDB = parsingRequest.urlToInsertToDB;  // 无需实现深复制，暂时保留
    }

    // 实现对象的深复制，用于一个对象创建出多个对象
    @Override
    protected Object clone() throws CloneNotSupportedException {

        ParsingRequest parsingRequest = (ParsingRequest)super.clone();
        parsingRequest.requestHeaders = requestHeaders;

        return parsingRequest;
    }


}
