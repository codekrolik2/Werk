================================================
The most important thing to know about WerkFlows
a.k.a - how to build bulletproof WerkFlows
================================================


The ABSOLUTELY MOST important thing to always remember is that
	**Job/Step state might fail to be saved in DB.**

At any given moment an instance of Werk can suffer "sudden death": e.g. get killed with -9 flag, suffer from things like network split, etc.
Such problems are not specific to Werk, but are typical to all computational systems.
Those also can be caused by dozens of other reasons, including, but not limited to: power outages or, less typical, some natural disasters.

As a result, instance's pulse record will be lost and all its jobs will be reassigned to other Werk instances.
That means that whatever execution/state change happened on that failed instance and wasn't persisted - will be lost.

Good news is that Werk's SQL engine architecture is such that at most one execution/transition cycle can be lost for a job.
That means there will be no multiple steps or even multiple executions of the same step that one should reason about in regards to the situations described above. 

New instances will by default be oblivious of possible work already (partially) done by the terminated instance, so they will restart the WerkFlow from the last saved point. The only way to make the previous execution context visible is to create a step smart enough to find out about possible previous executions.

For some steps duplicate execution doesn't matter, e.g. for idempotent steps, or steps that only change local state and don't interact with external systems.

For critical steps, however, this possibility should always be taken into consideration, all consequences of double processing studied and ideally some additional measures taken to avoid possible issues.

Such measures might include:
1) Duplicate step processing state information in local storage.
It's hard - Step's logic needs to understand the difference between local storages on different instances and know how to access the correct instance's storage.
It's not 100% safe - Still, the whole machine can suddenly die together with the Werk instance, rendering local storage on that machine unreachable and therefore unusable. 

2) Duplicate step processing state information in another network storage:
Also not the best remedy as additional storage is also prone to the same problems.  

3) Probably the best way - try to find a way to track the action in question in the systems that the step is interacting with.
The main idea is that if some of the systems a step is interacting with are down or unreachable - the step will have to rerun anyway, because it's impossible to execute an action on unreachable system. And if it's reachable, some preconditions might be checked or possible traces analyzed.

E.g.
If some records should be created - check if they already exist.
If some data should be changed - check if the changes are already in place.
If some resources have to be locked - check if lock is already set.

If possible, try to _deliberately_ leave such traces in external systems so that critical processing step can reason about whether it was run by any other instance earlier and what exactly has been accomplished and what needs to be done still, if anything.

Some systems like payment processing system provide tokens for operations for exactly this reason.
If you can use tokenized access, a multi-stage processing can be used.
E.g. 
First - obtain a token, save it to parameters, return ExecutionResult.REDO, this will commit state after execution in DB.
Then even if that persistence will fail, a new token will be generated on reprocessing and the old one will just be lost.
Otherwise - the processing will continue with the generated token, which could be used in various ways as a reference to figure out what's been done so far.

In general, if there is a good way to save some partial processing state in step parameters and return REDO, it might make sense to do it. Sometimes it's too much to encapsulate every single little thing into its own step, but remember that just returning REDO will automatically save your progress and will restart the same step, so you can continue from that persisted checkpoint.
The more often you save state, the easier it usually is to reason about things, provided the need to do so.

Overall, I'm not trying to give a universal recipe to follow, just pointing out the fact that duplicate processing is possible due to failures, and however rare such event might be, it's unavoidable. The best way to tackle it is to use some properties of the processing itself, because a universal recipe doesn't exist, so using deep analysis, knowing features of systems the step interacts with and applying creativity is the best way to make things unbreakable.

In any case, you always have another option...

4) ...which is - cross 3 times and leave it as is.
Maybe it will never happen.