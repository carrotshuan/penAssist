package com.example.demo.controller;

import com.example.demo.bean.Url;
import com.example.demo.service.ShowDataService;
import com.example.demo.service.UrlDaoService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UrlManagerController {

    @Autowired
    UrlDaoService urlDaoService;

    @Autowired
    ShowDataService showDataService;
    /**
     * 第二部分：提供前台页面查询当前数据库已经保存的所有还原过的Burp中的原始请求
     *
     * */
    @RequestMapping("/urlManager")
    public String urlManager(){

        return "UrlAnalyzePlatform";
    }

    /**
     * 2.1 前台页面加载后，首先查询数据库中的所有保存的还原过得URL的ID信息，返回数据库中的所有请求的id号
     * */
    @RequestMapping("/queryAllIDs")
    @ResponseBody
    public List<Integer> queryIDs(){

        return urlDaoService.findAllIDs();
    }

    /**
     * 根据提供的ID，在页面和后台打印对应查询信息
     * */
    @RequestMapping(value = "/queryDataByID", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Url queryDataByID(@Param("id") String id){

        showDataService.showDataParsedByID(id);

        Url url = urlDaoService.findDataByID(id);
        return url;
    }

    /**
     *  2.2 前台页面获取到所有ID后，按照组进行查询，根据提供要查询的一组ID号，返回对应数据库中的URL数据
     * */
    @RequestMapping(value = "/queryDataByIDs")
    @ResponseBody
    public Url[] queryDataByIDs(@RequestParam(value = "ids[]") Integer[] ids){
        System.out.println("Achieve ids length:" + ids.length);

        Url[] urls = new Url[ids.length];   // 保存用于返回信息的数组
        for (int i = 0; i < ids.length; i++){

            urls[i] = urlDaoService.findDataByID(ids[i].toString());
//            System.out.println("First time: i = "+i+",url[i] = "+urls[i].toString());
        }
        return urls;
    }

}
