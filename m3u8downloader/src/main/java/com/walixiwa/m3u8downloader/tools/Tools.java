package com.walixiwa.m3u8downloader.tools;

import java.io.File;

public class Tools {
    public static void deleteTaskFile(final String file, final TaskDeleteListener taskDeleteListener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                taskDeleteListener.onStart();
                delAllFile(file);
                taskDeleteListener.onFinish();
            }
        }).start();
    }

    private static boolean delAllFile(String path) {
        boolean flag = false;
        try {
            File file = new File(path);
            if (!file.exists()) {
                return flag;
            }
            if (!file.isDirectory()) {
                return flag;
            }
            String[] tempList = file.list();
            File temp = null;
            for (int i = 0; i < tempList.length; i++) {
                if (path.endsWith(File.separator)) {
                    temp = new File(path + tempList[i]);
                } else {
                    temp = new File(path + File.separator + tempList[i]);
                }
                if (temp.isFile()) {
                    temp.delete();
                }
                if (temp.isDirectory()) {
                    delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                    delFolder(path + "/" + tempList[i]);//再删除空文件夹
                    flag = true;
                }
            }
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    private static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
