package simulation

import akka.actor.Actor
import akka.actor.ActorSelection
import akka.actor.Props
import cell.Cell
import screen.Screen
import types._


object Simulation {
  def props(dims: Dimensions) = Props(new Simulation(dims))
}

class Simulation(dimensions: Dimensions) extends Actor {
  override def preStart(): Unit = {
    val screen = context.actorOf(
      Screen.props(dimensions),
      name = "screen"
    )

    val children = dimensions match {
      case Dimensions(width, height) => for {
        x <- List.range(0, width);
        y <- List.range(0, height)
      } yield context.actorOf(
        Cell.props(Position(x, y), subscribers(Position(x, y))),
        name = Cell.name(Position(x, y))
      )
    }
  }

  private def subscribers(pos: Position): List[ActorSelection] = {
    val neighboringCells = for {
      x <- List.range(pos.x - 1, pos.x + 2);
      y <- List.range(pos.y - 1, pos.y + 2)
      if x >= 0 && x < dimensions.width
      if y >= 0 && y < dimensions.height
      position = Position(x,y)
      if position != pos
    } yield context.actorSelection(Cell.name(position))

    context.actorSelection("screen") :: neighboringCells
  }

  def receive = {
    case _ => println("Simulation actor received a message")
  }
}
