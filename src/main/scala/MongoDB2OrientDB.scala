package main.scala

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoCollection
import org.bson.Document
import com.mongodb.Block
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import com.tinkerpop.blueprints.impls.orient.OrientVertexType
import com.tinkerpop.blueprints.impls.orient.OrientEdgeType
import com.orientechnologies.orient.core.metadata.schema.OType
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.Vertex
import scala.collection.mutable.HashSet
import com.tinkerpop.blueprints.Edge
import com.tinkerpop.blueprints.impls.orient.OrientVertex
import java.lang.Iterable
import com.tinkerpop.blueprints.Direction

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
    println("Attention:")
    println("\t\t- MongoDB server must be started before.")
    println("\t\t- OrientDB server don't have to be started before.")
    println("\t\t- First parameter is flag of drop old OrientDB database. Default is TRUE.")

    val dropFlag = if (args.length > 0) { if (args(0).equalsIgnoreCase("FASLE")) false else true } else { true }

    val titleEdgeLabel = "CO_CHU_DE"
    val odbName = "allvnexpress"

    // opens the DB (if not existing, it will create it)
    val uri: String = "plocal:/home/duytri/orientdb/databases/" + odbName
    val factory: OrientGraphFactory = new OrientGraphFactory(uri)
    val graph: OrientGraph = factory.getTx()

    try {

      var giaoduc: Vertex = null

      if (dropFlag == true) {

        if (graph.getVertexType("CHU_DE") == null) {

          // we now extend the Vertex class for CHU DE and BAI VIET
          val title: OrientVertexType = graph.createVertexType("CHU_DE")
          title.createProperty("Ten", OType.STRING)

          val document: OrientVertexType = graph.createVertexType("BAI_VIET")
          document.createProperty("MongoId", OType.STRING)
          document.createProperty("Noi_Dung", OType.STRING)

          // we now extend the Edge class for a "CO CHU DE" relationship
          // between CHU DE and BAI VIET
          val hasTitle: OrientEdgeType = graph.createEdgeType(titleEdgeLabel)
          hasTitle.createProperty("co_Chu_De", OType.LINK)

          graph.commit()

        } else {
          // cleans up the DB since it was already created in a preceding run
          graph.command(new OCommandSQL("DELETE VERTEX V")).execute()
          graph.command(new OCommandSQL("DELETE EDGE E")).execute()
          graph.commit()
        }

        //create CHU DE - Giao Duc vertex
        giaoduc = graph.addVertex("class:CHU_DE", "Ten", "Giao_Duc")
        graph.commit()
      } else {
        val res: Iterable[OrientVertex] = graph
          .command(new OCommandSQL(s"SELECT FROM CHU_DE WHERE Ten='Giao_Duc'"))
          .execute()

        giaoduc = res.iterator.next()
      }

      val cols = getCollection("allvnexpress", "all")

      val filerValue: Document = new Document("subject", "Giáo dục")

      val result = cols.find(filerValue)
      var rowCount = 1
      result.forEach(new Block[Document]() {
        @Override
        def apply(document: Document): Unit = {
          //create vertex
          val oneDoc: Vertex = graph.addVertex("class:BAI_VIET", Nil: _*)
          oneDoc.setProperty("MongoId", document.get("_id").toString())
          oneDoc.setProperty("Noi_Dung", document.get("content").toString().replaceAll("\t", ""))

          //create edge
          val johnDoeAcme: Edge = graph.addEdge(null, oneDoc, giaoduc, titleEdgeLabel)
          johnDoeAcme.setProperty("co_Chu_De", giaoduc)
          graph.commit()
          println("Inserted " + rowCount + " row(s)!")
          rowCount += 1
        }
      })

    } catch {
      case t: Throwable => {
        println("************************ ERROR ************************")
        t.printStackTrace() // TODO: handle error
        println()
        println("************************ ERROR ************************")
      }
    } finally {
      graph.shutdown()
      factory.close()
      mongoDBClose()
    }
  }
}