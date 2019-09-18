package com.walixiwa.m3u8downloader.tools;

import android.util.Log;

import com.walixiwa.m3u8downloader.tools.entity.M3U8Model;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M3U8Parser {
    private String link;
    private CallBack callBack;
    private M3U8Model m3U8Model = new M3U8Model();

    public M3U8Parser with(String link) {
        this.link = link;
        return this;
    }

    public M3U8Parser setCallBack(CallBack callBack) {
        this.callBack = callBack;
        return this;
    }

    public void start() {
        getUrlResource(link);
    }

    public interface CallBack {
        void callBack(boolean result, String m3U8, M3U8Model m3U8Model);
    }

    private void getUrlResource(final String link) {
        if (callBack != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String m3u8 = getHtml(link);
                        parseUrlResource(link, m3u8);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callBack.callBack(false, "", m3U8Model);
                    }
                }
            }).start();
        }
    }

    private void parseUrlResource(String url, String m3u8) {
        if (m3u8.contains(".m3u8")) {
            getUrlResource(getM3U8Url(url, m3u8));
        } else {
            analysisIndex(url, m3u8);
        }
    }

    private String getM3U8Url(String url, String content) {
        Pattern pattern = Pattern.compile(".*m3u8");
        Matcher ma = pattern.matcher(content);
        String m3u8Url = "";
        while (ma.find()) {
            m3u8Url = ma.group().startsWith("http") ? ma.group() : getUrlHeader(url) + ma.group();
            //Log.i("M3U8Parser", "m3u8Url: " + m3u8Url);
        }
        return m3u8Url;
    }


    private void analysisIndex(String url, String content) {
        Pattern pattern = Pattern.compile(".*ts");
        Matcher ma = pattern.matcher(content);
        while (ma.find()) {
            m3U8Model.getVideoTs1().add(ma.group());
            m3U8Model.getVideoTs2().add(ma.group().startsWith("http") ? ma.group() : getUrlHeader(url) + ma.group());
            //Log.i("M3U8Parser", "index: " + s);
        }
        callBack.callBack(m3U8Model.getVideoTs1().size() > 0, content, m3U8Model);
    }

    private String getUrlHeader(String url) {
        int index = url.lastIndexOf("/");
        return url.substring(0, index + 1);
    }

    private static String getHtml(String path) throws Exception {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(20 * 1000);
        InputStream inStream = conn.getInputStream();//通过输入流获取html数据
        byte[] data = readInputStream(inStream);//得到html的二进制数据
        return new String(data, StandardCharsets.UTF_8);
    }

    private static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }
}
