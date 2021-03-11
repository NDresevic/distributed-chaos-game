# Distributed Chaos Game
A distributed system that computes fractal structures by applying the [game of chaos](https://www.youtube.com/watch?v=kbKtFN71Lfs). The example of 
implemented games can be seen [here](https://www.geogebra.org/m/yr2XXPms).

The system allows the user the following:
* Run a calculation for one or more fractals.
* List of active nodes and their activities.
* Get a complete picture or part of a fractal.

The following describes the system architecture, types of nodes in the network, their use, and behavior. 
The user is also presented with commands that are used to interact with the system.
Also, there are more details about communication between nodes in the system, messages, and message sending protocol. 
The system's primary role is to do the job, i.e., drawing fractal images distributed. Also, all nodes in the system are equal, and the system is self-organizing.

<a href="url"><img src="https://github.com/NDresevic/distributed-chaos-game/blob/master/fractal.png" height="350"></a>

## Nodes in the system

There are two types of nodes in the system, bootstrap (only one in the whole system) and servent nodes
which are switched on and off from the network.

### Bootstrap
It is a unique node in the system that serves exclusively for each servent node's first addition to the network and its removal. For the system to function, 
The bootstrap server must be active at all times, and other nodes have the necessary information to contact it (IP address and port).
Communication with the bootstrap server is minimized, and it is not aware of the architecture system.

#### Bootstrap server configuration
To properly run the bootstrap server in the system, it is necessary to provide a configuration file that contains the following parameters:
`bootstrap.ip` IP address of the bootstrap server
<br />`bootstrap.port` port of the bootstrap server

### Servent
Represents the type of nodes in the network that make up the system. Each servant is uniquely described
by its position in the system, IP address, and port. The servant can perform
assigned work and, together with other servants, organizes the system. Also, this type of node communicates with the user through the console.

#### Servent configuration
For proper node ordering in the system, it is necessary to provide a configuration file and id as input parameters of the ServentMain class. Configuration file
should be of the following format:
<br />`ip` IP address where the node will listen
<br />`port` port on which the node will listen
<br />`bootstrap.ip` IP address of the bootstrap server
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

The organization of nodes in the system is consistent at all times. Each node in the system has a uniquely assigned ID based on its position in the network. The network architecture is a set of all nodes in the system numbered from 0 to N - 1, where N represents the total number of nodes. 
Each node has a list of adjacent nodes with which it can communicate directly, i.e., connected with them. In this system, every node has
a connection with the first successor, which is a circular list and additional connections, "skip" connections. More precisely, the N-th node has a connection with (N + 2) ^ I-th (modulo the total number of nodes in the network), where I take the values from the interval [0..N].

### Connecting the node to the network
For the new node to be included in the network, its task is first to contact the bootstrap server. After receiving the bootstrap response, which contains information about the last node in the system, a new node contacts the last node by sending a NEW NODE message. After that, the current last node adds the 
the new node at the end of the system, i.e., assign it an ID that is by one higher than its own and initiates an update of the whole system.

### Starting a new job
The start command with the accompanying data related to a specific job is used to start a new job in the system. The node over which this request was initiated then stops the system from making a new organization of jobs in the system and reorganizes the work done to avoid data loss.

### Organizing jobs
Before allocating nodes within a single job, the system distributes each job the nodes it can use for its calculation. This division is equal to the total number of active jobs and nodes in the system. After that, the nodes are divided within one job; if the number of nodes assigned to a particular job is such that the job cannot be divided into parts where each node has its fractal region, then there are also idle nodes. Nodes that are not idle
perform the job of creating a fractal image. These nodes have a clearly defined fractal ID. Hence, they draw a part of the image.

In addition to the newly established division of jobs and nodes in the system, the system updates, i.e., maintains the previously calculated points with corresponding jobs and fractal IDs if this is not the initial distribution. This is implemented by exchanging data between nodes after a new distribution is obtained.

### Stopping the job
Stopping an active job in the system causes similar events as starting a new one, which leads to reorganization and data exchange. Stop command with a parameter representing the job name leads to temporary concealment of the system and redistribution. The node over which this command was initiated is in charge of restructuring and sending a circular message to all nodes in the system to notify them of the new distribution. Also, nodes delete all calculated points and 
data related to the job being stopped.

### Disconnecting nodes from the network
Disconnecting a node from the system is done with the quit command. The node that leaves the system starts a circular message notifying the other nodes of its shutdown. That message has the role of updating the global state of the system and the list of neighbors. After that, the system is reorganized.

## Commands

The user can issue the following commands to the system:

**`status [X [id]]`**<br />
It shows the state of all started calculations (jobs) - the number of points on each fractal. Appoints for each fractal are how many nodes work on it, the fractal ID, 
and how many points each node has drawn. If X is specified as the job name, then the status is retrieved only for that job. If a job and a fractal ID are specified, 
then the status is retrieved only from the node with that ID.

**`start [X]`**<br />
The calculation for the given job X begins. X is the symbolic name of a job specified in the configuration file. If X is omitted, the user enters the parameters 
for the job on the console.

**`result X [id]`**<br />
Displays the results for the completed calculation for job X. The user can but does not have to specify the result's fractal ID. If left, then the whole job's result is retrieved, otherwise only for that fractal ID. The image is exported as a PNG file named *fractals/jobName_proportion.png* or 
*fractals/jobNamefractalID_proportion.png*.

**`stop X`**<br />
It stops the calculation for job X. The fractal completely disappears from the system, and the nodes are reassigned to other jobs.

**`quit`**<br />
It does neat node disconnection from the system.

## Communication between nodes in the network

### Bootstrap and Servent

Communication between the bootstrap and the servant only happens when servant is turned on and off from the system. Bootstrap can receive and process the following three types of messages:

- `Hail\nipX:portX\n` - a message sent by the server that wants to log in to the system. If the first node in the system, the bootstrap responds with `-1\n-1 \n` letting it know that it was the first to be successfully involved. If this is not the case, the bootstrap responds with a message
ipX: portX \ nipY: portY \ n, which contains the destination information of the first and last node
in the system.

- `New\nipX:portX\n` - message sent by a servant after successful login,
bootstrap then adds it to the list of active servants.

- `Quit\nipX:portX\n` - message sent by the servant who wants to log-off, bootstrap then removes it from its list of active servants.

### Servent and Servent

Communication between the two servants is done via a socket. Each node is uniquely determined by its IP address and the port on which it listens to messages. The message exchanged between these nodes is an extension to *BasicMessage* messages. *BasicMessage* is a Java serialized message that contains the IP address and port of the sender and the recipient, the unique identifier, the path traveled through the network to reach the final recipient, message text, and other attributes.

### Network routing

Messages through the network can be sent directly if the nodes are neighbors or over allowed jumps obtained based on each node's neighbor list. Certain types of messages need to read all the nodes in the system, so they are sent circularly throughout the system. Such
messages are forwarded to the first neighbor until the message bypasses all nodes in the network. Other message types are explicitly intended for one node, and they are routed through the network until
they reach their destination. Due to routing and the system's architecture, the entire path of messages from node A to node B tends to logarithmic depends on the system's total number of nodes. If node A is not directly connected to node B, intermediate nodes through which the message is transmitted are determined by continually selecting the nodes closest to node B, a direct connection to node A, until node B is reached.

## Message types

**NEW NODE**

*Sending a message:* A message sent by a new node after communication with the bootstrap server, i.e., after receiving information about whom to contact. Sends to the current last node in the network
that he wants to join the system.

*Receive message:* Assigns an ID 1 higher than its own to the message's sender and notifies him by sending **WELCOME** messages. It also does system locking.

**WELCOME**

*Send message:* A message sent by a node that has introduced a new node in the system.

*Receive message:* Gets its ID, initializes all the necessary structures to function in
sends an **UPDATE** message to the system and its neighbor along with its new information.

*Additional content:*
<br />`int id` - the id of the node that turned on
<br />`String firstServentIpAddressPort` - the destination to the first node in the network, to the first neighbor node involved

**UPDATE**

*Send message:* Send a circular message started by a node that has joined the system.

*Receive message:* Updates its data on other nodes based on the received data from the message and forwards it to the first neighbor if that message has not yet reached it. It sends a request to release a critical section and a request initiating a new division of labor.

*Additional content:*
<br />`Map <Integer, ServentInfo> nodesMap` - stores information about current nodes in the system, the key of the map is the node ID, and the value is information about it
<br />`Map <Integer, FractalIdJob> serventJobsMap` - information about currently active jobs in the system, the servants working on that job, and their assigned fractal IDs
<br />`List <Job> activeJobs` - list of currently active jobs in the system

**POISON**

It is used to stop the *FifoSendWorker* thread.

**QUIT**

*Send message:* Circularly send a message initiated by the server leaving the system after received the quit command.

*Receive message:* Update neighbor tables and information about a node that has left the system. If there are nodes that have not been notified about leaving the node from the system, the message is forwarded. Otherwise, a request is sent to reorganize the system without a single node and the job done by the node that left the system is sent to the appropriate node.

*Additional content:*
<br />`int quitterId` - the id of the server that left the system
<br />`String jobName` - the name of the job the servant was working on
<br />`String fractalId` - fractal ID for the job he was working on
<br />`List <Point> quitterComputedPoints` - calculated points before leaving the system

**JOB SCHEDULE**

*Send message:* A message sent as a request to reassign a job in the system, for example, after turning on the new node.

*Receive message:* Initiates the entire redistribution of work in the system.

*Additional content:*
<br />`JobScheduleType scheduleType` - the type of event that causes the redistribution of work, can be: *JOB_ADDED, JOB_REMOVED, SERVENT_ADDED, SERVENT_REMOVED*

**JOB EXECUTION**

*Send message:* The message is sent by a node that created a new organization of jobs in the system or a node that did the appropriate labor division.

*Receive message:* Updating data on a new job distribution, sending previously calculated points to the corresponding node if the node worked on a job in the previous distribution. If the node has received more than one fractal ID, it makes an additional work division based on that list and the obtained endpoints and sends them to the appropriate nodes in charge of that job. Otherwise, the node is given a specific job to perform and starts drawing points. It is also sending an *ACK JOB EXECUTION* message, i.e., receipt of *JOB EXECUTION* messages to the node that made the redistribution.

*Additional content:*
<br />`<String> list fractalIds` - list of fractal job IDs and boundary points
<br />`List <Point> startPoints` - endpoints of the fractal or part of the fractal being calculated
<br />`Job job` - the job for which the node is in charge
<br />`int level` - the level of division of fractal IDs, depth of division of one job
<br />`Map <Integer, FractalIdJob> serventJobsMap` - information about currently active jobs in the system, the servants working on that job, and their assigned fractal IDs
<br />`Map <FractalIdJob, FractalIdJob> mappedFractalsJobs` - a structure that represents mappings corresponding to new and old fractal IDs and their jobs
<br />`List <Job> activeJobs` - list of currently active jobs in the system
<br />`JobScheduleType scheduleType` - the type of event that causes the redistribution of work, can be:
*JOB_ADDED, JOB_REMOVED, SERVENT_ADDED, SERVENT_REMOVED*
<br />`int jobSchedulerId` - the id of the node that started the job redistribution

` There are also additional messages: ACK JOB EXECUTION, IDLE, ACK IDLE, COMPUTED POINTS, ASK STATUS, TELL STATUS, ASK JOB FRACTALID RESULT, TELL JOB FRACTALID RESULT, ASK JOB RESULT, TELL JOB RESULT, STOP JOB, REQUEST, REPLY, RELEASE, RELEASE CRITICAL SECTION.`
