#ifndef LN_H

#define LN_H

#include <string_view>

#include <cstdint>

#define SIZE 32				 // m_size of array cell
#define BASE 4294967296		 // base of notation (2^SIZE)
#define INPUT_BASE_SIZE 4	 // base of input notation in bits
#define INPUT_BASE 16		 // base of input notation (2^INPUT_BASE_SIZE) (16 by job assigment)
#define uintSIZE uint32_t	 // type of array cells

typedef unsigned int uint;

class LN
{
  public:
	int m_errorCode = 0;	// error code

	// Constructors
	explicit LN(long long int number = 0);	  // from long long
	LN(const LN&);							  // copy
	LN(LN&&) noexcept;						  // move
	LN(const char* number);					  // from const char *
	LN(std::string_view);					  // from string_view
	// Destructor
	~LN();

	// Binary operators
	LN operator+(const LN& val) const;	  // add
	LN operator-(const LN& val) const;	  // subtract
	LN operator*(const LN& val) const;	  // multiply
	LN operator/(const LN& val) const;	  // divide
	LN operator%(const LN& val) const;	  // mod

	// Unary operators
	LN operator~();			 // square root
	LN operator-() const;	 // negate

	// Comparison operators
	bool operator<(const LN& val);	   // less
	bool operator<=(const LN& val);	   // less or equal
	bool operator>(const LN& val);	   // greater
	bool operator>=(const LN& val);	   // greater or equal
	bool operator==(const LN& val);	   // equal
	bool operator!=(const LN& val);	   // not equal

	// Assigment operators
	LN& operator=(const LN& copy);	  // copy
	LN& operator=(LN&&) noexcept;	  // move
	LN& operator+=(const LN& val);	  // add
	LN& operator-=(const LN& val);	  // subtract
	LN& operator*=(const LN& val);	  // multiply
	LN& operator/=(const LN& val);	  // divide
	LN& operator%=(const LN& val);	  // mod

	// Conversion operators
	explicit operator long long();	  // to long long
	explicit operator bool() const;			// to bool
	char* toString();

  private:
	// Fields
	unsigned long long int m_size;	  // count of array cells
	int m_sign;						  // m_sign of m_number (-1, 0, 1)
	bool m_isNan = false;			  // Nan
	uintSIZE* m_number = nullptr;	  // array for m_number

	// Work with memory
	void resizeArr(LN& arr, unsigned long long newSize) const;	  // changing array m_size
	void copyArr(LN& ln1, const LN& ln2);						  // changing array m_size

	// ASCII
	char to16(char c);	  // int to ASCII
	int toInt(char c);	  // ASCII to int

	// Math
	void absAdding(LN& a, const LN& b) const;				// adding by module, result to var a
	void adding(LN& a, const LN& b) const;					// adding, result to var a
	void absSubtracting(LN& a, const LN& b) const;			// subtraction by module, a > b, result to var a
	void subtracting(LN& a, const LN& b) const;				// subtracting, result to var a
	void multiplying(LN& a, const LN& b) const;				// multiplying, result to var a
	void dividing(LN& a, const LN& b, bool isMod) const;	// dividing, if isMod, returns a % b, else a / b
	void squareRoot(LN& a);									// square root, result to var a
	void powLn(LN& a, long long int b) const;				// pow, result to var a
	int absCompare(const LN& a, const LN& b) const;	   // comparing by module (|a| > |b| -> 1, |a| == |b| -> 0, |a| <
													   // |b| -> -1)
	int compare(const LN& a, const LN& b);			   // comparing (a > b -> 1, a == b -> 0, a < b -> -1)
};

// Operators of creating
LN operator"" _ln(const char*);
#endif