package wallet.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import wallet.domain.WalletEvent;
import wallet.domain.WalletEvent.FundsDeposited;
import wallet.domain.WalletEvent.FundsWithdrawn;
import wallet.domain.WalletEvent.WalletCreated;
import wallet.domain.WalletEvent.WalletDeleted;
import wallet.domain.WalletWithBalance;

import java.util.List;

@ComponentId("wallet_by_balance")
public class WalletByBalance extends View {

  public record WalletWithBalanceList(List<WalletWithBalance> wallets) {
  }

  @Consume.FromEventSourcedEntity(value = WalletEntity.class, ignoreUnknown = true)
  public static class WalletByBalanceUpdater extends TableUpdater<WalletWithBalance> {

    public Effect<WalletWithBalance> onEvent(WalletEvent walletEvent) {
      return switch (walletEvent) {
        case WalletCreated walletCreated ->
          effects().updateRow(new WalletWithBalance(walletCreated.walletId(), walletCreated.balance()));
        case FundsDeposited fundsDeposited ->
          effects().updateRow(new WalletWithBalance(fundsDeposited.walletId(), fundsDeposited.balanceAfter()));
        case FundsWithdrawn fundsWithdrawn ->
          effects().updateRow(new WalletWithBalance(fundsWithdrawn.walletId(), fundsWithdrawn.balanceAfter()));
        case WalletDeleted __ -> effects().deleteRow();
      };
    }
  }

  @Query("SELECT * as wallets FROM wallet_by_balance WHERE balance < :balance")
  public QueryEffect<WalletWithBalanceList> getWalletWithBalanceBelow(int balance) {
    return queryResult();
  }

}
