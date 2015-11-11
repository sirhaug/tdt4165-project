import java.util.NoSuchElementException

import akka.actor._
import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.tailrec
import scala.concurrent.duration._
import akka.util.Timeout

case class GetAccountRequest(accountId: String)

case class CreateAccountRequest(initialBalance: Double)

case class IdentifyActor()

class Bank(val bankId: String) extends Actor {

  val accountCounter = new AtomicInteger(1000)

  def generateAccountId: Int = {

    @tailrec def getNext: Int = {
      val current = accountCounter.get()
      val updated = current + 1
      if (accountCounter.compareAndSet(current, updated)) return updated
      getNext
    }

    getNext

  } // END generateAccountId

  def createAccount(initialBalance: Double): ActorRef = {
    // Should create a new Account Actor and return its actor reference. Accounts should be assigned with unique ids (increment with 1).
    val id = this.generateAccountId
    context.actorOf(Props(classOf[Account], id, this.bankId, initialBalance))
  }

  def findAccount(accountId: String): Option[ActorRef] = {
    // Use BankManager to look up an account with ID accountId
    try {
      Some(BankManager.findAccount(this.bankId, accountId))
    }

    catch {
      case _:NoSuchElementException => None
    }
  }

  def findOtherBank(bankId: String): Option[ActorRef] = {
    // Use BankManager to look up a different bank with ID bankID
    try {
      Some(BankManager.findBank(bankId))
    }

    catch {
      case _:NoSuchElementException => None
    }
  }

  override def receive = {
    case CreateAccountRequest(initialBalance) => this.createAccount(initialBalance) // Create a new account
    case GetAccountRequest(id) => this.findAccount(id) // Return account
    case IdentifyActor => sender ! this
    case t: Transaction => processTransaction(t)

    case t: TransactionRequestReceipt => {
      // Forward receipt
      val accFind = this.findAccount(t.toAccountNumber)

      if (accFind.isDefined) {
        val acc = accFind.get
        acc ! t
      }
    } // END case TransactionRequestReceipt

    case msg => Console.println(s"'$msg' is $msg")
  }

  def processTransaction(t: Transaction): Unit = {
    implicit val timeout = new Timeout(5 seconds)
    val isInternal = t.to.length <= 4
    val toBankId = if (isInternal) bankId else t.to.substring(0, 4)
    val toAccountId = if (isInternal) t.to else t.to.substring(4)
    val transactionStatus = t.status
    
    // This method should forward Transaction t to an account or another bank, depending on the "to"-address.
    // HINT: Make use of the variables that have been defined above.
    if (isInternal) {
      val accFind = this.findAccount(toAccountId)

      if (accFind.isDefined) {
        val acc = accFind.get
        acc ! t
      }
    }

    else {
      val bankFind = this.findOtherBank(toBankId)

      if (bankFind.isDefined) {
        val bank = bankFind.get
        bank ! t
      }
    } // END if/else isInternal
  }
}