package wallet.kve;

import akka.Done;
import akka.javasdk.annotations.Consume;
import akka.javasdk.consumer.Consumer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

//@ComponentId("notify_about_low_balance_kve")
@Consume.FromKeyValueEntity(WalletEntity.class)
public class NotifyAboutLowBalance extends Consumer {

  public Effect onChange(Wallet wallet) {
    if (wallet.balance() < 50) {
      return effects().asyncDone(sendEmailTo(wallet.ownerId(), wallet.balance()));
    } else {
      return effects().ignore();
    }
  }

  private CompletionStage<Done> sendEmailTo(String ownerId, int currentBalance) {
    return CompletableFuture.supplyAsync(() -> {
      System.out.println("Sending email to: " + ownerId + ". Balance: " + currentBalance);
      return Done.getInstance();
    });
  }
}
