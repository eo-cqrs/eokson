<img alt="logo" src="https://eo-cqrs.github.io/.github/eo-cqrs.svg" height="100px" />

[![Managed By Self XDSD](https://self-xdsd.com/b/mbself.svg)](https://self-xdsd.com/p/eo-cqrs/eokson?provider=github)

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](https://www.rultor.com/b/eo-cars/eokson)](https://www.rultor.com/p/eo-cqrs/eokson)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)
<br>

[![mvn](https://github.com/eo-cqrs/eokson/actions/workflows/mvn.yaml/badge.svg)](https://github.com/eo-cqrs/eokson/actions/workflows/mvn.yaml)
[![maven central](http://maven-badges.herokuapp.com/maven-central/io.github.eo-cqrs/eokson/badge.svg)](https://search.maven.org/artifact/io.github.eo-cqrs/eokson)
[![javadoc](https://javadoc.io/badge2/io.github.eo-cqrs/eokson/javadoc.svg)](https://javadoc.io/doc/io.github.eo-cqrs/eokson)
[![codecov](https://codecov.io/gh/eo-cqrs/eokson/branch/master/graph/badge.svg?token=nDm0NhOfhF)](https://codecov.io/gh/eo-cqrs/eokson)

[![Hits-of-Code](https://hitsofcode.com/github/eo-cqrs/eokson)](https://hitsofcode.com/view/github/eo-cqrs/eokson)
[![Lines-of-Code](https://tokei.rs/b1/github/eo-cqrs/eokson)](https://github.com/eo-cqrs/eokson)
[![PDD status](http://www.0pdd.com/svg?name=eo-cqrs/eokson)](http://www.0pdd.com/p?name=eo-cqrs/eokson)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/eo-cqrs/eokson/blob/master/LICENSE.txt)

Project architect: [@h1alexbel](https://github.com/h1alexbel)

EOkson - Elegant, Object-Oriented JSON Transformations.

**Motivation**. We are not happy dealing with JSON in procedural way.
<br>
We offer everything through simple, declarative objects.

**Principles**. These are the [design principles](https://www.elegantobjects.org/#principles) behind EOkson.

**How to use**. All you need is this (get the latest
version [here](https://search.maven.org/artifact/io.github.eo-cqrs/eokson)):

Maven:
```xml
<dependency>
  <groupId>io.github.eo-cqrs</groupId>
  <artifactId>eokson</artifactId>
</dependency>
```

Gradle:
```groovy
dependencies {
    compile 'io.github.eo-cqrs:eokson:<version>'
}
```

### Create new `Json` object

```java
// From String:
String jsonAsString = "{\"nymph\": \"nereid\"}";
Json json = new Json.Of(jsonAsString);

// From InputStream:
InputStream stream = new ByteArrayInputStream(jsonAsString.getBytes());
json = new Json.Of(stream);

// From Jackson's JsonNode:
JsonNode node = new ObjectMapper().readTree(jsonAsString);
json = new Json.Of(node);
```

### SmartJson

Once we have the `Json` object, to use it in various ways,
the [Smart Object pattern](https://www.yegor256.com/2016/04/26/why-inputstream-design-is-wrong.html) is employed.

```java
// Convert it to String:
String textual = new SmartJson(json).textual();

// Convert it to pretty formatted String:
String pretty = new SmartJson(json).pretty();

// Convert it to byte array:
byte[] bytes = new SmartJson(json).byteArray();

// Get a String field value:
Optional<String> leaf = new SmartJson(json).leaf("nymph");

// Get a deeply nested Json:
SmartJson nested = new SmartJson(json).at("/path/to/nested/json");

// Get a deeply nested int:
int nestedInt = new SmartJson(json).at("/path/to/nested/int");
```

### MutableJson

While the main purpose of this library is to enable making custom implementations of the `Json` interface (see more on
that below), if you need to quickly assemble a `Json` by hand, `MutableJson` can be used. This API has a very
declarative notation.

```java
Json json = new MutableJson().with(
    "ocean",
    new MutableJson().with(
      "nereid1",
      new MutableJson()
        .with("name", "Thetis")
        .with("hair", "black")
    ).with(
      "nereid2",
      new MutableJson()
        .with("name", "Actaea")
        .with("hair", "blonde")
    )
  .with("stormy", true)
  );
System.out.println(new SmartJson(json).pretty());
```

The code above would print this:

```json
{
  "ocean": {
    "nereid1": {
      "name": "Thetis",
      "hair": "black"
    },
    "nereid2": {
      "name": "Actaea",
      "hair": "blonde"
    },
    "stormy": true
  }
}
```

### Custom implementations

If you have an object which needs to be able to display itself as JSON, sometimes it might be useful to just treat it as
a JSON to begin with. In that case that object will have to implement a JSON interface. In most (all?) other libraries,
JSON interfaces are huge, making it very difficult to implement them. With Nereides, all you need to do is provide the
JSON representation in a stream of bytes. The easiest way to do this is to encapsulate another `Json` and delegate to
it, or construct one on the spot.

Let's say we have a bank account which we need to display as JSON. We need its IBAN, nickname and balance, which (to
make this a less trivial example) we get from another service. One way to implement it is this:

```java
public final class BankAccount implements Json {
  private final String iban;
  private final String nickname;
  private final TransactionHistory transactions;

  // Constructor...

  public void makePayment(double amount) { /* Implementation... */ }
  // Other public methods...

  @Override
  public InputStream bytes() {
    return new MutableJson()
      .with("iban", iban)
      .with("nickname", nickname)
      .with("balance", transactions.balance(iban))
      .bytes();
  }
}
```

Even simpler way is to extend the `JsonEnvelope`, then you don't even need to implement `bytes()`:

```java
public final class BankAccount extends JsonEnvelope {
  public BankAccount(String iban, String nickname, TransactionHistory transactions) {
    super(new MutableJson()
      .with("iban", iban)
      .with("nickname", nickname)
      .with("balance", transactions.balance(iban))
    );
  }

  public void makePayment(double amount) { /* Implementation... */ }
  // Other public methods...
}
```

We can then make an HTTP response directly, e.g. with [Spring](https://spring.io/):

```java         
return new ResponseEntity<>(
  new SmartJson(
      new BankAccount(iban, nickname, transactions)
  ).byteArray(),
  HttpStatus.OK
);
```

...or with [Takes](https://github.com/yegor256/takes):

```java
return new RsWithType(
  new RsWithStatus(
    new RsWithBody(
      new BankAccount(iban, nickname, transactions).bytes()
      ),
      200
    ),
    "application/json"
);
```

...or insert it in some JSON datastore:

```java
accounts.insert(new BankAccount(iban,nickname));
```

...or compose it within a larger JSON:

```java
Json accounts = new MutableJson()
  .with("name","John")
  .with("surname","Smith")
  .with("account",new BankAccount(iban,nickname));
```

### Additional functionality

If available functionality in the current version of Nereid is not enough, the developer can always fall back to
jackson-databind. Convert `Json` to `ObjectNode`, do what you need with it, and construct a new `Json`.

```java
ObjectNode node = new SmartJson(json).objectNode();
// Do stuff with node using Jackson's API.
Json updated = new Json.Of(node);
```

## How to Contribute

Fork repository, make changes, send us a [pull request](https://www.yegor256.com/2014/04/15/github-guidelines.html).
We will review your changes and apply them to the `master` branch shortly,
provided they don't violate our quality standards. To avoid frustration,
before sending us your pull request please run full Maven build:

```bash
$ mvn clean install
```

You will need Maven 3.8.7+ and Java 17+.

Our [rultor image](https://github.com/eo-cqrs/eo-kafka-rultor-image) for CI/CD.
