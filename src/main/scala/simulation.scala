package simulation

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Actor
import akka.actor.Props
import scalafx.application.Platform
import cell.Cell
import screen.Screen
import types._

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
  var updateCount = 0
  var updates: List[CellStatusUpdate] = List()

  Future {
    println("Spawning thread for screen...")
    screen.main(Array("asdf"))
  }

  def receive = {
    case CellStatusUpdate(pos, status) =>
      updateCount += 1
      updates = CellStatusUpdate(pos, status) :: updates

      if (updateCount >= 100) {
        val updateLambdas = for (update <- updates) yield (() => {
          screen.updateStatus(update.pos, update.status)
        })

        Platform.runLater(() => updateLambdas.foreach(_()))

        updateCount = 0
        updates = List()
      }
  }
}
