package screen

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Actor
import akka.actor.Props
import scalafx.Includes._
import scalafx.application.Platform
import screenUI.ScreenUI
import types._


object Screen {
  def props(dimensions: Dimensions) = Props(new Screen(dimensions))
}

class Screen(dimensions: Dimensions) extends Actor {
  private val screen = new ScreenUI(dimensions)
  private var updateCount = 0
  private var updates = List[CellStatusUpdate]()

  override def preStart(): Unit = {
    Future {
      println("Spawning thread for visualization screen...")
      screen.main(Array[String]())
    }
  }

  def receive = {
    case CellStatusUpdate(pos, status) => {
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
}
