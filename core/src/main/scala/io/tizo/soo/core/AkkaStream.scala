package io.tizo.soo.core

import akka.actor.ActorSystem
import akka.stream.{OverflowStrategy, FlowMaterializer}
import akka.stream.scaladsl._
import scala.concurrent.Future
import io.tizo.soo.core.entity.{Hashtag, Author, Tweet}

/**
 * Created by tiiizhou on 12/24/14.
 */
class AkkaStream {

  implicit val system = ActorSystem("reactive-tweets")
  implicit val context = system.dispatcher
  implicit val mat = FlowMaterializer()

  val AkkaTeam = Author("akkateam")
  val Akka = Hashtag("#akka")

  def authorsPrint(tweets: Source[Tweet]) = {
    val authors: Source[Author] = tweets.filter(_.hashtags.contains(Akka)).map(_.author)
    authors.runWith(Sink.foreach(println)) //the same as authors.foreach(println)
  }

  def hashtagsPrint(tweets: Source[Tweet]) = {
    val hashtags: Source[Hashtag] = tweets.mapConcat(_.hashtags.toList)
    hashtags.runWith(Sink.foreach(println))
  }

  def authorsAndHashtagsPrint(tweets: Source[Tweet]) = {
    val writeAuthors: Sink[Author] = Sink.foreach(println)
    val writeHashtags: Sink[Hashtag] = Sink.foreach(println)

    val g = FlowGraph { implicit builder => {
      import FlowGraphImplicits._

      val b = Broadcast[Tweet]
      tweets ~> b ~> Flow[Tweet].map(_.author) ~> writeAuthors
                b ~> Flow[Tweet].mapConcat(_.hashtags.toList) ~> writeHashtags
    }}

    g.run
  }

  def authorsPrintWithBackPressure(tweets: Source[Tweet]) = {
    //This might be wrong here, need more in understanding...
    val authors = tweets.buffer(10, OverflowStrategy.dropHead).map(_.author).runWith(Sink.ignore)
  }

  def tweetsCount(tweets: Source[Tweet]) = {
    val sumSink = Sink.fold[Int, Int](0)(_ + _)

    val counter: RunnableFlow = tweets.map(t => 1).to(sumSink)
    val map: MaterializedMap = counter.run()

    val sum: Future[Int] = map.get(sumSink)

    sum.map {
      c => println(s"Total tweets processed: $c")
    }

  }

  def simpleTweetsCount(tweets:Source[Tweet]) = {
    val sumSink = Sink.fold[Int, Int](0)(_ + _)
    val sum: Future[Int] = tweets.map(t => 1).runWith(sumSink)
    sum.map {
      c => println(s"Total tweets processed: $c")
    }
  }

  def reusableFlowTweetsCount(tweets: Source[Tweet]) = {
    val sumSink = Sink.fold[Int, Int](0)(_ + _)

    val counter: RunnableFlow = tweets.filter(_.hashtags.contains(Akka)).map(t => 1).to(sumSink)

    val morningMaterialized = counter.run()
    val eveningMaterialized = counter.run()

    val morningTweetsCount: Future[Int] = morningMaterialized.get(sumSink)
    val eveningTweetsCount: Future[Int] = eveningMaterialized.get(sumSink)

    morningTweetsCount.map {
      c => println(s"Total morning tweets processed: $c")
    }

    eveningTweetsCount.map {
      c => println(s"Total evening tweets processed: $c")
    }
  }


}
