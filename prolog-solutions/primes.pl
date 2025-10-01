prime(A) :- prime_table(A), !.
prime(A, 1).
prime(A, B) :-
    mod(A, B) > 0,
    B1 is B - 1,
    prime(A, B1).
prime(A) :- B is A - 1, prime(A, B), assert(prime_table(A)).

composite(A) :- \+ prime(A).

prime_divisors(1, []).
prime_divisors(N, [H | T]) :- decompose(N, [H | T], 2).

square_divisors(1, []).
square_divisors(N, [H | T]) :- N1 is N * N, decompose(N1, [H | T], 2).

decompose(1, [], _).
decompose(N, [H | T], D) :-
    \+ prime(D),
    D1 is D + 1,
    decompose(N, [H | T], D1).
decompose(N, [H | T], D) :-
    \+ (D > N),
    prime(D),
    mod(N, D) > 0,
    D1 is D + 1,
    decompose(N, [H | T], D1).
decompose(N, [H | T], D) :-
    \+ (D > N),
    prime(D),
    H = D,
    0 is mod(N, D),
    N1 is div(N, D),
    decompose(N1, T, D).

