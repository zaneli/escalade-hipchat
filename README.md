#escalade-hipchat
[HipChat](https://www.hipchat.com/ "HipChat") API Scala wrapper library.

[![Build Status](https://api.travis-ci.org/zaneli/escalade-hipchat.png?branch=master)](https://travis-ci.org/zaneli/escalade-hipchat)

##Getting Started
* Create an API token. (Refer to [Official API Documentation](https://www.hipchat.com/docs/api "HipChat API Documentation"))
* Instantiate Rooms or Users class.
```
// for HipChat Cloud
val rooms = new com.zaneli.escalade.hipchat.Rooms(<API token>)
val users = new com.zaneli.escalade.hipchat.Users(<API token>)
```
```
// for HipChat Server
val rooms = new com.zaneli.escalade.hipchat.Rooms("http://example.hipchatserver.com", <API token>)
val users = new com.zaneli.escalade.hipchat.Users("http://example.hipchatserver.com", <API token>)
```
* Execute `call` method.
```
rooms.list.call
users.show.call(<user_id>)
```

##About Call Method
* All objects corresponding to each API have a `call` method.
```
val (res, rate) = rooms.list.call
val (res, rate) = users.show.call(<user_id>)
```

* All `call` methods return a pair, first is individual value and second is `escalade.hipchat.model.RateLimit`. (Refer to [Rate Limiting](https://www.hipchat.com/docs/api/rate_limiting "Rate Limiting"))
```
rate.limit     // The number of requests you are allowed per 5 minutes.
rate.remaining // How many requests you can make before hitting the limit.
rate.reset     // The next time (as a Unix timestamp) the limit will be updated.
```

##About Test Method
* All objects corresponding to each API have a `test` method for testing token. (Refer to [Testing a token](https://www.hipchat.com/docs/api/auth "Authentication"))
```
val result = rooms.list.test
val result = users.show.test
```

* All `test` methods return a `scala.util.Try`. If the token is valid, `scala.util.Success` will be returned. Otherwise, `scala.util.Failure` will be returned.
```
result match {
  case Success((res, rate)) => // execute call method
  case Failure(e) => throw e
}
```

##Maven Repository

for Scala 2.10.x and Scala 2.11.x

###pom.xml
    <repositories>
      <repository>
        <id>com.zaneli</id>
        <name>Zaneli Repository</name>
        <url>http://www.zaneli.com/repositories</url>
      </repository>
    </repositories>

    <dependencies>
      <dependency>
        <groupId>com.zaneli</groupId>
        <artifactId>escalade-hipchat_2.11</artifactId>
        <version>0.0.2</version>
      </dependency>
    </dependencies>

###build.sbt
    resolvers += "Zaneli Repository" at "http://www.zaneli.com/repositories"

    libraryDependencies ++= {
      Seq("com.zaneli" %% "escalade-hipchat" % "0.0.2")
    }
