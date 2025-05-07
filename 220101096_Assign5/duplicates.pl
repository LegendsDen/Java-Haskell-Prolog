dupli([H | T]) :- member(H, T), !.
dupli([_ | T]) :- dupli(T).
dupli([]) :- false.
