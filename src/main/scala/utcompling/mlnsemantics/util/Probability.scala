package utcompling.mlnsemantics.util

/**
 * This Numeric class represents probabilities using logarithms.  The
 * logProb logarithmic representation is completely hidden from the calling
 * code.
 *
 * This class exists to allow for the use of obvious operators (* for
 * multiplication instead of + on logarithms) and to prevent coding mistakes
 * resulting from the inadvertent mixing of logarithmic and non-logarithmic
 * Double representations of probabilities.  Additionally, it is possible to 
 * use the `sum` and `product` collection methods on collections of 
 * Probabilities, and get the expected results.
 *
 * All to* methods return the (non-logarithmic) probability stored.  The only
 * way to access the actual logarithmic value is by the 'logProb' field.
 */
final class Probability(val logProb: Double) extends Ordered[Probability] {
  def +(o: Probability): Probability = {
    if (logProb == Double.NegativeInfinity)
      o
    else if (o.logProb == Double.NegativeInfinity)
      this
    else if (logProb > o.logProb)
      new Probability(logProb + math.log(1 + math.exp(o.logProb - logProb)))
    else
      new Probability(o.logProb + math.log(1 + math.exp(logProb - o.logProb)))
  }
  def *(o: Probability): Probability = new Probability(logProb + o.logProb)
  def /(o: Probability): Probability = new Probability(logProb - o.logProb)

  override def equals(o: Any): Boolean = o match {
    case o: Probability => logProb == o.logProb
    case _ => false
  }
  override def hashCode(): Int = toDouble ##
  
  override def compare(that: Probability) = logProb.compare(that.logProb)
  def approx(o: Probability, tolerance: Double): Boolean = {
    if(this == Probability.zero && o == Probability.zero)
      true
    else
      (logProb - o.logProb).abs < tolerance
  }
  def approx(o: Probability): Boolean = this.approx(o, 0.00000001)

  def toInt = toDouble.toInt
  def toLong = toDouble.toLong
  def toFloat = toDouble.toFloat
  def toDouble = math.exp(logProb)
  
  override def toString = "Probability(%s)".format(toDouble)
}

object Probability {

  def apply(d: Double) = new Probability(math.log(d))

  val zero = new Probability(Double.NegativeInfinity)
  val one = new Probability(0.0)

  trait ProbabilityOrdering extends Ordering[Probability] {
    override def compare(a: Probability, b: Probability) = a compare b
  }

  class EnrichedNumeric[N](self: N)(implicit num: Numeric[N]) {
    def toProbability = Probability(num.toDouble(self))
  }
  implicit def enrichNumeric(self: Double) = new EnrichedNumeric(self)

  trait ProbabilityIsFractional extends Fractional[Probability] {
    def plus(x: Probability, y: Probability): Probability = x + y
    def minus(x: Probability, y: Probability): Probability = sys.error("not implemented")
    def times(x: Probability, y: Probability): Probability = x * y
    def div(x: Probability, y: Probability): Probability = x / y
    def negate(x: Probability): Probability = sys.error("Probability values cannot be negated")
    def fromInt(x: Int): Probability = x.toDouble.toProbability
    def toInt(x: Probability): Int = x.toInt
    def toLong(x: Probability): Long = x.toLong
    def toFloat(x: Probability): Float = x.toFloat
    def toDouble(x: Probability): Double = x.toDouble
  }

  implicit object ProbabilityIsFractional extends ProbabilityIsFractional with ProbabilityOrdering

}
