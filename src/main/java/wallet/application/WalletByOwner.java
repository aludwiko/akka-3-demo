package wallet.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import wallet.domain.WalletEvent.WalletCreated;
import wallet.domain.WalletEvent.WalletDeleted;
import wallet.domain.WalletWithOwner;

import java.util.List;

@ComponentId("wallet_by_owner")
public class WalletByOwner extends View {

  public record WalletWithOwnerList(List<WalletWithOwner> wallets) {
  }

  @Consume.FromEventSourcedEntity(value = WalletEntity.class, ignoreUnknown = true)
  public static class WalletByOwnerUpdater extends TableUpdater<WalletWithOwner> {

    public Effect<WalletWithOwner> onCreated(WalletCreated walletCreated) {
      var walletWithOwner = new WalletWithOwner(walletCreated.walletId(), walletCreated.ownerId());
      return effects().updateRow(walletWithOwner);
    }

    public Effect<WalletWithOwner> onDelete(WalletDeleted __) {
      return effects().deleteRow();
    }
  }

  @Query("SELECT * as wallets FROM wallet_by_owner WHERE ownerId = :ownerId")
  public QueryEffect<WalletWithOwnerList> getWalletByOwner(String ownerId) {
    return queryResult();
  }
}
