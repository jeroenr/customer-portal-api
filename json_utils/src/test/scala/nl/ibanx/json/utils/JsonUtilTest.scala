package net.fromamsterdamwithlove.json.utils
import org.specs2.mutable.Specification
import net.fromamsterdamwithlove.json.utils.MarshallableImplicits._
import collection.Map

/**
 * Created with IntelliJ IDEA.
 * User: jeroen
 * Date: 11/29/13
 * Time: 4:19 PM
 * To change this template use File | Settings | File Templates.
 */
class JsonUtilTest extends Specification {

  "JsonUtil" should {
    "serialize empty map to JSON" in {
       val map = Map()
       map.toJson must equalTo("{}")
    }

    "serialize map to JSON" in {
       val map = Map("a" -> 1, "b" -> "bar")
       map.toJson must equalTo("{\"a\":1,\"b\":\"bar\"}")
    }

    "serialize nested map to JSON" in {
       val map = Map("m" -> Map(1 -> "foo"))
       map.toJson must equalTo("{\"m\":{\"1\":\"foo\"}}")
    }

    "serialize symbol map to JSON" in {
       val map: Map[Symbol, Any] = Map('a -> 1, 'b -> "bar")
       JsonUtil.toJson(map) must equalTo("{\"a\":1,\"b\":\"bar\"}")
    }

    "deserialize empty JSON to empty map" in {
       "{}".toMapOf[AnyRef] must equalTo(Map())
    }

    "deserialize JSON to map" in {
       val json = "{\"a\":1,\"b\":\"bar\"}"
       json.toMapOf[AnyRef] must equalTo(Map("a" -> 1, "b" -> "bar"))
    }

    "deserialize nested JSON to nested map" in {
       val json = "{\"m\":{\"foo\":\"bar\"}}" 
       json.toMapOf[Map[String,AnyRef]] must equalTo(Map("m" -> Map("foo" -> "bar")))
    }

    "deserialize nested JSON to case class" in {
       val json = "{\"foo\":\"bar\",\"bar\":10}" 
       json.fromJson[A] must equalTo(A("bar",10))
    }

  }

}

case class A(foo: String, bar: Int)
