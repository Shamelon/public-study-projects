#include "elliptic_curve.cpp"
#include <random>
#include <chrono>

using namespace std::chrono;

Point find_point_on_curve(const EllipticCurve &curve) {
    random_device rd;
    mt19937 gen(rd());
    uniform_int_distribution<long long> dist(0, curve.p - 1);

    for (int attempts = 0; attempts < 1000; ++attempts) {
        long long x_val = dist(gen);
        GFNum x(x_val, curve.p);

        GFNum rhs = x * x * x + GFNum(curve.a, curve.p) * x + GFNum(curve.b, curve.p);

        if (rhs.val == 0) {
            GFNum y(0, curve.p);
            return {x, y, curve};
        }

        GFNum legendre = rhs.pow((curve.p - 1) / 2);
        if (legendre == GFNum(1, curve.p)) {
            GFNum y = rhs.sqrt();
            return {x, y, curve};
        }
    }

    return Point(curve);
}

struct Step {
    GFNum a{}, b{};
    Point R;
};

void next_step(Step &s, const Point &P, const Point &Q) {
    switch (s.R.x.val % 3) {
        case 0:
            s.R += P;
            ++s.a;
            break;
        case 1:
            s.R += s.R;
            s.a += s.a;
            s.b += s.b;
            break;
        case 2:
            s.R += Q;
            ++s.b;
            break;
    }
}

long long pollards_rho(const Point &P, const Point &Q, long long order) {
    if (P.is_infinity) throw invalid_argument("P cannot be infinity");
    if (order == 0) throw invalid_argument("Order cannot be zero");

    random_device rd;
    mt19937 gen(rd());
    uniform_int_distribution<long long> dist(1, order - 1);

    auto init_random_step = [&]() -> Step {
        long long a = dist(gen);
        long long b = dist(gen);
        return {GFNum(a, order), GFNum(b, order), P * a + Q * b};
    };


    Step tortoise = init_random_step();
    Step hare = tortoise;
    next_step(hare, P, Q);

    long long stepCount = 0;
    while (tortoise.R != hare.R) {
        next_step(tortoise, P, Q);
        next_step(hare, P, Q);
        next_step(hare, P, Q);
        stepCount++;
        if (stepCount % 1000000 == 0) {
            cout << stepCount / 1000000 << " million steps taken" << endl;
        }
    }

    GFNum a_diff = tortoise.a - hare.a;
    GFNum b_diff = hare.b - tortoise.b;

    if (b_diff.val == 0) {
        return pollards_rho(P, Q, order);
    }

    long long g = gcd(b_diff.val, order);
    if (g != 1) {
        return pollards_rho(P, Q, order);
    }

    long long x = (a_diff * (GFNum(1, order) / b_diff)).val;

    for (long long k = 0; k < g; ++k) {
        long long candidate = x + k * order;
        if (P * candidate == Q) {
            return candidate;
        }
    }

    return pollards_rho(P, Q, order);
}

void testPollardSmall() {
    long long p = 3623;
    long long q = 3566;
    EllipticCurve curve = {14, 19, p};
    long long n = 947;
    Point P(GFNum(6, p), GFNum(730, p), curve);
    Point Q = P * n;
    long long log = pollards_rho(P, Q, q);
    cout << log;
}

void testPollard40() {
    long long p = 1099511627791;
    long long q = 1099513257113;
    EllipticCurve curve = {490064540513, 170079681745, p};
    Point P(GFNum(817257933787, p), GFNum(590395998743, p), curve);
    long long n = 99511624791; // Expected discrete logarithm
    Point Q = P * n;

    auto start = high_resolution_clock::now();

    long long result = pollards_rho(P, Q, q);

    auto stop = high_resolution_clock::now();
    auto duration = duration_cast<milliseconds>(stop - start);

    cout << "Result: " << result << endl;
    cout << "Time taken: " << duration.count() << " milliseconds" << endl;
}

void testPollard48() {
    long long p = 281474976710677;
    long long q = 281474987479363;
    EllipticCurve curve = {187997080572537, 198915293914922, p};
    Point P(GFNum(2873972676574, p), GFNum(270090453202684, p), curve);
    long long n = 1099519627791; // Expected discrete logarithm
    Point Q = P * n;

    auto start = high_resolution_clock::now();

    long long result = pollards_rho(P, Q, q);

    auto stop = high_resolution_clock::now();
    auto duration = duration_cast<milliseconds>(stop - start);

    cout << "Result: " << result << endl;
    cout << "Time taken: " << duration.count() << " milliseconds" << endl;
}

void testPollard56() {
    long long p = 72057594037928017;
    long long q = 72057594089783747;
    EllipticCurve curve = {15222514519776677, 7110318376978981, p};
    Point P(GFNum(65738287878334536, p), GFNum(34206366656006446, p), curve);
    long long n = 4205759308978; // Expected discrete logarithm
    Point Q = P * n;

    auto start = high_resolution_clock::now();

    long long result = pollards_rho(P, Q, q);

    auto stop = high_resolution_clock::now();
    auto duration = duration_cast<milliseconds>(stop - start);

    cout << "Result: " << result << endl;
    cout << "Time taken: " << duration.count() << " milliseconds" << endl;
}

int main() {
    testPollard48();
    return 0;
}