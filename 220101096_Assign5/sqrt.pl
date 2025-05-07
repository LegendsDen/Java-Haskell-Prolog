squareroot(X, Result, Accuracy) :-
    X > 0,
    InitialGuess is X / 3,
    iterate_sqrt(X, InitialGuess, Accuracy, Result).

iterate_sqrt(X, Guess, Accuracy, Guess) :-
    Diff is abs(Guess * Guess - X),
    Diff < Accuracy, !.

iterate_sqrt(X, Guess, Accuracy, Result) :-
    NewGuess is (Guess + X / Guess) / 2,
    iterate_sqrt(X, NewGuess, Accuracy, Result).
