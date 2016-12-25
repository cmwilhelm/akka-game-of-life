package cell

import scala.util.Random
import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import scala.concurrent.duration._
import types._


object Cell {
  val seed = Random

  def props(pos: Position, dims: Dimensions) = Props(new Cell(pos, dims))
  def name(pos: Position) = pos.x + "-" + pos.y
  def initialDelay = 1000 milliseconds
  def interval() = (500 + seed.nextInt(1000)) milliseconds
  def initialStatus() = seed.nextInt(8) match {
    case 0 => Alive
    case _ => Dead
  }
}

class Cell(val pos: Position, val dims: Dimensions) extends Actor {
  import context.dispatcher

  var livingNeighbors: Set[Position] = Set()
  var status: CellStatus = Cell.initialStatus()
  val neighborPositions: List[Position] = for {
    x <- List.range(pos.x - 1, pos.x + 2);
    y <- List.range(pos.y - 1, pos.y + 2)
    if x >= 0 && x < dims.width
    if y >= 0 && y < dims.height
    if Position(x, y) != pos
  } yield Position(x, y)

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
    informNeighbors(neighborPositions, update)
    context.parent ! update
  }

  private def informNeighbors(neighborPositions: List[Position], message: Message) {
    neighborPositions.foreach((nPos: Position) => {
      context.actorSelection("../" + Cell.name(nPos)) ! message
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
