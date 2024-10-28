package wallet;

import akka.http.scaladsl.model.StatusCodes;
import akka.javasdk.testkit.TestKitSupport;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import wallet.application.TransferWorkflow;
import wallet.application.WalletEntity;
import wallet.application.WalletResponse;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static wallet.domain.TransferStatus.COMPLETED;


public class WalletIntegrationTest extends TestKitSupport {

  @Test
  public void shouldCreateWallet() throws Exception {
    // given
    var walletId = randomWalletId();

    // when
    var response = await(httpClient.POST("/wallet/" + walletId + "/create/o1/100")
      .invokeAsync());

    // then
    assertThat(response.status()).isEqualTo(StatusCodes.Created());
  }

  @Test
  public void shouldDepositFunds() throws Exception {
    // given
    var walletId = randomWalletId();
    await(httpClient.POST("/wallet/" + walletId + "/create/o1/100").invokeAsync());

    // when
    var response = await(httpClient.POST("/wallet/" + walletId + "/deposit/50")
      .invokeAsync());


    // then
    assertThat(response.status()).isEqualTo(StatusCodes.OK());
    var wallet = await(httpClient.GET("/wallet/" + walletId)
      .responseBodyAs(WalletResponse.class)
      .invokeAsync())
      .body();
    assertThat(wallet.balance()).isEqualTo(150);
  }


  @Test
  public void shouldWithdrawFunds() throws Exception {
    // given
    var walletId = randomWalletId();
    await(httpClient.POST("/wallet/" + walletId + "/create/o1/100").invokeAsync());

    // when
    var response = await(httpClient.POST("/wallet/" + walletId + "/withdraw/50")
      .invokeAsync());


    // then
    assertThat(response.status()).isEqualTo(StatusCodes.OK());
    var wallet = await(httpClient.GET("/wallet/" + walletId)
      .responseBodyAs(WalletResponse.class)
      .invokeAsync())
      .body();
    assertThat(wallet.balance()).isEqualTo(50);
  }

  @Test
  public void shouldTransferFunds() throws Exception {
    // given
    var walletId1 = randomWalletId();
    var walletId2 = randomWalletId();
    var transferId = "t1";
    await(httpClient.POST("/wallet/" + walletId1 + "/create/o1/100").invokeAsync());
    await(httpClient.POST("/wallet/" + walletId2 + "/create/o1/100").invokeAsync());

    // when
    var response = await(httpClient.POST("/transfer/" + transferId)
      .withRequestBody(new TransferWorkflow.StartTransfer(walletId1, walletId2, 30))
      .invokeAsync());

    // then
    Awaitility.await()
      .ignoreExceptions()
      .atMost(20, TimeUnit.SECONDS)
      .untilAsserted(() -> {
        var wallet1 = await(componentClient.forEventSourcedEntity(walletId1)
          .method(WalletEntity::get)
          .invokeAsync());
        var wallet2 = await(componentClient.forEventSourcedEntity(walletId2)
          .method(WalletEntity::get)
          .invokeAsync());
        var transferState = await(componentClient.forWorkflow(transferId)
          .method(TransferWorkflow::get)
          .invokeAsync());

        assertThat(transferState.transferStatus()).isEqualTo(COMPLETED);

        assertThat(wallet1.balance()).isEqualTo(70);
        assertThat(wallet2.balance()).isEqualTo(130);
      });
  }

  private String randomWalletId() {
    return UUID.randomUUID().toString().substring(0, 7);
  }
}
