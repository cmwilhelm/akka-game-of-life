package types

case class Position(x: Int, y: Int)
case class Dimensions(width: Int, height: Int)

abstract class CellStatus
case object Alive extends CellStatus
case object Dead extends CellStatus

abstract class Message
case class CellStatusUpdate(pos: Position, status: CellStatus) extends Message
case object NeedsInit extends Message
case object NeedsUpdate extends Message
