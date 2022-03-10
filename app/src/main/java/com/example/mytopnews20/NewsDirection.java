package com.example.mytopnews20;

public class NewsDirection {
    private String introduction; // 新闻梗概
    private byte[] imageId; // 新闻栏图片
    private String urlId; // 新闻的地址Id， 还没实现调用权限，所以uriId暂时用不到
    private String mediaName;
    private String punish_time;

    public NewsDirection(String introduction, byte[] imageId, String uriId, String mediaName, String time) {
        this.introduction = introduction;
        this.imageId = imageId;
        this.urlId = uriId;
        this.mediaName = mediaName;
        this.punish_time = time;
    }

    public String getIntroduction() {
        return introduction;
    }

    public byte[] getImageId() {
        return imageId;
    }

    public String getUrlId() {
        return urlId;
    }

    public String getMediaName(){
        return mediaName;
    }

    public  String getPunish_time() {
        return punish_time;
    }
}
