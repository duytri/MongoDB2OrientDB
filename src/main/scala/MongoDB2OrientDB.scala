package main.scala

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoCollection
import org.bson.Document
import com.mongodb.Block

object MongoDB2OrientDB {
  var mongoClient: MongoClient = null

  def mongoDBConnect(): Unit = mongoClient = new MongoClient("localhost", 27017)

  def mongoDBClose(): Unit = if (mongoClient != null) mongoClient.close() else println("Not connected yet!")

  def getDB(dbName: String): MongoDatabase = {
    mongoDBConnect()
    mongoClient.getDatabase(dbName)
  }

  def getCollection(dbName: String, colName: String): MongoCollection[Document] = {
    val db = getDB(dbName)
    db.getCollection(colName)
  }

  def main(args: Array[String]): Unit = {
    val cols = getCollection("allvnexpress", "all")
    //println(cols.find().first().toJson())
    val result = cols.find()
    result.forEach(new Block[Document] () {
      @Override
      def apply(document: Document): Unit = {
        System.out.println(document);
      }
    })

    mongoDBClose()
  }
}