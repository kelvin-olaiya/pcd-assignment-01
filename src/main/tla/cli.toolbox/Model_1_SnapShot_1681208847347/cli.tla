---- MODULE cli ----
EXTENDS TLC, Integers, Sequences

(*--fair algorithm CLI
variables
    counter = [i \in 1..3 |-> 0];
    batches = << <<>>, <<>> >>;
    mutex = 1;
    
define
NoLostUpdates == <>(counter[1] = 6 /\ counter[2] = 4 /\ counter[3] = 2)
MutualExclusion == []~(pc[1] = "CS" /\ pc[2] = "CS")
end define;


macro signal(m) begin
     m := m + 1
end macro;

macro wait(m) begin
    await m > 0;
    m := m - 1;
end macro;

procedure distribute() begin
    batch_1: batches[1] := <<2, 4, 5, 10, 3, 14>>;
    batch_2: batches[2] := <<3, 5, 1, 7, 9, 4>>;
    distributed:
    return;
end procedure;
    

procedure submit_file(length=-1) 
variable temp = 0;
begin Update:
        if length < 5 then
            temp := counter[1];
            update1: counter[1] := temp + 1;
        elsif length < 10 then
            temp := counter[2];
            update2: counter[2] := temp + 1;
        else
            temp := counter[3];
            update3: counter[3] := temp + 1;
        end if;
        submitted:
        return;
end procedure;

fair process master = 0
begin
    distribution: call distribute();
    finished: skip;
end process; 

fair+ process worker \in { 1, 2 }
variables
    batch = <<>>;
    current_file = 0;
begin Count:
  await batches[self] /= <<>>;
  batch := batches[self];
  loop:
  while batch /= <<>> do
        current_file := Head(batch);
        batch := Tail(batch);
        wait(mutex);
        CS: call submit_file(current_file);
        end_cs: signal(mutex);
  end while;
end process;

end algorithm;*)
\* BEGIN TRANSLATION (chksum(pcal) = "d1004aa1" /\ chksum(tla) = "4fbbd9d9")
VARIABLES counter, batches, mutex, pc, stack

(* define statement *)
NoLostUpdates == <>(counter[1] = 6 /\ counter[2] = 4 /\ counter[3] = 2)
MutualExclusion == []~(pc[1] = "CS" /\ pc[2] = "CS")

VARIABLES length, temp, batch, current_file

vars == << counter, batches, mutex, pc, stack, length, temp, batch, 
           current_file >>

ProcSet == {0} \cup ({ 1, 2 })

Init == (* Global variables *)
        /\ counter = [i \in 1..3 |-> 0]
        /\ batches = << <<>>, <<>> >>
        /\ mutex = 1
        (* Procedure submit_file *)
        /\ length = [ self \in ProcSet |-> -1]
        /\ temp = [ self \in ProcSet |-> 0]
        (* Process worker *)
        /\ batch = [self \in { 1, 2 } |-> <<>>]
        /\ current_file = [self \in { 1, 2 } |-> 0]
        /\ stack = [self \in ProcSet |-> << >>]
        /\ pc = [self \in ProcSet |-> CASE self = 0 -> "distribution"
                                        [] self \in { 1, 2 } -> "Count"]

batch_1(self) == /\ pc[self] = "batch_1"
                 /\ batches' = [batches EXCEPT ![1] = <<2, 4, 5, 10, 3, 14>>]
                 /\ pc' = [pc EXCEPT ![self] = "batch_2"]
                 /\ UNCHANGED << counter, mutex, stack, length, temp, batch, 
                                 current_file >>

batch_2(self) == /\ pc[self] = "batch_2"
                 /\ batches' = [batches EXCEPT ![2] = <<3, 5, 1, 7, 9, 4>>]
                 /\ pc' = [pc EXCEPT ![self] = "distributed"]
                 /\ UNCHANGED << counter, mutex, stack, length, temp, batch, 
                                 current_file >>

distributed(self) == /\ pc[self] = "distributed"
                     /\ pc' = [pc EXCEPT ![self] = Head(stack[self]).pc]
                     /\ stack' = [stack EXCEPT ![self] = Tail(stack[self])]
                     /\ UNCHANGED << counter, batches, mutex, length, temp, 
                                     batch, current_file >>

distribute(self) == batch_1(self) \/ batch_2(self) \/ distributed(self)

Update(self) == /\ pc[self] = "Update"
                /\ IF length[self] < 5
                      THEN /\ temp' = [temp EXCEPT ![self] = counter[1]]
                           /\ pc' = [pc EXCEPT ![self] = "update1"]
                      ELSE /\ IF length[self] < 10
                                 THEN /\ temp' = [temp EXCEPT ![self] = counter[2]]
                                      /\ pc' = [pc EXCEPT ![self] = "update2"]
                                 ELSE /\ temp' = [temp EXCEPT ![self] = counter[3]]
                                      /\ pc' = [pc EXCEPT ![self] = "update3"]
                /\ UNCHANGED << counter, batches, mutex, stack, length, batch, 
                                current_file >>

update1(self) == /\ pc[self] = "update1"
                 /\ counter' = [counter EXCEPT ![1] = temp[self] + 1]
                 /\ pc' = [pc EXCEPT ![self] = "submitted"]
                 /\ UNCHANGED << batches, mutex, stack, length, temp, batch, 
                                 current_file >>

update2(self) == /\ pc[self] = "update2"
                 /\ counter' = [counter EXCEPT ![2] = temp[self] + 1]
                 /\ pc' = [pc EXCEPT ![self] = "submitted"]
                 /\ UNCHANGED << batches, mutex, stack, length, temp, batch, 
                                 current_file >>

update3(self) == /\ pc[self] = "update3"
                 /\ counter' = [counter EXCEPT ![3] = temp[self] + 1]
                 /\ pc' = [pc EXCEPT ![self] = "submitted"]
                 /\ UNCHANGED << batches, mutex, stack, length, temp, batch, 
                                 current_file >>

submitted(self) == /\ pc[self] = "submitted"
                   /\ pc' = [pc EXCEPT ![self] = Head(stack[self]).pc]
                   /\ temp' = [temp EXCEPT ![self] = Head(stack[self]).temp]
                   /\ length' = [length EXCEPT ![self] = Head(stack[self]).length]
                   /\ stack' = [stack EXCEPT ![self] = Tail(stack[self])]
                   /\ UNCHANGED << counter, batches, mutex, batch, 
                                   current_file >>

submit_file(self) == Update(self) \/ update1(self) \/ update2(self)
                        \/ update3(self) \/ submitted(self)

distribution == /\ pc[0] = "distribution"
                /\ stack' = [stack EXCEPT ![0] = << [ procedure |->  "distribute",
                                                      pc        |->  "finished" ] >>
                                                  \o stack[0]]
                /\ pc' = [pc EXCEPT ![0] = "batch_1"]
                /\ UNCHANGED << counter, batches, mutex, length, temp, batch, 
                                current_file >>

finished == /\ pc[0] = "finished"
            /\ TRUE
            /\ pc' = [pc EXCEPT ![0] = "Done"]
            /\ UNCHANGED << counter, batches, mutex, stack, length, temp, 
                            batch, current_file >>

master == distribution \/ finished

Count(self) == /\ pc[self] = "Count"
               /\ batches[self] /= <<>>
               /\ batch' = [batch EXCEPT ![self] = batches[self]]
               /\ pc' = [pc EXCEPT ![self] = "loop"]
               /\ UNCHANGED << counter, batches, mutex, stack, length, temp, 
                               current_file >>

loop(self) == /\ pc[self] = "loop"
              /\ IF batch[self] /= <<>>
                    THEN /\ current_file' = [current_file EXCEPT ![self] = Head(batch[self])]
                         /\ batch' = [batch EXCEPT ![self] = Tail(batch[self])]
                         /\ mutex > 0
                         /\ mutex' = mutex - 1
                         /\ pc' = [pc EXCEPT ![self] = "CS"]
                    ELSE /\ pc' = [pc EXCEPT ![self] = "Done"]
                         /\ UNCHANGED << mutex, batch, current_file >>
              /\ UNCHANGED << counter, batches, stack, length, temp >>

CS(self) == /\ pc[self] = "CS"
            /\ /\ length' = [length EXCEPT ![self] = current_file[self]]
               /\ stack' = [stack EXCEPT ![self] = << [ procedure |->  "submit_file",
                                                        pc        |->  "end_cs",
                                                        temp      |->  temp[self],
                                                        length    |->  length[self] ] >>
                                                    \o stack[self]]
            /\ temp' = [temp EXCEPT ![self] = 0]
            /\ pc' = [pc EXCEPT ![self] = "Update"]
            /\ UNCHANGED << counter, batches, mutex, batch, current_file >>

end_cs(self) == /\ pc[self] = "end_cs"
                /\ mutex' = mutex + 1
                /\ pc' = [pc EXCEPT ![self] = "loop"]
                /\ UNCHANGED << counter, batches, stack, length, temp, batch, 
                                current_file >>

worker(self) == Count(self) \/ loop(self) \/ CS(self) \/ end_cs(self)

(* Allow infinite stuttering to prevent deadlock on termination. *)
Terminating == /\ \A self \in ProcSet: pc[self] = "Done"
               /\ UNCHANGED vars

Next == master
           \/ (\E self \in ProcSet: distribute(self) \/ submit_file(self))
           \/ (\E self \in { 1, 2 }: worker(self))
           \/ Terminating

Spec == /\ Init /\ [][Next]_vars
        /\ WF_vars(Next)
        /\ WF_vars(master) /\ WF_vars(distribute(0))
        /\ \A self \in { 1, 2 } : SF_vars(worker(self)) /\ SF_vars(submit_file(self))

Termination == <>(\A self \in ProcSet: pc[self] = "Done")

\* END TRANSLATION 
====
