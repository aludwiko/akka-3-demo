package wallet.kve;

import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import wallet.domain.WalletWithOwner;

import java.util.List;

//@ComponentId("wallet_by_owner_kve")
public class WalletByOwner extends View {

  public record WalletWithOwnerList(List<WalletWithOwner> wallets) {
  }

  @Consume.FromKeyValueEntity(WalletEntity.class)
  public static class CustomersByName extends TableUpdater<WalletWithOwner> {

    public Effect<WalletWithOwner> onChange(Wallet wallet) {
      var walletWithOwner = new WalletWithOwner(wallet.id(), wallet.ownerId());
      return effects().updateRow(walletWithOwner);
    }
  }

  @Query("SELECT * as wallets FROM wallet_by_owner_kve WHERE ownerId = :ownerId")
  public QueryEffect<WalletWithOwnerList> getWalletByOwner(String ownerId) {
    return queryResult();
  }
}
