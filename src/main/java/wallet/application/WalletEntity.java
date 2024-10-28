package wallet.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import wallet.application.Response.Failure;
import wallet.application.Response.Success;
import wallet.domain.Or;
import wallet.domain.Wallet;
import wallet.domain.WalletCommand;
import wallet.domain.WalletEvent;

import static akka.Done.done;

@ComponentId("wallet")
public class WalletEntity extends EventSourcedEntity<Wallet, WalletEvent> {

  @Override
  public Wallet emptyState() {
    return Wallet.EMPTY;
  }

  @Override
  public Wallet applyEvent(WalletEvent walletEvent) {
    return currentState().apply(walletEvent);
  }

  public Effect<Done> process(WalletCommand command) {
    if (isNotValid(command)) {
      return effects().error("wallet command rejected");
    } else {
      Wallet wallet = currentState();
      return switch (wallet.process(command)) {
        case Or.Left(var error) -> effects().error(error.name());
        case Or.Right(var event) -> effects().persist(event).thenReply(__ -> done());
      };
    }
  }

  /**
   * the same as process but returns a Response (used by the workflow implementation)
   */
  public Effect<Response> processWithResponse(WalletCommand command) {
    if (isNotValid(command)) {
      return effects().reply(Failure.of("wallet command rejected"));
    } else {
      Wallet wallet = currentState();
      return switch (wallet.process(command)) {
        case Or.Left(var error) -> effects().reply(Failure.of(error.name()));
        case Or.Right(var event) -> effects().persist(event).thenReply(__ -> Success.of("ok"));
      };
    }
  }

  private boolean isNotValid(WalletCommand command) {
    if (command instanceof WalletCommand.Create) {
      return !currentState().isEmpty();
    } else {
      return currentState().isEmpty();
    }
  }

  public Effect<WalletResponse> get() {
    if (currentState().isEmpty()) {
      return effects().error("wallet not created");
    } else {
      return effects().reply(WalletResponse.from(currentState()));
    }
  }

}
