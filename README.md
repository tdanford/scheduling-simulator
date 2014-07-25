# Scheduling Simulation

An exercise in building a simulator for cloud environments, so we can experiment with different methods of task scheduling. There are some Spark-specific adapters here since we're aiming specifically for an implementation in that system, but there's nothing Spark-specific about the core simulation.

Current requirements include: 
- framework for writing multiple schedulers
- each component of the system is immutable
- possible to copy the state of the system at any time, to support forward-looking simulation.

## Running a simulation

The tests should all pass when you run 
```
mvn clean install
```

but there's no main method or central point-of-entry yet.
