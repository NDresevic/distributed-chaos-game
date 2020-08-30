# distributed-chaos-game
A distributed system that computes fractal structures by applying the [game of chaos](https://www.youtube.com/watch?v=kbKtFN71Lfs). The example of 
implemented games can be seen [here](https://www.geogebra.org/m/yr2XXPms).

The system allows the user the following:
* Run a calculation for one or more fractals.
* List of active nodes and their activities.
* Get a complete picture or part of a fractal.

In following is described the system architecture, types of nodes in the network, their use and behavior. 
The user is also presented with commands that are used to interact with the system.
Also, the way of communication between system nodes, messages and message sending protocol. 
The main role of the system is to do the job, ie. drawing fractal images that take place
distributed. Also, all nodes in the system are equal and the system is self-organizing.

<a href="url"><img src="https://github.com/NDresevic/distributed-chaos-game/blob/master/fractal.png" height="350"></a>

## Nodes in the system

There are two types of nodes in the system, bootstrap (only one in the whole system) and servent nodes
which are switched on and off from the network).

### Bootstrap
It is a unique node in the system that serves exclusively for the first addition of each servent node to the network and it's removal. For the system to function, 
it is necessary for the bootstrap server to be active
at all times and that other nodes have the necessary information to contact it (IP address and port).
Communication with the bootstrap server is minimized and it is not aware of the architecture system.

#### Bootstrap server configuration
To properly run the bootstrap server in the system, it is necessary to provide a configuration file that contains the following parameters:
`bootstrap.ip` ip address of the bootstrap server
<br />`bootstrap.port` port of the bootstrap server

### Servent
Represents the type of nodes in the network that make up the system. Each servant is uniquely described
by its position in the system, IP address and port. The servant has the ability to perform
assigned work and together with other servants organizes the system. Also, this type of node communicates with the user through the console.

#### Servent configuration
For proper node ordering in the system, it is necessary to provide a configuration file and id as input parameters of the ServentMain class. Configuration file
should be of the following format:
<br />`ip` ip address where the node will listen
<br />`port` port on which the node will listen
<br />`bootstrap.ip` ip address of the bootstrap server
<br />`bootstrap.port` port of the bootstrap server
<br />`weak_failure_limit` weak failure limit
<br />`strong_failure_limit` strong failure limit
<br />`job_count` number of predefined jobs

Each job has the following attributes:
<br />`job[index].name` name, unique symbolic name for the job
<br />`job[index].points.count` number of points of fractal structure. (int, 3 <= N <= 10)
<br />`job[index].proportion` distance between the current point and the destination at which a new dot appears (double in the range 0-1)
<br />`job[index].width` image width ie. area on which points are calculated (int)
<br />`job[index].height` image height ie. area on which points are calculated (int)
<br />`job[index].points.coordinates` set of coordinates of points in the format (x, y) separated with the sign “;”

## System architecture

The organization of nodes in the system is consistent at all times. Each node in the system has a uniquely assigned ID based on which its position in the 
network is known. The network architecture is a set of all nodes in the system numbered from 0 to N - 1, where N represents the total number of nodes. 
Each node has a list of adjacent nodes with which it can communicate directly, ie. it is connected with them. In this system, every node has
connection with the first successor, which is a circular list and additional connections, "skip" connections. More precisely, the N-th node has a connection 
with (N + 2) ^ I-th (modulo the total number of nodes in the network), where I takes the values from the interval [0..N].

### Connecting the node to the network
In order for the new node to be included in the network, its task is to first contact the bootstrap server. After receiving the response from bootstrap, which 
contains information about the last node in the system, a new node contacts the last node by sending a NEW NODE message. After that, the current last node adds the 
new node at the end of the system, ie. assigns it an ID that us by 1 higher than its own and initiates an update of the whole system.

### Starting a new job
The start command with the accompanying data related to a specific job is used to start new job in the system. The node over which this request was 
initiated then stops the system in order to make a new organization of jobs in the system and reorganizes the work done so far in order to avoid data loss.

### Organizing jobs
Before allocating nodes within a single job, the system distributes to each job the nodes it can used for its calculation. This division is equal to what the 
total number of active jobs and nodes in the system allows. After that, the nodes are divided within one job, if the number of nodes assigned to a particular 
job is such that the job cannot be divided into parts where each node has its own fractal region, then there are also idle nodes. Nodes that are not idle
perform the job of creating a fractal image. These nodes have a clearly defined fractal ID, hence, they draw a part of the image.

In addition to the newly established division of jobs and nodes in the system, if this is not initial distribution then the system updates, ie. maintains 
the previously calculated points with corresponding jobs and fractal IDs. This is implemented by exchanging data between nodes after a new distribution is obtained.

### Stopping the job
Stopping an active job in the system causes similar events as starting a new one, ie. leads to reorganization and data exchange. Stop command with parameter which
represents the job name leads to a temporary concealment of the system and redistribution. Node over which this command was initiated is in charge of 
restructuring and sending a circular message to all nodes in the system to notify them of the new distribution. Also, nodes delete all calculated points and 
data related to the job being stopped.

### Disconnecting nodes from the network
Disconnecting a node from the system is done with the quit command. The node that leaves the system starts a circular message notifying the other nodes of 
its shutdown. That message has the role of updating the global state of the system and the list of neighbors. After that, system is reorganized.

## Commands

The user can issue the following commands to the system:

**`status [X [id]]`**<br />
Shows the state of all started calculations (jobs) - the number of points on each fractal. Appoints for each fractal how many nodes work on it, the fractal ID, 
and how many points has each node drew. If X is specified as the job name, then the status is retrieved only for that job. If a job and a fractal ID are specified, 
then the status is retrieved only from the node with that ID.

**`start [X]`**<br />
The calculation for the given job X begins. X is the symbolic name of a job specified in configuration file. If X is omitted, the user enters the parameters 
for the job on the console.

**`result X [id]`**<br />
Displays the results for the completed calculation for job X. The user can, but does not have to specify the fractal ID for the result. If left, then the result 
for the whole job is retrieved, otherwise only for that fractal ID. The image is exported as a PNG file named *fractals/jobName_proportion.png* or 
*fractals/jobNamefractalID_proportion.png*.

**`stop X`**<br />
It stops the calculation for job X. The fractal completely disappears from the system, and the nodes are reassigned to other jobs.

**`quit`**<br />
Neat node disconnection from the system.
