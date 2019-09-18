package com.walixiwa.m3u8downloader.tools.entity;

import java.util.ArrayList;
import java.util.List;

public class M3U8Model {
    private List<String> videoTs1 = new ArrayList<>();
    private List<String> videoTs2 = new ArrayList<>();

    public List<String> getVideoTs1() {
        return videoTs1;
    }

    public void setVideoTs1(List<String> videoTs1) {
        this.videoTs1 = videoTs1;
    }

    public List<String> getVideoTs2() {
        return videoTs2;
    }

    public void setVideoTs2(List<String> videoTs2) {
        this.videoTs2 = videoTs2;
    }
}
