package com.idec.invoicesystem;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class BcryptGen {
    public static void main(String[] args) {
        String connectionString = "mongodb+srv://admin:Dinethmi2004@cluster0.is2yhon.mongodb.net/invoicedb?retryWrites=true&w=majority&appName=Cluster0&serverSelectionTimeoutMS=5000&connectTimeoutMS=5000&authSource=admin";
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("invoicedb");
            MongoCollection<Document> collection = database.getCollection("users");
            
            System.out.println("--- USERS IN DATABASE ---");
            for (Document doc : collection.find()) {
                System.out.println("Username: " + doc.get("username") + " | Role: " + doc.get("role") + " | Password Hash: " + doc.get("password"));
            }
            System.out.println("-------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
