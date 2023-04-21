package exceptions

/**
 * Thrown when trying to generate next diamond when the previous one was an impossible one.
 */
class ImpossibleDiamondException(msg: String) extends Throwable
