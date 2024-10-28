package wallet.domain;

import wallet.domain.WalletCommand.Create;
import wallet.domain.WalletCommand.Delete;
import wallet.domain.WalletCommand.Deposit;
import wallet.domain.WalletCommand.Withdraw;
import wallet.domain.WalletEvent.FundsDeposited;
import wallet.domain.WalletEvent.FundsWithdrawn;
import wallet.domain.WalletEvent.WalletCreated;
import wallet.domain.WalletEvent.WalletDeleted;

import java.util.UUID;

import static wallet.domain.Or.left;
import static wallet.domain.Or.right;
import static wallet.domain.WalletError.INVALID_AMOUNT;
import static wallet.domain.WalletError.NOT_SUFFICIENT_FUNDS;

public record Wallet(String id, String ownerId, int balance) {

  public static final Wallet EMPTY = new Wallet("", "", 0);

  public Or<WalletError, WalletEvent> process(WalletCommand walletCommand) {
    return switch (walletCommand) {
      case Create create -> handle(create);
      case Deposit deposit -> handle(deposit);
      case Withdraw withdraw -> handle(withdraw);
      case Delete delete -> handle(delete);
    };
  }

  private Or<WalletError, WalletEvent> handle(Create create) {
    if (create.initBalance() <= 0) {
      return left(INVALID_AMOUNT);
    } else {
      return right(new WalletCreated(create.id(), create.ownerId(), create.initBalance()));
    }
  }

  private Or<WalletError, WalletEvent> handle(Deposit deposit) {
    var amount = deposit.amount();
    if (amount <= 0) {
      return left(INVALID_AMOUNT);
    } else {
      int balanceAfter = balance + amount;
      return right(new FundsDeposited(id, ownerId, amount, balanceAfter));
    }
  }

  private Or<WalletError, WalletEvent> handle(Withdraw withdraw) {
    var amount = withdraw.amount();
    if (amount <= 0) {
      return left(INVALID_AMOUNT);
    } else if (amount > balance) {
      return left(NOT_SUFFICIENT_FUNDS);
    } else {
      return right(new FundsWithdrawn(id, ownerId, amount, balance - amount));
    }
  }

  private Or<WalletError, WalletEvent> handle(Delete __) {
    //some validation here
    return right(new WalletDeleted(id, ownerId));
  }

  public Wallet apply(WalletEvent walletEvent) {
    return switch (walletEvent) {
      case WalletCreated created -> new Wallet(created.walletId(), created.ownerId(), created.balance());
      case FundsDeposited fundsDeposited -> new Wallet(id, ownerId, fundsDeposited.balanceAfter());
      case FundsWithdrawn fundsWithdrawn -> new Wallet(id, ownerId, fundsWithdrawn.balanceAfter());
      case WalletDeleted __ -> this; //ignore
    };
  }

  public boolean isEmpty() {
    return id.equals("");
  }

  private String newLockId() {
    return UUID.randomUUID().toString();
  }
}
