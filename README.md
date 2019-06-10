frdomain-extras.kt
========

Arrow port of the additional [Functional and Reactive Domain Modeling](https://www.manning.com/books/functional-and-reactive-domain-modeling) code samples (Chapter 6). You can find the Scala version [here](https://github.com/debasishg/frdomain-extras).

This project contains two implementations:

- Implementation specialized with [Arrow's IO][arrow-io] effect
- [Taggess final][tagless] approach implemented with:
    - Arrow's IO
    - [Single][rx-single] from RxJava2[rxjava2]

From the book page:

> Functional and Reactive Domain Modeling teaches you how to think of the domain model in terms of pure functions and how to compose them to build larger abstractions. You will start with the basics of functional programming and gradually progress to the advanced concepts and patterns that you need to know to implement complex domain models. The book demonstrates how advanced FP patterns like algebraic data types, typeclass based design, and isolation of side-effects can make your model compose for readability and verifiability.  On the subject of reactive modeling, the book focuses on higher order concurrency patterns like actors and futures. It uses the Akka framework as the reference implementation and demonstrates how advanced architectural patterns like event sourcing and CQRS can be put to great use in implementing scalable models. You will learn techniques that are radically different from the standard RDBMS based applications that are based on mutation of records. You'll also pick up important patterns like using asynchronous messaging for interaction based on non blocking concurrency and model persistence, which delivers the speed of in-memory processing along with suitable guarantees of reliability.

[arrow-io]: https://arrow-kt.io/docs/effects/io/
[tagless]: http://okmij.org/ftp/tagless-final/index.html
[rx-single]: http://reactivex.io/documentation/single.html
[rxjava2]: https://github.com/ReactiveX/RxJava

