package simulation

import akka.actor.Actor
import akka.actor.Props
import cell.Cell
import screen.Screen
import types._


object Simulation {
  def props(dims: Dimensions) = Props(new Simulation(dims))
}

class Simulation(dimensions: Dimensions) extends Actor {
  private val screen = context.actorOf(
    Screen.props(dimensions),
    name = "screen"
  )
  private val children = dimensions match {
    case Dimensions(width, height) => for {
      x <- List.range(0, width);
      y <- List.range(0, height)
    } yield context.actorOf(
      Cell.props(Position(x, y), subscribers(Position(x, y))),
      name = Cell.name(Position(x, y))
    )
  }

  def subscribers(pos: Position): List[String] = {
    val neighboringCells = for {
      x <- List.range(pos.x - 1, pos.x + 2);
      y <- List.range(pos.y - 1, pos.y + 2)
      if x >= 0 && x < dimensions.width
      if y >= 0 && y < dimensions.height
      if Position(x, y) != pos
    } yield Cell.name(Position(x, y))

    "screen" :: neighboringCells
  }

  def receive = {
    case _ => println("Simulation actor received a message")
  }
}
