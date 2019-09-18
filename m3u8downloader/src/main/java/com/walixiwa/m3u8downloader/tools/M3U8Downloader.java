package com.walixiwa.m3u8downloader.tools;

import android.text.TextUtils;
import android.util.Log;

import com.walixiwa.m3u8downloader.tools.entity.M3U8Model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class M3U8Downloader {
    private M3U8DownloadListener downloadListener;
    private M3U8Model m3U8Model;
    private String localFile;
    private int count = 0;
    private long curLength = 0;
    private String m3u8;
    private boolean pause = false;
    private String url;

    private int threadCount = 3;
    private ExecutorService fixedThreadPool;

    private boolean isRunning = false;

    public M3U8Downloader setUrl(String url) {
        this.url = url;
        return this;
    }

    public void start() {
        if (downloadListener != null) {
            downloadListener.onPreparing(url);
        }
        new M3U8Parser().with(url).setCallBack(new M3U8Parser.CallBack() {
            @Override
            public void callBack(boolean result, String value, M3U8Model model) {
                if (result) {
                    m3u8 = value;
                    m3U8Model = model;
                    startDownload();
                } else {
                    if (downloadListener != null) {
                        downloadListener.onError(url, "m3u8解析失败");
                    }
                }
            }
        }).start();
    }

    public M3U8Downloader setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    public M3U8Downloader setDownloadListener(M3U8DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
        return this;
    }

    public M3U8Downloader setSaveFile(String localFile) {
        this.localFile = localFile;
        return this;
    }

    public void stopDownload() {
        pause = true;
        fixedThreadPool.shutdownNow();
    }

    private void startDownload() {
        isRunning = true;
        count = 0;
        pause = false;
        curLength = 0;
        localFile = localFile + md5Format(m3u8) + "/";
        if (!new File(localFile).exists()) {
            new File(localFile).mkdirs();
        }
        if (downloadListener != null) {
            downloadListener.onStart(url);
        }
        fixedThreadPool = Executors.newFixedThreadPool(threadCount);//设置线程池最大线程数
        for (int i = 0; i < m3U8Model.getVideoTs2().size(); i++) {
            final String url = m3U8Model.getVideoTs2().get(i);
            final File localTsFile = new File(localFile + getTsName(m3U8Model.getVideoTs2().get(i)) + ".ts");//生成本地ts文件路径
            final File localTMPFile = new File(localFile + getTsName(m3U8Model.getVideoTs2().get(i)) + ".ts");//生成本地downloadTMP文件路径
            if (localTsFile.exists()) {
                //已存在Ts文件则掠过
                curLength = curLength + localTsFile.length();
                count = count + 1;
                downloadListener.onDownloading(url, count, m3U8Model.getVideoTs2().size());
            } else {
                fixedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        downloadFile(url, localTMPFile);
                        count = count + 1;
                        if (downloadListener != null) {
                            downloadListener.onProgress(url, curLength, (int) (count / m3U8Model.getVideoTs2().size() * 100L));
                            downloadListener.onDownloading(url, count, m3U8Model.getVideoTs2().size());
                        }
                    }
                });
            }
        }
        fixedThreadPool.shutdown();
        boolean isFlag = true;
        while (isFlag) {
            if (fixedThreadPool.isTerminated()) {
                //下载完成，生成本地M3U8
                isFlag = false;
                if (downloadListener != null) {
                    if (pause) {
                        downloadListener.onPause(url);
                    } else {
                        writeM3U8();
                        downloadListener.onSuccess(url, localFile + "localVideo.m3u8");
                    }
                }
                isRunning = false;
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void downloadFile(String link, File localFile) {
        FileOutputStream fos = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10 * 1000);
            conn.setReadTimeout(30 * 60 * 1000);
            if (conn.getResponseCode() == 200) {
                inputStream = conn.getInputStream();
                fos = new FileOutputStream(localFile);//会自动创建文件
                int len = 0;
                byte[] buf = new byte[8 * 1024 * 1024];
                while ((len = inputStream.read(buf)) != -1) {
                    curLength = curLength + len;
                    fos.write(buf, 0, len);//写入流中
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {//关流
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
            }
        }
        File newFile = new File(localFile.getAbsolutePath().replace(".downloadTMP", ".ts"));
        if (newFile.exists()) {
            localFile.renameTo(newFile);
        }
    }

    private String getTsName(String tsUrl) {
        return md5Format(tsUrl);
    }

    private static String md5Format(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


    public void writeM3U8() {
        String result = m3u8;
        for (int i = 0; i < m3U8Model.getVideoTs2().size(); i++) {
            String localTs = getTsName(m3U8Model.getVideoTs2().get(i)) + ".ts";
            result = result.replace(m3U8Model.getVideoTs1().get(i), localTs);
        }
        try {
            File file = new File(localFile + "localVideo.m3u8");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(file.length());
            raf.write(result.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File.");
        }
    }


}
