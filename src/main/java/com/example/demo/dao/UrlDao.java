package com.example.demo.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UrlDao {

    // 首先，在本地数据库建表，使用语句：
    // create table url_t  ( ID int primary key AUTO_INCREMENT, Method text, Host text, Args text, HashKey text, OriginalUrl text);
    public static final String url_table_name = "url_t";

    /**
     * 插入一条记录
     *
     * @param host 主机名
     * @param args 参数
     * @param hashKey 主机名和参数组成的唯一键
     * @param originalUrl 原始URL信息
     */
    @Insert("INSERT INTO " + url_table_name + " (Method, Host, Args, HashKey, OriginalUrl) VALUES (#{method}, #{host}, #{args}, #{hashKey}, #{originalUrl})")
    void insertUrl(@Param("method") String method, @Param("host") String host, @Param("args") String args,
                   @Param("hashKey") String hashKey, @Param("originalUrl") String originalUrl);

    /**
     * 查询所有用户信息
     */
    @Select("SELECT ID FROM " + url_table_name)
    List<Integer> selectAllIDs();

    /***
     * 根据提供id查询url信息，尝试替换host,args为函数参数，实现数据直接传参查询
     */
    @Select("SELECT Method FROM "+url_table_name+" WHERE ID = #{id}")
    String selectMethodByID(@Param("id") String id);
    @Select("SELECT Host FROM "+url_table_name+" WHERE ID = #{id}")
    String selectHostByID(@Param("id") String id);
    @Select("SELECT Args FROM "+url_table_name+" WHERE ID = #{id}")
    String selectArgsByID(@Param("id") String id);
    @Select("SELECT HashKey FROM "+url_table_name+" WHERE ID = #{id}")
    String selectHashKeyByID(@Param("id") String id);
    @Select("SELECT OriginalUrl FROM "+url_table_name+" WHERE ID = #{id}")
    String selectOriginalUrlByID(@Param("id") String id);
}
