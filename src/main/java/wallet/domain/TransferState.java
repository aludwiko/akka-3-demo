package wallet.domain;

import static wallet.domain.TransferStatus.ABORTED;
import static wallet.domain.TransferStatus.COMPLETED;
import static wallet.domain.TransferStatus.DEPOSIT_FAILED;
import static wallet.domain.TransferStatus.FAILED;
import static wallet.domain.TransferStatus.SUCCESSFUL_WITHDRAWAL;

public record TransferState(String fromWalletId, String toWalletId, int amount, TransferStatus transferStatus) {
  public TransferState asSuccessfulWithdrawal() {
    return new TransferState(fromWalletId, toWalletId, amount, SUCCESSFUL_WITHDRAWAL);
  }

  public TransferState asCompleted() {
    return new TransferState(fromWalletId, toWalletId, amount, COMPLETED);
  }

  public TransferState asAborted() {
    return new TransferState(fromWalletId, toWalletId, amount, ABORTED);
  }

  public TransferState asFailed() {
    return new TransferState(fromWalletId, toWalletId, amount, FAILED);
  }

  public TransferState asDepositFailed() {
    return new TransferState(fromWalletId, toWalletId, amount, DEPOSIT_FAILED);
  }
}
