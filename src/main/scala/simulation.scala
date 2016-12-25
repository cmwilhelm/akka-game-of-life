package simulation

import akka.actor.Actor
import akka.actor.Props
import cell.Cell
import screen.Screen
import types._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Simulation {
  def props(dims: Dimensions) = Props(new Simulation(dims))
}

class Simulation(dimensions: Dimensions) extends Actor {
  val children = dimensions match {
    case Dimensions(width, height) => for {
      x <- List.range(0, width);
      y <- List.range(0, height)
    } yield context.actorOf(
      Cell.props(Position(x, y), dimensions),
      name = Cell.name(Position(x, y))
    )
  }
  val screen = new Screen(dimensions)

  Future {
    println("Spawning thread for screen...")
    screen.main(Array("asdf"))
  }

  def receive = {
    case CellStatusUpdate(pos, status) => screen.updateStatus(pos, status)
  }
}
