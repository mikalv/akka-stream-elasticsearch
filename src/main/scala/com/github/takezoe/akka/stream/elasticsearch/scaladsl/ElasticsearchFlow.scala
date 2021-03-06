package com.github.takezoe.akka.stream.elasticsearch.scaladsl

import akka.NotUsed
import akka.stream.scaladsl.Flow
import org.elasticsearch.client.{Response, RestClient}
import spray.json._
import com.github.takezoe.akka.stream.elasticsearch._

object ElasticsearchFlow {

  /**
   * Scala API: creates a [[ElasticsearchFlowStage]] that accepts as JsObject
   */
  def apply(indexName: String, typeName: String, settings: ElasticsearchSinkSettings)(
      implicit client: RestClient
  ): Flow[IncomingMessage[JsObject], Seq[IncomingMessage[JsObject]], NotUsed] =
    Flow
      .fromGraph(
        new ElasticsearchFlowStage[JsObject, Seq[IncomingMessage[JsObject]]](
          indexName,
          typeName,
          client,
          settings,
          identity,
          new SprayJsonWriter[JsObject]()(DefaultJsonProtocol.RootJsObjectFormat)
        )
      )
      .mapAsync(1)(identity)

  /**
   * Scala API: creates a [[ElasticsearchFlowStage]] that accepts specific type
   */
  def typed[T](indexName: String, typeName: String, settings: ElasticsearchSinkSettings)(
      implicit client: RestClient,
      writer: JsonWriter[T]
  ): Flow[IncomingMessage[T], Seq[IncomingMessage[T]], NotUsed] =
    Flow
      .fromGraph(
        new ElasticsearchFlowStage[T, Seq[IncomingMessage[T]]](indexName,
                                                               typeName,
                                                               client,
                                                               settings,
                                                               identity,
                                                               new SprayJsonWriter[T]()(writer))
      )
      .mapAsync(1)(identity)

  private class SprayJsonWriter[T](implicit writer: JsonWriter[T]) extends MessageWriter[T] {
    override def convert(message: T): String = message.toJson.toString()
  }

}
