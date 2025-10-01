const func = (f) => (f1, f2 = function(x, y, z) {}) => (x, y, z) => f(f1(x, y, z), f2(x, y, z))
const cnst = (value) => (x, y, z) => value
const variable = (name) => function (x, y, z) {
        switch (name) {
            case 'x': return x
            case 'y': return y
            case 'z': return z
        }
}

const add = func((a, b) => a + b)
const subtract = func((a, b) => a - b)
const multiply = func((a, b) => a * b)
const divide = func((a, b) => a / b)
const negate = func(a => -a)
const sin = func(a => Math.sin(a))
const cos = func(a => Math.cos(a))
const one = cnst(1)
const two = cnst(2)