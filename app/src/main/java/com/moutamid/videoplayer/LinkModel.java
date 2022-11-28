package com.moutamid.videoplayer;

public class LinkModel {
    String link;
    boolean rotate;

    public LinkModel() {
    }

    public LinkModel(String link) {
        this.link = link;
    }

    public LinkModel(String link, boolean rotate) {
        this.link = link;
        this.rotate = rotate;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isRotate() {
        return rotate;
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
    }
}
