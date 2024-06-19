package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class DocumentPersistenceManagerTest {
    @Test
    void simpleUsage(){
        try {
            URI uri = new URI("http://www.yu.edu/documents/doc1");
            Document document = new DocumentImpl(uri,
                    "Hello World", null);
            PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(null);
            pm.serialize(uri, document);
            assertEquals(document, pm.deserialize(uri));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void noPathSerialization(){
        try{
            URI uri = new URI("https://doc1");
            Document document = new DocumentImpl(uri,"Hello World", null);
            PersistenceManager<URI, Document> persistenceManager
                    = new DocumentPersistenceManager(null);
            persistenceManager.serialize(uri, document);
            assertEquals(document, persistenceManager.deserialize(uri));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void SerializationWithPath(){
        try{
            URI uri = new URI("https://dir1/file2");
            Document document = new DocumentImpl(uri,"Hello World", null);
            PersistenceManager<URI, Document> persistenceManager
                    = new DocumentPersistenceManager(null);
            persistenceManager.serialize(uri, document);
            assertEquals(document, persistenceManager.deserialize(uri));
            assertTrue(persistenceManager.delete(uri));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void NoPathDelete(){
        try{
            URI uri = new URI("https://doc1");
            Document document = new DocumentImpl(uri,"Hello World", null);
            PersistenceManager<URI, Document> persistenceManager
                    = new DocumentPersistenceManager(null);
            persistenceManager.serialize(uri, document);
            assertEquals(document, persistenceManager.deserialize(uri));
            assertTrue(persistenceManager.delete(uri));
            assertFalse(persistenceManager.delete(uri));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void multipleFiles(){
        try{
            URI uri1 = new URI("https://dir1/file1");
            URI uri2 = new URI("https://dir1/file2");
            URI uri3 = new URI("https://dir1/file3");
            Document document1 = new DocumentImpl(uri1,"Hello World", null);
            Document document2 = new DocumentImpl(uri2,"Document two is here", null);
            Document document3 = new DocumentImpl(uri3, "Document three is here", null);
            document2.setMetadataValue("Name", "John");
            document2.setMetadataValue("Date", "Sunday, only 3 days till this assignment is due");
            PersistenceManager<URI, Document> persistenceManager
                    = new DocumentPersistenceManager(null);
            persistenceManager.serialize(uri1, document1);
            persistenceManager.serialize(uri2, document2);
            persistenceManager.serialize(uri3, document3);
            assertEquals(document2, persistenceManager.deserialize(uri2));
            assertEquals(document3, persistenceManager.deserialize(uri3));
            assertEquals(document1, persistenceManager.deserialize(uri1));
            persistenceManager.delete(uri1);
            persistenceManager.delete(uri2);
            persistenceManager.delete(uri3);

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void basePath(){
        try{
            URI uri1 = new URI("http://www.yu.edu/documents/doc1");
            Document document = new DocumentImpl(uri1,"Hello World", null);
            PersistenceManager<URI, Document> persistenceManager
                    = new DocumentPersistenceManager(new File("dir1/file2"));
            persistenceManager.serialize(uri1, document);
            assertEquals(document, persistenceManager.deserialize(uri1));
            assertTrue(persistenceManager.delete(uri1));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void BinaryDocs(){
        try{
            URI uri1 = new URI("https://dir1/file1");
            URI uri2 = new URI("https://dir1/file2");
            URI uri3 = new URI("https://dir1/file3");
            Document document1 = new DocumentImpl(uri1,"Hello World".getBytes());
            Document document2 = new DocumentImpl(uri2,"Document two is here".getBytes());
            Document document3 = new DocumentImpl(uri3, "Document three is here".getBytes());
            document2.setMetadataValue("Name", "John");
            document2.setMetadataValue("Date", "Sunday, only 3 days till this assignment is due");
            PersistenceManager<URI, Document> persistenceManager
                    = new DocumentPersistenceManager(null);
            persistenceManager.serialize(uri1, document1);
            persistenceManager.serialize(uri2, document2);
            persistenceManager.serialize(uri3, document3);
            assertEquals(document2, persistenceManager.deserialize(uri2));
            assertEquals(document3, persistenceManager.deserialize(uri3));
            assertEquals(document1, persistenceManager.deserialize(uri1));
            persistenceManager.delete(uri1);
            persistenceManager.delete(uri2);
            persistenceManager.delete(uri3);

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void nonExistantPath(){
        PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(null);

            assertThrows(FileNotFoundException.class, ()-> pm.deserialize(new URI("https://pie")));

    }
    @Test
    void deleting(){
        try{
            URI uri = new URI("https://doc1");
            Document document = new DocumentImpl(uri,"Hello World", null);
            PersistenceManager<URI, Document> persistenceManager
                    = new DocumentPersistenceManager(null);
            persistenceManager.serialize(uri, document);
            assertTrue(persistenceManager.delete(uri));
            //assertNull(persistenceManager.deserialize(uri));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void deleteDirs(){
        try{
            URI uri = new URI("https://dir1/dir2/dir3/subdir/doc1");
            URI uri2 = new URI("https://dir1/dir2/dir3/subdir/doc2");
            URI uri3 = new URI("https://dir1/dir2/dir3/doc3");
            URI uri4 = new URI("https://dir1/dir2/dir3/doc4");
            URI uri5 = new URI("https://dir1/dir2/dir3/subdir/doc5");
            Document document1 = new DocumentImpl(uri,"Hello World", null);
            Document document2 = new DocumentImpl(uri2,"Hello World", null);
            Document document3 = new DocumentImpl(uri3,"Hello World", null);
            Document document4 = new DocumentImpl(uri4,"Hello World", null);
            Document document5 = new DocumentImpl(uri5,"Hello World", null);

            PersistenceManager<URI, Document> persistenceManager
                    = new DocumentPersistenceManager(null);
            persistenceManager.serialize(uri, document1);
            persistenceManager.serialize(uri2, document2);
            persistenceManager.serialize(uri3, document3);
            persistenceManager.serialize(uri4, document4);
            persistenceManager.serialize(uri5, document5);
            assertTrue(persistenceManager.delete(uri));
            assertEquals(document2, persistenceManager.deserialize(uri2));
            assertTrue(persistenceManager.delete(uri3));
            assertEquals(document4, persistenceManager.deserialize(uri4));
            assertTrue(persistenceManager.delete(uri5));persistenceManager.delete(uri2);
            assertEquals(document4, persistenceManager.deserialize(uri4));
            persistenceManager.delete(uri4);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void anotherManager(){
        try{
            URI uri = new URI("https://dir1/file2");
            Document document = new DocumentImpl(uri,"Hello World", null);
            PersistenceManager<URI, Document> persistenceManager
                    = new DocumentPersistenceManager(null);
            persistenceManager.serialize(uri, document);
            PersistenceManager<URI, Document> persistenceManager2
                    = new DocumentPersistenceManager(null);
            assertEquals(document, persistenceManager2.deserialize(uri));
            assertTrue(persistenceManager.delete(uri));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}