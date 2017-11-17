package exceptions

/**
 * Triggered when the user puts wrong parameters into the Weight creating functions.
 */
class WrongParameterException(val msg: String) extends Throwable

