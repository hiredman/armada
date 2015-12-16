# armada

A Clojure library for maintaining group members via gossiping. Nodes
ping each other and include information about one other node that they
know of with their pings. Group members are gossiped via pings, and
failing pings can detect group memebers that disapear. 

## Usage

The code runs as some go-loops and communicates via core.async
channels. The test namespace `com.manigfeald.http` has an example of
wiring it up to http via ring.

## License

Copyright © 2015 Kevin Downey

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
