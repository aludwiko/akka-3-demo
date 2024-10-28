package wallet.api;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Delete;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import wallet.application.WalletByBalance;
import wallet.application.WalletByBalance.WalletWithBalanceList;
import wallet.application.WalletByOwner;
import wallet.application.WalletByOwner.WalletWithOwnerList;
import wallet.application.WalletEntity;
import wallet.application.WalletResponse;
import wallet.domain.Wallet;
import wallet.domain.WalletCommand;
import wallet.domain.WalletCommand.Create;
import wallet.domain.WalletCommand.Deposit;
import wallet.domain.WalletCommand.Withdraw;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.javasdk.http.HttpResponses.created;
import static akka.javasdk.http.HttpResponses.ok;


@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/wallet")
public class WalletEndpoint {

  private final ComponentClient componentClient;

  public WalletEndpoint(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  @Post("/{id}/create/{ownerId}/{initBalance}")
  public CompletionStage<HttpResponse> create(String id, String ownerId, int initBalance) {
    return
      componentClient.forEventSourcedEntity(id)
        .method(WalletEntity::process)
        .invokeAsync(new Create(id, ownerId, initBalance))
        .thenApply(__ -> created());
  }

  @Post("/{id}/withdraw/{amount}")
  public CompletionStage<HttpResponse> withdraw(String id, int amount) {
    return
      componentClient.forEventSourcedEntity(id)
        .method(WalletEntity::process)
        .invokeAsync(new Withdraw(amount))
        .thenApply(__ -> ok());
  }

  @Post("/{id}/deposit/{amount}")
  public CompletionStage<HttpResponse> deposit(String id, int amount) {
    return
      componentClient.forEventSourcedEntity(id)
        .method(WalletEntity::process)
        .invokeAsync(new Deposit(amount))
        .thenApply(__ -> ok());
  }

  @Get("/{id}")
  public CompletionStage<WalletResponse> get(String id) {
    return
      componentClient.forEventSourcedEntity(id)
        .method(WalletEntity::get)
        .invokeAsync();
  }

  @Delete("/{id}")
  public CompletionStage<HttpResponse> delete(String id) {
    return
      componentClient.forEventSourcedEntity(id)
        .method(WalletEntity::process)
        .invokeAsync(new WalletCommand.Delete())
        .thenApply(__ -> ok());
  }

  @Get("/by-owner/{ownerId}")
  public CompletionStage<WalletWithOwnerList> getByOwner(String ownerId) {
    return
      componentClient.forView()
        .method(WalletByOwner::getWalletByOwner)
        .invokeAsync(ownerId);
  }

  @Get("/by-balance/{balance}")
  public CompletionStage<WalletWithBalanceList> getByBalance(int balance) {
    return
      componentClient.forView()
        .method(WalletByBalance::getWalletWithBalanceBelow)
        .invokeAsync(balance);
  }

  @Get("/test/test")
  public CompletionStage<List<Wallet>> test() {
    return CompletableFuture.completedFuture(List.of(new Wallet("1", "owner1", 1), new Wallet("2", "owner2", 2)));
  }
}
