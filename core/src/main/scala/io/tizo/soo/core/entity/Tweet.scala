package io.tizo.soo.core.entity

/**
 * Created by tiiizhou on 12/24/14.
 */
final case class Tweet(author: Author, timeStamp: Long, body: String) {

  def hashtags: Set[Hashtag] = {
    body.split(" ").collect {
      case t if t.startsWith("#") => Hashtag(t)
    }.toSet
  }

}
