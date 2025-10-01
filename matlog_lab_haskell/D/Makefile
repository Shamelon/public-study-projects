#HC=/opt/ghc/7.10.3/bin/ghc
HC=ghc
SOURCES=src/Main.hs
PACKAGE=hw0.zip

.PHONY: pack all run clean

all: parser

run: parser
	./parser

clean:
	rm -rf src/*.o src/*.hi 2>&1
	rm -f parser 2>&1

parser: $(SOURCES)
	$(HC) -i./src -tmpdir . ./src/Main.hs -o parser 2>&1

pack:
	zip $(PACKAGE) -r Makefile src 2>&1
