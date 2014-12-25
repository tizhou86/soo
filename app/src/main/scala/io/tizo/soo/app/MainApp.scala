package io.tizo.soo.app

import io.tizo.soo.core.AkkaStream
import io.tizo.soo.core.entity.{Author, Tweet}
import akka.stream.scaladsl._

/**
 * Created by tiiizhou on 12/24/14.
 */
object MainApp extends App {

  val t1 = Tweet(Author("akkateam"), 1, "aaa #akka")

  val tweets: Source[Tweet] = Source.single(t1)

  val akkaStream = new AkkaStream().tweetsCount(tweets)

}
