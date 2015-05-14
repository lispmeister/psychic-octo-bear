package com.deeprecursion.mft

import java.util.UUID

import akka.actor._
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.github.levkhomich.akka.tracing.{ActorTracing, TracingSupport}
import com.typesafe.config._
import net.liftweb.json._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Try, Random}


case class Put(id: String) extends TracingSupport {
  val name = productPrefix
}

case class Ack(id: String, responseCode: Int) extends TracingSupport {
  val name = productPrefix
}

/* The service backend
 */
class ServiceActor extends Actor with ActorTracing {

  implicit val askTimeout: Timeout = 200.milliseconds
  implicit val formats = DefaultFormats

  override def preStart(): Unit = {
    println("Starting ServiceActor")
  }

  def receive = {

    case msg @ Put(id) =>
      trace.sample(msg, "psychic-octo-bear")

      val dstName = "s3"
      val dst = context.actorSelection("../" + dstName)
      val label = msg.name + " " + id
      trace.recordKeyValue(msg, self.path.name, label)
      trace.record(msg, id)

      println("\t\t" + self.path.name + " received Put: " + id)
      println("\t\t" + self.path.name + " calls " + dstName + ": " + id)
      // use asChildOf to continue the span
      import context.dispatcher
      dst ? Put(id).asChildOf(msg) recover {
        case e: Exception =>
          // trace exception
          trace.record(msg, e.toString)
          println("\t\t" + self.path.name + " error: " + id)
          Ack(id, 500)
      } map {
        case ack @ Ack(id, responseCode) =>
          println("\t\t" + self.path.name + " received Ack with code: "+
            responseCode + " and id: " + id)
          // close trace by marking response
          println("\t\t" + self.path.name + " acks: " + id)
          ack.asResponseTo(msg)
      } pipeTo sender

    case _  =>

  }
}

/* An example of an actor talking to an external persistence backend
 * like AWS S3.
 */
class S3Actor extends Actor with ActorTracing {
  implicit val askTimeout: Timeout = 100.milliseconds
  implicit val formats = DefaultFormats

  override def preStart(): Unit = {
    println("Starting S3Actor")
  }

  def receive = {
    case msg @ Put(id)  =>
      println("\t\t\t" + self.path.name + " received Put: " + id)
      trace.sample(msg, "psychic-octo-bear")

      // introduce random timeouts
      val t = Random.nextInt(400)
      println("\t\t\tSleeping for " + t + " milliseconds....")
      Thread.sleep(t)

      val ack = Ack(id, 200)
      val label = ack.name + " " + ack.id
      trace.recordKeyValue(msg, self.path.name, label)
      trace.record(ack, ack.id)
      println("\t\t\t" + self.path.name + " acks: " + id)

      sender ! ack.asResponseTo(msg)

    case _  =>
  }
}

/* An web app
 */
class WebActor extends Actor with ActorTracing {

  implicit val askTimeout: Timeout = 1000.milliseconds
  implicit val formats = DefaultFormats

  override def preStart(): Unit = {
    println("Starting WebActor")
  }

  def receive = {
    case msg @ Put(id) =>
      trace.sample(msg, "psychic-octo-bear")

      val dstName = "service"
      val dst = context.actorSelection("../" + dstName)
      val label = msg.name + " " + id
      trace.recordKeyValue(msg, self.path.name, label)
      trace.record(msg, id)

      println("\t" + self.path.name + " received Put: " + id)
      println("\t" + self.path.name + " calls " + dstName + ": " + id)
      import context.dispatcher
      dst ? Put(id).asChildOf(msg) recover {
        case e: Exception =>
          // trace exception
          trace.record(msg, e.toString)
          println("\tAck error: " + id)
          Ack(id, 500)
      } map {
        case ack @ Ack(id, responseCode) =>
          println("\t" + self.path.name + " received Ack with code: "+
            responseCode + " and id: " + id)
          // close trace by marking response
          println("\t" + self.path.name + " acks: " + id)
          ack.asResponseTo(msg)
      } pipeTo sender

    case _ =>

  }
}

object PsychicOctoBear extends App {
  implicit val askTimeout: Timeout = 1000.milliseconds

  val config = ConfigFactory.load("psychic-octo-bear.conf")
  val system = ActorSystem("PsychicOctoBear", config)

  val web = system.actorOf(Props[WebActor], name="web")
  val service = system.actorOf(Props[ServiceActor], name="service")
  val s3 = system.actorOf(Props[S3Actor], name="s3")

  // wait for actors to start
  Thread.sleep(2000)

  Try {

    // send messages
    for (_ <- 1 to 10) {
      val uuid = UUID.randomUUID().toString
      println("Call Web: " + uuid)
      val future = web ? Put(uuid)
      val result = Await.result(future, askTimeout.duration).asInstanceOf[Ack]
      println("Ack with code: " + result.responseCode + " and id: " + result.id)
      println()
      Thread.sleep(1000)
    }

    system.awaitTermination(1.second)

  }

  system.shutdown()
}

