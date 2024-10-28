package wallet.kve;

import akka.Done;
import akka.javasdk.keyvalueentity.KeyValueEntity;
import wallet.domain.Or.Left;
import wallet.domain.Or.Right;
import wallet.domain.WalletCommand.Create;

import static akka.Done.done;

//@ComponentId("wallet-kve")
public class WalletEntity extends KeyValueEntity<Wallet> {

  public Effect<Done> create(Create create) {
    if (currentState() != null) {
      return effects().error("wallet already created");
    } else {
      Wallet newWallet = new Wallet(create.id(), create.ownerId(), create.initBalance());
      return effects()
        .updateState(newWallet)
        .thenReply(done());
    }
  }

  public Effect<Done> deposit(int amount) {
    if (currentState() == null) {
      return effects().error("wallet not created");
    } else {
      Wallet wallet = currentState();
      return switch (wallet.deposit(amount)) {
        case Left(var error) -> effects().error(error.name());
        case Right(var updatedWallet) -> effects().updateState(updatedWallet).thenReply(done());
      };
    }
  }

  public Effect<Done> withdraw(int amount) {
    if (currentState() == null) {
      return effects().error("wallet not created");
    } else {
      return switch (currentState().withdraw(amount)) {
        case Left(var error) -> effects().error(error.name());
        case Right(var updatedWallet) -> effects().updateState(updatedWallet).thenReply(done());
      };
    }
  }

  public Effect<Wallet> get() {
    if (currentState() == null) {
      return effects().error("wallet not created");
    } else {
      return effects().reply(currentState());
    }
  }

  public Effect<Done> delete() {
    if (currentState() != null) {
      return effects().error("wallet not created");
    } else {
      return effects()
        .deleteEntity()
        .thenReply(done());
    }
  }

}
