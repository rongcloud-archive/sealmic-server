package cn.rongcloud.http;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.rongcloud.config.IMProperties;
import cn.rongcloud.utils.CodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HttpHelper {
    private static final String APPKEY = "RC-App-Key";
    private static final String NONCE = "RC-Nonce";
    private static final String TIMESTAMP = "RC-Timestamp";
    private static final String SIGNATURE = "RC-Signature";

    private SSLContext sslCtx = null;

    @Autowired
    IMProperties imProperties;
    
    @PostConstruct
    private void init() {
        log.info("init HttpHelper");
        try {
            sslCtx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] xcs, String string)
                        throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] xcs, String string)
                        throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslCtx.init(null, new TrustManager[]{tm}, null);
        } catch (Exception e) {
            log.error("SSLContext exception:{}", e);
        }

        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }

        });

        HttpsURLConnection.setDefaultSSLSocketFactory(sslCtx.getSocketFactory());
    }

    // 设置body体
    public void setBodyParameter(StringBuilder sb, HttpURLConnection conn)
        throws IOException {
        String str = sb.toString();
        log.info("Call server api with url: {}, data: {}", conn.getURL().toString(), str);
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        out.writeBytes(str);
        out.flush();
        out.close();
    }

    public HttpURLConnection createGetHttpConnection(String uri)
        throws MalformedURLException, IOException {
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(30000);
        conn.setRequestMethod("GET");
        return conn;
    }

    public void setBodyParameter(String str, HttpURLConnection conn) throws IOException {
        log.info("Call IM server api with url: {}, data: {}", conn.getURL().toString(), str);
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        out.write(str.getBytes("utf-8"));
        out.flush();
        out.close();
    }

    public HttpURLConnection createWhiteBoardPostHttpConnection(String host, String uri, String contentType)
        throws MalformedURLException, IOException, ProtocolException {

        URL url = new URL(host + uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        conn.setRequestProperty("Content-Type", contentType);

        return conn;
    }

    public HttpURLConnection createCommonPostHttpConnection(String host, String appKey,
        String appSecret, String uri, String contentType)
        throws MalformedURLException, IOException, ProtocolException {
        String nonce = String.valueOf(Math.random() * 1000000);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        StringBuilder toSign = new StringBuilder(appSecret).append(nonce).append(timestamp);
        String sign = CodeUtil.hexSHA1(toSign.toString());
        uri = host + uri;
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn == null) {
            log.info("open url connectin fail, url={}", uri);
            return null;
        }

        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        conn.setRequestProperty(APPKEY, appKey);
        conn.setRequestProperty(NONCE, nonce);
        conn.setRequestProperty(TIMESTAMP, timestamp);
        conn.setRequestProperty(SIGNATURE, sign);
        conn.setRequestProperty("Content-Type", contentType);
        return conn;
    }

    public HttpURLConnection createCommonGetHttpConnection(String host, String appKey,
        String appSecret, String uri, String contentType)
        throws MalformedURLException, IOException, ProtocolException {
        String nonce = String.valueOf(Math.random() * 1000000);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        StringBuilder toSign = new StringBuilder(appSecret).append(nonce).append(timestamp);
        String sign = CodeUtil.hexSHA1(toSign.toString());
        uri = host + uri;
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn == null) {
            log.info("open url connectin fail, url={}", uri);
            return null;
        }

        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        conn.setRequestProperty(APPKEY, appKey);
        conn.setRequestProperty(NONCE, nonce);
        conn.setRequestProperty(TIMESTAMP, timestamp);
        conn.setRequestProperty(SIGNATURE, sign);
        conn.setRequestProperty("Content-Type", contentType);
        return conn;
    }

    public HttpURLConnection createIMGetHttpConnection(String uri, String contentType)
        throws MalformedURLException, IOException, ProtocolException {
        return createCommonGetHttpConnection(imProperties.getHost(),
            imProperties.getAppKey(), imProperties.getSecret(), uri,
            contentType);
    }

    public HttpURLConnection createIMPostHttpConnection(String uri, String contentType)
        throws MalformedURLException, IOException, ProtocolException {
        return createCommonPostHttpConnection(imProperties.getHost(),
                imProperties.getAppKey(), imProperties.getSecret(), uri,
            contentType);
    }

    public byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;
    }

    public String returnResult(HttpURLConnection conn) throws Exception, IOException {
        InputStream input = null;
        if (conn.getResponseCode() == 200) {
            input = conn.getInputStream();
        } else {
            input = conn.getErrorStream();
        }
        String result = new String(readInputStream(input), "UTF-8");
        log.info("IM server api response:{}", result);
        return result;
    }

    public HttpURLConnection createPostHttpConnection(String uri, String contentType)
        throws IOException {

        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn == null) {
            log.info("open url connection fail, url={}", uri);
            return null;
        }

        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("Content-Type", contentType);
        return conn;
    }
}
