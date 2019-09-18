package com.walixiwa.m3u8downloader.tools;

public interface M3U8DownloadListener {
    void onPreparing(String url);

    void onStart(String url);

    void onDownloading(String url, int curCount, int totalCount);

    void onProgress(String url, long curLength, int progress);

    void onPause(String url);

    void onSuccess(String url, String localM3U8);

    void onError(String url, String errorMsg);
}
