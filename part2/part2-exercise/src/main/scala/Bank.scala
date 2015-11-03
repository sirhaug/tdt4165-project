import scala.concurrent.forkjoin.ForkJoinPool
import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.tailrec

class Bank(val allowedAttempts: Integer = 3) {

  private val uid = new AtomicInteger(0)
  private val transactionsQueue: TransactionQueue = new TransactionQueue()
  private val processedTransactions: TransactionQueue = new TransactionQueue()
  private val executorContext = None

  /*
  object UIDGenerator {
    @tailrec def getNext: Int = {
      val current = uid.get
      val updated = current + 1
      if (uid.compareAndSet(current, updated)) return updated
      getNext
    }
  }
  */

  def addTransactionToQueue(from: Account, to: Account, amount: Double): Unit = {
    transactionsQueue push new Transaction(
      transactionsQueue, processedTransactions, from, to, amount, allowedAttempts)
  }

  def generateAccountId: Int = {

    @tailrec def getNext: Int = {
      val current = uid.get()
      val updated = current + 1
      if (uid.compareAndSet(current, updated)) return updated
      getNext
    }

    getNext

  }

  private def processTransactions(): Unit = {}

  def addAccount(initialBalance: Double): Account = {
    new Account(this, initialBalance)
  }

  def getProcessedTransactionsAsList: List[Transaction] = {
    processedTransactions.iterator.toList
  }

}

