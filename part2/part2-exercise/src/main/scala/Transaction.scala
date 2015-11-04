
import exceptions._
import scala.collection.mutable

object TransactionStatus extends Enumeration {
  val SUCCESS, PENDING, FAILED = Value
}

class TransactionQueue {
  var queue = new mutable.Queue[Transaction]()

  // Remove and return the first element from the queue
  def pop: Transaction = this.queue.dequeue()

  // Return whether the queue is empty
  def isEmpty: Boolean = this.queue.isEmpty

  // Add new element to the back of the queue
  def push(t: Transaction): Unit = this.queue.enqueue(t)

  // Return the first element from the queue without removing it
  def peek: Transaction = this.queue.head

  // Return an iterator to allow you to iterate over the queue
  def iterator: Iterator[Transaction] = this.queue.iterator

} // END class TransactionQueue

class Transaction(val transactionsQueue: TransactionQueue,
                  val processedTransactions: TransactionQueue,
                  val from: Account,
                  val to: Account,
                  val amount: Double,
                  val allowedAttempts: Int) extends Runnable {

  var status: TransactionStatus.Value = TransactionStatus.PENDING
  @volatile var numberOfFailedAttempts = 0

  override def run(): Unit = {

    def doTransaction() = {
      from withdraw amount
      to deposit amount
    }

    def doFail() = {
      this.synchronized {
        this.status = TransactionStatus.FAILED
        this.numberOfFailedAttempts += 1
        this.transactionsQueue.push(this)
      }
    }

    if (this.status == TransactionStatus.FAILED
      && this.numberOfFailedAttempts >= this.allowedAttempts) {

      this.processedTransactions.push(this)

    } else {

      try {

        if (from.uid < to.uid) from synchronized {
          to synchronized {
            doTransaction()
          }
        } else to synchronized {
          from synchronized {
            doTransaction()
          }
        }

        this.status = TransactionStatus.SUCCESS
        this.processedTransactions.push(this)

      } catch {

        case iae: IllegalAmountException =>
          doFail()

        case nsfe: NoSufficientFundsException =>
          doFail()

      } // END try/catch

    } // END if / else status == FAILED

  } // END run()

} // END class Transactions
