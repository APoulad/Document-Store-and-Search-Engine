package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class MinHeapImplTest {
    MinHeap<Person> personHeap;
    private class Person implements Comparable<Person>{
        public int age;
        public Person(int age){
            this.age=age;
        }
        @Override
        public int compareTo(Person o) {
            return age-o.age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return age == person.age;
        }

        @Override
        public int hashCode() {
            return Objects.hash(age);
        }
    }
    @BeforeEach
    void setup(){
        this.personHeap = new MinHeapImpl<>();
    }
    @Test
    void insert(){
        personHeap.insert(new Person(20));
        assertEquals(20, personHeap.peek().age);
    }
    @Test
    void doublingSize(){
        personHeap.insert(new Person(20));
        personHeap.insert(new Person(10));
        personHeap.insert(new Person(21));
        personHeap.insert(new Person(22));
        personHeap.insert(new Person(23));
        personHeap.insert(new Person(24));
        personHeap.insert(new Person(35));
        personHeap.insert(new Person(26));
        personHeap.insert(new Person(11));
        personHeap.insert(new Person(12));
        personHeap.insert(new Person(13));
        personHeap.insert(new Person(14));
        assertEquals(10, this.personHeap.remove().age);
        assertEquals(11, this.personHeap.remove().age);
        assertEquals(12, this.personHeap.remove().age);
        assertEquals(13, this.personHeap.remove().age);
    }
    @Test
    void changingVal(){
        Person p1 = new Person(1);
        Person p2 = new Person(2);
        Person p3 = new Person(3);
        Person p4 = new Person(4);
        Person p5 = new Person(5);
        Person p6 = new Person(6);
        Person p7 = new Person(7);
        Person p8 = new Person(8);
        this.personHeap.insert(p1);
        this.personHeap.insert(p2);
        this.personHeap.insert(p3);
        this.personHeap.insert(p4);
        this.personHeap.insert(p5);
        this.personHeap.insert(p6);
        this.personHeap.insert(p7);
        this.personHeap.insert(p8);
        assertEquals(p1, this.personHeap.peek());
        p1.age=10;
        personHeap.reHeapify(p1);
        assertEquals(p2, this.personHeap.peek());
        p5.age =1;
        personHeap.reHeapify(p5);
        assertEquals(p5, this.personHeap.peek());
        for (int i = 0; i < 7; i++) {
            this.personHeap.remove();
        }
        assertEquals(p1, this.personHeap.remove());
    }
    @Test
    void putSame(){
        this.personHeap.insert(new Person(1));
        this.personHeap.insert(new Person(1));
        assertEquals(1, this.personHeap.remove().age);
        assertEquals(1, this.personHeap.remove().age);
        assertThrows(NoSuchElementException.class, ()-> this.personHeap.remove());
    }
    @Test
    void withDocs(){
        MinHeap<Document> docHeap = new MinHeapImpl<>();
        try {
            Document doc1 = new DocumentImpl(new URI("1st"), "Words for Doc one");
            Document doc2 = new DocumentImpl(new URI("2nd"), "Words for Doc two");
            Document doc3 = new DocumentImpl(new URI("3rd"), "Words for Doc three");
            Document doc4 = new DocumentImpl(new URI("4th"), "Words for Doc four");
            docHeap.insert(doc1);docHeap.insert(doc3);docHeap.insert(doc2);docHeap.insert(doc4);
            assertEquals(doc1, docHeap.remove());
            assertEquals(doc2, docHeap.remove());
            assertEquals(doc3, docHeap.remove());
            assertEquals(doc4, docHeap.remove());
            docHeap.insert(doc4);docHeap.insert(doc3);docHeap.insert(doc1);docHeap.insert(doc2);
            assertEquals(doc1, docHeap.remove());
            assertEquals(doc2, docHeap.remove());
            assertEquals(doc3, docHeap.remove());
            assertEquals(doc4, docHeap.remove());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
