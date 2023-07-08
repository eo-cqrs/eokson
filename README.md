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

EOkson - Elegant, Object-Oriented JSON Manipulations.

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

you can create it from string:
```java
Json json = new JsonOf("{\"chair\": \"Herman Miller Aeron\"}");
```

also, you can create JSON with Jackson's `JsonNode`:
```java
JsonNode node = new ObjectMapper().readTree("{\"chair\": \"Herman Miller Aeron\"}");
json = new JsonOf(node);
```

or with file:
```java
Json json = new JsonOf(path to file);
```

### Jocument, smart JSON document

Textual representation:
```java
String textual = new Jocument(json).textual();
```

Pretty textual representation:
```java
String pretty = new Jocument(json).pretty();
```

Represent JSON as an array of bytes:
````java
byte[] bytes = new Jocument(json).byteArray();
````

```json
{
  "chair": "Herman Miller Aeron"
}
```

Get JSON field value:
````java
String leaf = new Jocument(json).leaf("chair");
````
Output: Herman Miller Aeron.

Get a nested JSON:

```json
{
  "amazon": {
    "shop": {
        "books": [
          {
            "name": "Code Complete",
            "price": 30
          },
          {
            "name": "PMP exam prep.",
            "price": 60
          }
        ]
      }
    }
  }
```

```java
Jocument nested = Jocument(json).at("/amazon/shop/books/0");
```

The result will be:
```json
{
  "name": "Code Complete",
  "price": 30
}
```

Back to jackson-databind:
```java
ObjectNode node = new Jocument(json).objectNode();
Json updated = new JsonOf(node);
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
System.out.println(new Jocument(json).pretty());
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

Creating JSON with a nested array:

```java
new Jocument(
  new MutableJson()
        .with(
          "amazon", new MutableJson()
            .with(
              "shop",
              new MutableJson()
                .with(
                  "books",
                  List.of(
                    new MutableJson()
                      .with("name", "Code Complete")
                      .with("price", 30),
                    new MutableJson()
                      .with("name", "PMP exam prep.")
                      .with("price", 60)
                  )
                )
            )
        )
).pretty();
```

It will print you following JSON document:
```json
{
  "amazon": {
    "shop": {
      "books": [
        {
          "name": "Code Complete",
          "price": 30
        },
        {
          "name": "PMP exam prep.",
          "price": 60
        }
      ]
    }
  }
}
```

### Implementing Json

You can implement your own JSON model
using [Json](https://github.com/eo-cqrs/eokson/blob/master/src/main/java/io/github/eocqrs/eokson/Json.java) interface:

```java
public final class BankAccount implements Json {
  private final String iban;
  private final String nickname;
  private final TransactionHistory transactions;

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

Or extending the
[JsonEnvelope](https://github.com/eo-cqrs/eokson/blob/master/src/main/java/io/github/eocqrs/eokson/JsonEnvelope.java):

```java
public final class BankAccount extends JsonEnvelope {
  public BankAccount(String iban, String nickname, TransactionHistory transactions) {
    super(new MutableJson()
      .with("iban", iban)
      .with("nickname", nickname)
      .with("balance", transactions.balance(iban))
    );
  }
}
```

### Integration with RESTful APIs

Here is the example of `eokson` usage with [Spring Framework](https://spring.io/):

```java         
return new ResponseEntity<>(
  new Jocument(
      new BankAccount(iban, nickname, transactions)
  ).byteArray(),
  HttpStatus.OK
);
```

and using [Takes](https://takes.org):

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

Also, you can insert it in some JSON datastore:

```java
accounts.insert(new BankAccount(iban,nickname));
```

### JSON to XML

You can easily transform JSON to XML using [JsonXML](https://github.com/eo-cqrs/eokson/blob/master/src/main/java/io/github/eocqrs/eokson/JsonXML.java):

```json
{
  "test": "true",
  "simple": "true",
  "project": "eokson-0.3.2"
}
```

```java
final String xml = new JsonXML(new JsonOf(json), "root").asString();
```

here is XML output:

```xml
<?xml version='1.0' encoding='UTF-8'?>
<root>
  <test>true</test>
  <simple>true</simple>
  <project>eokson-0.3.2</project>
</root>
```

Also, you can integrate `eokson` with [jcabi-xml](https://github.com/jcabi/jcabi-xml):

```java
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XML;

final XML xml = new XMLDocument(
  new JsonXML(
    new JsonOf(
      json
    ),
    "test"
  ).asString()
);
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
