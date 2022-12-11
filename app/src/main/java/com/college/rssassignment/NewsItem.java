package com.college.rssassignment;

public class NewsItem {

    public NewsItem(String title, String description, String image) {
        this.title = title;
        this.description = description;
        this.Image = image;
    }

    private String description;
    private String Image;
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }


}
