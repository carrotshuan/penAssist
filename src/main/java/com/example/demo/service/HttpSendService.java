package com.example.demo.service;

import com.example.demo.bean.ParsingRequest;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;

@Component
public class HttpSendService {

    // utf-8字符编码
    public final String CHARSET_UTF_8 = "utf-8";
    // HTTP内容类型
    public final String CONTENT_TYPE_TEXT_HTML = "text/xml";
    // HTTP内容类型，相当于form表单的形式，提交数据
    public final String CONTENT_TYPE_FORM_URL = "application/x-www-form-urlencoded";
    // HTTP内容类型，相当于form表单的形式，提交数据
    public final String CONTENT_TYPE_JSON_URL = "application/json;charset=utf-8";

    // 连接管理器
    private PoolingHttpClientConnectionManager pool;
    // 请求配置
    private RequestConfig requestConfig;

    // 代理IP和端口
//    @Value("${proxy.send_IP}")
    private String PROXY_IP; // 代理主机IP，从application.properties中获取
    private Integer PROXY_PROT;  // 代理主机端口，从application.properties中获取

    /******************                初始化                      ******************/
    // 初始化发送请求的连接器配置，由于构造函数初始化在注解装配之前，如需先装配则要将注解作为参数放入构造函数中
    public HttpSendService(@Value("${proxy.send_IP}") String PROXY_IP, @Value("${proxy.send_PORT}") Integer PROXY_PROT){

        this.PROXY_IP = PROXY_IP;
        this.PROXY_PROT = PROXY_PROT;
        HttpHost proxy = null;

        try {
            proxy = new HttpHost(PROXY_IP, PROXY_PROT);
        }catch (Exception e){
            System.out.println("端口绑定出错！");
            e.printStackTrace();
            throw e;
        }

        try {
            SSLContext sslContext = new org.apache.http.ssl.SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                        @Override
                        public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                            return true;
                        }   // 对所有HTTPS的对端不校验证书
                    }
            ).build();

            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);

            // 配置同时支持 HTTP 和 HTPPS
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register(
                    "http", PlainConnectionSocketFactory.getSocketFactory()).register(
                    "https", sslsf).build();

            // 初始化连接管理器
            pool = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            // 将最大连接数增加到200，实际项目最好从配置文件中读取这个值
            pool.setMaxTotal(200);
            // 设置最大路由
            pool.setDefaultMaxPerRoute(5);
            // 根据默认超时限制初始化requestConfig
            int socketTimeout = 10000;
            int connectTimeout = 10000;
            int connectionRequestTimeout = 10000;

            // 设置请求超时时间
            requestConfig = RequestConfig.custom().setConnectionRequestTimeout(
                    connectionRequestTimeout).setSocketTimeout(socketTimeout).setConnectTimeout(
                    connectTimeout).setProxy(proxy).build();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        // 设置请求超时时间
        requestConfig = RequestConfig.custom().setSocketTimeout(50000).setConnectTimeout(50000)
                .setConnectionRequestTimeout(50000)
                .setProxy(proxy).build();
    }

    public CloseableHttpClient getHttpClient() {

        CloseableHttpClient httpClient = HttpClients.custom()
                // 设置连接池管理
                .setConnectionManager(pool)
                // 设置请求配置
                .setDefaultRequestConfig(requestConfig)
                // 设置重试次数
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .build();

        return httpClient;
    }

    /******************                发送组装好的各种类型的请求                      ******************/

    /**
     * 发送组装好参数的 Get 请求
     *
     * @param httpGet
     * @return
     */
    private String sendHttpGet(HttpGet httpGet) {

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String responseContent = null;  // 响应内容

        try {

            // 创建默认的httpClient实例.
            httpClient = getHttpClient();
            // 配置请求信息
            httpGet.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpGet);
            // 得到响应实例
            HttpEntity entity = response.getEntity();

            // 可以获得响应头
//             org.apache.http.Header[] headers = response.getHeaders(HttpHeaders.CONTENT_TYPE);
//             for (Header header : headers) {
//                System.out.println(header.getName()+":"+header.getValue());
//             }

//            Header[] headers = response.getAllHeaders();
//            for (Header header : headers){
//                System.out.printf(header.getName()+": "+header.getValue()+"\n");
//            }
//            System.out.println(response.toString());
            // 得到响应类型
            // System.out.println(ContentType.getOrDefault(response.getEntity()).getMimeType());
//            System.out.printf("Response code is:" + response.getStatusLine().getStatusCode());
//            // 判断响应状态
//            if (response.getStatusLine().getStatusCode() >= 300) {
//                throw new Exception(
//                        "HTTP Request is not success, Response code is " + response.getStatusLine().getStatusCode());
//            }

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, CHARSET_UTF_8);
                EntityUtils.consume(entity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }


    /**
     * 发送组装好参数的 Post 请求
     *
     * @param httpPost
     * @return
     */
    private String sendHttpPost(HttpPost httpPost) {

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String responseContent = null; // 响应内容

        try {
            // 创建默认的httpClient实例
            httpClient = getHttpClient();
            // 配置请求信息
            httpPost.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpPost);
            // 得到响应实例
            HttpEntity entity = response.getEntity();

            // 可以获得响应头
            // Header[] headers = response.getHeaders(HttpHeaders.CONTENT_TYPE);
            // for (Header header : headers) {
            // System.out.println(header.getName());
            // }

            // 得到响应类型
            // System.out.println(ContentType.getOrDefault(response.getEntity()).getMimeType());

//            // 判断响应状态
//            if (response.getStatusLine().getStatusCode() >= 300) {
//                throw new Exception(
//                        "HTTP Request is not success, Response code is " + response.getStatusLine().getStatusCode());
//            }

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, CHARSET_UTF_8);
                EntityUtils.consume(entity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }


    /**
     * 发送组装好参数的 PUT 请求
     *
     * @param httpPut
     * @return
     */
    private String sendHttpPut(HttpPut httpPut){

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String responseContent = null; // 响应内容

        try{
            httpClient = getHttpClient();
            httpPut.setConfig(requestConfig);
            response = httpClient.execute(httpPut);
            HttpEntity entity = response.getEntity();

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, CHARSET_UTF_8);
                EntityUtils.consume(entity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return responseContent;
    }


    /**
     * 发送组装好参数的 DELETE 请求
     *
     * @param httpDelete
     * @return
     */
    private String sendHttpDelete(HttpDeleteWithBody httpDelete){

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String responseContent = null; // 响应内容

        try{
            httpClient = getHttpClient();
            httpDelete.setConfig(requestConfig);
            response = httpClient.execute(httpDelete);
            HttpEntity entity = response.getEntity();

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, CHARSET_UTF_8);
                EntityUtils.consume(entity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }


    /******************                根据解析好的请求格式发送请求                      ******************/
    /**
     * 提供解析过得请求parsedRequest，发送 GET 请求
     *
     * @param parsedRequest 解析过的请求
     */
    public String sendParsedHttpGetRequest(ParsingRequest parsedRequest) {

        //还原原始请求URL
        String httpUrl = "https://"+parsedRequest.requestHeaders.get("Host").trim()+parsedRequest.requestUrl;

        // 创建GET请求
        HttpGet httpGet = new HttpGet(httpUrl);

        // 设置header与原始请求中的一致
        httpGet.setHeaders(null);   //清空默认headers

        for (String headerKey : parsedRequest.requestHeaders.keySet()){

            if ( headerKey.equals("Content-Length") ==  false ) {// 请求不能设置Content-Length值，代码库自动添加

                // Debug
                // System.out.println("当前设置    "+headerKey+":"+parsedRequest.requestHeaders.get(headerKey));

                httpGet.setHeader(headerKey, parsedRequest.requestHeaders.get(headerKey).trim()); // 将header值去掉两端空格后填入
            }
        }

        return sendHttpGet(httpGet);
    }

    /**
     * 提供解析过的请求parsedRequest，发送 POST 请求 todo://更改为按照解析后的请求进行发送数据和URL
     *
     * @param parsedRequest 解析过的请求
     */
    public String sendParsedHttpPostRequest(ParsingRequest parsedRequest) {

        //还原原始请求URL
        String httpUrl = "https://"+parsedRequest.requestHeaders.get("Host").trim()+parsedRequest.requestUrl;

        // 创建POST请求
        HttpPost httpPost = new HttpPost(httpUrl);

        // 设置header与原始请求中的一致
        httpPost.setHeaders(null);   //清空默认headers
        for (String headerKey : parsedRequest.requestHeaders.keySet()){

            if ( headerKey.equals("Content-Length") ==  false ) // 请求不能设置Content-Length值，代码库自动添加
                httpPost.setHeader(headerKey,parsedRequest.requestHeaders.get(headerKey).trim());
        }

        // 设置发送的POST数据与更改后的请求数据部分的一致
        String postData = parsedRequest.requestData;

        // Debug
        // System.out.println("最终发送的POST数据为：" + postData);

        try {
            // 设置参数
            if (postData != null && postData.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(postData, "UTF-8");
//                stringEntity.setContentType(CONTENT_TYPE_FORM_URL); // 可以设置类型，也可以不设置
                httpPost.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sendHttpPost(httpPost);
    }




    /**
     * 提供解析过的请求parsedRequest，发送 PUT 请求
     *
     * @param parsedRequest 解析过的请求
     */
    public String sendParsedHttpPutRequest(ParsingRequest parsedRequest) {

        //还原原始请求URL
        String httpUrl = "https://"+parsedRequest.requestHeaders.get("Host").trim()+parsedRequest.requestUrl;

        // 创建PUT请求
        HttpPut httpPut = new HttpPut(httpUrl);

        // 设置header与原始请求中的一致
        httpPut.setHeaders(null);   //清空默认headers
        for (String headerKey : parsedRequest.requestHeaders.keySet()){

            if ( headerKey.equals("Content-Length") ==  false ) // 请求不能设置Content-Length值，代码库自动添加
                httpPut.setHeader(headerKey,parsedRequest.requestHeaders.get(headerKey));
        }

        // 设置发送的PUT数据与原始请求中的一致
        String putData = parsedRequest.requestBody;

        // Debug
        // System.out.println("最终发送的Put数据为：" + putData);

        try {
            // 设置参数
            if (putData != null && putData.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(putData, "UTF-8");
//                stringEntity.setContentType(CONTENT_TYPE_FORM_URL); // 可以设置类型，也可以不设置
                httpPut.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sendHttpPut(httpPut);
    }

    /**
     * Delete请求的数据部分与 POST/PUT 方法不同，需要先设置如下类
     *
     * */
    private static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
        public static final String METHOD_NAME = "DELETE";

        public String getMethod() {
            return METHOD_NAME;
        }

        public HttpDeleteWithBody(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        public HttpDeleteWithBody(final URI uri) {
            super();
            setURI(uri);
        }

        public HttpDeleteWithBody() {
            super();
        }
    }

    /**
     * 提供解析过的请求parsedRequest，发送 DELETE 请求
     *
     * @param parsedRequest 解析过的请求
     */
    public String sendParsedHttpDeleteRequest(ParsingRequest parsedRequest) {

        //还原原始请求URL
        String httpUrl = "https://"+parsedRequest.requestHeaders.get("Host").trim()+parsedRequest.requestUrl;

        // 创建POST请求
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(httpUrl);

        // 设置header与原始请求中的一致
        httpDelete.setHeaders(null);   //清空默认headers

        for (String headerKey : parsedRequest.requestHeaders.keySet()){

            if ( headerKey.equals("Content-Length") ==  false ) // 请求不能设置Content-Length值，代码库自动添加
                httpDelete.setHeader(headerKey,parsedRequest.requestHeaders.get(headerKey));
        }

        // 设置发送的DELETE数据与原始请求中的一致
        String deleteData = parsedRequest.requestBody;

        // Debug
        // System.out.println("最终发送的Delete数据为：" + deleteData);

        try {
            // 设置参数
            if (deleteData != null && deleteData.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(deleteData, "UTF-8");
//                stringEntity.setContentType(CONTENT_TYPE_FORM_URL); // 可以设置类型，也可以不设置
                httpDelete.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sendHttpDelete(httpDelete);
    }



    /******************                以下皆为参考代码                      ******************/
    /**
     * 仅提供URL，发送 GET 请求
     *
     * @param httpUrl
     */
    public String sendHttpGet(String httpUrl) {
        // 创建get请求
        HttpGet httpGet = new HttpGet(httpUrl);

//        httpGet.setHeader("Host","www.baidu.com");
        httpGet.setHeader("Accept-Language","zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        httpGet.setHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:70.0) Gecko/20100101 Firefox/70.0");
        httpGet.setHeader("Accept-Encoding","xxxxx");
        httpGet.setHeader("Cookie","jessionid=1;name=lilei");

        return sendHttpGet(httpGet);
    }

    /**
     * 仅提供URL，发送无参数的 POST 请求
     *
     * @param httpUrl 地址
     */
    public String sendHttpPost(String httpUrl) {
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);

//        httpPost.setHeader("Host","www.baidu.com");
        httpPost.setHeader("Accept-Language","zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        httpPost.setHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:70.0) Gecko/20100101 Firefox/70.0");
        httpPost.setHeader("Accept-Encoding","xxxxx");
        httpPost.setHeader("Cookie","jessionid=1;name=lilei");

        return sendHttpPost(httpPost);
    }


    /**
     * 发送参数格式为FORM的 POST 请求
     *
     * @param httpUrl 地址
     * @param params 参数(格式:key1=value1&key2=value2)
     *
     */
    public String sendFormHttpPost(String httpUrl, String params) {
        HttpPost httpPost = new HttpPost(httpUrl);  // 创建httpPost

        httpPost.setHeader("Host","www.baidu.com");
        httpPost.setHeader("Accept-Language","zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        httpPost.setHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:70.0) Gecko/20100101 Firefox/70.0");
        httpPost.setHeader("Accept-Encoding","xxxxx");
        httpPost.setHeader("Cookie","jessionid=1;name=lilei");

        try {
            // 设置参数
            if (params != null && params.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(params, "UTF-8");
//                stringEntity.setContentType(CONTENT_TYPE_FORM_URL); // 可以设置类型，也可以不设置
                httpPost.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpPost(httpPost);
    }

    /**
     * 发送JSON格式的 POST 请求
     *
     * @param httpUrl 地址
     * @param paramsJson 参数(格式 json)
     *
     */
    public String sendHttpPostJson(String httpUrl, String paramsJson) {
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        try {
            // 设置参数
            if (paramsJson != null && paramsJson.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsJson, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_JSON_URL);
                httpPost.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpPost(httpPost);
    }

    /**
     * 发送XML格式的 POST 请求
     *
     * @param httpUrl   地址
     * @param paramsXml  参数(格式 Xml)
     *
     */
    public String sendHttpPostXml(String httpUrl, String paramsXml) {
        HttpPost httpPost = new HttpPost(httpUrl);  // 创建httpPost
        try {
            // 设置参数
            if (paramsXml != null && paramsXml.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsXml, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_TEXT_HTML);
                httpPost.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpPost(httpPost);
    }


    /**
     * 发送map转换为Form格式的 POST 请求并发送出去
     *
     * @param maps 参数
     */
    public String sendHttpPost(String httpUrl, Map<String, String> maps) {
        String param = convertStringParamter(maps);

        return sendFormHttpPost(httpUrl, param);
    }

    /**
     * 将map集合的键值对转化成：key1=value1&key2=value2 的形式
     *
     * @param parameterMap
     *            需要转化的键值对集合
     * @return 字符串
     */
    public String convertStringParamter(Map parameterMap) {
        StringBuffer parameterBuffer = new StringBuffer();
        if (parameterMap != null) {
            Iterator iterator = parameterMap.keySet().iterator();
            String key = null;
            String value = null;
            while (iterator.hasNext()) {
                key = (String) iterator.next();
                if (parameterMap.get(key) != null) {
                    value = (String) parameterMap.get(key);
                } else {
                    value = "";
                }
                parameterBuffer.append(key).append("=").append(value);
                if (iterator.hasNext()) {
                    parameterBuffer.append("&");
                }
            }
        }
        return parameterBuffer.toString();
    }

    /**
     * 发送参数格式为FORM的 PUT 请求
     *
     * @param httpUrl 地址
     * @param params 参数(格式:key1=value1&key2=value2)
     *
     */
    public String sendHttpPut(String httpUrl, String params) {
        HttpPut httpPut = new HttpPut(httpUrl);  // 创建httpPost
        try {
            // 设置参数
            if (params != null && params.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(params, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_FORM_URL);
                httpPut.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpPut(httpPut);
    }



    /**
     * 发送参数格式为FORM的 DELETE 请求
     *
     * @param httpUrl 地址
     * @param params 参数(格式:key1=value1&key2=value2)
     *
     */
    public String sendHttpDelete(String httpUrl, String params) {
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(httpUrl);

        try {
            // 设置参数
            if (params != null && params.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(params, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_FORM_URL);
                httpDelete.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpDelete(httpDelete);
    }
}
