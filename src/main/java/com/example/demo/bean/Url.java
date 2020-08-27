package com.example.demo.bean;

/**
 * 用于保存解析单个请求的详细参数信息
 *
 * */
public class Url {
    private int id;
    private String method = "";
    private String host = "";
    private String args = "";
    private String hashkey = "";
    private String originalUrl = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getHashkey() {
        return hashkey;
    }

    public void setHashkey(String hashkey) {
        this.hashkey = hashkey;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originUrl) {
        this.originalUrl = originUrl;
    }

    @Override
    public String toString() {
        return super.toString()+"id: "+id+",method:"+method+",host:"+getHost()+",args:"+getArgs()+",hashkey:"+getHashkey()+",originUrl:"+getOriginalUrl();
    }
}
