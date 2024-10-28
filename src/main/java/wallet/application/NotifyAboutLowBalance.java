package wallet.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.consumer.Consumer;
import wallet.domain.WalletEvent.FundsWithdrawn;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ComponentId("notify_about_low_balance")
@Consume.FromEventSourcedEntity(value = WalletEntity.class, ignoreUnknown = true)
public class NotifyAboutLowBalance extends Consumer {

  public Effect handle(FundsWithdrawn fundsWithdrawn) {
    if (fundsWithdrawn.balanceAfter() < 50) {
      return effects().asyncDone(sendEmailTo(fundsWithdrawn.ownerId(), fundsWithdrawn.balanceAfter()));
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
