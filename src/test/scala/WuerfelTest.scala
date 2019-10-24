import model.Wuerfel
import org.scalatest.{Matchers, WordSpec}


class WuerfelTest extends WordSpec with Matchers{
  "A Wuerfel" when { "new" should{
    val wuerfel = Wuerfel()
    "have a number between 1 and 6" in {
      wuerfel.eyecount should be > 0
      wuerfel.eyecount should be < 7
    }
    "when thrown should have the same or a different Number between 1 and 6 " in {
      val newWuerfel = wuerfel.throwDice()
      newWuerfel.eyecount should be > 0
      newWuerfel.eyecount should be < 7
    }
    "should have a cool String representation" in{
      wuerfel.toString() should be (wuerfel.eyecount.toString)
    }
    "A Pash if 2 dices have the same eyecount" in {
      wuerfel.checkPash(wuerfel) should be (true)
      val aDice = Wuerfel()
      wuerfel.checkPash(aDice) should be (false)
    }
  }
  }

}
