package edu.yu.cs.com1320.project.stage6.impl;

import com.google.gson.Gson;
import edu.yu.cs.com1320.project.stage6.Document;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.util.LinkedList;


public class GsonTest {
    public static void main(String[] args) {
        try {
            Document document = new DocumentImpl(new URI("http://www.yu.edu/documents/doc1"),
                    "Hello World", null);
            Gson gson = new Gson();
            FileWriter writer1 =  new FileWriter("test.json");
            gson.toJson(System.getProperty("user.dir"),writer1);
            writer1.close();
            FileWriter writer2 =  new FileWriter("myDoc.json");
            gson.toJson(FileSystems.getDefault().getSeparator(), writer2);
            writer2.close();
        } catch (URISyntaxException  | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void studyForFinal(){
    }
}
