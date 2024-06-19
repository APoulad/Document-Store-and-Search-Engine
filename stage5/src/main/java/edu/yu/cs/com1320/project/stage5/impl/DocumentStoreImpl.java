package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.System.nanoTime;


public class DocumentStoreImpl implements edu.yu.cs.com1320.project.stage5.DocumentStore {
    HashTable<URI, Document> storage;
    Stack<Undoable> commandStack;
    Trie<Document> documentTrie;
    int maxDocs;
    int maxBites;
    int storageSize;
    MinHeap<Document> docHeap;
    public DocumentStoreImpl(){
        storage = new HashTableImpl<>();
        commandStack = new StackImpl<>();
        documentTrie = new TrieImpl<>();
        docHeap = new MinHeapImpl<>();
    }
    public String setMetadata(URI uri, String key, String value){
        Document myDoc = checkNullBlank(uri, key);
        String oldData = myDoc.getMetadataValue(key);
        this.commandStack.push(new GenericCommand<>(uri, (URI uri1) -> myDoc.setMetadataValue(key, oldData)));
        myDoc.setMetadataValue(key, value);
        updateDocTime(myDoc,nanoTime());
        return oldData;
    }
    private Document checkNullBlank(URI uri, String key){
        if(uri==null || uri.toString().isEmpty() || key==null || key.isEmpty()){
            throw new IllegalArgumentException("No Nulls/blanks");
        }
        if(this.storage.get(uri) == null){
            throw new IllegalArgumentException("No Document");
        }
        return this.storage.get(uri);
    }
    public String getMetadata(URI uri, String key){
        Document myDoc = checkNullBlank(uri, key);
        return updateDocTime(myDoc, nanoTime()).getMetadataValue(key);
    }
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException{
        int sizeOfDoc = input.available();
        if(uri==null || uri.toString().isEmpty() || format==null || (maxBites!=0 && sizeOfDoc>maxBites)){
            throw new IllegalArgumentException("No good!");
        }
        if(input==null){
            if(!this.storage.containsKey(uri)) return 0;
            int hi = get(uri).hashCode();
            this.delete(uri);
            return hi;
        }
        Document mydoc = null;
        Document oldDoc = this.storage.get(uri);
        mydoc = getMydoc(input, uri, format, mydoc);
        this.storage.put(uri, mydoc);
        if(oldDoc==null){
            dealWithHeap(sizeOfDoc, mydoc);//this doesn't take into accounts overwrites
            this.commandStack.push(new GenericCommand<>(uri, this::deleteForUndos));
            return 0;
        }//after here is an overwrite, we must deal with the heap
        updateStorageAndTrie(oldDoc);
        dealWithHeap(sizeOfDoc, mydoc);
        this.commandStack.push(new GenericCommand<>(uri, (URI url) -> putBackNoSides(uri, oldDoc)));
        return oldDoc.hashCode();
    }
    private void dealWithHeap(int inputSize, Document mydoc) {
        this.docHeap.insert(mydoc);
        this.storageSize += inputSize;
        memoryManagement();
    }

    private void memoryManagement() {
        if(this.maxBites != 0){
            while (this.maxBites < storageSize){
                deleteUndosForURI(this.docHeap.peek().getKey());
                removeDoc(this.docHeap.remove());
            }
        }
        if(this.maxDocs != 0){
            while (this.maxDocs < this.storage.size()){
                deleteUndosForURI(this.docHeap.peek().getKey());
                removeDoc(this.docHeap.remove());
            }
        }
    }
    private void removeDoc(Document toDelete) {
        updateStorageAndTrie(toDelete);
        this.storage.put(toDelete.getKey(), null);
    }
    private void updateStorageAndTrie(Document toDelete) {
        if(toDelete.getDocumentTxt()!=null){
            this.storageSize -= toDelete.getDocumentTxt().getBytes().length;
            toDelete.getWords().forEach(word -> this.documentTrie.delete(word, toDelete));
        }else this.storageSize -= toDelete.getDocumentBinaryData().length;
    }
    private void putBackNoSides(URI uri, Document doc){
        deleteForUndos(uri);
        putNoSides(uri, doc);
    }
    private void deleteForUndos(URI uri){
        Document document = this.storage.get(uri);
        deleteFromHeap(document);
        this.storage.put(uri, null);
    }
    private Document getMydoc(InputStream input, URI uri, DocumentFormat format, Document mydoc) throws IOException {
        try{
            if (format == DocumentFormat.BINARY) {
                mydoc = new DocumentImpl(uri, input.readAllBytes());
            } else if (format == DocumentFormat.TXT) {
                String myVals = new String(input.readAllBytes());
                mydoc = new DocumentImpl(uri, myVals);
                updateTree(mydoc, myVals);
            }
        }catch (IOException e){
            throw new IOException("Something went wrong with the input stream");
        }
        return mydoc;
    }
    private void updateTree(Document doc, String text){
        String[] allText = text.split(" ");
        for(String bite : allText){
            this.documentTrie.put(bite, doc);
        }
    }
    public Document get(URI url){
        Document doc = this.storage.get(url);
        if(doc != null) updateDocTime(doc, nanoTime());
        return doc;
    }
    public boolean delete(URI url){
        if(url==null || url.toString().isEmpty()){
            throw new IllegalArgumentException("No Nulls/blanks");
        }
        Document old = this.storage.get(url);
        deleteFromHeap(old);
        this.commandStack.push(new GenericCommand<>(url, (URI uri) -> putNoSides(uri, old)));
        return this.storage.put(url, null)!=null;
    }
    private void deleteFromHeap(Document document){
        Stack<Document> tempStack = new StackImpl<>();
        while(this.docHeap.peek()!=null && !this.docHeap.peek().equals(document)){
            tempStack.push(docHeap.remove());
        }
        if(this.docHeap.peek()!=null) updateStorageAndTrie(this.docHeap.remove());
        while(tempStack.size()>0) this.docHeap.insert(tempStack.pop());
    }
    private void putNoSides(URI uri, Document doc){
        if(doc.getDocumentBinaryData()!=null){
            if(this.maxBites!=0 && doc.getDocumentBinaryData().length>this.maxBites) return;
            this.storageSize += doc.getDocumentBinaryData().length;
        }
        else{
            if(this.maxBites!=0 && doc.getDocumentTxt().getBytes().length>this.maxBites) return;
            this.storageSize += doc.getDocumentTxt().getBytes().length;
            updateTree(doc, doc.getDocumentTxt());
        }
        this.storage.put(uri, doc);
        this.docHeap.insert(doc);
        this.updateDocTime(doc, nanoTime());
        this.memoryManagement();
    }
    public void undo() throws IllegalStateException {
        checkEmptyStack();
        this.commandStack.pop().undo();
    }
    private void checkEmptyStack() throws IllegalStateException{
        if(this.commandStack.size()==0){
            throw new IllegalStateException("Nothing to undo");
        }
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
    private void deleteUndosForURI(URI uri){
        Stack<Undoable> tempStack = new StackImpl<>();
        int sizer = this.commandStack.size();
        for (int i = 0; i < sizer; i++) {
            Undoable toUndo = this.commandStack.pop();
            tempStack.push(toUndo);
            if(toUndo instanceof CommandSet) {
                CommandSet<URI> commandSet = (CommandSet<URI>) toUndo;
                if (commandSet.containsTarget(uri)) {
                    tempStack.pop();
                    // need to figure out how to remove this command. Maybe just undo it then delete the undoing
                    //iterate through every GC in this set, and then duplicate it and add them back filtering this URI's
                    Set<GenericCommand<URI>> replacement = commandSet.stream().filter(gc->!gc.getTarget().equals(uri)).collect(Collectors.toSet());
                    CommandSet<URI> filteredCommandSet = new CommandSet<>();
                    replacement.forEach(filteredCommandSet::addCommand);
                    if(!replacement.isEmpty()) tempStack.push(filteredCommandSet);
                }
            }else{
                    GenericCommand<URI> genericCommand = (GenericCommand<URI>) toUndo;
                    if(genericCommand.getTarget().equals(uri)){
                        tempStack.pop();
                    }
                }
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
    public List<Document> search(String keyword) {
        List<Document> searched = this.documentTrie.getSorted(keyword, (o1, o2) -> Integer.compare(o2.wordCount(keyword), o1.wordCount(keyword)));
        long time = nanoTime();
        for(Document doc : searched) updateDocTime(doc, time);
        return searched;
    }
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        List<Document> searched = this.documentTrie.getAllWithPrefixSorted(keywordPrefix, (o1, o2) -> Integer.compare(o2.wordCount(keywordPrefix), o1.wordCount(keywordPrefix)));
        long time = nanoTime();
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
    private Set<URI> deleteAndGetUris(Set<Document> toR) {//this converts the search functions Set<docs> into Set<URI>
        Set<URI> uris = new HashSet<>();
        CommandSet<URI> undos = new CommandSet<>();
        for(Document doc : toR){
            URI uri = doc.getKey();
            uris.add(uri);
            this.storage.put(uri, null);
            undos.addCommand(new GenericCommand<>(uri, (URI mine) -> putNoSides(mine, doc)));
        }
        this.commandStack.push(undos);
        return uris;
    }
    @Override
    public List<Document> searchByMetadata(Map<String, String> keysValues) {
        long time = nanoTime();
        return this.storage.values().stream().filter(doc->isMatches(keysValues, doc, time)).collect(Collectors.toList());
    }
    private boolean isMatches(Map<String, String> keysValues, Document doc, long time) {
        if(doc == null) return false;
        boolean matches = true;
        for(Map.Entry<String, String> entry : keysValues.entrySet()){
            if(!entry.getValue().equals(doc.getMetadata().get(entry.getKey()))){
                matches=false;
            }
        }
        if(matches) updateDocTime(doc, time);
        return matches;
    }
    @Override
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        return removing(keysValues, this.documentTrie.getSorted(keyword, (o1, o2) -> Integer.compare(o2.wordCount(keyword), o1.wordCount(keyword))));
    }
    @Override
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        return removing(keysValues, this.documentTrie.getAllWithPrefixSorted(keywordPrefix, (o1, o2) -> Integer.compare(o2.wordCount(keywordPrefix), o1.wordCount(keywordPrefix))));
    }
    private List<Document> removing(Map<String, String> keysValues, List<Document> documentList){
        long time = nanoTime();
        return documentList.stream().filter(doc->isMatches(keysValues, doc, time)).collect(Collectors.toList());
    }//instead of removeThese
    @Override
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) {
        Set<Document> temp = new HashSet<>(searchByMetadata(keysValues));
        return trieDelete(temp);
    }
    @Override
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        Set<Document> temp = new HashSet<>(searchByKeywordAndMetadata(keyword, keysValues));
        return trieDelete(temp);
    }
    @Override
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        Set<Document> temp = new HashSet<>(searchByPrefixAndMetadata(keywordPrefix, keysValues));
        return trieDelete(temp);
    }
    @Override
    public void setMaxDocumentCount(int limit) {
        if(limit<1) throw new IllegalArgumentException("Limit <1");
        this.maxDocs = limit;
        memoryManagement();
    }
    @Override
    public void setMaxDocumentBytes(int limit) {
        if(limit<1) throw new IllegalArgumentException("Limit <1");
        this.maxBites = limit;
        memoryManagement();
    }
    private Set<URI> trieDelete(Set<Document> temp) {
        temp.forEach(document -> document.getWords().forEach(word -> this.documentTrie.delete(word, document)));
        return deleteAndGetUris(temp);
    }
    private Document updateDocTime(Document document, long time){
        document.setLastUseTime(time);
        this.docHeap.reHeapify(document);
        return document;
    }
}