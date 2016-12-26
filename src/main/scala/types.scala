package types

final case class Position(x: Int, y: Int)
final case class Dimensions(width: Int, height: Int)

sealed trait CellStatus
final case class Alive(duration: Int) extends CellStatus
final case object Dead extends CellStatus

sealed trait Message
final case class CellStatusUpdate(pos: Position, status: CellStatus) extends Message
final case object NeedsInit extends Message
final case object NeedsUpdate extends Message
