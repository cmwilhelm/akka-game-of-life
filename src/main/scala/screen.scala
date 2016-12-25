package screen

import scala.collection.mutable
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import scalafx.beans.property._
import types._


class Screen(dimensions: Dimensions) extends JFXApp {
  val scaleFactor = 3
  val statuses    = mutable.Map[Position, BooleanProperty]()

  for {
    x <- 0 until dimensions.width;
    y <- 0 until dimensions.height
  } statuses(Position(x,y)) = BooleanProperty(false)

  stage = new JFXApp.PrimaryStage {
    title.value = "Game of Life"
    width = dimensions.width * scaleFactor
    height = dimensions.height * scaleFactor
    scene = new Scene {
      fill = White
      content = for {
        i <- 0 until dimensions.width;
        j <- 0 until dimensions.height
      } yield (
        new Rectangle {
          x = i * scaleFactor
          y = j * scaleFactor
          width = scaleFactor
          height = scaleFactor
          fill <== when (statuses(Position(i,j))) choose Red otherwise White
        }
      )
    }
  }

  def updateStatus(pos: Position, status: CellStatus): Unit = {
    status match {
      case Alive => statuses(pos).value = true
      case Dead  => statuses(pos).value = false
    }
  }
}
