package com.hammer.downloader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.walixiwa.m3u8downloader.tools.M3U8DownloadListener;
import com.walixiwa.m3u8downloader.tools.M3U8Downloader;
import com.walixiwa.m3u8downloader.tools.M3U8Parser;
import com.walixiwa.m3u8downloader.tools.entity.M3U8Model;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private String url;

    private TextView tv_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        url = "http://yanzishan.shuhu-zuida.com/20190918/21309_0bfb4989/index.m3u8";

        initView();
    }

    private void initView() {
        tv_info = findViewById(R.id.tv_info);
        final EditText editText = findViewById(R.id.et_url);
        findViewById(R.id.bn_parse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                url = editText.getText().toString();
                M3U8Downloader.getInstance()
                        .setUrl(url)
                        .setThreadCount(10)
                        .setSaveFile(getDownloadPath(getApplicationContext()) + "/")
                        .setDownloadListener(new M3U8DownloadListener() {
                            @Override
                            public void onPreparing(String url) {
                                Log.e("info", "解析资源文件");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_info.setText("解析资源文件");
                                    }
                                });

                            }

                            @Override
                            public void onStart(String url) {
                                Log.e("info", "开始下载");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_info.setText("开始下载");
                                    }
                                });

                            }


                            @Override
                            public void onDownloading(String url, long c, final int curCount, final int totalCount) {
                                Log.e("info", "正在下载：" + curCount + "|" + totalCount);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_info.setText("正在下载：" + curCount + "|" + totalCount);
                                    }
                                });

                            }

                            @Override
                            public void onPause(String url) {
                                Log.e("info", "暂停");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_info.setText("暂停");
                                    }
                                });

                            }

                            @Override
                            public void onSuccess(String url, final String localM3U8) {
                                Log.e("info", "下载成功：" + localM3U8);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_info.setText("下载成功：" + localM3U8);
                                    }
                                });

                            }

                            @Override
                            public void onError(String url, final String errorMsg) {
                                Log.e("info", "onError: " + errorMsg);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_info.setText("onError: " + errorMsg);
                                    }
                                });

                            }
                        })
                        .start();
            }
        });


        findViewById(R.id.bn_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                M3U8Downloader.getInstance().stopDownload();
            }
        });
    }

    private static String getDownloadPath(Context context) {
        String dataBasePath;
        File dir = context.getExternalFilesDir("");
        if (dir != null) {
            dataBasePath = dir.getAbsolutePath() + "/down";
        } else {
            dataBasePath = context.getFilesDir().getAbsolutePath() + "/down";
        }
        if (!new File(dataBasePath).exists()) {
            new File(dataBasePath).mkdirs();
        }
        return dataBasePath;
    }
}
