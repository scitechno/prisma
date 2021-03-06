package cool.graph.websocket.websockets

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import cool.graph.messagebus.testkits.spechelpers.InMemoryMessageBusTestKits
import cool.graph.subscriptions.SubscriptionDependenciesForTest
import cool.graph.websocket.WebsocketSession
import cool.graph.websocket.protocol.Request
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class WebsocketSessionSpec
    extends InMemoryMessageBusTestKits(ActorSystem("websocket-session-spec"))
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures {

  override def afterAll = shutdown()

  "The WebsocketSession" should {
    "send a message with the body STOP to the requests queue AND a Poison Pill to the outActor when it is stopped" in {
      withQueueTestKit[Request] { testKit =>
        val projectId                 = "projectId"
        val sessionId                 = "sessionId"
        val outgoing                  = TestProbe().ref
        val manager                   = TestProbe().ref
        val probe                     = TestProbe()
        implicit val testDependencies = new SubscriptionDependenciesForTest()

        probe.watch(outgoing)

        val session = system.actorOf(Props(WebsocketSession(projectId, sessionId, outgoing, manager, testKit, isV7protocol = true)))

        system.stop(session)
        probe.expectTerminated(outgoing)
        testKit.expectPublishedMsg(Request(sessionId, projectId, "STOP"))
      }
    }
  }
}
