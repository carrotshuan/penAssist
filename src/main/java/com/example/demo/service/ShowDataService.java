package com.example.demo.service;

import com.example.demo.bean.Url;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShowDataService {

    @Autowired
    UrlDaoService urlDaoService;    // 数据库中保存所有URL数据

//    public void showDataParsedByID(String id){
//
//        ParsingRequest parsingRequest = new ParsingRequest();
//        Url url =  urlDaoService.findDataByID(id);
//
//        System.out.println("Burp转发前的请求，解析的信息如下：");
//        System.out.println("请求行："+parsingRequest.initRequestLine);
//        System.out.println("请求头："+parsingRequest.initRequestHeader);
//        System.out.println("请求体："+parsingRequest.initRequestBody);
//        System.out.println("请求方法:"+parsingRequest.requestMethod);
//        System.out.println("请求URL:"+parsingRequest.requestUrl);
//        System.out.println("请求协议:"+parsingRequest.requestProtocal);
//        System.out.println("请求Content-Type:"+parsingRequest.requestContentType);
//
//        System.out.println("按键值对输出格式化请求头：");
//        for (String key : parsingRequest.requestHeaders.keySet()){
//            System.out.println("\t"+key+":"+parsingRequest.requestHeaders.get(key));
//        }
//
//        // 参数为空时，不进行输出
//        if(parsingRequest.requestArgs != null) {
//
//            System.out.println("输出提取数据中的参数信息：");
//            for (int i = 0; i < parsingRequest.requestArgs.size(); i++) {
//                System.out.println("\t" + parsingRequest.requestArgs.get(i));
//            }
//        }
//        else
//            System.out.println("提取到的请求数据参数为空.");
//
//        System.out.println("提取请求中的数据部分为："+parsingRequest.requestData);
//        System.out.println("插入数据库的Host："+parsingRequest.urlToInsertToDB.getHost());
//        System.out.println("插入数据库的Args："+parsingRequest.urlToInsertToDB.getArgs());
//        System.out.println("插入数据库的HashKey："+parsingRequest.urlToInsertToDB.getHashkey());
//        System.out.println("插入数据库的OriginalUrl："+parsingRequest.urlToInsertToDB.getOriginalUrl());
//        System.out.println("请求解析显示结束.");
//    }

    public void showDataParsedByID(String id){

        Url url =  urlDaoService.findDataByID(id);

        System.out.println("请求id对应的Host："+url.getHost());
        System.out.println("请求id对应的方法："+url.getMethod());
        System.out.println("请求id对应的参数："+url.getArgs());
        System.out.println("请求id对应的HashKey："+url.getHashkey());
        System.out.println("请求id对应的OriginalUrl："+url.getOriginalUrl());
        System.out.println("请求查询结束.");
    }
}
