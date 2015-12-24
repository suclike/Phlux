Phlux
=======

Phlux is an Android library which helps to architect applications in a functional way (with immutable data in mind).

It is inspired by Clojure and Facebook Flux.

### Why Phlux?

1. The original Flux architecture has too many boxes and arrows.
All that dispatchers, stores - this is too complex for Android apps.
Phlux leverages the power of the KISS principle.

2. Unlike Flux, we don't have React library for our views.
Creating and supporting a such library requires a great amount of time investment.
Phlux does not require such library.

3. Android has troubles with background task continuation, so the architecture
must be ready for stress conditions.
(wiping out background tasks and static variables is one of such conditions).
Phlux handles lifecycle problems and it does it even better than any MVP/C implementation.

4. Immutable data is a key ingredient in building reliable software.
All data in Phlux is immutable. Unlike MVP/C/VM libraries,
Phlux requires to use immutable data.

5. We can *update* views instead of re-creating them. Sure, this is not as
"functional" as some of us want, but it is a good compromise. Android XML tools
are quite good and I personally don't want to lose them.
Phlux allows to use the current workflow and unlike
Flux, it does not require to hardcode all views.

6. Despite MVP/C libraries reduce the pain of Android lifecycles,
there are still some lifecycles and there is still some pain.
Phlux allows to forget about Android lifecycles even more!

7. Get ready for [Hot Code Swapping](https://www.youtube.com/watch?v=YYin_N6xXxQ&feature=youtu.be&t=37m32s)!

So I decided to implement a simplified version of Flux that
is more Android-friendly and allows to call `view.update(immutableState)`
at the end of all.

`update` Looks like a ViewHolder's method, isn't it?
A ViewHolder does not have many variables in it
(it must not have variables at all if you're doing everything properly).
So how about turning your entire activity into a simple ViewHolder?

### Schema

While Phlux has little amount of boxes and arrows, it still has some. It is nice to know them:

![Data Flow](https://raw.githubusercontent.com/konmik/Phlux/resources/doc/data_flow.png)

This schema represents the Phlux data flow. Whenever an action occurs (be it a background task
completion or an UI event) it applies to *State* and then *View* should automatically
be updated from the new *State* instance.

Phlux automatically manages updates and background task connections.

When the application needs to update a view it should ask Phlux to `apply()` a function to the corresponding state.
Phlux manages all the internal stuff to avoid any data modification. The only variable that gets changed after
the called function is *Phlux Root*.

![Data Model](https://github.com/konmik/Phlux/blob/resources/doc/data_model.png)

This is the Phlux data model. It is extremely simple.

Phlux has root, it is just `Map<String, Scope>`.
Every *Scope* has a state of a corresponding *View*.
Every *Scope* also has a list of current background tasks.

Whenever a process gets wiped out Phlux automatically restores scopes
and relaunches their tasks that did not complete yet.

The entire architecture is so simple (14Kb jar), it is hard to believe that
no one have it implemented like this yet (if you have, then why didn't you release the
damn library so I could just relax and write reliable apps easily?)

### Why functional

Functional programming is a programming style/language that is based on two practicies:

- Code composition by passing references to functons (lambdas) into other functions.
- Immutable data.

First point is out of the current interest.

The second point is unbelievable important for good archivecture and application simplicity.
Every variable we have needs a special treating. We must constantly care about variables to
have values that correspond to other values in the application. We need to keep eye on the
order of variable initialization and changes. Every variable we have adds complexity.
We've called one method and changes are propagating across the application
without strict control. We've passed a reference to an object that has mutable variables
in it and we've got troubles.
In a multi-threading environment this complexity becomes stunning.

But do we really need tons of variables in each application? And, what is a minimun amount
of variables we can afford? How can we architect our apps to get most of the
"minimum variables amout" principle?

A Phlux-driven application can afford to have just two variables.

*"But immutability comes with a price of increased garbage collection!"*

Not so bad, when we need to replace the root application variable we can reference
parts of the previous root variable. What will be GC'ed is only the difference
between two values which is not a problem. Be honest - who cares about reusing
content of previous variables that was allocated for usage in an adapter? No one, we
just drop them. The same is here, but we can do this in a much more reliable way.

`AutoParcel` has a great support of the Builder pattern that allows to easily
create copies of modified data structures. `Solid` allows to do the same with collections
using streams. Java alone is not capable of applying this strategy, but with these two
libraries the job can be done.

### Data requirements

Most data in a Phlux application must implement `Parcelable` interface.
This is because any data can be wiped out by Android and so it must be parceled.

I recommend using
[AutoParcel](https://github.com/frankiesardo/auto-parcel)
and
[Solid](https://github.com/konmik/solid)
libraries because they allow to have both - immutable and parcelable data the same time.

In case if you don't want some data to be parceled (for example you have a large list
of items that you can easily fetch from a database) you may use
[Transient](https://github.com/konmik/Phlux/blob/master/phlux/src/main/java/phlux/Transient.java)
class inside of `AutoParcel` data object.

`Transient` is just a reference, It implements `Parcelable` but it does not put
the referenced object into a parcel during serialization.

### Background tasks

On Android background tasks must save their arguments to be restarted in case of being wiped out.
So they must implement `Parcelable` as well.
[PhluxBackground](https://github.com/konmik/Phlux/blob/master/phlux/src/main/java/phlux/PhluxBackground.java)

Background tasks may have `sticky` property to automatically refresh temporary data when it gets lost during
Android lifecycles.

### Library status

The library status is: "Wow, I can do this!".
I can create an application which has only *one* mutable variable!
(OK, there are *two* mutable variables - one for data and another one for the callback list.)

Overall, I feel that the library has a great potential. It is clearly better than MVP/C libraries.
The library can potentially fit very well into MVVM but I do not care about data binding much).

### TODO

- In some cases we need to call background and UI tasks in a long sequence.
Show a dialog, execute a background task, choose some data from an activity,
show a dialog again, pass some data to server, etc. Things are worse because on Android
we can lost the current state and views easily, our dialogs can be dismissed without a user action, and so on.
This is still a pain, so I think about implementing Interactor pattern on top of Phlux.

- Dagger integration example.

- 100% test coverage, as usual.

- Docs? :D
