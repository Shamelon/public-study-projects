#ifndef GRASSHOPPER_FUNCTIONS_H
#define GRASSHOPPER_FUNCTIONS_H

#include <bitset>
#include <cstdint>
#include <vector>

using namespace std;

typedef uint8_t b128[16];
typedef uint8_t b256[32];

void S(b128& in);

void R(b128& in);

void L(b128& in);

void X(const b128& key, b128& in);

void invS(b128& in);

void invR(b128& in);

void invL(b128& in);

void encrypt(b128& in, const vector<b128>& keys);
void decrypt(b128& in, const vector<b128>& keys);

void feistel(b128& a1, b128& a0, const b128& key);

void keyDeployment(vector<b128>& keyStorage, const b256& key);

#endif
