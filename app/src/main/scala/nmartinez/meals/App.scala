package nmartinez.meals

import cats.effect.IO
import scala.scalajs.js.annotation.*
import org.scalajs.dom.document
import tyrian.*
import tyrian.Html.*
import tyrian.cmds.Logger
import cats.effect.*
import scala.concurrent.duration.*

object App {
  sealed trait Msg
  case class Increase(amount: Int) extends Msg
  case class Decrease(amount: Int) extends Msg
  case class Model(count: Int)
}

@JSExportTopLevel("MealPlanRandomizerApp")
class App extends TyrianApp[App.Msg, App.Model] {
  import App.*
  // TyrianApp gets parametrized with [type of message, type of model]
  // (can start from something as simple as [Int, String])

  /*
  Ways to send messages:
  - trigger a command
  - create a subscription
  - listen for an event
  */

  // triggered when TyrianApp is created
  // sets initial model
  override def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model(0), Cmd.None)

  // potentially endless stream of messages
  override def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None //Sub.every[IO](1.second).map(_ => Increment(1))


  // triggered whenever a new message is received
  // updates the model (aka state): model => message => (new model, new command)
  override def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Increase(amount) => (
      model.copy(count = model.count + amount),
      Logger.consoleLog[IO]("Changing count by " + amount)
    )
    case Decrease(amount) => (model.copy(count = model.count - amount), Cmd.None)

  // triggered whenever model changes on update()
  // updates the html
  override def view(model: Model): Html[Msg] =
    div(
      button(onClick(Increase(1)))("Increase"),
      button(onClick(Decrease(1)))("Decrease"),
      div(s"Tyrian running: ${model.count}")
    )

  // ScalaJS example without Tyrian
  // usage: MealPlanRandomizerApp.doSomething("app") from app.js
  @JSExport
  def doSomething(containerId: String) =
    document.getElementById(containerId).innerHTML = "HTML content injected with Scala!"

}
