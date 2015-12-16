# armada

A Clojure library for maintaining group members via gossiping. Nodes
ping each other and include information about one other node that they
know of with their pings. Group members are gossiped via pings, and
failing pings can detect group memebers that disapear. 

## Usage

The code runs as some go-loops and communicates via core.async
channels. The test namespace `com.manigfeald.http` has an example of
wiring it up to http via ring.

## Performance


Here is a plot of node counts and milliseconds till all the nodes are
aware of all the others:

![consensus plot](/plots/consensus.png?raw=true)

Here is a plot of node counts and milliseconds till the first node and
the last node are aware of each other:

![line plot](/plots/line.png?raw=true)

This information can be thought of as a lower bound on the runtime,
any real world use over the network will likely take long and perform
worse.

## License

Copyright © 2015 Kevin Downey

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
