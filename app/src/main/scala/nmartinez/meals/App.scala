package nmartinez.meals

import scala.scalajs.js.annotation.*
import org.scalajs.dom.document
@JSExportTopLevel("MealPlanRandomizerApp")
class App {
  @JSExport
  def doSomething(containerId: String) =
    document.getElementById(containerId).innerHTML = "HTML content injected with Scala!!!"
}
