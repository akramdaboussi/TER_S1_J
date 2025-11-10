package Database;
import Model.MazeData;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.*;
import org.bson.Document;

public class MongoDBService {

    private final MongoCollection<Document> collection;
    private boolean est_connecté = false;

    public MongoDBService() {

        MongoCollection<Document> tempCollection = null;
        String connectionString = System.getenv("MONGODB_URI");

        if (connectionString == null) {
            System.err.println("ERREUR: La variable d'environnement MONGODB_URI n'est pas définie. Le stockage est désactivé.");
        } else {
            try {
                // Initialisation du client, de la base et de la collection
                MongoClient mongoClient = MongoClients.create(connectionString);
                tempCollection = mongoClient.getDatabase("pacman_mazes").getCollection("mazes_evaluation");
                System.out.println("Connexion à MongoDB Atlas réussie.");
                this.est_connecté = true;
            } catch (Exception e) {
                System.err.println("Erreur de connexion à MongoDB: " + e.getMessage());
            }
        }
        this.collection = tempCollection;
    }

    public boolean est_connecté() {
        return est_connecté;
    }

    /**
     * Enregistre un nouveau labyrinthe dans la collection MongoDB
     */
    public void saveMaze(MazeData data) {
        if (!est_connecté) return;
        
        try {
            Document mazeDoc = new Document("ident", data.ident())
                    .append("width", data.width())
                    .append("height", data.height())
                    .append("grid", data.grid()) // Stockage de la représentation graphique
                    .append("dateGenerated", new java.util.Date())
                    .append("note", 0); // Note par défaut
            
            this.collection.insertOne(mazeDoc);
            System.out.println("Labyrinthe " + data.ident() + " stocké en base.");
        } catch (Exception e) {
            System.err.println("Erreur de stockage MongoDB: " + e.getMessage());
        }
    }

    /**
     * Met à jour la note d'un labyrinthe existant
     */
    public long updateRating(String ident, int note) {
        if (!est_connecté) return 0;

        try {
            UpdateResult result = this.collection.updateOne(
                Filters.eq("ident", ident),
                Updates.set("note", note)
            );
            return result.getModifiedCount();
        } catch (Exception e) {
            System.err.println("Erreur de mise à jour de la note MongoDB: " + e.getMessage());
            return 0;
        }
    }
}