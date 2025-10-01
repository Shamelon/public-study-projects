// errors
function ParseError(message, position1 = null, position2 = null) {
    this.message = message
    if (position1 != null) { this.message += " in position " + position1 }
    if (position2 != null) { this.message += " and " + position2 }
    Error.call(this, message)
}
ParseError.prototype = Object.create(Error.prototype)
ParseError.prototype.name = "ParseError"
function CountOfArgsError(operation, expected, found) {
    this.message = "For operation \"" + operation + "\" expected " + expected + " arguments, found " + found
    Error.call(this, this.message)
}
CountOfArgsError.prototype = Object.create(Error.prototype)
CountOfArgsError.prototype.name = "CountOfArgsError"


// operations' prototype
function Op() {}
Op.prototype = {
    evaluate: function(x, y, z) {
        if (this.expr.length === 0) {
            return 0;
        } else if (this.expr.length === 1) {
            return this.f(this.expr[0].evaluate(x, y, z), 0);
        } else {
            const result = this.expr.slice(1).reduce(
                (ac, a) => this.f(ac, a.evaluate(x, y, z)),
                this.expr[0].evaluate(x, y, z)
            );
            return this.resultFunc(result)
        }
    },
    toString: function() {
        let result = ''
        for(let i = 0; i < this.expr.length; i ++) {
            result += this.expr[i].toString() + ' '
        }
        result += this.opName
        return result
    },
    prefix: function() {
        let result = '(' + this.opName
        if (this.expr.length === 0) { result += ' '}
        for(let i = 0; i < this.expr.length; i ++) {
            result +=  ' ' + this.expr[i].prefix()
        }
        result += ')'
        return result
    }

}

// creator of operations
const create = function (f, opName, doFunc = false) {
    const func = function (...expr) {
        this.f = f
        this.expr = expr
        this.opName = opName
        if (doFunc) {
            this.resultFunc = a => a / expr.length
        } else {
            this.resultFunc = a => a
        }
    }
    func.prototype = Object.create(Op.prototype)
    return func
}


// operations
const Add =  create((a, b) => a + b, "+")
const Subtract =  create((a, b) => a - b, "-")
const Multiply =  create((a, b) => a * b, "*")
const Divide =  create((a, b) => a / b, "/")
const Negate =  create(a => -a, "negate")
const Exp =  create(a => Math.exp(a), "exp")
const Ln =  create(a => Math.log(a), "ln")
const Sum = create((a, b) => a + b, "sum")
const Avg = create((a, b) => a + b, "avg", true)

// Const
function Const(value) {
    this.evaluate = () => value
    this.toString = () => value.toString()
    this.prefix = () => value.toString()
}

// Variable
function Variable(name) {
    this.evaluate = (x, y, z) => {
        switch (name) {
            case 'x': return x
            case 'y': return y
            case 'z': return z
        }
    }
    this.toString = () => name
    this.prefix = () => name
}

// function for map
// Если операция принимает неограниченное кол-во аргументов, то nArgs = -1
const operate = (operation, nArgs, stringOp) => stack => {
    let n = nArgs
    if (nArgs === -1) { n = stack.length }
    if (n === 0) { return new operation() }
    if (stack.length >= n) {
        let opArgs = [];
        for (let i = n; i > 1; i --) {
            opArgs.push(stack[stack.length - i])
        }
        opArgs.push(stack.splice(stack.length - n)[n - 1])
        return new operation(...opArgs);
    } else {
        throw new CountOfArgsError(stringOp, n, stack.length)
    }
}

// Map for parsers
const variables = ["x", "y", "z"]

const exprMap = new Map([
    ["negate", operate(Negate, 1, "negate")],
    ["exp", operate(Exp, 1, "exp")],
    ["ln", operate(Ln, 1, "ln")],
    ["+", operate(Add, 2, "+")],
    ["-", operate(Subtract, 2, "-")],
    ["*", operate(Multiply, 2, "*")],
    ["/", operate(Divide, 2, "/")],
    ["sum", operate(Sum, -1, "sum")],
    ["avg", operate(Avg, -1, "avg")]
])

// parse
const parse = function (expr) {
    let stack = []
    expr = expr.split(' ').filter(i => !!i)
    for (let i = 0; i < expr.length; i ++) {
        if (!isNaN(expr[i])) {
            stack.push(new Const(parseInt(expr[i])))
        } else if (variables.includes(expr[i])) {
            stack.push(new Variable(expr[i]))
        } else {
            stack.push(exprMap.get(expr[i])(stack))
        }
    }
    return stack[0]
}

// =========================== parsePrefix ===========================
// Разделение на элементы: скобки и последовательности непробельных символов
const separators = [' ', '(', ')', undefined]
const parseElements = (expr) => {
    let elements = []
    let start = 0
    for (let i = 0; i < expr.length + 1; i ++) {
        if (separators.includes(expr[i])) {
            if (start < i) {
                elements.push(expr.slice(start, i))
            }
            start = i + 1
            if ((expr[i] === '(') || (expr[i] === ')')) {
                elements.push(expr[i])
            }
        }
    }
    return elements
}

// Рекурсивный парсер
const prefixParser = (elements, position, isBracket) => {
    let stack = []
    let op = undefined;
    let opPosition = undefined;
    let isClosed = false
    let endPosition = elements.length
    // Выделение операции, переменных и костант
    for (let i = position; i < elements.length; i ++) {
        if (!isNaN(elements[i])) {
            stack.push(new Const(parseInt(elements[i])))
        } else if (variables.includes(elements[i])) {
            stack.push(new Variable(elements[i]))
        } else if (elements[i] === '(') {
            let result = prefixParser(elements, i + 1, true)
            stack.push(result[0])
            i = result[1]
        } else if (elements[i] === ')') {
            isClosed = true
            endPosition = i
            break
        } else {
            if (op === undefined) {
                op = elements[i]
                opPosition = i + 1
            } else {
                throw new ParseError("Unexpected operation \"" + op + "\"", i + 1)
            }
        }
    }
    // Проверка на правильность постановки скобок
    if (!isBracket && isClosed) {
        throw new ParseError("Unexpected \")\" after \"" + elements[endPosition - 1] + "\"", endPosition)
    } else if (isBracket && !isClosed) {
        throw new ParseError("Unclosed \"(\" before \"" + elements[position] + "\"", position + 1)
    }
    // Пустота
    if (stack.length === 0 && op === undefined) {
        if (isBracket) {
            throw new ParseError("Empty brackets", position, position + 1)
        }
        throw new ParseError("Empty input")
    }
    // Работа с одиночными переменными и константами
    if (op === undefined) {
        if (stack.length === 1) {
            if (isBracket) {
                throw new ParseError("Unexpected brackets around \"" +
                    stack[0].prefix() + "\"", endPosition)
            }
            return [stack[0], endPosition]
        } else if (stack.length === 2) {
            throw new ParseError("Expected binary operation before \"" +
                stack[0].prefix() + "\"", position + 1)
        } else if (stack.length > 2) {
            throw new ParseError("Expected operation of " + stack.length +
                " arguments before \"" + stack[0].prefix() + "\"",position + 1)
        }
    // Применение операции
    } else {
        let stackOldLength = stack.length
        const expr = exprMap.get(op);
        if (expr === undefined) {
            if (stack.length === 0) {
                throw new ParseError("Unknown object \"" + op + "\"", opPosition)
            } else if (stack.length === 1) {
                throw new ParseError("Unknown unary operation \"" + op + "\"", opPosition)
            } else if (stack.length === 2) {
                throw new ParseError("Unknown binary operation \"" + op + "\"", opPosition)
            }
        } else {
            stack.push(expr(stack))
        }
    }
    if (stack.length === 1) {
        return [stack[0], endPosition]
    } else {
        // Много аргументов
        throw new CountOfArgsError(op,  stackOldLength - stack.length + 1, stackOldLength)
    }
}

// Основной парсер
const parsePrefix = (expr) => {
    const elements = parseElements(expr)
    return prefixParser(elements, 0, false)[0]
}
// ===================================================================
