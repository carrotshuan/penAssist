package com.example.demo.service;

import com.example.demo.bean.Url;
import com.example.demo.dao.UrlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 增、查 解析后的 URL 参数
 * */
@Service
public class UrlDaoService {

    @Autowired
    private UrlDao urlDao;

    /**
     * 插入一条URL数据
     */
    public void insertUrl(String method, String host, String args, String hashKey, String originalUrl) {
        urlDao.insertUrl(method, host, args, hashKey, originalUrl);
    }

    /**
     * 查询所有的url的ID
     */
    /**
     * 查找所有用户ID
     */
    public List<Integer> findAllIDs() {
        return urlDao.selectAllIDs();
    }

    /***
     * 根据提供id查询url信息
     */
    public Url findDataByID(String id){
        Url url = new Url();

        url.setId(Integer.parseInt(id));
        url.setMethod(urlDao.selectMethodByID(id));
        url.setHost(urlDao.selectHostByID(id));
        url.setArgs(urlDao.selectArgsByID(id));
        url.setHashkey(urlDao.selectHashKeyByID(id));
        url.setOriginalUrl(urlDao.selectOriginalUrlByID(id));

        return url;
    }
}
