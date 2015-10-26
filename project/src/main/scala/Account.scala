import exceptions.{NoSufficientFundsException, IllegalAmountException}

class Account(initialBalance: Double, val uid: Int = Bank getUniqueId) {
  var balance = initialBalance
  
  def withdraw(amount: Double): Unit = {
    if (amount > this.balance) {

      throw new NoSufficientFundsException()

    } else if ( amount < 0) {

      throw new IllegalAmountException()

    } else balance -= amount

  }
  
  def deposit(amount: Double): Unit = {
    if (amount < 0) {

      throw new IllegalAmountException()

    } else this.balance += amount
  }
  
  def getBalanceAmount: Double = {
    this.balance
  }

}