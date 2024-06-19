package edu.yu.cs.com1320.project.stage6.impl;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class DocumentPersistenceManager implements PersistenceManager<URI, edu.yu.cs.com1320.project.stage6.Document> {
    private Gson gson;
    private String baseDir;
    public DocumentPersistenceManager(File baseDir){
        this.gson = new Gson();

        // create file if it doesn't exist, piazza 523
        if(baseDir==null){
            this.baseDir = System.getProperty("user.dir");
        }else{
            if(!baseDir.exists())
                baseDir.mkdir();
            this.baseDir = baseDir.getPath();
        }
    }
    @Override
    public void serialize(URI uri, edu.yu.cs.com1320.project.stage6.Document document) throws IOException {
        int locationOfSep = uri.toString().lastIndexOf("/");
        String dirToPut = baseDir + FileSystems.getDefault().getSeparator() +
                uri.toString().substring(7)+".json";//, locationOfSep);
        File file = new File(dirToPut);
        file.getParentFile().mkdirs();

        FileWriter writer = new FileWriter(dirToPut);
        //gson.toJson(val, writer);
        //writer.close();
        JsonSerializer<Document> serializer = getDocumentJsonSerializer();

        JsonElement serialized = serializer.serialize(document,null, null);
        gson.toJson(serialized,writer);
        writer.close();
    }

    private JsonSerializer<Document> getDocumentJsonSerializer() {
        JsonSerializer<Document> serializer = (src, typeOfSrc, context) -> {
            JsonObject jsonObject = new JsonObject();
            if(src.getDocumentTxt()!=null) {
                jsonObject.add("text", new JsonPrimitive(src.getDocumentTxt()));
                jsonObject.add("wordMap", gson.toJsonTree(src.getWordMap()));
                jsonObject.add("Type", new JsonPrimitive("Text"));
            }else{
                jsonObject.add("BinaryData", gson.toJsonTree(src.getDocumentBinaryData()));
                jsonObject.add("Type", new JsonPrimitive("Binary"));
            }
            jsonObject.add("uri",new JsonPrimitive(src.getKey().toString()));
            jsonObject.add("MDMap", gson.toJsonTree(src.getMetadata()));
            jsonObject.add("time", new JsonPrimitive(src.getLastUseTime()));
            return jsonObject;
        };
        return serializer;
    }

    @Override
    public edu.yu.cs.com1320.project.stage6.Document deserialize(URI uri) throws IOException {
        String dirToRead = baseDir + FileSystems.getDefault().getSeparator() +
                uri.toString().substring(7) + ".json";

        FileReader reader = new FileReader(dirToRead);
        //return (Value) gson.fromJson(reader, DocumentImpl.class);
        JsonDeserializer<Document> deserializer = getDocumentJsonDeserializer();
        JsonElement element = JsonParser.parseReader(reader);
        reader.close();
        edu.yu.cs.com1320.project.stage6.Document deserializedDocument = deserializer.deserialize(element, edu.yu.cs.com1320.project.stage6.Document.class,null);
        return deserializedDocument;
    }

    private JsonDeserializer<Document> getDocumentJsonDeserializer() {
        JsonDeserializer<Document> deserializer = (json, typeOfT, context) -> {
            JsonObject serializedDocument = json.getAsJsonObject();
            String uriString = serializedDocument.get("uri").getAsString();
            URI docUri = null;
            try {
                docUri = new URI(uriString);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            Document deserializedDoc;
            if(serializedDocument.get("Type").getAsString().equals("Text")) {
                String text = serializedDocument.get("text").getAsString();
                Map<String, Integer> wordMap = gson.fromJson(serializedDocument.get("wordMap"), Map.class);
                deserializedDoc = new DocumentImpl(docUri, text, wordMap);
            }else{
                byte[] bytes = gson.fromJson(serializedDocument.get("BinaryData"), byte[].class);
                deserializedDoc = new DocumentImpl(docUri, bytes);
            }
            long time = serializedDocument.get("time").getAsLong();
            HashMap<String, String> MDMap = gson.fromJson(serializedDocument.get("MDMap"), HashMap.class);
            deserializedDoc.setLastUseTime(time);
            deserializedDoc.setMetadata(MDMap);
            return deserializedDoc;
        };
        return deserializer;
    }

    /**
     * delete the file stored on disk that corresponds to the given key
     *
     * @param uri
     * @return true or false to indicate if deletion occured or not
     * @throws IOException
     */
    @Override
    public boolean delete(URI uri) throws IOException {
        String PathToDelete = baseDir + FileSystems.getDefault().getSeparator() +
                uri.toString().substring(7) + ".json";
        File toDel = new File(PathToDelete);
        boolean deleted = Files.deleteIfExists(toDel.toPath());
        if(deleted)
            Files.walk(toDel.getParentFile().toPath())
                    .map(Path::toFile)
                    .filter(File::isDirectory)
                    .filter(file -> file.listFiles().length==0)
                    .forEach(File::delete);
        return deleted;
    }
}
