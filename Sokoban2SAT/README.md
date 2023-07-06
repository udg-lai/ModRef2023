# Java programs translating a Sokoban instance to CNF

- `sokoban2sat_forbiddenPositions_cnf.java`

    Character and box movements, sequential plan.
    
- `sokoban2sat_forbiddenPositions_reachability_counting_cnf.java`

    Only box movements, reachability _path encoding_, sequential plan.
    
- `sokoban2sat_forbiddenPositions_reachability_counting_cnf_descending.java`

    Only box movements, reachability _path encoding_, sequential plan, with `noop` actions (for descending approach).
    
- `sokoban2sat_forbiddenPositions_reachability_counting_cnf_parallel.java`

    Only box movements, reachability _path encoding_, parallel plan.

- `sokoban2sat_forbiddenPositions_reachability_ordering_cnf_parallel.java`

    Only box movements, reachability _DAG encoding_, parallel plan.

- `sokoban2sat_forbiddenPositions_reachability_tree_cnf_parallel.java`

    Only box movements, reachability _spanning tree encoding_, parallel plan.
    
