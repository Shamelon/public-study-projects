#include <stdexcept>
#include <iostream>

using namespace std;

struct GFNum {
    long long val;
    long long p;

    GFNum() : val(0), p(0) {}

    GFNum(long long val, long long p) : val(val % p), p(p) {}

    [[nodiscard]] GFNum inv() const {
        long long a = val, b = p;
        long long newX = 0, newY = 1;

        long long q;
        long long temp;
        while (a != 1) {
            q = a / b;

            temp = b;
            b = a - q * b;
            a = temp;

            temp = newX;
            newX = newY - q * newX;
            newY = temp;
        }

        return {newY < 0 ? newY + p : newY, p};
    }

    GFNum &operator/=(const GFNum &other) {
        *this *= other.inv();
        return *this;
    }

    friend GFNum operator/(GFNum lhs, const GFNum &rhs) {
        lhs /= rhs;
        return lhs;
    }

    GFNum operator-() const {
        return {p - val, p};
    }

    GFNum &operator-=(const GFNum &other) {
        if (val >= other.val) {
            val -= other.val;
        } else {
            val += p - other.val;
        }
        return *this;
    }

    friend GFNum operator-(GFNum lhs, const GFNum &rhs) {
        lhs -= rhs;
        return lhs;
    }

    GFNum &operator+=(const GFNum &other) {
        val += other.val;
        if (val >= p) {
            val -= p;
        }
        return *this;
    }

    GFNum &operator++() {
        val += 1;
        if (val >= p) {
            val = 0;
        }
        return *this;
    }

    friend GFNum operator+(GFNum lhs, const GFNum &rhs) {
        lhs += rhs;
        return lhs;
    }

    GFNum &operator*=(const GFNum &other) {
        val = (long long) ((__uint128_t) val * other.val % p);
        return *this;
    }

    friend GFNum operator*(GFNum lhs, const GFNum &rhs) {
        lhs *= rhs;
        return lhs;
    }

    bool operator==(const GFNum &other) const {
        return val == other.val;
    }

    bool operator!=(const GFNum &other) const {
        return !(*this == other);
    }

    [[nodiscard]] GFNum sqrt() const {
        if (val == 0) return {0, p};

        if (p % 4 == 3) {
            return pow((p + 1) / 4);
        }
        if (p % 8 == 5) {
            GFNum x = pow((p + 3) / 8);
            if ((x * x).val == val) {
                return x;
            }

            GFNum z = GFNum(2, p).pow((p - 1) / 4);
            return x * z;
        }

        return tonelli_shanks();
    }

    [[nodiscard]] GFNum pow(long long exponent) const {
        GFNum result(1, p);
        GFNum base = *this;
        while (exponent > 0) {
            if (exponent % 2 == 1) {
                result *= base;
            }
            base *= base;
            exponent /= 2;
        }
        return result;
    }
private:
    [[nodiscard]] GFNum tonelli_shanks() const {
        long long Q = p - 1;
        long long S = 0;
        while (Q % 2 == 0) {
            Q /= 2;
            S++;
        }

        // Ищем квадратичный невычет z
        GFNum z(2, p);
        while (z.pow((p - 1) / 2) == GFNum(1, p)) {
            z.val++;
        }

        GFNum c = z.pow(Q);
        GFNum x = pow((Q + 1) / 2);
        GFNum t = pow(Q);
        long long M = S;

        while (t != GFNum(1, p)) {
            long long i = 0;
            GFNum tt = t;
            while (tt != GFNum(1, p) && i < M) {
                tt *= tt;
                i++;
            }
            if (i == M) throw std::runtime_error("Tonelli-Shanks failed");

            GFNum b = c.pow((long long) 1 << (M - i - 1));
            x *= b;
            t *= b * b;
            c = b * b;
            M = i;
        }

        return x;
    }
};

struct EllipticCurve {
    long long a;
    long long b;
    long long p;

    EllipticCurve(long long a, long long b, long long p) : a(a % p), b(b % p), p(p) {}
};

struct Point {
    GFNum x;
    GFNum y;
    EllipticCurve curve;
    bool is_infinity;

    // Конструктор для обычной точки
    Point(GFNum x, GFNum y, EllipticCurve curve)
            : x(x), y(y), curve(curve), is_infinity(false) {
    }

    // Конструктор для точки на бесконечности (нейтральный элемент)
    explicit Point(EllipticCurve curve)
            : x(GFNum(0, curve.p)), y(GFNum(0, curve.p)), curve(curve), is_infinity(true) {}


    Point &operator+=(const Point &other) {
        if (is_infinity) {
            *this = other;
            return *this;
        }
        if (other.is_infinity) {
            return *this;
        }
        if (x == other.x && y != other.y) {
            *this = Point(curve);
            return *this;
        }

        GFNum lambda;
        if (x == other.x && y == other.y) {
            GFNum denominator = y + y;
            if (denominator.val == 0) {
                *this = Point(curve);
                return *this;
            }
            lambda = (GFNum(3, curve.p) * x * x + GFNum(curve.a, curve.p)) / denominator;
        } else {
            GFNum denominator = other.x - x;
            if (denominator.val == 0) {
                *this = Point(curve);
                return *this;
            }
            lambda = (other.y - y) / denominator;
        }

        GFNum x3 = lambda * lambda - x - other.x;
        GFNum y3 = lambda * (x - x3) - y;

        x = x3;
        y = y3;
        is_infinity = false;
        return *this;
    }

    friend Point operator+(Point lhs, const Point &rhs) {
        lhs += rhs;
        return lhs;
    }

    friend Point operator*(const Point &point, long long scalar) {
        if (point.is_infinity) return point;

        Point result(point.curve);
        Point temp = point;
        while (scalar > 0) {
            if (scalar & 1) {
                result += temp;
            }
            temp += temp;
            scalar >>= 1;
        }
        return result;
    }

    bool operator==(const Point& other) const {
        if (is_infinity || other.is_infinity) return is_infinity == other.is_infinity;
        return x == other.x && y == other.y;
    }

    bool operator!=(const Point& other) const {
        return !(*this == other);
    }
};

ostream &operator<<(std::ostream &strm, const GFNum &a) {
    return strm << a.val;
}

ostream &operator<<(std::ostream &strm, const Point &a) {
    if (a.is_infinity) {
        return strm << "inf";
    }
    return strm << "(" << a.x << ", " << a.y << ")";
}
