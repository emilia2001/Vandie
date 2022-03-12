package com.example.socialnetworkfinal.domain;

import javafx.scene.image.Image;

public class DTOUser {
    Long id;
    String name;
    Image picture;

    public DTOUser(Long id, String name, Image picture) {
        this.id = id;
        this.name = name;
        this.picture = picture;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Image getPicture() {
        return picture;
    }
}
