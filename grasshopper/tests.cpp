#include "tests.h"
#include <cstdio>
#include <sstream>
#include <algorithm>
#include <iostream>
#include <bitset>
#include <vector>
#include <chrono>

void Tests::testS() {
    cout << "TEST S" << endl;
    b128 curr;
    hexToB128(curr, "ffeeddccbbaa99881122334455667700");
    S(curr);
    test(1,
         ("ffeeddccbbaa99881122334455667700"),
         curr,
         ("b66cd8887d38e8d77765aeea0c9a7efc"));
    S(curr);
    test(2,
         ("b66cd8887d38e8d77765aeea0c9a7efc"),
         curr,
         ("559d8dd7bd06cbfe7e7b262523280d39"));
    S(curr);
    test(3,
         ("559d8dd7bd06cbfe7e7b262523280d39"),
         curr,
         ("0c3322fed531e4630d80ef5c5a81c50b"));
    S(curr);
    test(4,
         ("0c3322fed531e4630d80ef5c5a81c50b"),
         curr,
         ("23ae65633f842d29c5df529c13f5acda"));
    cout << "TEST S inverse" << endl;
    invS(curr);
    test(4,
         ("23ae65633f842d29c5df529c13f5acda"),
         curr,
         ("0c3322fed531e4630d80ef5c5a81c50b"));
    invS(curr);
    test(3,
         ("0c3322fed531e4630d80ef5c5a81c50b"),
         curr,
         ("559d8dd7bd06cbfe7e7b262523280d39"));
    invS(curr);
    test(2,
         ("559d8dd7bd06cbfe7e7b262523280d39"),
         curr,
         ("b66cd8887d38e8d77765aeea0c9a7efc"));
    invS(curr);
    test(1,
         ("b66cd8887d38e8d77765aeea0c9a7efc"),
         curr,
         ("ffeeddccbbaa99881122334455667700"));
}

void Tests::testR() {
    cout << "TEST R" << endl;
    b128 curr;
    hexToB128(curr, "00000000000000000000000000000100");
    R(curr);
    test(1,
         ("00000000000000000000000000000100"),
         curr,
         ("94000000000000000000000000000001"));
    R(curr);
    test(2,
         ("94000000000000000000000000000001"),
         curr,
         ("a5940000000000000000000000000000"));
    R(curr);
    test(3,
         ("a5940000000000000000000000000000"),
         curr,
         ("64a59400000000000000000000000000"));
    R(curr);
    test(4,
         ("64a59400000000000000000000000000"),
         curr,
         ("0d64a594000000000000000000000000"));
    cout << "TEST R inverse" << endl;
    invR(curr);
    test(4,
         ("0d64a594000000000000000000000000"),
         curr,
         ("64a59400000000000000000000000000"));
    invR(curr);
    test(3,
         ("64a59400000000000000000000000000"),
         curr,
         ("a5940000000000000000000000000000"));
    invR(curr);
    test(2,
         ("a5940000000000000000000000000000"),
         curr,
         ("94000000000000000000000000000001"));
    invR(curr);
    test(1,
         ("94000000000000000000000000000001"),
         curr,
         ("00000000000000000000000000000100"));
}

void Tests::testL() {
    cout << "TEST L" << endl;
    b128 curr;
    hexToB128(curr, "64a59400000000000000000000000000");
    L(curr);
    test(1,
         ("64a59400000000000000000000000000"),
         curr,
         ("d456584dd0e3e84cc3166e4b7fa2890d"));
    L(curr);
    test(2,
         ("d456584dd0e3e84cc3166e4b7fa2890d"),
         curr,
         ("79d26221b87b584cd42fbc4ffea5de9a"));
    L(curr);
    test(3,
         ("79d26221b87b584cd42fbc4ffea5de9a"),
         curr,
         ("0e93691a0cfc60408b7b68f66b513c13"));
    L(curr);
    test(4,
         ("0e93691a0cfc60408b7b68f66b513c13"),
         curr,
         ("e6a8094fee0aa204fd97bcb0b44b8580"));
    cout << "TEST L inverse" << endl;
    invL(curr);
    test(4,
         ("e6a8094fee0aa204fd97bcb0b44b8580"),
         curr,
         ("0e93691a0cfc60408b7b68f66b513c13"));
    invL(curr);
    test(3,
         ("0e93691a0cfc60408b7b68f66b513c13"),
         curr,
         ("79d26221b87b584cd42fbc4ffea5de9a"));
    invL(curr);
    test(2,
         ("79d26221b87b584cd42fbc4ffea5de9a"),
         curr,
         ("d456584dd0e3e84cc3166e4b7fa2890d"));
    invL(curr);
    test(1,
         ("d456584dd0e3e84cc3166e4b7fa2890d"),
         curr,
         ("64a59400000000000000000000000000"));
}

void Tests::testEncrypt() {
    cout << "TEST Encrypt" << endl;

    vector<b128> keys(10);
    b256 key;
    hexToB256(key, "8899aabbccddeeff0011223344556677fedcba98765432100123456789abcdef");
    keyDeployment(keys, key);
    b128 curr;
    hexToB128(curr, "1122334455667700ffeeddccbbaa9988");
    encrypt(curr, keys);
    test(1,
         ("1122334455667700ffeeddccbbaa9988"),
         curr,
         ("7f679d90bebc24305a468d42b9d4edcd"));
}

void Tests::testDecrypt() {
    cout << "TEST Decrypt" << endl;

    vector<b128> keys(10);
    b256 key;
    hexToB256(key, "8899aabbccddeeff0011223344556677fedcba98765432100123456789abcdef");
    keyDeployment(keys, key);
    b128 curr;
    hexToB128(curr, "7f679d90bebc24305a468d42b9d4edcd");
    decrypt(curr, keys);
    test(1,
         ("7f679d90bebc24305a468d42b9d4edcd"),
         curr,
         ("1122334455667700ffeeddccbbaa9988"));
}


void Tests::testKeyDeployment() {
    string cs[] = {
            "6ea276726c487ab85d27bd10dd849401",
            "dc87ece4d890f4b3ba4eb92079cbeb02",
            "b2259a96b4d88e0be7690430a44f7f03",
            "7bcd1b0b73e32ba5b79cb140f2551504",
            "156f6d791fab511deabb0c502fd18105",
            "a74af7efab73df160dd208608b9efe06",
            "c9e8819dc73ba5ae50f5b570561a6a07",
            "f6593616e6055689adfba18027aa2a08",
    };

    string expectLeft[]{
            "c3d5fa01ebe36f7a9374427ad7ca8949",
            "37777748e56453377d5e262d90903f87",
            "f9eae5f29b2815e31f11ac5d9c29fb01",
            "e980089683d00d4be37dd3434699b98f",
            "b7bd70acea4460714f4ebe13835cf004",
            "1a46ea1cf6ccd236467287df93fdf974",
            "3d4553d8e9cfec6815ebadc40a9ffd04",
            "db31485315694343228d6aef8cc78c44",
    };

    string expectRight[]{
            "8899aabbccddeeff0011223344556677",
            "c3d5fa01ebe36f7a9374427ad7ca8949",
            "37777748e56453377d5e262d90903f87",
            "f9eae5f29b2815e31f11ac5d9c29fb01",
            "e980089683d00d4be37dd3434699b98f",
            "b7bd70acea4460714f4ebe13835cf004",
            "1a46ea1cf6ccd236467287df93fdf974",
            "3d4553d8e9cfec6815ebadc40a9ffd04",
    };

    b128 a1;
    hexToB128(a1, "8899aabbccddeeff0011223344556677");
    b128 a0;
    hexToB128(a0, "fedcba98765432100123456789abcdef");

    cout << "Test Feistel" << endl;

    b128 key;
    for (int i = 0; i < 8; ++i) {
        hexToB128(key, cs[i]);
        feistel(a1, a0, key);
        if (b128ToHex(a1) == expectLeft[i] && b128ToHex(a0) == expectRight[i]) {
            continue;
        }
        cout << "    Round " << i + 1 << " failed" << endl;
        cout << "    For C " << cs[i] << endl;
        cout << "Expected: " << expectLeft[i] << ", " << expectRight[i] << endl;
        cout << "  Actual: " << b128ToHex(a1) << ", " << b128ToHex(a0) << endl;
        return;
    }
    cout << "Good" << endl;

    vector<string> keysExpect = {
            "8899aabbccddeeff0011223344556677",
            "fedcba98765432100123456789abcdef",
            "db31485315694343228d6aef8cc78c44",
            "3d4553d8e9cfec6815ebadc40a9ffd04",
            "57646468c44a5e28d3e59246f429f1ac",
            "bd079435165c6432b532e82834da581b",
            "51e640757e8745de705727265a0098b1",
            "5a7925017b9fdd3ed72a91a22286f984",
            "bb44e25378c73123a5f32f73cdb6e517",
            "72e9dd7416bcf45b755dbaa88e4a4043"
    };

    cout << "Test Key Deployment" << endl;
    vector<b128> keys(10);
    b256 key1;
    hexToB256(key1, "8899aabbccddeeff0011223344556677fedcba98765432100123456789abcdef");
    keyDeployment(keys, key1);
    for (int i = 0; i < 10; ++i) {
        if (b128ToHex(keys[i]) == keysExpect[i]) {
            continue;
        }
        cout << "Key " << i + 1 << " is wrong" << endl;
        cout << "Expected: " << keysExpect[i] << endl;
        cout << "  Actual: " << b128ToHex(keys[i]) << endl;
        return;
    }

    cout << "Good" << endl;
}

void Tests::testFunctionsAll() {
    testS();
    testR();
    testL();
    testKeyDeployment();
    testEncrypt();
    testDecrypt();
}

void Tests::testEncryptSpeed() {
    const size_t targetSizeMB = 100;
    const size_t targetSizeBytes = targetSizeMB * 1024 * 1024;

    size_t totalEncryptedSize = 0;

    b128 block;
    hexToB128(block,"55ed8dd7bd06cbfe7e72784523280d39");

    b256 key;
    hexToB256(key,"8899aabbccddeeff0011223344556677fedcba98765432100123456789abcdef");

    vector<b128> keys(10);

    keyDeployment(keys, key);

    // Время начала
    auto startTime = chrono::high_resolution_clock::now();

    while (totalEncryptedSize < targetSizeBytes) {
        encrypt(block, keys);
        totalEncryptedSize += 16;
    }

    auto endTime = chrono::high_resolution_clock::now();

    auto duration = (double) chrono::duration_cast<chrono::milliseconds>(endTime - startTime).count();

    double durationInSeconds = duration / 1000.0;
    double speedMBps = targetSizeMB / durationInSeconds;

    cout << "Encrypted size: " << totalEncryptedSize / (1024 * 1024) << " MB" << endl;
    cout << "Working time: " << durationInSeconds << " seconds" << endl;
    cout << "Speed: " << speedMBps << " MB/s" << endl;
}

void Tests::test(int n, const string& value, const b128& actual, const string& expected) {
    cout << n << ". ";
    if (expected == b128ToHex(actual)) {
        cout << "Good" << endl;
        return;
    }

    cout << "      For " << value << endl;
    cout << "   Expected: " << expected << endl;
    cout << "   Actual  : " << b128ToHex(actual) << endl;
}

void Tests::hexToB128(b128 &block, string hexStr) {
    fill(begin(block), end(block), 0);
    reverse(hexStr.begin(), hexStr.end());
    for (size_t i = 0; i < hexStr.length(); ++i) {
        int value = (hexStr[i] >= '0' && hexStr[i] <= '9') ? (hexStr[i] - '0') : (hexStr[i] - 'a' + 10);
        block[i / 2] += value * (i & 1 ? 16 : 1);
    }
}

void Tests::hexToB256(b256& block, string hexStr) {
    fill(begin(block), end(block), 0);
    reverse(hexStr.begin(), hexStr.end());
    for (size_t i = 0; i < hexStr.length(); ++i) {
        int value = (hexStr[i] >= '0' && hexStr[i] <= '9') ? (hexStr[i] - '0') : (hexStr[i] - 'a' + 10);
        block[i / 2] += value * (i & 1 ? 16 : 1);
    }
}

string Tests::b128ToHex(const b128& bits) {
    stringstream hexStream;
    for (auto el : bits) {
        for (auto value : {el % 16, el / 16})
        if (value < 10) {
            hexStream << value;
        } else {
            hexStream << static_cast<char>('a' + (value - 10));
        }
    }

    string res = hexStream.str();
    reverse(res.begin(), res.end());
    return res;
}

string Tests::b256ToHex(const b256& bits) {
    stringstream hexStream;
    for (auto el : bits) {
        for (auto value : {el % 16, el / 16})
            if (value < 10) {
                hexStream << value;
            } else {
                hexStream << static_cast<char>('a' + (value - 10));
            }
    }

    string res = hexStream.str();
    reverse(res.begin(), res.end());
    return res;
}
