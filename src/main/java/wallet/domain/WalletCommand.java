package wallet.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
  @JsonSubTypes.Type(value = WalletCommand.Create.class),
  @JsonSubTypes.Type(value = WalletCommand.Deposit.class),
  @JsonSubTypes.Type(value = WalletCommand.Withdraw.class),
  @JsonSubTypes.Type(value = WalletCommand.Delete.class)})
public sealed interface WalletCommand {

  record Create(String id, String ownerId, int initBalance) implements WalletCommand {
  }

  record Deposit(int amount) implements WalletCommand {
  }

  record Withdraw(int amount) implements WalletCommand {
  }

  record Delete() implements WalletCommand {
  }
}