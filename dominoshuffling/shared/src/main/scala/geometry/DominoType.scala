package geometry


sealed trait DominoType {
  def name: String = """[A-Z][a-z]*""".r.findAllIn(toString).mkString(" ")

  def defaultColor: (Int, Int, Int)
}

case object NorthGoing extends DominoType {
  def defaultColor: (Int, Int, Int) = (255, 0, 0)
}
case object SouthGoing extends DominoType {
  def defaultColor: (Int, Int, Int) = (0, 0, 255)
}
case object EastGoing extends DominoType {
  def defaultColor: (Int, Int, Int) = (0, 255, 0)
}
case object WestGoing extends DominoType {
  def defaultColor: (Int, Int, Int) = (255, 255, 0)
}


object DominoType {

  val types: List[DominoType] = List(
    NorthGoing, SouthGoing, EastGoing, WestGoing
  )

}