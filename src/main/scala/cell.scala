package cell

import scala.util.Random
import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import scala.concurrent.duration._
import types._


object Cell {
  val seed = Random

  def props(pos: Position, subscribers: List[String]) = {
    Props(new Cell(pos, subscribers))
  }
  def name(pos: Position) = pos.x + "-" + pos.y
  def initialDelay = 1000 milliseconds
  def interval() = (500 + seed.nextInt(1000)) milliseconds
  def initialStatus() = seed.nextInt(8) match {
    case 0 => Alive
    case _ => Dead
  }
}

class Cell(pos: Position, subscribers: List[String]) extends Actor {
  import context.dispatcher

  private var livingNeighbors: Set[Position] = Set()
  private var status: CellStatus = Cell.initialStatus()

  override def preStart(): Unit = {
    context.system.scheduler.scheduleOnce(
      Cell.initialDelay,
      self,
      NeedsInit
    )
  }

  def receive = {
    case CellStatusUpdate(pos, status) => handleCellStatusUpdate(pos, status)
    case NeedsInit                     => handleNeedsInit()
    case NeedsUpdate                   => handleNeedsUpdate()
  }

  private def handleCellStatusUpdate(pos: Position, status: CellStatus): Unit = {
    status match {
      case Alive => livingNeighbors += pos
      case Dead  => livingNeighbors -= pos
    }
  }

  private def handleNeedsInit(): Unit = {
    broadcastStatus()
    scheduleUpdate()
  }

  private def handleNeedsUpdate(): Unit = {
    status = newStatus(livingNeighbors)
    broadcastStatus()
    scheduleUpdate()
  }

  private def newStatus(livingNeighbors: Set[Position]): CellStatus = {
    (status, livingNeighbors.size) match {
      case (_, 3)     => Alive
      case (Alive, 2) => Alive
      case (_, _)     => Dead
    }
  }

  private def broadcastStatus() {
    val update = CellStatusUpdate(pos, status)
    subscribers foreach ((subscriber: String) => {
      context.actorSelection("../" + subscriber) ! update
    })
  }

  private def scheduleUpdate() = {
    context.system.scheduler.scheduleOnce(
      Cell.interval(),
      self,
      NeedsUpdate
    )
  }
}
