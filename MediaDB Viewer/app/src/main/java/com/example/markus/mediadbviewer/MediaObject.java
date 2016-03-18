package com.example.markus.mediadbviewer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class MediaObject implements Serializable {

    public ArrayList<String> Genre = new ArrayList<String>(); // Genres des Films
    public LinkedHashMap<String, String> schauspieler = new LinkedHashMap<>(); // Schauspieler des Films
    public HashMap<String, String> simpleValues = new HashMap<String, String>();

}
