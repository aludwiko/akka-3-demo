package wallet.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.workflow.Workflow;
import akka.javasdk.workflow.Workflow.Effect.TransitionalEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wallet.domain.TransferState;
import wallet.domain.WalletCommand.Deposit;
import wallet.domain.WalletCommand.Withdraw;

import static akka.Done.done;
import static wallet.domain.TransferStatus.STARTED;

@ComponentId("transfer")
public class TransferWorkflow extends Workflow<TransferState> {

  private final Logger logger = LoggerFactory.getLogger(TransferWorkflow.class);

  public record StartTransfer(String from, String to, int amount) {
  }

  private final String withdrawStepName = "withdraw";
  private final String depositStepName = "deposit";
  private final String abortStepName = "abort";

  final private ComponentClient componentClient;

  public TransferWorkflow(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  @Override
  public WorkflowDef<TransferState> definition() {

    var withdraw =
      step(withdrawStepName)
        .asyncCall(() -> {
          TransferState transferState = currentState();
          return componentClient.forEventSourcedEntity(transferState.fromWalletId())
            .method(WalletEntity::processWithResponse)
            .invokeAsync(new Withdraw(transferState.amount()));

        })
        .andThen(Response.class, this::moveToDeposit);

    var deposit =
      step(depositStepName)
        .asyncCall(() -> {
          TransferState transferState = currentState();
          return componentClient.forEventSourcedEntity(transferState.toWalletId())
            .method(WalletEntity::processWithResponse)
            .invokeAsync(new Deposit(transferState.amount()));
        })
        .andThen(Response.class, this::completeTransfer);

    var abort =
      step(abortStepName)
        .asyncCall(String.class, message -> {
          TransferState transferState = currentState();
          logger.info("compensating withdraw from walletId=" + transferState.fromWalletId());
          return componentClient.forEventSourcedEntity(transferState.fromWalletId())
            .method(WalletEntity::processWithResponse)
            .invokeAsync(new Deposit(transferState.amount()));
        })
        .andThen(Response.class, r -> effects().updateState(currentState().asAborted()).end());

    return workflow()
      .addStep(withdraw)
      .addStep(deposit, maxRetries(3).failoverTo(abortStepName))
      .addStep(abort);
  }

  private TransitionalEffect<Void> moveToDeposit(Response response) {
    TransferState transferState = currentState();

    return switch (response) {
      case Response.Success __ -> {
        TransferState updatedTransfer = transferState.asSuccessfulWithdrawal();
        logger.info("move to deposit: {}", updatedTransfer);
        yield effects()
          .updateState(updatedTransfer)
          .transitionTo(depositStepName);
      }
      case Response.Failure __ -> {
        TransferState updatedTransfer = transferState.asFailed();
        logger.info("finished as failed: {}", updatedTransfer);
        yield effects()
          .updateState(updatedTransfer)
          .end();
      }
    };
  }

  private TransitionalEffect<Void> completeTransfer(Response response) {
    return switch (response) {
      case Response.Success __ -> {
        TransferState updatedTransfer = currentState().asCompleted();
        logger.info("transfer completed: {}", updatedTransfer);
        yield effects()
          .updateState(updatedTransfer)
          .end();
      }
      case Response.Failure __ -> throw new IllegalStateException("unexpected failure for deposit");
    };
  }

  public Effect<Done> start(StartTransfer startTransfer) {
    if (currentState() != null) {
      return effects().error("transfer is running");
    } else {
      return effects()
        .updateState(new TransferState(startTransfer.from, startTransfer.to, startTransfer.amount, STARTED))
        .transitionTo(withdrawStepName)
        .thenReply(done());
    }
  }

  public Effect<TransferState> get() {
    if (currentState() == null) {
      return effects().error("transfer not exists");
    } else {
      return effects().reply(currentState());
    }
  }
}
