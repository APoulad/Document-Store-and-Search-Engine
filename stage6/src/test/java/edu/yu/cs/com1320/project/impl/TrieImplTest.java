package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TrieImplTest {
    Trie<Integer> trie;
    @BeforeEach
    void setTrie(){
        this.trie=new TrieImpl<>();
    }
    @Test
    void putting(){
        this.trie.put("Hello", 10);
        Set<Integer> set = new HashSet<>();
        set.add(10);
        assertTrue(this.trie.get("Hello").contains(10));
        assertEquals(set, this.trie.get("Hello"));
    }
    @Test
    void prefix(){
        this.trie.put("Help", 20);
        this.trie.put("Hello", 10);
        this.trie.put("Heckle", 203);
        this.trie.put("hello", 2);
        List<Integer> set = new ArrayList<>();
        set.add(10);set.add(20);set.add(203);
        assertEquals(set, this.trie.getAllWithPrefixSorted("He", Integer::compare));
    }
    @Test
    void getSorted(){
        this.trie.put("Hello", 20);
        this.trie.put("Hello", 10);
        this.trie.put("Hello", 203);
        this.trie.put("hello", 2);
        List<Integer> set = new ArrayList<>();
        set.add(10);set.add(20);set.add(203);
        assertEquals(set, this.trie.getSorted("Hello", Integer::compare));
    }
    @Test
    void deleteAll(){
        this.trie.put("Hello", 20);
        this.trie.put("Hello", 10);
        this.trie.put("Hello", 203);
        this.trie.put("hello", 2);
        Set<Integer> set = new HashSet<>();
        set.add(10);set.add(20);set.add(203);
        assertEquals(set, this.trie.deleteAll("Hello"));
        assertTrue(this.trie.get("Hello").isEmpty());
    }
    @Test
    void deletePrefix(){
        this.trie.put("Help", 20);
        this.trie.put("Hello", 10);
        this.trie.put("Heckle", 203);
        this.trie.put("hello", 2);
        Set<Integer> set = new HashSet<>();
        set.add(10);set.add(20);//set.add(203);
        assertEquals(set, this.trie.deleteAllWithPrefix("Hel"));
        //assertTrue(this.trie.getAllWithPrefixSorted("Hel", Integer::compare).isEmpty());
        List<Integer> integerList = new ArrayList<>();
        assertEquals(integerList, this.trie.getAllWithPrefixSorted("Hel", Integer::compare));
        integerList.add(203);
        assertEquals(integerList, this.trie.getAllWithPrefixSorted("He", Integer::compare));
    }
    @Test
    void stamDelete(){
        this.trie.put("Help", 20);
        this.trie.put("Help", 10);
        this.trie.put("Help", 200);
        this.trie.put("Help", 2);
        assertEquals(10, this.trie.delete("Help", 10));
        Set<Integer> set = new HashSet<>();
        set.add(20); set.add(200); set.add(2);
        assertEquals(set, this.trie.get("Help"));
    }
    @Test
    void badValues(){
        this.trie.put("Hel'p", 20);
        this.trie.put("He\"lp", 10);
        this.trie.put("He$lp", 200);
        this.trie.put("Help", 2);
        Set<Integer> set = new HashSet<>();
        set.add(20); set.add(200); set.add(2); set.add(10);
        assertEquals(set, this.trie.get("Help"));
    }
    @Test
    void deleteAllNotExist(){
        this.trie.deleteAll("Hi");
    }

}
