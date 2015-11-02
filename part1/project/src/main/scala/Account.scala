import exceptions.{NoSufficientFundsException, IllegalAmountException}

class Account(initialBalance: Double, val uid: Int = Bank getUniqueId) {
  var balance = new Balance(initialBalance)
  
  def withdraw(amount: Double): Unit = {
    if (amount > this.balance.get()) {

      throw new NoSufficientFundsException()

    } else if ( amount < 0) {

      throw new IllegalAmountException()

    } else {

      balance.synchronized {

        val newBalance = this.balance.get() - amount
        this.balance.set(newBalance)

      }
    }
  }
  
  def deposit(amount: Double): Unit = {
    if (amount < 0) {

      throw new IllegalAmountException()

    } else {

      balance.synchronized {

        val newBalance = this.balance.get() + amount
        this.balance.set(newBalance)

      }
    }
  }
  
  def getBalanceAmount: Double = {

    this.balance.get()

  }
}