has_duplicates([H | T]) :- member(H, T), !.
has_duplicates([_ | T]) :- has_duplicates(T).
has_duplicates([]) :- false.
