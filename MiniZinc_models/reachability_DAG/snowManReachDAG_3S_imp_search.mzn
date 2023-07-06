include "globals.mzn";

int: nsteps;
int: dimx;
int: dimy;
array[1..dimx,1..dimy] of int:mapa;
int: filav ;
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


int: N = dimx*dimy;


int: E = (dimx-1)*dimy + dimx*(dimy-1);


array [1..E] of int: from;
array [1..E] of int: to;



array[1..nsteps+1,1..dimx,1..dimy] of var 0..9: estats;


array[1..nsteps,1..2] of var 2..max(dimx,dimy)-1: action_coordinates; 

array[1..nsteps,1..2] of var 2..max(dimx,dimy)-1: next_coordinates; 

array[1..nsteps+1,1..2] of var 2..max(dimx,dimy)-1: avatar_coordinates; 

array[1..nsteps,1..2] of var 2..max(dimx,dimy)-1: ante_coordinates; 

array[1..nsteps] of var 1..4: actions;




array[1..nsteps,1..dimx*dimy] of var bool: nodes; 
array[1..nsteps,1..(dimx-1)*dimy+dimx*(dimy-1)] of var bool: arestes; 



constraint forall(step in 1..nsteps,i in 1..dimx,j in 1..dimy) 
              (estats[step,i,j] in {1,2,3,4,5,6,7,9} 
                -> 
                nodes[step,(i-1)*dimy+j]=false);





constraint 
  forall(step in 1..nsteps)
   ( path(from,
                     to,
                     (avatar_coordinates[step,1]-1)*dimy+avatar_coordinates[step,2],
                     (ante_coordinates[step,1]-1)*dimy+ante_coordinates[step,2],
                     [nodes[step,k]|k in 1..dimx*dimy],
                     [arestes[step,k]|k in 1..(dimx-1)*dimy+dimx*(dimy-1)])
   );
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%5




constraint avatar_coordinates[1,1]= filav /\ avatar_coordinates[1,2]= colav;
constraint forall(i in 1..dimx,j in 1..dimy) (estats[1,i,j]=mapa[i,j]);

constraint count([estats[nsteps+1,i,j]|i in 1..dimx,j in 1..dimy],7,3);

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
                        ) 
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
                        ) 
                       /\
                       (
                         (estats[step,action_coordinates[step,1], action_coordinates[step,2]] = 2 /\ 
                          estats[step,next_coordinates[step,1], next_coordinates[step,2]] = 4) 
                         ->
                            (  estats[step+1,action_coordinates[step,1], action_coordinates[step,2]] = 0 /\ 
                               estats[step+1,next_coordinates[step,1], next_coordinates[step,2]] = 6 /\ 
                               avatar_coordinates[step+1,1]=action_coordinates[step,1] /\
                               avatar_coordinates[step+1,2]=action_coordinates[step,2])    
                        )  
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
                         ) 
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
                         )
                      );

constraint forall(step in 1..nsteps)
                     (forall(i in 1..dimx,j in 1..dimy)( 
                          ( ( i!=action_coordinates[step,1]\/ j!=action_coordinates[step,2] ) /\ 
                            ( i!=next_coordinates[step,1]\/ j!=next_coordinates[step,2] ))
                           -> estats[step+1,i,j]=estats[step,i,j])
                      ); 
                      
constraint forall(step in 1..nsteps)(estats[step,ante_coordinates[step,1],ante_coordinates[step,2]] in {0,8});


constraint forall(step in 1..nsteps-1,i in 1..dimx,j in 1..dimy)(estats[step,i,j] = 9 -> estats[step+1,i,j]=9);


constraint forall(step in 1..nsteps)(
   global_cardinality_low_up([estats[step,i,j]| i in 1..dimx, j in 1..dimy],[1,2,3,4,5,6,7],[0,0,0,0,0,0,0],[9,6,4,3,3,3,3]));



solve :: seq_search([                   
    int_search([actions[s],action_coordinates[s,1],action_coordinates[s,2]],input_order,indomain_min) |s in 1..nsteps] 
    ) satisfy;
    
output [show(estats[s,i,j])++if (j=dimy)then"\n" else " " endif++ if (i=dimx /\ j=dimy)then "---------------\n" else "" endif| s in 1..nsteps+1, i in 1..dimx,j in 1..dimy]


