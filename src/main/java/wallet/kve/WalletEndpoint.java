package wallet.kve;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Delete;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import wallet.domain.WalletCommand.Create;

import java.util.concurrent.CompletionStage;

import static akka.javasdk.http.HttpResponses.created;
import static akka.javasdk.http.HttpResponses.ok;


@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
//@HttpEndpoint("/wallet-kve/{id}/")
public class WalletEndpoint {

  private final ComponentClient componentClient;

  public WalletEndpoint(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  @Post("create/{ownerId}/{initBalance}")
  public CompletionStage<HttpResponse> create(String id, String ownerId, int initBalance) {
    return
      componentClient.forKeyValueEntity(id)
        .method(WalletEntity::create)
        .invokeAsync(new Create(id, ownerId, initBalance))
        .thenApply(__ -> created());
  }

  @Post("withdraw/{amount}")
  public CompletionStage<HttpResponse> withdraw(String id, int amount) {
    return
      componentClient.forKeyValueEntity(id)
        .method(WalletEntity::withdraw)
        .invokeAsync(amount)
        .thenApply(__ -> ok());
  }

  @Post("deposit/{amount}")
  public CompletionStage<HttpResponse> deposit(String id, int amount) {
    return
      componentClient.forKeyValueEntity(id)
        .method(WalletEntity::deposit)
        .invokeAsync(amount)
        .thenApply(__ -> ok());
  }

  @Get
  public CompletionStage<Wallet> get(String id) {
    return
      componentClient.forKeyValueEntity(id)
        .method(WalletEntity::get)
        .invokeAsync();
  }

  @Delete
  public CompletionStage<HttpResponse> delete(String id) {
    return
      componentClient.forKeyValueEntity(id)
        .method(WalletEntity::delete)
        .invokeAsync()
        .thenApply(__ -> ok());
  }
}
