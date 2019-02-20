package com.dskwrk.java;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Track {
    private String artist;
    private String name;
    private String album;
    private int runTime;
    private List<String> ytLinks;
    private String fileName;
    private int status;

    Track(String artist, String name) {
        this(artist, name, null);
    }

    Track(String artist, String name, String album) {
        this.ytLinks = new ArrayList<>();
        this.album = album;
        this.artist = artist;
        this.name = name;
        this.status = 0;
    }

    public String getLink() {
        return this.ytLinks.get(Service.getInstance().ytIndex);
    }

}
