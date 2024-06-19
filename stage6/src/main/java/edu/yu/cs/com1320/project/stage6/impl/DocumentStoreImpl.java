package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.System.nanoTime;


public class DocumentStoreImpl implements edu.yu.cs.com1320.project.stage6.DocumentStore {
    private class uriWithTime implements java.lang.Comparable<uriWithTime>{
        private final URI uri;
        private long time;
        public uriWithTime(URI uri, long time){
            this.time= time;
            this.uri=uri;
        }
        @Override
        public int compareTo(uriWithTime o) {
            return (int) (this.time-o.time);
        }
        public URI getKey(){
            return uri;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            uriWithTime that = (uriWithTime) o;
            return Objects.equals(uri, that.uri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri);
        }
    }
    BTree<URI, Document> storage;
    Stack<Undoable> commandStack;
    Trie<URI> documentTrie;
    HashMap<String, HashMap<String, Set<URI>>> MDMap;
    HashMap<URI, uriWithTime> uriToHeap;
    int maxDocs;
    int maxBites;
    int storageSize;
    int docsInMemory;
    MinHeap<uriWithTime> docHeap;
    public DocumentStoreImpl(){
        this(null);
    }
    public DocumentStoreImpl(File dir){
        storage = new BTreeImpl<>();
        commandStack = new StackImpl<>();
        documentTrie = new TrieImpl<>();
        docHeap = new MinHeapImpl<>();
        MDMap = new HashMap<>();
        uriToHeap = new HashMap<>();
        storage.setPersistenceManager(new DocumentPersistenceManager(dir));
    }
    public String setMetadata(URI uri, String key, String value) throws IOException{
        Document myDoc = checkNullBlank(uri, key);
        String oldData = myDoc.getMetadataValue(key);
        this.commandStack.push(new GenericCommand<>(uri, (URI uri1) -> myDoc.setMetadataValue(key, oldData)));
        myDoc.setMetadataValue(key, value);
        updateDocTime(myDoc,nanoTime());
        if(!MDMap.containsKey(key)){
            Set<URI> uris = new HashSet<>();
            uris.add(uri);
            HashMap<String, Set<URI>> toInsert = new HashMap<>();
            toInsert.put(value, uris);
            MDMap.put(key, toInsert);
        }else if(!MDMap.get(key).containsKey(value)) {
            Set<URI> uris = new HashSet<>();
            uris.add(uri);
            MDMap.get(key).put(value, uris);
        } else MDMap.get(key).get(value).add(uri);
        return oldData;
    }
    private Document checkNullBlank(URI uri, String key){
        if(uri==null || uri.toString().isEmpty() || key==null || key.isEmpty())
            throw new IllegalArgumentException("No Nulls/blanks");
        Document doc = this.storage.get(uri);
        if(doc == null) throw new IllegalArgumentException("No Document");
        return doc;
    }
    public String getMetadata(URI uri, String key) throws IOException{
        return updateDocTime(checkNullBlank(uri, key), nanoTime()).getMetadataValue(key);
    }
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException{
        if(input==null){
            if(this.storage.get(uri)==null) return 0;
            int hi = get(uri).hashCode();
            this.delete(uri);
            return hi;
        }
        int sizeOfDoc = input.available();
        if(uri==null || uri.toString().isEmpty() || format==null || (maxBites!=0 && sizeOfDoc>maxBites)){
            throw new IllegalArgumentException("No good!");
        }
        Document mydoc = null;
        Document oldDoc = this.storage.get(uri);
        //the problem is when this is overwriting something in memory, we have to now increase the count
        mydoc = getMydoc(input, uri, format, mydoc);
        this.storage.put(uri, mydoc);
        if(oldDoc==null){
            dealWithHeap(sizeOfDoc, mydoc);//this doesn't take into accounts overwrites
            this.commandStack.push(new GenericCommand<>(uri, this::deleteForUndos));
            return 0;
        }//after here is an overwrite, we must deal with the heap
        if(!uriToHeap.containsKey(uri)){
            this.docsInMemory++;
            if(mydoc.getDocumentTxt()!=null) this.storageSize += mydoc.getDocumentTxt().getBytes().length;
            else this.storageSize += mydoc.getDocumentBinaryData().length;
        }//TODO make this method less than 30
        updateStorageAndTrie(oldDoc);
        dealWithHeap(sizeOfDoc, mydoc);
        this.commandStack.push(new GenericCommand<>(uri, (URI url) -> {
            try {putBackNoSides(uri, oldDoc);} catch (IOException e) {throw new RuntimeException(e);}
        }));
        return oldDoc.hashCode();
    }
    private void dealWithHeap(int inputSize, Document mydoc) throws IOException {
        this.uriToHeap.put(mydoc.getKey(), new uriWithTime(mydoc.getKey(), mydoc.getLastUseTime()));
        this.docHeap.insert(uriToHeap.get(mydoc.getKey()));
        this.storageSize += inputSize;
        this.docsInMemory++;
        memoryManagement();
    }
    private void memoryManagement() throws IOException {
        //here we never have a hard delete except for the case of an overwrite
        if(this.maxBites != 0) while (this.maxBites < storageSize) popToDisk();
        if(this.maxDocs != 0) while (this.maxDocs < docsInMemory) popToDisk();
    }
    private void popToDisk() throws IOException {
        URI uri = docHeap.remove().getKey();
        Document toDelete = this.storage.get(uri);
        this.storage.moveToDisk(uri);
        this.docsInMemory--;
        this.uriToHeap.remove(uri);
        if(toDelete.getDocumentTxt()!=null)
            this.storageSize -= toDelete.getDocumentTxt().getBytes().length;
        else this.storageSize -= toDelete.getDocumentBinaryData().length;
    }
    private void updateStorageAndTrie(Document toDelete) {
        if(toDelete==null) return;
        if(toDelete.getDocumentTxt()!=null){
            this.storageSize -= toDelete.getDocumentTxt().getBytes().length;
            toDelete.getWords().forEach(word -> this.documentTrie.delete(word, toDelete.getKey()));
        }else this.storageSize -= toDelete.getDocumentBinaryData().length;
        this.docsInMemory--;
        metaDataDelete(toDelete);
        uriToHeap.remove(toDelete.getKey());
    }
    private void putBackNoSides(URI uri, Document doc) throws IOException {
        deleteForUndos(uri);
        putNoSides(uri, doc);
    }
    private void deleteForUndos(URI uri){
        deleteFromHeap(uri);
        this.storage.put(uri, null);
    }
    private Document getMydoc(InputStream input, URI uri, DocumentFormat format, Document mydoc) throws IOException {
        if (format == DocumentFormat.BINARY) {
            mydoc = new DocumentImpl(uri, input.readAllBytes());
        } else if (format == DocumentFormat.TXT) {
            String myVals = new String(input.readAllBytes());
            mydoc = new DocumentImpl(uri, myVals,null);
            updateTree(uri, myVals);
        }
        return mydoc;
    }
    private void updateTree(URI uri, String text){
        String[] allText = text.split(" ");
        for(String bite : allText) this.documentTrie.put(bite, uri);
    }
    public Document get(URI url) throws IOException{
        Document doc = this.storage.get(url);
        if(doc != null) updateDocTime(doc, nanoTime());
        return doc;
    }
    public boolean delete(URI url){
        if(url==null || url.toString().isEmpty()){
            throw new IllegalArgumentException("No Nulls/blanks");
        }
        Document old = this.storage.get(url);
        if(old==null) return false;
        deleteFromHeap(url);
        updateDocTime(old,nanoTime());
        this.commandStack.push(new GenericCommand<>(url, (URI uri) -> {
            try {
                putNoSides(uri, old);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        return this.storage.put(url, null)!=null;
    }
    private void deleteFromHeap(URI uri){//this calls storage.get
        Stack<uriWithTime> tempStack = new StackImpl<>();
        while(this.docHeap.peek()!=null && !this.docHeap.peek().getKey().equals(uri)){
            tempStack.push(docHeap.remove());
        }
        if(this.docHeap.peek()!=null) updateStorageAndTrie(this.storage.get(docHeap.remove().getKey()));
        while(tempStack.size()>0) this.docHeap.insert(tempStack.pop());
    }
    private void putNoSides(URI uri, Document doc) throws IOException {
        if(doc.getDocumentBinaryData()!=null){
            if(this.maxBites!=0 && doc.getDocumentBinaryData().length>this.maxBites) throw new IllegalStateException("@Piazza 443");
            this.storageSize += doc.getDocumentBinaryData().length;
        }
        else{
            if(this.maxBites!=0 && doc.getDocumentTxt().getBytes().length>this.maxBites) throw new IllegalStateException("@Piazza 443");
            this.storageSize += doc.getDocumentTxt().getBytes().length;
            updateTree(uri, doc.getDocumentTxt());
        }
        this.docsInMemory++;
        this.storage.put(uri, doc);
        this.uriToHeap.put(uri, new uriWithTime(uri, doc.getLastUseTime()));//piazza 555 the time shouldn't be updated when undoing
        this.docHeap.insert(uriToHeap.get(uri));

        for(Map.Entry<String, String> entry : doc.getMetadata().entrySet()){
            if(!MDMap.containsKey(entry.getKey())) {
                HashMap<String, Set<URI>> map = new HashMap<>();
                Set<URI> set = new HashSet<>();
                set.add(uri);
                map.put(entry.getValue(), set);
                MDMap.put(entry.getKey(), map);
            }else if(!MDMap.get(entry.getKey()).containsKey(entry.getValue())) {
                Set<URI> uris = new HashSet<>();
                uris.add(uri);
                MDMap.get(entry.getKey()).put(entry.getValue(), uris);
            } else MDMap.get(entry.getKey()).get(entry.getValue()).add(uri);
        }
        this.updateDocTime(doc, doc.getLastUseTime());//we need this to actually change the time on the document
        this.memoryManagement();
    }
    public void undo() throws IllegalStateException {
        checkEmptyStack();
        this.commandStack.pop().undo();
    }
    private void checkEmptyStack() throws IllegalStateException{
        if(this.commandStack.size()==0) throw new IllegalStateException("Nothing to undo");
    }
    public void undo(URI url) throws IllegalStateException {
        checkEmptyStack();
        Stack<Undoable> tempStack = new StackImpl<>();
        int sizer = this.commandStack.size();
        for (int i = 0; i < sizer; i++) {
            Undoable toUndo = this.commandStack.pop();
            if (foundUri(url, toUndo, tempStack)) return;
        }
        while(tempStack.size()>0) this.commandStack.push(tempStack.pop());
    }
    private boolean foundUri(URI url, Undoable toUndo, Stack<Undoable> tempStack) {
        if(toUndo instanceof CommandSet){
            CommandSet<URI> commandSet = (CommandSet<URI>) toUndo;
            if(commandSet.containsTarget(url)){//we found it
                commandSet.undo(url);
                if(!commandSet.isEmpty())
                    this.commandStack.push(toUndo);
                while(tempStack.size()>0) this.commandStack.push(tempStack.pop());
                return true;
            }else tempStack.push(toUndo);
        }else{
            GenericCommand<URI> genericCommand = (GenericCommand<URI>) toUndo;
            if(genericCommand.getTarget().equals(url)){//we found it
                genericCommand.undo();
                while(tempStack.size()>0) this.commandStack.push(tempStack.pop());
                return true;
            }else tempStack.push(toUndo);
        }
        return false;
    }
    @Override
    public List<Document> search(String keyword) throws IOException{
        List<URI> searched1 = this.documentTrie.getSorted(keyword, (o1, o2) ->
                Integer.compare(storage.get(o2).wordCount(keyword), storage.get(o1).wordCount(keyword)));
        long time = nanoTime();
        List<Document> searched = searched1.stream().map(uri -> storage.get(uri)).collect(Collectors.toList());
        for(Document doc : searched) updateDocTime(doc, time);
        return searched;
    }
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) throws IOException{
        List<URI> searched1 = this.documentTrie.getAllWithPrefixSorted(keywordPrefix, (o1, o2) ->
                Integer.compare(storage.get(o2).wordCount(keywordPrefix), storage.get(o1).wordCount(keywordPrefix)));
        long time = nanoTime();
        List<Document> searched = searched1.stream().map(uri -> storage.get(uri)).collect(Collectors.toList());
        searched.forEach(document -> updateDocTime(document, time));
        return searched;
    }
    @Override
    public Set<URI> deleteAll(String keyword) {
        return deleteAndGetUris(this.documentTrie.deleteAll(keyword));
    }
    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        return deleteAndGetUris(this.documentTrie.deleteAllWithPrefix(keywordPrefix));
    }
    private Set<URI> deleteAndGetUris(Set<URI> toR) {
        CommandSet<URI> undos = new CommandSet<>();
        for(URI uri : toR){
            deleteFromHeap(uri);
            Document doc = this.storage.get(uri);
            if(doc==null) continue;
            metaDataDelete(doc);
            this.storage.put(uri, null);
            undos.addCommand(new GenericCommand<>(uri, (URI mine) -> {
                try {
                    putNoSides(mine, doc);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        this.commandStack.push(undos);
        return toR;
    }
    @Override
    public List<Document> searchByMetadata(Map<String, String> keysValues) throws IOException{
        long time = nanoTime();
        Set<URI> initialGuess = keysValues.entrySet().stream().filter(entry -> MDMap.containsKey(entry.getKey()) && MDMap.get(entry.getKey()).containsKey(entry.getValue())).flatMap(entry -> MDMap.get(entry.getKey()).get(entry.getValue()).stream()).collect(Collectors.toSet());
        return initialGuess.stream().filter(uri -> isMatches(keysValues, uri, time)).map(uri -> storage.get(uri)).collect(Collectors.toList());
    }
    private boolean isMatches(Map<String, String> keyValues, URI uri, long time){
        if(uri==null) return false;
        for(Map.Entry<String, String> entry : keyValues.entrySet())
            if (!MDMap.containsKey(entry.getKey()) || !MDMap.get(entry.getKey()).containsKey(entry.getValue()) ||
                    !MDMap.get(entry.getKey()).get(entry.getValue()).contains(uri)) return false;
        updateDocTime(countDocumentsInMemory(uri, storage.get(uri)), time);
        return true;
    }
    private boolean isMatches(Map<String, String> keysValues, Document doc, long time){
        if(doc == null) return false;
        boolean matches = true;
        for(Map.Entry<String, String> entry : keysValues.entrySet())
            if (!entry.getValue().equals(doc.getMetadata().get(entry.getKey()))) matches = false;
        if(matches){
            countDocumentsInMemory(doc.getKey(), doc);
            updateDocTime(doc, time);
        }
        else if(!uriToHeap.containsKey(doc.getKey())) try {
            storage.moveToDisk(doc.getKey());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return matches;
    }
    @Override
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException{
        long time = nanoTime();
        return this.documentTrie.get(keyword).stream().filter(uri -> isMatches(keysValues, uri, time))
                .map(uri -> storage.get(uri)).collect(Collectors.toList());
    }
    @Override
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException{
        return removing(keysValues, this.documentTrie.getAllWithPrefixSorted(keywordPrefix, (o1, o2) ->
                Integer.compare(storage.get(o2).wordCount(keywordPrefix), storage.get(o1).wordCount(keywordPrefix))));
    }
    private List<Document> removing(Map<String, String> keysValues, List<URI> documentList1){
        long time = nanoTime();
        return documentList1.stream().map(uri -> storage.get(uri)).toList()
                .stream().filter(doc->isMatches(keysValues,doc, time)).collect(Collectors.toList());
    }
    @Override
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) throws IOException{
        return trieDelete(new HashSet<>(searchByMetadata(keysValues)));
    }
    @Override
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException{
        return trieDelete(new HashSet<>(searchByKeywordAndMetadata(keyword, keysValues)));
    }
    @Override
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException{
        return trieDelete(new HashSet<>(searchByPrefixAndMetadata(keywordPrefix, keysValues)));
    }
    @Override
    public void setMaxDocumentCount(int limit) {
        if(limit<1) throw new IllegalArgumentException("Limit <1");
        this.maxDocs = limit;
        try {
            memoryManagement();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void setMaxDocumentBytes(int limit) {
        if(limit<1) throw new IllegalArgumentException("Limit <1");
        this.maxBites = limit;
        try {
            memoryManagement();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private Set<URI> trieDelete(Set<Document> temp) {//this also deletes MD
        temp.forEach(doc -> metaDataDelete(doc).getWords().forEach(word -> this.documentTrie.delete(word, doc.getKey())));
        return deleteAndGetUris(temp.stream().map(Document::getKey).collect(Collectors.toSet()));
    }
    private Document metaDataDelete(Document doc){
        for(Map.Entry<String, String> entry : doc.getMetadata().entrySet())
            if (MDMap.containsKey(entry.getKey()) && MDMap.get(entry.getKey()).containsKey(entry.getValue()))
                MDMap.get(entry.getKey()).get(entry.getValue()).remove(doc.getKey());
        return doc;
    }
    private Document updateDocTime(Document document, long time){
        document.setLastUseTime(time);
        if(!this.uriToHeap.containsKey(document.getKey())){
            this.uriToHeap.put(document.getKey(), new uriWithTime(document.getKey(), time));
            this.docHeap.insert(uriToHeap.get(document.getKey()));
            //this.docsInMemory++;
        }
        uriToHeap.get(document.getKey()).time=time;
        this.docHeap.reHeapify(uriToHeap.get(document.getKey()));
        //this works bc .equals here only compares uris
        //while .compareTo is only based on time
        try {
            memoryManagement();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return document;
    }
    private Document countDocumentsInMemory(URI uri, Document doc){
        if(!this.uriToHeap.containsKey(uri)){
            this.docsInMemory++;
            if(doc.getDocumentTxt()!=null) this.storageSize += doc.getDocumentTxt().getBytes().length;
            else this.storageSize += doc.getDocumentBinaryData().length;
        }
        return doc;
    }
}
