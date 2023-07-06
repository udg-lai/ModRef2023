include "globals.mzn";

int: nsteps; %number of steps/actions considered
int: dimx; %dimensions of the map/instance considered
int: dimy;
array[1..dimx,1..dimy] of int:mapa; %Initial map/configuration of the instance
int: filav ; %coordinates of avatar
int: colav;                                                           
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 0 -> free
% 1 -> small ball
% 2 -> medium ball
% 3 -> small above medium ball
% 4 -> big ball
% 5 -> small above big ball
% 6 -> medium above big ball
% 7 -> small above medium above big ball
% 8 -> SNOW 
% 9 -> occupied cell
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Variables representing the evolution of the map/instance from 1 to nsteps+1
array[1..nsteps+1,1..dimx,1..dimy] of var 0..9: estats;

% coordinates where the action is executed all along the nsteps 
array[1..nsteps,1..2] of var 2..max(dimx,dimy)-1: action_coordinates; 

% coordinates where the ball will be moved after the action all along the nsteps
array[1..nsteps,1..2] of var 2..max(dimx,dimy)-1: next_coordinates; 

% coordinates where the avatar will be after the action all along the nsteps
array[1..nsteps+1,1..2] of var 2..max(dimx,dimy)-1: avatar_coordinates; 

% coordinates from where the avatar performs the action all along the nsteps
array[1..nsteps,1..2] of var 2..max(dimx,dimy)-1: ante_coordinates; 

% action performed all along the nsteps, 1 Up, 2 Down, 3 Left, 4 Right
array[1..nsteps] of var 1..4: actions;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%55


% Auxiliar variable to draw a path from avatar previous position to ante position
array[1..nsteps,1..dimx,1..dimy] of var 0..2: cami; %0 free, 1 occupied, 2 path



% Inicialization of the  variable for paths
% occupied 
constraint forall(step in 1..nsteps,i in 1..dimx,j in 1..dimy) (estats[step,i,j] in {1,2,3,4,5,6,7,9} -> cami[step,i,j]=1);
% source-target
constraint forall(step in 1..nsteps)
  (cami[step,avatar_coordinates[step,1],avatar_coordinates[step,2]] = 2 /\
  cami[step,ante_coordinates[step,1],ante_coordinates[step,2]] = 2); 



% building the paths...
constraint 
  forall(step in 1..nsteps)
   ((avatar_coordinates[step,1]!=ante_coordinates[step,1] \/
    avatar_coordinates[step,2]!=ante_coordinates[step,2]) 
     ->
     forall(i in 1..dimx,j in 1..dimy) 
      (cami[step,i,j]=2 ->
        (if ((avatar_coordinates[step,1]=i /\ avatar_coordinates[step,2]=j) \/ 
             (ante_coordinates[step,1]=i   /\ ante_coordinates[step,2]=j)) 
             then
              count([cami[step,i-1,j],cami[step,i+1,j],cami[step,i,j-1],cami[step,i,j+1]],2, 1) 
             else
              count([cami[step,i-1,j],cami[step,i+1,j],cami[step,i,j-1],cami[step,i,j+1]],2, 2) 
         endif)
      )
      );




% Inicialization of the first state
constraint avatar_coordinates[1,1]= filav /\ avatar_coordinates[1,2]= colav;
constraint forall(i in 1..dimx,j in 1..dimy) (estats[1,i,j]=mapa[i,j]);

% Fixing the goal for the last state
constraint exists(i in 1..dimx,j in 1..dimy) (estats[nsteps+1,i,j]=7);

% Transitions between states relating action, next  and ante_coordinates 
constraint forall(step in 1..nsteps)
                    (
                      ( 
                       (estats[step,action_coordinates[step,1], action_coordinates[step,2]] = 1 /\ 
                        estats[step,next_coordinates[step,1], next_coordinates[step,2]] in {0,2,4,6,8})
                       \/
                       (estats[step,action_coordinates[step,1], action_coordinates[step,2]] = 2 /\ 
                        estats[step,next_coordinates[step,1], next_coordinates[step,2]] in {0,4,8})
                       \/
                       (estats[step,action_coordinates[step,1], action_coordinates[step,2]] in {3,4,5,6} /\
                        estats[step,next_coordinates[step,1], next_coordinates[step,2]] in {0,8})
                       )
                       );
                       
 constraint forall(step in 1..nsteps)
                      ( 
                      % 1 Up, 2 Down, 3 Left, 4 Right
                       (actions[step]=1 -> (next_coordinates[step,1]= action_coordinates[step,1]-1 /\
                                            next_coordinates[step,2]= action_coordinates[step,2]   /\
                                            ante_coordinates[step,1]= action_coordinates[step,1]+1 /\  
                                            ante_coordinates[step,2]= action_coordinates[step,2] ))
                        /\
                       (actions[step]=2 -> (next_coordinates[step,1]= action_coordinates[step,1]+1 /\  
                                            next_coordinates[step,2]= action_coordinates[step,2]   /\
                                            ante_coordinates[step,1]= action_coordinates[step,1]-1 /\  
                                            ante_coordinates[step,2]= action_coordinates[step,2] ))   
                       /\
                       (actions[step]=3 -> (next_coordinates[step,1]= action_coordinates[step,1]   /\  
                                            next_coordinates[step,2]= action_coordinates[step,2]-1 /\
                                            ante_coordinates[step,1]= action_coordinates[step,1]   /\  
                                            ante_coordinates[step,2]= action_coordinates[step,2]+1 ))   
                       /\
                       (actions[step]=4 -> (next_coordinates[step,1]= action_coordinates[step,1]   /\  
                                            next_coordinates[step,2]= action_coordinates[step,2]+1 /\
                                            ante_coordinates[step,1]= action_coordinates[step,1]   /\  
                                            ante_coordinates[step,2]= action_coordinates[step,2]-1 )) 
                      );
                      
                
% Balls movements         
 constraint forall(step in 1..nsteps) 
                     (    
                      (
                        (estats[step,action_coordinates[step,1], action_coordinates[step,2]] in {1,2,4}/\
                         estats[step,next_coordinates[step,1], next_coordinates[step,2]] in{0,8})  
                         ->
                         (
                            (  estats[step+1,action_coordinates[step,1], action_coordinates[step,2]] = 0 /\ 
                               avatar_coordinates[step+1,1]=action_coordinates[step,1] /\
                               avatar_coordinates[step+1,2]=action_coordinates[step,2] 
                             )
                             /\
                             (
                             estats[step,next_coordinates[step,1], next_coordinates[step,2]] = 0 ->
                             estats[step+1,next_coordinates[step,1], next_coordinates[step,2]] = 
                                estats[step,action_coordinates[step,1], action_coordinates[step,2]]
                             )
                             /\
                             (
                              (estats[step,next_coordinates[step,1], next_coordinates[step,2]] = 8 /\
                              estats[step,action_coordinates[step,1], action_coordinates[step,2]] = 1)
                              ->
                              estats[step+1,next_coordinates[step,1], next_coordinates[step,2]] = 2
                             )
                              /\
                             (
                              (estats[step,next_coordinates[step,1], next_coordinates[step,2]] = 8 /\
                              estats[step,action_coordinates[step,1], action_coordinates[step,2]] = 2)
                              ->
                              estats[step+1,next_coordinates[step,1], next_coordinates[step,2]] = 4
                             )
                             /\
                             (
                              (estats[step,next_coordinates[step,1], next_coordinates[step,2]] = 8 /\
                              estats[step,action_coordinates[step,1], action_coordinates[step,2]] = 4)
                              ->
                              estats[step+1,next_coordinates[step,1], next_coordinates[step,2]] = 4
                             )   
                         
                          )
                        ) % Moving ball withouth nothing behind
                        
                      /\
                      (
                        (estats[step,action_coordinates[step,1], action_coordinates[step,2]] = 1 /\
                         estats[step,next_coordinates[step,1], next_coordinates[step,2]] in {2,4,6})
                         ->
                            (  estats[step+1,action_coordinates[step,1], action_coordinates[step,2]] = 0 /\ 
                               estats[step+1,next_coordinates[step,1], next_coordinates[step,2]] = 
                                estats[step,next_coordinates[step,1], next_coordinates[step,2]]+1 /\ 
                               avatar_coordinates[step+1,1]=action_coordinates[step,1] /\
                               avatar_coordinates[step+1,2]=action_coordinates[step,2])    
                        ) %pushing small ball on top of other balls
                        
                       /\
                       (
                         (estats[step,action_coordinates[step,1], action_coordinates[step,2]] = 2 /\ 
                          estats[step,next_coordinates[step,1], next_coordinates[step,2]] = 4) 
                         ->
                            (  estats[step+1,action_coordinates[step,1], action_coordinates[step,2]] = 0 /\ 
                               estats[step+1,next_coordinates[step,1], next_coordinates[step,2]] = 6 /\ 
                               avatar_coordinates[step+1,1]=action_coordinates[step,1] /\
                               avatar_coordinates[step+1,2]=action_coordinates[step,2])    
                        )  % pushing medium ball on top of big ball
                        
                        /\
                        (
                          (estats[step,action_coordinates[step,1], action_coordinates[step,2]] in {3,5} /\
                           estats[step,next_coordinates[step,1], next_coordinates[step,2]] in {0,8}) 
                          ->
                            (  estats[step+1,action_coordinates[step,1], action_coordinates[step,2]] =
                               estats[step,action_coordinates[step,1], action_coordinates[step,2]]-1 /\  
                               (estats[step,next_coordinates[step,1], next_coordinates[step,2]] =0 ->    
                                estats[step+1,next_coordinates[step,1], next_coordinates[step,2]] = 1) /\
                               (estats[step,next_coordinates[step,1], next_coordinates[step,2]] =8 ->    
                                estats[step+1,next_coordinates[step,1], next_coordinates[step,2]] = 2) /\
                               avatar_coordinates[step+1,1]=ante_coordinates[step,1] /\
                               avatar_coordinates[step+1,2]=ante_coordinates[step,2])    
                         ) % popping small ball
                         
                        /\
                        (
                            (estats[step,action_coordinates[step,1], action_coordinates[step,2]] =6 /\
                             estats[step,next_coordinates[step,1], next_coordinates[step,2]] in {0,8}) 
                            ->
                             (  estats[step+1,action_coordinates[step,1], action_coordinates[step,2]] = 4 /\ 
                               (estats[step,next_coordinates[step,1], next_coordinates[step,2]] =0 ->   
                                estats[step+1,next_coordinates[step,1], next_coordinates[step,2]] = 2) /\
                               (estats[step,next_coordinates[step,1], next_coordinates[step,2]] =8 ->   
                                estats[step+1,next_coordinates[step,1], next_coordinates[step,2]] = 4) /\
                                avatar_coordinates[step+1,1]=ante_coordinates[step,1] /\
                                avatar_coordinates[step+1,2]=ante_coordinates[step,2])   
                         )% popping medium ball
    
                      );

% Locations not involved in the actions remain unchanged
constraint forall(step in 1..nsteps)
                     (forall(i in 1..dimx,j in 1..dimy)( 
                          ( ( i!=action_coordinates[step,1]\/ j!=action_coordinates[step,2] ) /\ 
                            ( i!=next_coordinates[step,1]\/ j!=next_coordinates[step,2] ))
                           -> estats[step+1,i,j]=estats[step,i,j])
                      ); 
                      
% Avatar is always in positions without walls or balls
constraint forall(step in 1..nsteps)(estats[step,ante_coordinates[step,1],ante_coordinates[step,2]] in {0,8});


% Walls remain untouched
constraint forall(step in 1..nsteps-1,i in 1..dimx,j in 1..dimy)(estats[step,i,j] = 9 -> estats[step+1,i,j]=9);


% Implied constraint about number of balls
constraint forall(step in 1..nsteps)(
  global_cardinality_low_up([estats[step,i,j]| i in 1..dimx, j in 1..dimy],[1,2,3,4,5,6,7],[0,0,0,0,0,0,0],[3,2,1,1,1,1,1]));
          

% Search strategy ordered by states
solve :: seq_search([                   
    int_search([actions[s],action_coordinates[s,1],action_coordinates[s,2]],input_order,indomain_min) |s in 1..nsteps] 
    ) satisfy;
    
output [show(estats[s,i,j])++if (j=dimy)then"\n" else " " endif++ if (i=dimx /\ j=dimy)then "---------------\n" else "" endif| s in 1..nsteps+1, i in 1..dimx,j in 1..dimy]


