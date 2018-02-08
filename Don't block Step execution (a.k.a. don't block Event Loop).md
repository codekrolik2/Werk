**Don't block Step execution (a.k.a. don't block Event Loop)**

In the core of Werk is thread pool that operates similarly to Event Loop.
Each thread in the pool gets jobs ready for processing from the Job queue and processes their current steps, just like network event loop receives and processes data from registered connections.
If a Step would block its Execution or Transition processing, effectively that would render a Thread that is processing that Step incapable of processing other Steps.
Provided a sufficient number of such Steps that block for significant periods of time, the whole processing in a given WerkEngine might come to a halt.

On top of that, in SQLWerkEngine - (by default) DB Transaction is created before Step processing starts and commits after it's done. The wait for undefined period of time while executing can cause Transaction to timeout or break for other reasons, like network issues, and result in undefined behavior at execution cycle finalization/commit phase. In this case job/step state updates almost certainly will be lost.
Steps marked as "shortTransaction" will not keep the transaction open during the execution cycle, only creating a transaction after execution is done, updating the states and commiting right away.
However, steps with such annotations can't use a common transactional state which might in some cases cripple the functionality.
But more importantly, other reasons to avoid blocking in Steps as much as possible are as serious.

The best way to eliminate blocking is to understand the actions that Steps perform in their execution and determine what can potentially block for a significant amount of time.

There are a few ways of handling such operations:

1) Setting timeouts for your operations.
Probably the simplest and least efficient way, because wait for timeout will still block Werk engine Threads.
Still better than without timeouts!

2) Execution in a different thread pool.
If some Threads have to be blocked - at least don't block those from WerkEngine Pool.
Move the blocking logic to another thread pool, so that if something blocks and halts, it's not affecting execution of other Jobs/Steps on the server.

Then StepExec can periodically check for completion returning REDO with some delay. In this case actual Werk Thread won't be blocked for the entire execution time, only for brief periods for completion status checks.
Even better option is to use callbacks, by returning CALLBACK (call ExecutionResult.waitForCallback()).

3) Using asynchronous execution capabilities of the system you're interacting with.
The particular approach really depends on a system that your Step is interacting with.

Probably the best real life example of using this approach would be a typical task to execute bash script.
Let's say a Step wants to call "abc.sh", wait for it to finish and then analyze the output from STDOUT.
The possible options are:

3.1) Using nohup for non-blocking execution.
Instead of just calling the script and blocking until it finishes, it's possible to use some construct like "nohup abc.sh > out.out".
That will launch script in background, while a Step can periodically check for completion returning REDO with some delay, e.g. 5 seconds.
Once the script is completed, the output could be analyzed by reading out.out.

3.2) Werk server switch. Local VS Remote execution.
Obviously, that's a bit more sophisticated than just launching the script and blocking, especially so because while waiting for script completion Werk process might die or lose heartbeat, which means another Werk instance, potentially running on a different machine, will acquire the Job.
Therefore, if your step doesn't ssh to some predefined remote machine to run the script there, instead just launching the script on local machine where it's current WerkEngine is executing, it's HIGHLY important to keep in mind that the location of execution might switch to a different machine at any time between step execution cycles due to events that are beyond what's feasible to control in a digital system.
So for any asynchronous _local_ processing it's paramount to understand which _machine_ that local processing takes place at, and later use that information to understand whether that context is local or remote, to avoid issues connected to Werk server switch.
Or just use local-server-agnostic processing on a remote machine (pool of remote machines).

More information on side-effects and possible workarounds of "Sudden death"/"Heartbeat loss" can be found in the document "Most important thing to know about WerkFlows.md"

4) Using an actual EventLoop to asynchronously execute IO (Improved #2)
Interaction with external systems most commonly comes in form of IO (typically network communication).
Such interaction can be implemented using industry-standard non-blocking network frameworks based on Event Loops, like Netty of VertX.
In this case the execution can be taken care of by the Event Loop in more efficient way than by a thread pool (due to non-blocking execution), using much less resources.

A lot of things can be relatively easily implemented on top of EventLoops. For example, in addition to standard Web protocols VertX provides non-blocking network drivers to major databases, like MySQL and Mongo, as well as integration with other technologies like Redis, Kafka, RabbitMQ, gRPC, etc. 

More info on VertX:
http://vertx.io/docs/#web
http://vertx.io/docs/#data_access
http://vertx.io/docs/#integration
http://vertx.io/docs/#services

Similarly to #2, Step can periodically check for completion returning REDO or use CALLBACK.

//--------------------------------------------------------------------------- 

All best practices for programming for event loops can and should be used in implementation of Werk Steps.

E.g.: the text below is an adaptation of excerpt from NodeJs document.
https://nodejs.org/en/docs/guides/dont-block-the-event-loop/ 

//--------------------------------------------------------------------------- 

Werk runs Steps in the WerkTreadPool, and handles Job queue management. Werk scales well, better than more heavyweight approaches. The secret to Werk's scalability is that it uses a small number of threads to handle many Jobs. If Werk can make do with fewer threads, then it can spend more of your system's time and memory working on clients rather than on paying space and time overheads for threads (memory, context-switching). But because Werk has only a few threads, you must structure your Steps to use them wisely.

Here's a good rule of thumb for keeping your Werk server speedy: Werk is fast when the work associated with each client at any given time is "small".

This applies to StepExec and Transitioners.

Why should I avoid blocking the WerkTreadPool?
Werk uses a small number of threads to handle many Jobs/Steps.
If a thread is taking a long time to process a StepExec or Transitioner, we call it "blocked". While a thread is blocked working on behalf of one Step, it cannot handle execution of any other Steps. This provides two motivations for not blocking WerkTreadPool:

Performance: If you regularly perform heavyweight activity on either type of thread, the throughput (step executions/second) of your server will suffer.
Self-DoS: Suboptimally implemented Step could submit this "evil block", make your threads block, and keep them from working on other Steps. This would be a Denial of Service attack.

//--------------------------------------------------------------------------- 

^
Essentially I just replaced Node with Werk in this text, everything else holds true :)
