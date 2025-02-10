#ifndef GRASSHOPPER_TESTS_H
#define GRASSHOPPER_TESTS_H

#include "functions.h"

using namespace std;

class Tests {
public:
    static void testS();

    static void testR();

    static void testL();

    static void testKeyDeployment();

    static void testEncrypt();

    static void testDecrypt();

    static void testFunctionsAll();

    static void testEncryptSpeed();

    static void hexToB128(b128& block, string s);

    static void hexToB256(b256& block, string s);

    static string b128ToHex(const b128& s);

    static string b256ToHex(const b256& s);

private:
    static void test(int n, const string& value, const b128& actual, const string& expected);
};


#endif
