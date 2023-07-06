
The three directories contain the three models used in MiniZinc with their corresponding instances:
- basic corresponds to the step-by-step approach
- reachability_DAG to the reachability approach considering the path constraint from MiniZinc implementing the DAG encoding
- reachability_PATH corresponds to the reachability approach considering the path encoding

All directories have three Minizinc models to use, with one, two or three snowmen to build.
Models for just one snowman are commented.
NOTICE that instances DO NOT CONTAIN the nsteps variable initialized!!! therefore, one needs to iteratively solve the desired instance by increasing the number of steps (parameter nstesps in the instance file) until a solution is found.
