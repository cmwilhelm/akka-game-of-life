package gameOfLife

import akka.actor.ActorSystem
import simulation.Simulation
import types._


object GameOfLife {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("GameOfLife")
    val simulation = system.actorOf(Simulation.props(Dimensions(200, 200)))
  }
}
