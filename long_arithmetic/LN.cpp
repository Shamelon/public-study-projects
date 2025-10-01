#include "LN.h"

#include "return_codes.h"

#include <complex>

#define min(a, b) (((a) < (b)) ? (a) : (b))
#define max(a, b) (((a) > (b)) ? (a) : (b))

using namespace std;

void LN::resizeArr(LN& ln, unsigned long long int newArrSize) const
{
    try
    {
        uintSIZE* tmp;
        tmp = new uintSIZE[ln.m_size];
        for (long long int i = 0; i < ln.m_size; ++i)
        {
            tmp[i] = ln.m_number[i];
        }
        if (ln.m_number != nullptr)
        {
            ln.m_number = nullptr;
            delete[] ln.m_number;
        }

        ln.m_number = new uintSIZE[newArrSize];
        for (long long int i = 0; i < min(ln.m_size, newArrSize); ++i)
        {
            ln.m_number[i] = tmp[i];
        }
        if (newArrSize > ln.m_size)
        {
            for (unsigned long long int i = ln.m_size; i < newArrSize; ++i)
            {
                ln.m_number[i] = 0;
            }
        }
        ln.m_size = newArrSize;
        tmp = nullptr;
        delete[] tmp;
    } catch (const bad_alloc& e)
    {
        ln.m_errorCode = ERROR_OUT_OF_MEMORY;
    }
}

void LN::copyArr(LN& ln1, const LN& ln2)
{
    for (long long int i = 0; i < ln2.m_size; ++i)
    {
        ln1.m_number[i] = ln2.m_number[i];
    }
}

char LN::to16(char c)
{
    return c < 10 ? c + 48 : c + 55;
}

int LN::toInt(char c)
{
    if ('a' <= c && c <= 'f')
    {
        return (int)c - 87;
    }
    else if ('A' <= c && c <= 'F')
    {
        return (int)c - 55;
    }
    else if ('0' <= c && c <= '9')
    {
        return (int)c - 48;
    }
    else
    {
        return 0;
    }
}

// ---------------------------------- CONSTRUCTORS ----------------------------------
LN::LN(long long int number)
{
    if (number == 0)
    {
        m_size = 0;
        m_sign = 0;
    }
    else
    {
        if (number > 0)
        {
            m_sign = 1;
        }
        else
        {
            number *= -1;
            m_sign = -1;
        }
        m_size = floor(log(number) / log(BASE)) + 1;
    }

    try
    {
        m_number = new uintSIZE[m_size];
    } catch (const bad_alloc& e)
    {
        m_errorCode = ERROR_OUT_OF_MEMORY;
        return;
    }

    long long int i = 0;
    while (number > 0)
    {
        m_number[i] = number % BASE;
        number /= BASE;
        i++;
    }
}

LN::LN(const LN& copy)
{
    m_errorCode = copy.m_errorCode;
    m_sign = copy.m_sign;
    m_size = copy.m_size;
    m_isNan = copy.m_isNan;
    if (m_number != nullptr)
    {
        m_number = nullptr;
        delete[] m_number;
    }
    try
    {
        m_number = new uintSIZE[m_size];
    } catch (const bad_alloc& e)
    {
        m_errorCode = ERROR_OUT_OF_MEMORY;
        return;
    }
    copyArr(*this, copy);
}

LN::LN(LN&& moved) noexcept
{
    m_errorCode = moved.m_errorCode;
    m_sign = moved.m_sign;
    m_size = moved.m_size;
    m_isNan = moved.m_isNan;
    m_number = moved.m_number;
    moved.m_number = nullptr;
    delete[] moved.m_number;
}

LN::LN(std::string_view number)
{
    long long int n = number.length();
    uint buf = (SIZE / INPUT_BASE_SIZE);

    if (n == 0 || n == 1 && number[0] == '0' || n == 2 && number[0] == '-' && number[1] == '0')
    {
        m_size = 0;
        m_sign = 0;
    }
    else
    {
        long long int start;	// number < 0 -> 1, number > 0 -> 0
        if (number[0] == '-')
        {
            m_sign = -1;
            start = 1;
            n -= 1;
        }
        else
        {
            m_sign = 1;
            start = 0;
        }
        m_size = (n - 1) / buf + 1;
        try
        {
            m_number = new uintSIZE[m_size];
        } catch (const bad_alloc& e)
        {
            m_errorCode = ERROR_OUT_OF_MEMORY;
            return;
        }

        long long int curr = n + start - 1;
        for (long long int i = 0; i < m_size; ++i)
        {
            m_number[i] = 0;
            for (long long int j = 0; j < buf; ++j)
            {
                if (curr < start)
                {
                    break;
                }
                else
                {
                    m_number[i] += (toInt(number[curr])) * ((long long int)pow(INPUT_BASE, j));
                    curr--;
                }
            }
        }
    }
}

LN::LN(const char* number)
{
    long long int n = 0;
    while (number[n] != 0)
    {
        n++;
    }
    uint buf = (SIZE / INPUT_BASE_SIZE);

    if (n == 0 || n == 1 && number[0] == '0' || n == 2 && number[0] == '-' && number[1] == '0')
    {
        m_size = 0;
        m_sign = 0;
    }
    else
    {
        long long int start;	// m_number < 0 -> 1, m_number > 0 -> 0
        if (number[0] == '-')
        {
            m_sign = -1;
            start = 1;
            n -= 1;
        }
        else
        {
            m_sign = 1;
            start = 0;
        }
        m_size = (n - 1) / buf + 1;
        try
        {
            m_number = new uintSIZE[m_size];
        } catch (const bad_alloc& e)
        {
            m_errorCode = ERROR_OUT_OF_MEMORY;
            return;
        }

        long long int curr = n + start - 1;
        for (long long int i = 0; i < m_size; ++i)
        {
            m_number[i] = 0;
            for (long long int j = 0; j < buf; ++j)
            {
                if (curr < start)
                {
                    break;
                }
                else
                {
                    m_number[i] += ((long long int)toInt(number[curr])) * ((long long int)pow(INPUT_BASE, j));
                    curr--;
                }
            }
        }
    }
}

LN::~LN()
{
    m_number = nullptr;
    delete[] m_number;
}

// -----------------------------------------------------------------------------------

// ------------------------------------ OPERATORS ------------------------------------
LN& LN::operator=(const LN& copy)
{
    if (&copy == this) {
        return *this;
    }
    m_errorCode = copy.m_errorCode;
    m_sign = copy.m_sign;
    m_size = copy.m_size;
    m_isNan = copy.m_isNan;
    if (m_number != nullptr)
    {
        delete[] m_number;
    }
    try
    {
        m_number = new uintSIZE[m_size];
    } catch (const bad_alloc& e)
    {
        m_errorCode = ERROR_OUT_OF_MEMORY;
        return *this;
    }
    copyArr(*this, copy);
    return *this;
}

LN& LN::operator=(LN&& moved) noexcept
{
    m_errorCode = moved.m_errorCode;
    m_sign = moved.m_sign;
    m_size = moved.m_size;
    m_isNan = moved.m_isNan;
    m_number = moved.m_number;
    moved.m_number = nullptr;
    delete[] moved.m_number;
    return *this;
}

LN LN::operator+(const LN& val) const
{
    LN a;
    a = *this;
    adding(a, val);
    return std::move(a);
}

LN LN::operator-(const LN& val) const
{
    LN a;
    a = *this;
    subtracting(a, val);
    return std::move(a);
}

LN LN::operator*(const LN& val) const
{
    LN a;
    a = *this;
    multiplying(a, val);
    return std::move(a);
}

LN LN::operator/(const LN& val) const
{
    LN a;
    a = *this;
    dividing(a, val, false);
    return std::move(a);
}

LN LN::operator%(const LN& val) const
{
    LN a;
    a = *this;
    dividing(a, val, true);
    return std::move(a);
}

LN LN::operator~()
{
    LN a;
    a = *this;
    squareRoot(a);
    return std::move(a);
}

LN LN::operator-() const
{
    LN a;
    a = *this;
    a.m_sign *= -1;
    return std::move(a);
}

LN& LN::operator+=(const LN& val)
{
    adding(*this, val);
    return *this;
}

LN& LN::operator-=(const LN& val)
{
    subtracting(*this, val);
    return *this;
}

LN& LN::operator*=(const LN& val)
{
    multiplying(*this, val);
    return *this;
}

LN& LN::operator/=(const LN& val)
{
    dividing(*this, val, false);
    return *this;
}

LN& LN::operator%=(const LN& val)
{
    dividing(*this, val, true);
    return *this;
}

bool LN::operator<(const LN& val)
{
    if (m_isNan || val.m_isNan)
    {
        return false;
    }
    return compare(*this, val) < 0;
}

bool LN::operator<=(const LN& val)
{
    if (m_isNan || val.m_isNan)
    {
        return false;
    }
    return compare(*this, val) <= 0;
}

bool LN::operator>(const LN& val)
{
    if (m_isNan || val.m_isNan)
    {
        return false;
    }
    return compare(*this, val) > 0;
}

bool LN::operator>=(const LN& val)
{
    if (m_isNan || val.m_isNan)
    {
        return false;
    }
    return compare(*this, val) >= 0;
}

bool LN::operator==(const LN& val)
{
    if (m_isNan || val.m_isNan)
    {
        return false;
    }
    return compare(*this, val) == 0;
}

bool LN::operator!=(const LN& val)
{
    if (m_isNan || val.m_isNan)
    {
        return true;
    }
    return compare(*this, val) != 0;
}

LN::operator long long()
{
    long long int s = 0;
    long long int k = 1;
    LN max = LN((long long int)9223372036854775807);
    if (absCompare(*this, max) > 0)
        m_errorCode = ERROR_DATA_INVALID;
    for (long long int i = 0; i < m_size; ++i)
    {
        if (i > 0)
        {
            k *= BASE;
        }
        s += m_number[i] * k;
    }
    return s * m_sign;
}

LN::operator bool() const
{
    if (m_sign == 0)
    {
        return true;
    }
    else
    {
        return false;
    }
}

LN operator"" _ln(const char* value)
{
    return LN(value);
}
// -----------------------------------------------------------------------------------

void LN::absAdding(LN& a, const LN& b) const
{
    int d = 0;	  // "в уме"
    long long int s;
    long long int i;
    for (i = 0; i < min(a.m_size, b.m_size); ++i)
    {
        s = (long long int)a.m_number[i] + (long long int)b.m_number[i] + d;
        a.m_number[i] = s % BASE;
        d = s / BASE;
    }
    if (a.m_size >= b.m_size)
    {
        while (d == 1)
        {
            if (a.m_size <= i)
            {
                try
                {
                    resizeArr(a, a.m_size + 1);
                } catch (const bad_alloc& e)
                {
                    a.m_errorCode = ERROR_OUT_OF_MEMORY;
                    return;
                }
                a.m_number[i] = 1;
                break;
            }
            s = (long long int)a.m_number[i] + d;
            a.m_number[i] = s % BASE;
            d = s / BASE;
            i++;
        }
    }
    else
    {
        try
        {
            resizeArr(a, b.m_size);
        } catch (const bad_alloc& e)
        {
            a.m_errorCode = ERROR_OUT_OF_MEMORY;
            return;
        }
        for (i = i; i < b.m_size; ++i)
        {
            s = (long long int)b.m_number[i] + d;
            a.m_number[i] = s % BASE;
            d = s / BASE;
        }
        if (d == 1)
        {
            try
            {
                resizeArr(a, a.m_size + 1);
            } catch (const bad_alloc& e)
            {
                a.m_errorCode = ERROR_OUT_OF_MEMORY;
                return;
            }
            a.m_number[i] = 1;
        }
    }
}

void LN::absSubtracting(LN& a, const LN& b) const
{
    int d = 0;
    long long int s;
    long long int i;
    for (i = 0; i < min(a.m_size, b.m_size); ++i)
    {
        s = (long long int)a.m_number[i] - (long long int)b.m_number[i] + d;
        if (s < 0)
        {
            a.m_number[i] = s + BASE;
            d = -1;
        }
        else
        {
            a.m_number[i] = s;
            d = 0;
        }
    }
    while (d == -1)
    {
        s = (long long int)a.m_number[i] + d;
        if (s < 0)
        {
            a.m_number[i] = s + BASE;
            d = -1;
        }
        else
        {
            a.m_number[i] = s;
            d = 0;
        }
        i++;
    }
    if (a.m_number[a.m_size - 1] == 0)
    {
        try
        {
            resizeArr(a, a.m_size - 1);
        } catch (const bad_alloc& e)
        {
            a.m_errorCode = ERROR_OUT_OF_MEMORY;
            return;
        }
    }
}

void LN::multiplying(LN& a, const LN& b) const
{
    if (a.m_isNan || b.m_isNan)
    {
        a.m_isNan = true;
        return;
    }

    LN c;
    c.m_sign = a.m_sign * b.m_sign;
    if (c.m_sign == 0)
    {
        a.m_size = 0;
        a.m_sign = 0;
        return;
    }

    try
    {
        resizeArr(c, a.m_size + b.m_size);
    } catch (const bad_alloc& e)
    {
        a.m_errorCode = ERROR_OUT_OF_MEMORY;
        return;
    }
    long long int s;
    long long int d = 0;	// "in mind"
    for (long long int k = 0; k < b.m_size; ++k)
    {
        for (long long int i = 0; i < a.m_size; ++i)
        {
            s = a.m_number[i] * b.m_number[k] + d;
            d = (s + c.m_number[i + k]) / BASE;
            c.m_number[i + k] += s % BASE;
        }
        c.m_number[a.m_size + k] += d;
        d = 0;
    }

    long long int senior = c.m_size - 1;
    while (c.m_number[senior] == 0)
    {
        senior -= 1;
    }
    try
    {
        resizeArr(c, senior + 1);
    } catch (const bad_alloc& e)
    {
        a.m_errorCode = ERROR_OUT_OF_MEMORY;
        return;
    }
    a = std::move(c);
}

void LN::dividing(LN& a, const LN& b, bool isMod) const
{
    if (a.m_isNan || b.m_isNan)
    {
        a.m_isNan = true;
        return;
    }
    if (a.m_sign == 0 && b.m_sign != 0 || absCompare(a, b) < 0)
    {
        if (!isMod)
        {
            a.m_sign = 0;
            a.m_size = 0;
        }
        return;
    }
    if (b.m_sign == 0)
    {
        a.m_isNan = true;
        return;
    }

    // balancing
    LN bCopy;
    bCopy = b;
    long long int k = 0;
    while (bCopy.m_number[bCopy.m_size - 1] < BASE / 2)
    {
        multiplying(a, 2_ln);
        multiplying(bCopy, 2_ln);
        k++;
    }

    LN c;
    bool negated = false;
    c.m_sign = a.m_sign * b.m_sign;
    if (a.m_sign == -1)
    {
        a.m_sign = 1;
        negated = true;
    }
    long long int m = a.m_size - bCopy.m_size;
    LN eaten;
    eaten = LN(BASE);
    powLn(eaten, m);
    multiplying(eaten, bCopy);
    if (absCompare(a, eaten) > -1)
    {
        try
        {
            resizeArr(c, m + 1);
        } catch (const bad_alloc& e)
        {
            a.m_errorCode = ERROR_OUT_OF_MEMORY;
            return;
        }
        c.m_number[m] = 1;
        absSubtracting(a, eaten);
    }
    else
    {
        try
        {
            resizeArr(c, m);
        } catch (const bad_alloc& e)
        {
            a.m_errorCode = ERROR_OUT_OF_MEMORY;
            return;
        }
    }
    long long int tmp;
    long long int tmpBase;
    LN base;
    for (long long int i = m - 1; i > -1; --i)
    {
        tmp = (a.m_number[bCopy.m_size + i] * BASE + a.m_number[bCopy.m_size + i - 1]) / bCopy.m_number[bCopy.m_size - 1];
        tmpBase = BASE - 1;
        c.m_number[i] = min(tmp, tmpBase);
        eaten = LN((long long int)c.m_number[i]);
        base = LN(BASE);
        powLn(base, i);
        eaten *= base * bCopy;
        eaten.m_sign = 1;
        a -= eaten;
        eaten = LN(BASE);
        powLn(eaten, i);
        eaten *= bCopy;
        eaten.m_sign = 1;
        while (a.m_sign < 0)
        {
            c.m_number[i] -= 1;
            a += eaten;
        }
    }
    if (isMod)
    {
        a /= LN((long long int)pow(2, k));
        if (negated)
        {
            a.m_sign = -1;
        }
    }
    else
    {
        a = std::move(c);
    }
}

void LN::squareRoot(LN& a)
{
    if (a.m_sign == -1 || a.m_isNan)
    {
        a.m_isNan = true;
        return;
    }
    else if (a.m_sign == 0)
    {
        a.m_size = 0;
        a.m_sign = 0;
        return;
    }
    LN c;
    c.m_sign = 1;
    try
    {
        resizeArr(c, (a.m_size + 1) / 2);
    } catch (const bad_alloc& e)
    {
        a.m_errorCode = ERROR_OUT_OF_MEMORY;
        return;
    }
    LN s;
    LN pref;
    pref = LN();
    LN eaten;
    if (a.m_size % 2 == 1)
    {
        try
        {
            resizeArr(a, a.m_size + 1);
        } catch (const bad_alloc& e)
        {
            a.m_errorCode = ERROR_OUT_OF_MEMORY;
            return;
        }
    }

    // binary search
    for (long long int i = a.m_size - 1; i > 0; i -= 2)
    {
        s += LN(a.m_number[i] * BASE + a.m_number[i - 1]);
        long long int currDig = BASE / 2;
        long long int start = 0;
        long long int end = BASE;
        while (end - start > 1)
        {
            eaten = (pref + LN(currDig)) * LN(currDig);
            if (eaten > s)
            {
                end = currDig;
            }
            else
            {
                start = currDig;
            }
            currDig = (start + end) / 2;
        }
        s -= (pref + LN(currDig)) * LN(currDig);
        s *= LN(BASE * BASE);
        c.m_number[i / 2] = currDig;
        pref *= LN(BASE);
        pref += LN(currDig * BASE * 2);
    }
    a = std::move(c);
}

int LN::absCompare(const LN& a, const LN& b) const
{
    if (a.m_size > b.m_size)
    {
        return 1;
    }
    else if (a.m_size < b.m_size)
    {
        return -1;
    }
    else
    {
        for (long long int i = a.m_size - 1; i > -1; --i)
        {
            if (a.m_number[i] > b.m_number[i])
            {
                return 1;
            }
            else if (a.m_number[i] < b.m_number[i])
            {
                return -1;
            }
        }
        return 0;
    }
}

int LN::compare(const LN& a, const LN& b)
{
    if (a.m_sign > b.m_sign)
    {
        return 1;
    }
    else if (a.m_sign < b.m_sign)
    {
        return -1;
    }
    else
    {
        return absCompare(a, b) * a.m_sign;
    }
}

void LN::adding(LN& a, const LN& b) const
{
    if (a.m_isNan || b.m_isNan)
    {
        a.m_isNan = true;
        return;
    }
    if (a.m_sign * b.m_sign == 0)	 // a == 0 || b == 0
    {
        if (b.m_size > a.m_size)
        {	 // a == 0 && b != 0
            a = b;
        }
        else if (b.m_size == a.m_size)
        {	 // a == 0 && b == 0
            a.m_sign = 0;
            a.m_size = 0;
        }
    }
    else if (a.m_sign * b.m_sign > 0)
    {
        absAdding(a, b);
    }
    else
    {
        if (absCompare(a, b) > 0)
        {
            absSubtracting(a, b);
        }
        else if (absCompare(a, b) == 0)
        {
            a.m_sign = 0;
            a.m_size = 0;
        }
        else
        {
            LN tmp;
            tmp = b;
            absSubtracting(tmp, a);
            a = tmp;
        }
    }
}

void LN::subtracting(LN& a, const LN& b) const
{
    if (a.m_isNan || b.m_isNan)
    {
        a.m_isNan = true;
        return;
    }
    if (a.m_sign * b.m_sign == 0)	 // a == 0 || b == 0
    {
        if (b.m_size > a.m_size)
        {	 // a == 0 && b != 0
            a = b;
            a.m_sign = -1 * b.m_sign;
        }
        else if (b.m_size == a.m_size)
        {	 // a == 0 && b == 0
            a.m_sign = 0;
            a.m_size = 0;
        }
    }
    else if (a.m_sign * b.m_sign > 0)
    {
        if (absCompare(a, b) > 0)
        {
            absSubtracting(a, b);
        }
        else if (absCompare(a, b) == 0)
        {
            a.m_sign = 0;
            a.m_size = 0;
        }
        else
        {
            LN tmp;
            tmp = b;
            absSubtracting(tmp, a);
            a = tmp;
            a.m_sign = -1 * b.m_sign;
        }
    }
    else
    {
        absAdding(a, b);
    }
}

void LN::powLn(LN& a, long long int b) const
{
    LN res;
    res = LN(1);
    long long int pow = b;
    while (pow > 0)
    {
        if (pow % 2 == 1)
        {
            multiplying(res, a);
        }
        multiplying(a, a);
        pow /= 2;
    }
    a = res;
}

char* LN::toString()
{
    char* target;
    const uint buf = (SIZE / INPUT_BASE_SIZE);

    if (m_isNan)
    {
        try
        {
            target = new char[4];
        } catch (const bad_alloc& e)
        {
            m_errorCode = ERROR_OUT_OF_MEMORY;
        }
        target[0] = 'N';
        target[1] = 'a';
        target[2] = 'N';
        target[3] = '\0';
        return target;
    }
    if (m_sign == 0)
    {
        try
        {
            target = new char[2];
        } catch (const bad_alloc& e)
        {
            m_errorCode = ERROR_OUT_OF_MEMORY;
        }
        target[0] = '0';
        target[1] = '\0';
        return target;
    }

    long long int currNum = m_number[m_size - 1];
    char currChars[buf];
    for (int i = buf - 1; i > -1; --i)
    {
        currChars[i] = currNum % INPUT_BASE;
        currNum /= INPUT_BASE;
    }

    int k = 0;
    int delay = 0;
    while (currChars[k] == 0)
    {
        delay++;
        k++;
    }
    int minus = 0;
    if (m_sign < 0)
    {
        minus = 1;
        try
        {
            target = new char[m_size * SIZE / INPUT_BASE_SIZE - delay + 2];
        } catch (const bad_alloc& e)
        {
            m_errorCode = ERROR_OUT_OF_MEMORY;
        }
        target[0] = '-';
    }
    else
    {
        try
        {
            target = new char[m_size * SIZE / INPUT_BASE_SIZE - delay + 1];
        } catch (const bad_alloc& e)
        {
            m_errorCode = ERROR_OUT_OF_MEMORY;
        }
    }

    for (int i = minus; i < buf - delay + minus; ++i)
    {
        target[i] = to16(currChars[i + delay - minus]);
    }

    for (long long int i = 1; i < m_size; ++i)
    {
        currNum = m_number[m_size - 1 - i];
        for (int j = buf - 1; j > -1; --j)
        {
            target[j + i * buf - delay + minus] = to16(currNum % INPUT_BASE);
            currNum /= INPUT_BASE;
        }
    }
    target[m_size * SIZE / INPUT_BASE_SIZE - delay + minus] = '\0';
    return target;
}