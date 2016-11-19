package main.scala

import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.collection.mutable.Document

object MongoDB2OrientDB {
  var mongoClient: MongoClient = null

  def mongoDBConnect(): Unit = mongoClient = MongoClient()

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
    println(cols.count().toString())
    mongoDBClose()
  }
}