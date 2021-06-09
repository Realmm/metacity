package org.metacity.metacity.storage;

import com.mongodb.async.client.MongoCollection;
import org.bson.Document;
import org.metacity.database.MongoDB;
import org.metacity.metacity.player.MetaPlayer;

import java.util.Optional;

public class MongoDatabase extends MongoDB {

    private MongoCollection<Document> playerCollection;

    public MongoDatabase() {
        super("metacitydb", "localhost", 27017);
        createCollection("metaplayers", () -> {
            playerCollection = getCollection("metaplayers");
        });
    }

    public void save(MetaPlayer p) {
        if (playerCollection == null) throw new IllegalStateException("Player collection not instantiated yet");
        getDocument(playerCollection, "_id", p.);
    }


}
