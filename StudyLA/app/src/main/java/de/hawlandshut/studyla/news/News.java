package de.hawlandshut.studyla.news;

/**
* Objektklasse News
* Wird zur Übergabe von einzelnen News an den NewsAdapter verwendet
* @Fragment: NewsFragment
 */

public class News {

    private String name;
    private String description;
    private String imageSRC;
    private String url;

    public News(){

    }

    public News(String name, String description, String imageSRC, String url){
        this.name = name;
        this.description = description;
        this.imageSRC = imageSRC;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageSRC() {
        return imageSRC;
    }

    public void setImageSRC(String imageSRC) {
        this.imageSRC = imageSRC;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
