package org.metacity.database;

import com.mongodb.ServerAddress;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ConnectionPoolSettings;
import org.bson.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Interact with a MongoDB database using this utility class
 */
public abstract class MongoDB {

    private MongoClient client;

    private final String database;
    private final String host;
    private final int port;

    /**
     * Initialise a MongoDB database utility class
     * @param database The name of the database want to use or want to create
     * @param host The IP of the database, i.e localhost
     * @param port The port of the IP, i.e 27017
     */
    public MongoDB(String database, String host, int port) {
        this.database = database;
        this.host = host;
        this.port = port;
        initializeDatabase();
    }

    /**
     * Initialise the database
     */
    protected void initializeDatabase() {
        ClusterSettings clusterSettings = ClusterSettings.builder()
                .hosts(Collections.singletonList(new ServerAddress(host, port)))
                .build();

        ConnectionPoolSettings connectionPoolSettings = ConnectionPoolSettings.builder()
                .maxSize(10)
                .build();

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .clusterSettings(clusterSettings)
                .connectionPoolSettings(connectionPoolSettings)
                .build();
        this.client = MongoClients.create(clientSettings);
    }

    /**
     * Close the database
     */
    public void closeDatabase() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    /**
     * Delete a collection from the database
     * @param s The name of the collection to delete
     * @param callback The callback for the event
     */
    protected void deleteCollection(String s, @Nullable Runnable callback) {
        getCollection(s).drop(new ErrorOnlyCallBack<Void>() {
            @Override
            public void onResult(Void result) {
                if (callback != null) callback.run();
            }
        });
    }

    /**
     * Create a collection in the database
     * @param s The name of the collection to create
     * @param callback The callback for the event
     */
    protected void createCollection(String s, @Nullable Runnable callback) {
        MongoDatabase database = getDatabase();
        Set<String> list = new HashSet<>();

        Latch latch = new Latch();

        database.listCollectionNames().forEach(list::add, (result, throwable) -> latch.countDown());

        latch.await();

        if (!list.contains(s)) {
            database.createCollection(s,
                    new CreateCollectionOptions().autoIndex(true), new ErrorOnlyCallBack<Void>() {
                        @Override
                        public void onResult(Void result) {
                            if (callback != null) callback.run();
                        }
                    });
        } else {
            if (callback != null) callback.run();
        }
    }

    protected Collection<String> getDocumentNames() {
        Set<String> names = new HashSet<>();

        Latch latch = new Latch();
        getDatabase().listCollectionNames().forEach(names::add, (aVoid, throwable) -> latch.countDown());
        latch.await();
        return names;
    }

    /**
     * Get the first matching document from the database
     * @param documents The collection of documents to look through {@see #getCollection(String collection)}
     * @param key The key of the document you want to get
     * @param id The id of the document you want to get
     * @param callback The callback for the event
     */
    protected void getDocument(MongoCollection<Document> documents, String key, String id, @Nullable DatabaseResultCallback<Document> callback) {
        documents.find(new Document(key, id)).first(new ErrorOnlyCallBack<Document>() {
            @Override
            public void onResult(Document document) {
                if (callback != null) callback.accept(document);
            }
        });
    }

    /**
     * Insert a document into the database
     * @param documents The collection of documents to insert into {@see #getCollection(String collection)}
     * @param document The document to insert
     * @param callback The callback for the event
     */
    protected void insert(MongoCollection<Document> documents, Document document, @Nullable Runnable callback) {
        documents.insertOne(document, new ErrorOnlyCallBack<Void>() {
            @Override
            public void onResult(Void aVoid) {
                if (callback != null) callback.run();
            }
        });
    }

    /**
     * Updates a document already in a database. If the document is not present, it inserts the document instead
     * @param documents The collection of documents to update the document from {@see #getCollection(String collection)}
     * @param toUpdate The document to update
     * @param updated The updated version of the document
     * @param callback The callback for the event
     */
    protected void update(MongoCollection<Document> documents, Document toUpdate, Document updated, @Nullable DatabaseResultCallback<UpdateResult> callback) {
        Document d = new Document("$set", updated);
        documents.updateOne(toUpdate, d, new UpdateOptions().upsert(true), new ErrorOnlyCallBack<UpdateResult>() {
            @Override
            public void onResult(UpdateResult result) {
                if (callback != null) callback.accept(result);
            }
        });
    }

    /**
     * Delete a document from the database
     * @param documents The collection of documents to delete from {@see #getCollection(String collection)}
     * @param document The document to delete
     * @param callback The callback for the event
     */
    protected void delete(MongoCollection<Document> documents, Document document, @Nullable DatabaseResultCallback<DeleteResult> callback) {
        if (document == null) return;
        documents.deleteOne(document, new ErrorOnlyCallBack<DeleteResult>() {
            @Override
            public void onResult(DeleteResult result) {
                if (callback != null) callback.accept(result);
            }
        });
    }

    /**
     * Get a collection from the database
     * @param collection The name of the collection to get
     * @return The {@link MongoCollection<Document>} in the database
     */
    protected MongoCollection<Document> getCollection(String collection) {
        return getDatabase().getCollection(collection);
    }

    /**
     * Get the database
     * @return The {@link MongoDatabase}
     */
    protected MongoDatabase getDatabase() {
        return client.getDatabase(this.database);
    }

    private abstract static class ErrorOnlyCallBack<T> implements SingleResultCallback<T> {
        @Override
        public void onResult(T t, Throwable throwable) {
            if (throwable != null) {
                throwable.printStackTrace();
            }
            onResult(t);
        }

        public abstract void onResult(T result);
    }

}
