package wallet.kve;

import wallet.domain.Or;
import wallet.domain.WalletError;

import static wallet.domain.Or.left;
import static wallet.domain.Or.right;
import static wallet.domain.WalletError.INVALID_AMOUNT;
import static wallet.domain.WalletError.NOT_SUFFICIENT_FUNDS;

public record Wallet(String id, String ownerId, int balance) {

  public Or<WalletError, Wallet> deposit(int amount) {
    if (amount <= 0) {
      return left(INVALID_AMOUNT);
    } else {
      return right(new Wallet(id, ownerId, balance + amount));
    }
  }

  public Or<WalletError, Wallet> withdraw(int amount) {
    if (amount <= 0) {
      return left(INVALID_AMOUNT);
    } else if (amount > balance) {
      return left(NOT_SUFFICIENT_FUNDS);
    } else {
      return right(new Wallet(id, ownerId, balance - amount));
    }
  }
}
