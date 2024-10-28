package wallet.domain;

public sealed interface WalletEvent {

  record WalletCreated(String walletId, String ownerId, int balance) implements WalletEvent {
  }

  record FundsDeposited(String walletId, String ownerId, int amount, int balanceAfter) implements WalletEvent {
  }

  record FundsWithdrawn(String walletId, String ownerId, int amount, int balanceAfter) implements WalletEvent {
  }

  record WalletDeleted(String walletId, String ownerId) implements WalletEvent {
  }
}
