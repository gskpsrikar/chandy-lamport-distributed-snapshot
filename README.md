# Distributed Systems Project-1: Chandy-Lamport Global Snapshot Protocol
This project implements a distributed system consisting of n nodes, numbered 0 to n − 1, arranged in a certain topology. The topology and information about other parameters are provided in a configuration file.

- All channels in the system are bidirectional.
- All channels are implemented using SCTP protocol and satisfy FIFO property.
- The socket connections are created at the beginning of the program and will stay intact until the end of the program.

The nodes are setup on the UTD's Computer Science Department cluster.

## Frequently used Linux commands
#### SSH into control node from MobaXterm:
```
ssh sxs210570@csjaws.utdallas.edu
```
(Use PuTTy if you are doing it from Windows)


#### Cloning git repository
```
git clone https://xxx_ACCESS_KEY_HERE_xxx@github.com/gskpsrikar/distributed-systems-project-1.git
```

#### SSH into dcxx machines
```
ssh sxs210570@dc01.utdallas.edu
```

#### "cd" into the project repository in the terminal
*Note: Doing this because the autofill on the csjaws machine is hanging up*

```
cd distributed-systems-project-1
```

## References
- [Cloning Private Repository from Github Using Personal Token](https://www.youtube.com/watch?v=rzgtnS04MXE)
  - Refer this but use 'fine grained access' feature that is provided in GitHub developer settings.
- Socket Programming tutorials
  - [Socket Programming in Java | Client Server Architecture | Java Networking | Edureka](https://www.youtube.com/watch?v=BqBKEXLqdvI&t=686s)