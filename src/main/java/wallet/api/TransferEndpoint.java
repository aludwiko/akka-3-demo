package wallet.api;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import wallet.application.TransferWorkflow;
import wallet.application.TransferWorkflow.StartTransfer;
import wallet.domain.TransferState;

import java.util.concurrent.CompletionStage;

import static akka.javasdk.http.HttpResponses.ok;


@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/transfer/{id}/")
public class TransferEndpoint {

  private final ComponentClient componentClient;

  public TransferEndpoint(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  @Post
  public CompletionStage<HttpResponse> start(String id, StartTransfer startTransfer) {
    return
      componentClient.forWorkflow(id)
        .method(TransferWorkflow::start)
        .invokeAsync(startTransfer)
        .thenApply(__ -> ok());
  }

  @Get
  public CompletionStage<TransferState> get(String id) {
    return
      componentClient.forWorkflow(id)
        .method(TransferWorkflow::get)
        .invokeAsync();
  }
}
