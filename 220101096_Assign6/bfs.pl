:- dynamic faultynode/1.

% Main predicate with error handling
shortest_path(Src, Dst, Path) :-
    % Input validation
    (   \+ mazelink(Src, _)
    ->  format('Error: Source node ~w does not exist.~n', [Src]), fail
    ;   faultynode(Src)
    ->  format('Error: Source node ~w is faulty.~n', [Src]), fail
    ;   \+ mazelink(Dst, _)
    ->  format('Error: Destination node ~w does not exist.~n', [Dst]), fail
    ;   faultynode(Dst)
    ->  format('Error: Destination node ~w is faulty.~n', [Dst]), fail
    % Dijkstra's algorithm
    ;   dijkstra(Src, Dst, Path),
        (   Path == []
        ->  format('No path found from ~w to ~w.~n', [Src, Dst]), fail
        ;   format('Shortest path: '), print_path(Path)
        )
    ).

% Dijkstra's algorithm implementation
dijkstra(Src, Dst, Path) :-
    % Initialize distances and previous nodes
    findall(Node, (mazelink(Node, _); mazelink(_, Node)), Nodes),
    list_to_set(Nodes, AllNodes),
    maplist(init_distance(Src), AllNodes),
    
    % Priority queue starts with source node
    PriorityQueue = [Src-0],
    
    % Main algorithm loop
    dijkstra_loop(PriorityQueue, Dst, AllNodes),
    
    % Reconstruct the path
    reconstruct_path(Dst, Path).

% Initialize distances (0 for source, infinity for others)
init_distance(Src, Node) :-
    (Node == Src 
     -> assertz(distance(Node, 0))
     ; assertz(distance(Node, inf))).

% Dijkstra main loop
dijkstra_loop([], _, _) :- !.
dijkstra_loop([Current-Dist|Queue], Dst, AllNodes) :-
    (Current == Dst -> ! ; true),
    findall(Neighbor-NewDist,
            (mazelink(Current, Neighbor),
            \+ faultynode(Neighbor),
            distance(Current, CurrentDist),
            distance(Neighbor, NeighborDist),
            NewDist is CurrentDist + 1, % All edges have weight 1 in maze
            NewDist < NeighborDist,
            retract(distance(Neighbor, _)),
            assertz(distance(Neighbor, NewDist)),
            assertz(previous(Neighbor, Current))),
            Neighbors),
    append(Queue, Neighbors, NewQueue),
    sort_by_distance(NewQueue, SortedQueue),
    dijkstra_loop(SortedQueue, Dst, AllNodes).

% Sort helper for priority queue
sort_by_distance(Queue, Sorted) :-
    predsort(compare_distances, Queue, Sorted).

compare_distances(<, _-Dist1, _-Dist2) :- Dist1 < Dist2, !.
compare_distances(>, _, _).

% Reconstruct path from destination to source
reconstruct_path(Dst, Path) :-
    (distance(Dst, inf) -> Path = [] ;
     reconstruct_path(Dst, [], Path)).

reconstruct_path(Node, Acc, Path) :-
    (previous(Node, Prev)
     -> reconstruct_path(Prev, [Node|Acc], Path)
     ;  Path = [Node|Acc]).

% Clean up dynamic predicates
cleanup :-
    retractall(distance(_, _)),
    retractall(previous(_, _)).

% Path printing helper
print_path([]) :- nl.
print_path([H|T]) :-
    write(H),
    (T == [] -> nl ; write(' -> '), print_path(T)).


% Faulty node management with full error checking
add_faulty(Node) :-
    (   \+ mazelink(Node, _), \+ mazelink(_, Node)
    ->  format('Error: Node ~w does not exist in the maze.~n', [Node]), fail
    ;   faultynode(Node)
    ->  format('Node ~w is already marked as faulty.~n', [Node])
    ;   assertz(faultynode(Node)),
        % Invalidate any cached paths that might be affected
        retractall(memo_path(Node, _, _)),
        retractall(memo_path(_, Node, _)),
        format('Marked node ~w as faulty.~n', [Node])
    ).

remove_faulty(Node) :-
    (   \+ mazelink(Node, _), \+ mazelink(_, Node)
    ->  format('Error: Node ~w does not exist in the maze.~n', [Node]), fail
    ;   retract(faultynode(Node))
    ->  % Invalidate any cached paths that might be affected
        retractall(memo_path(Node, _, _)),
        retractall(memo_path(_, Node, _)),
        format('Removed faulty status from node ~w.~n', [Node])
    ;   format('Node ~w was not marked as faulty.~n', [Node])
    ).

% Helper to check if a node exists in the maze
node_exists(Node) :-
    mazelink(Node, _) ; mazelink(_, Node).
% List all faulty nodes (optimized)
list_faulty :-
    (   setof(Node, faultynode(Node), FaultyNodes)
    ->  writeln('Faulty nodes:'), print_list(FaultyNodes)
    ;   writeln('No faulty nodes in the maze.')
    ).
% Help command
help :-
    writeln('Available commands:'),
    writeln('shortest_path(Start, End, Path) - Find shortest path'),
    writeln('add_faulty(Node) - Mark node as faulty'),
    writeln('remove_faulty(Node) - Remove faulty status'),
    writeln('list_faulty - List all faulty nodes').

% Helper to print lists
print_list([]).
print_list([H|T]) :-
    format('~w~n', [H]),
    print_list(T).