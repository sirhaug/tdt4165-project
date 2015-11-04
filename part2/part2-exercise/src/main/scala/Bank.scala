import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.ExecutionContext
import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.tailrec

class Bank(val allowedAttempts: Integer = 3) {

  private val uid = new AtomicInteger(0)
  private val transactionsQueue: TransactionQueue = new TransactionQueue()
  private val processedTransactions: TransactionQueue = new TransactionQueue()
  private val numberOfUsableThreads = 4
  private val executorContext = ExecutionContext.fromExecutor(new ForkJoinPool(numberOfUsableThreads))

  Main.thread(processTransactions())

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

  } // END generateAccountId

  private def processTransactions(): Unit = {
    // Do Something
    // Run/call executorContext

    if (!this.transactionsQueue.isEmpty) {

      val transaction = this.transactionsQueue.peek

      if (transaction.status == TransactionStatus.PENDING
        || (transaction.status == TransactionStatus.FAILED
          && transaction.numberOfFailedAttempts <= this.allowedAttempts)) {

        this.executorContext.execute(transaction)

      } else {

        this.processedTransactions.push(this.transactionsQueue.pop)

      }

    } // END if (!this.transactionsQueue.isEmpty)

    Thread.sleep(100)
    processTransactions()

  }

  def addAccount(initialBalance: Double): Account = {
    new Account(this, initialBalance)
  }

  def getProcessedTransactionsAsList: List[Transaction] = {
    processedTransactions.iterator.toList
  }

} // END class Bank

