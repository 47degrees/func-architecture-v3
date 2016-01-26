autoscale: true
build-lists: true

---

# Functional Programming Patterns v3 #


(for the pragmatic programmer)

~

@raulraja CTO @47deg

---

## Acknowledgment ##

- Cats : Functional programming in Scala
- Rúnar Bjarnason : Compositional Application Architecture With Reasonably Priced Monads
- Noel Markham : A purely functional approach to building large applications
- Wouter Swierstra : FUNCTIONAL PEARL Data types a la carte
- Rapture : Jon Pretty

---

All meaningful architectural patterns can be achieved with pure FP

---

When I build an app I want it to be

- Free of Interpretation
- Composable pieces
- Dependency Injection / IOC
- Fault Tolerance

---

When I build an app I want it to be

- Free of Interpretation : **Free Monads** 
- Composable pieces : **Coproducts** 
- Dependency Injection / IOC : **Implicits & Kleisli** 
- Fault tolerant : **Dependently typed checked exceptions** 

---

## Interpretation : Free Monads ##

What is a Free Monad?

-- A monad on a custom algebra that can be run through an Interpreter

---

## Interpretation : Free Monads ##

What is an Application?

-- A collection of algebras and the Coproduct resulting from their interaction

---

## Interpretation : Free Monads ##

Let's build an app that reads a contact and performs some operations with it

---

## Interpretation : Free Monads ##

A very simple model

```scala
scala> case class Contact(
     |       firstName: String,
     |       lastName: String,
     |       phoneNumber: String)
defined class Contact
```

---

## Interpretation : Free Monads ##

Our first Algebra is interaction with a user

```scala
scala> sealed trait Interact[A]
defined trait Interact

scala> case class Ask(prompt: String) extends Interact[String]
defined class Ask

scala> case class Tell(msg: String) extends Interact[Unit]
defined class Tell
```

---

## Interpretation : Free Monads ##

Our second Algebra is about persistence

```scala
scala> sealed trait DataOp[A]
defined trait DataOp

scala> case class AddContact(a: Contact) extends DataOp[Unit]
defined class AddContact

scala> case class GetAllContacts() extends DataOp[List[Contact]]
defined class GetAllContacts
```

---

## Interpretation : Free Monads ##

**An application is the Coproduct of its algebras**

```scala
scala> import cats.data.Coproduct
import cats.data.Coproduct

scala> type AgendaApp[A] = Coproduct[DataOp, Interact, A]
defined type alias AgendaApp
```

---

## Interpretation : Free Monads ##

**We can now lift different algebras to our App monad and compose them**

```scala
scala> import cats.free.{Inject, Free}
import cats.free.{Inject, Free}

scala> class Interacts[F[_]](implicit I: Inject[Interact, F]) {
     | 
     |   def tell(msg: String): Free[F, Unit] = Free.inject[Interact, F](Tell(msg))
     | 
     |   def ask(prompt: String): Free[F, String] = Free.inject[Interact, F](Ask(prompt))
     | 
     | }
<console>:18: warning: higher-kinded type should be enabled
by making the implicit value scala.language.higherKinds visible.
This can be achieved by adding the import clause 'import scala.language.higherKinds'
or by setting the compiler option -language:higherKinds.
See the Scala docs for value scala.language.higherKinds for a discussion
why the feature should be explicitly enabled.
       class Interacts[F[_]](implicit I: Inject[Interact, F]) {
                       ^
defined class Interacts

scala> object Interacts {
     | 
     |   implicit def interacts[F[_]](implicit I: Inject[Interact, F]): Interacts[F] = new Interacts[F]
     | 
     | }
<console>:21: warning: higher-kinded type should be enabled
by making the implicit value scala.language.higherKinds visible.
This can be achieved by adding the import clause 'import scala.language.higherKinds'
or by setting the compiler option -language:higherKinds.
See the Scala docs for value scala.language.higherKinds for a discussion
why the feature should be explicitly enabled.
         implicit def interacts[F[_]](implicit I: Inject[Interact, F]): Interacts[F] = new Interacts[F]
                                ^
defined object Interacts
warning: previously defined class Interacts is not a companion to object Interacts.
Companions must be defined together; you may wish to use :paste mode for this.
```

---

## Interpretation : Free Monads ##

**We can now lift different algebras to our App monad and compose them**

```scala
scala> class DataSource[F[_]](implicit I: Inject[DataOp, F]) {
     | 
     |   def addContact(a: Contact): Free[F, Unit] = Free.inject[DataOp, F](AddContact(a))
     |     
     |   def getAllContacts: Free[F, List[Contact]] = Free.inject[DataOp, F](GetAllContacts())
     | 
     | } 
<console>:20: warning: higher-kinded type should be enabled
by making the implicit value scala.language.higherKinds visible.
This can be achieved by adding the import clause 'import scala.language.higherKinds'
or by setting the compiler option -language:higherKinds.
See the Scala docs for value scala.language.higherKinds for a discussion
why the feature should be explicitly enabled.
       class DataSource[F[_]](implicit I: Inject[DataOp, F]) {
                        ^
defined class DataSource

scala> object DataSource {
     | 
     |   implicit def dataSource[F[_]](implicit I: Inject[DataOp, F]): DataSource[F] = new DataSource[F]
     | 
     | }
<console>:23: warning: higher-kinded type should be enabled
by making the implicit value scala.language.higherKinds visible.
This can be achieved by adding the import clause 'import scala.language.higherKinds'
or by setting the compiler option -language:higherKinds.
See the Scala docs for value scala.language.higherKinds for a discussion
why the feature should be explicitly enabled.
         implicit def dataSource[F[_]](implicit I: Inject[DataOp, F]): DataSource[F] = new DataSource[F]
                                 ^
defined object DataSource
warning: previously defined class DataSource is not a companion to object DataSource.
Companions must be defined together; you may wish to use :paste mode for this.
```

---

## Interpretation : Free Monads ##

At this point a program is nothing but **Data**
describing the sequence of execution but **FREE** 
of its runtime interpretation.

```scala
scala> def program(implicit I : Interacts[AgendaApp], D : DataSource[AgendaApp]) = { 
     | 
     |   import I._, D._
     | 
     |   for {
     |    firstName <- ask("First Name:")
     |    lastName <- ask("Last Name:")
     |    phoneNumber <- ask("Phone Number:")
     |    _ <- addContact(Contact(firstName, lastName, phoneNumber))
     |    contacts <- getAllContacts
     |    _ <- tell(contacts.toString)
     |   } yield ()
     | }
program: (implicit I: Interacts[AgendaApp], implicit D: DataSource[AgendaApp])cats.free.Free[AgendaApp,Unit]
```

---

## Interpretation : Free Monads ##

We isolate interpretations 
via Natural transformations AKA `Interpreters`.

In other words with map over 
the outer type constructor of our Algebras

```scala
scala> import cats.{~>, Id}
import cats.{$tilde$greater, Id}

scala> object ConsoleContactReader extends (Interact ~> Id) {
     |   def apply[A](i: Interact[A]) = i match {
     |     case Ask(prompt) =>
     |       println(prompt)
     |       scala.io.StdIn.readLine()
     |     case Tell(msg) =>
     |       println(msg)
     |   }
     | }
defined object ConsoleContactReader
```

---

## Interpretation : Free Monads ##

We isolate interpretations 
via Natural transformations AKA `Interpreters`.

In other words with map over 
the outer type constructor of our Algebras

```scala
scala> import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ListBuffer

scala> object InMemoryDatasourceInterpreter extends (DataOp ~> Id) {
     |   
     |   private[this] val memDataSet = new ListBuffer[Contact]
     |    
     |   override def apply[A](fa: DataOp[A]) = fa match {
     |     case AddContact(a) => memDataSet.append(a); ()
     |     case GetAllContacts() => memDataSet.toList
     |   }
     | }
defined object InMemoryDatasourceInterpreter
```

---

## Interpretation : Free Monads ##

Now that we have a way to combine interpreters 
we can lift them to the app Coproduct

```scala
scala> val interpreters: AgendaApp ~> Id = InMemoryDatasourceInterpreter or ConsoleContactReader
interpreters: cats.~>[AgendaApp,cats.Id] = cats.arrow.NaturalTransformation$$anon$2@3c4dd725
```

---

## Interpretation : Free Monads ##

And we can finally apply our program applying the interpreter to the free monad

```scala
import Interacts._, DataSource._

val evaled = program foldMap interpreters
```

---

## Composability ##

Composition gives us the power
to easily mix simple functions
to achieve more complex workflows.

---

## Composability ##

We can achieve monadic function composition
with **Kleisli Arrows**

`A ⇒ M[B]`

In other words a function that
for a given input it returns a type constructor…

`List[B], Option[B], Either[B], Task[B], Future[B]…`

---

## Composability ##

When the type constructor `M[_]` it's a Monad it can be monadically composed

```scala
val composed = for {
  a <- Kleisli((x : String) ⇒ Option(x.toInt + 1))
  b <- Kleisli((x : String) ⇒ Option(x.toInt * 2))
} yield a + b
```

---

## Composability ##

The deferred injection of the input parameter enables
**Dependency Injection**. This is an alternative to implicits
commonly known as DI with the Reader monad.

```scala
val composed = for {
  a <- Kleisli((x : String) ⇒ Option(x.toInt + 1))
  b <- Kleisli((x : String) ⇒ Option(x.toInt * 2))
} yield a + b

composed.run("1")
```

---

## Requirements ##

- **Free of Interpretation**
- **Composable pieces**
- **Dependency Injection / IOC**
- Fault Tolerance

---

## Fault Tolerance ##

Most containers and patterns generalize to the 
most common super-type or simply `Throwable` loosing type information.

```scala
val f = scala.concurrent.Future.failed(new NumberFormatException)
val t = scala.util.Try(throw new NumberFormatException)
val d = for {
 a <- 1.right[NumberFormatException]
 b <- (new RuntimeException).left[Int]
} yield a + b
```

---

## Fault Tolerance ##

We don't have to settle for `Throwable`!!!

We could use instead…

- Nested disjunctions
- Delimited, Monadic, Dependently-typed, Accumulating Checked Exceptions

---

## Fault Tolerance : Dependently-typed Acc Exceptions ##

Introducing `rapture.core.Result` 

---

## Fault Tolerance : Dependently-typed Acc Exceptions ##

`Result` is similar to `\/` but has 3 possible outcomes

(Answer, Errata, Unforeseen)

```scala
val op = for {
  a <- Result.catching[NumberFormatException]("1".toInt)
  b <- Result.errata[Int, IllegalArgumentException](
           new IllegalArgumentException("expected"))
} yield a + b
```

---

## Fault Tolerance : Dependently-typed Acc Exceptions ##

`Result` uses dependently typed monadic exception accumulation

```scala
val op = for {
  a <- Result.catching[NumberFormatException]("1".toInt)
  b <- Result.errata[Int, IllegalArgumentException](
            new IllegalArgumentException("expected"))
} yield a + b
```

---

## Fault Tolerance : Dependently-typed Acc Exceptions ##

You may recover by `resolving` errors to an `Answer`.

```scala
op resolve (
    each[IllegalArgumentException](_ ⇒ 0),
    each[NumberFormatException](_ ⇒ 0),
    each[IndexOutOfBoundsException](_ ⇒ 0))
```

---

## Fault Tolerance : Dependently-typed Acc Exceptions ##

Or `reconcile` exceptions into a new custom one.

```scala
case class MyCustomException(e : Exception) extends Exception(e.getMessage)

op reconcile (
    each[IllegalArgumentException](MyCustomException(_)),
    each[NumberFormatException](MyCustomException(_)),
    each[IndexOutOfBoundsException](MyCustomException(_)))
```

---

## Recap ##

- **Free Monads** : Free of Interpretation
- **Coproducts** : Composable pieces
- **Implicits & Kleisli** : Dependency Injection / IOC
- **Dependently typed checked exceptions** Fault tolerant

---

## What's next? ##

If you want to sequence or comprehend over unrelated monads you need Transformers.

Transformers are supermonads that help you flatten through nested monads such as
Future[Option] or Kleisli[Task[Disjuntion]] binding to the most inner value.

http://www.47deg.com/blog/fp-for-the-average-joe-part-2-scalaz-monad-transformers

---

## Questions? & Thanks! ##

@raulraja
@47deg
http://github.com/47deg/func-architecture-v3
https://speakerdeck.com/raulraja/functional-programming-patterns-v3

---

