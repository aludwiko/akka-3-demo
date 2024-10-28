# akka-3-demo


To understand the Akka concepts that are the basis for this example, see [Development Process](https://doc.akka.io/concepts/development-process.html) in the documentation.


This project contains the skeleton to create an Akka service. To understand more about these components, see [Developing services](https://doc.akka.io/java/index.html). Examples can be found [here](https://doc.akka.io/samples/index.html).


Use Maven to build your project:

```shell
mvn compile
```


When running an Akka service locally.

To start your service locally, run:

```shell
mvn compile exec:java
```

This command will start your Akka service. With your Akka service running, the endpoint it's available at:

```shell
#export WALLET_URL=https://long-bush-9465.gcp-us-east1.apps.akka.st
export WALLET_URL=http://localhost:9000
```


```shell
curl $WALLET_URL/hello/andrzej
```

## Exercise the wallet

Create a wallet:

```shell
curl -i -XPOST $WALLET_URL/wallet/w1/create/o1/100
```

Withdraw funds:

```shell
curl -i -XPOST $WALLET_URL/wallet/w1/withdraw/20
```

Deposit funds:

```shell
curl -i -XPOST $WALLET_URL/wallet/w1/deposit/10
```

Get a wallet:

```shell
curl $WALLET_URL/wallet/w1
```

Create a second wallet:

```shell
curl -i -XPOST $WALLET_URL/wallet/w2/create/o1/100
```

Start a transfer:

```shell
curl -i -XPOST $WALLET_URL/transfer/t1 \
  --header "Content-Type: application/json" \
  --data '{"from":"w1","to":"w2","amount":30}'
```

Get transfer:

```shell
curl $WALLET_URL/transfer/t1
```

Get a wallet:

```shell
curl $WALLET_URL/wallet/w1
curl $WALLET_URL/wallet/w2
```

Get wallet by owner

```shell
curl -i $WALLET_URL/wallet/by-owner/o1
```

To deploy your service, install the `akka` CLI as documented in
[Install Akka CLI](https://doc.akka.io/akka-cli/index.html)
and configure a Docker Registry to upload your docker image to.

You will need to update the `dockerImage` property in the `pom.xml` and refer to
[Configuring registries](https://doc.akka.io/operations/projects/container-registries.html)
for more information on how to make your docker image available to Akka.

Finally, you can use the [Akka Console](https://console.kalix.io)
to create a project and then deploy your service into the project by first packaging and publishing the docker image through `mvn deploy` and then deploying the image through the `akka` CLI.
