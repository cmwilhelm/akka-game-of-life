package screenUI

import scala.collection.mutable

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import javafx.scene.{paint => jfxsp}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.beans.property.ObjectProperty
import types._


class ScreenUI(dimensions: Dimensions) extends JFXApp {
  val scaleFactor = 3
  val statuses = mutable.Map[Position, ObjectProperty[jfxsp.Color]]()

  for {
    x <- 0 until dimensions.width;
    y <- 0 until dimensions.height
  } statuses(Position(x,y)) = ObjectProperty(Color.White)

  stage = new JFXApp.PrimaryStage {
    title.value = "Game of Life"
    width = dimensions.width * scaleFactor
    height = dimensions.height * scaleFactor
    scene = new Scene {
      fill = Color.White
      content = for {
        i <- 0 until dimensions.width;
        j <- 0 until dimensions.height
      } yield (
        new Rectangle {
          x = i * scaleFactor
          y = j * scaleFactor
          width = scaleFactor
          height = scaleFactor
          fill <== statuses(Position(i,j))
        }
      )
    }
  }

  def updateStatus(pos: Position, status: CellStatus): Unit = {
    statuses(pos).value = status match {
      case Dead     => Color.White
      case Alive(0) => Color(102/255.0, 1, 102/255.0, 1)
      case Alive(1) => Color(51/255.0, 204/255.0, 51/255.0, 1)
      case Alive(2) => Color(0, 153/255.0, 51/255.0, 1)
      case Alive(3) => Color(0, 102/255.0, 0, 1)
      case _        => Color(0, 77/255.0, 0, 1)
    }
  }
}
