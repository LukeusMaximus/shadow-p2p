TEXMFLOCAL="/usr/share/texmf"

SOURCES=$(wildcard *.tex)
TARGETS=$(patsubst %.tex, %.pdf, ${SOURCES})

all : ${TARGETS}

${TARGETS} : %.pdf : %.tex $(wildcard %.bib)
	@TEXMFLOCAL="${TEXMFLOCAL}" pdflatex ${*}
	@if grep -q "bibliography" ${*}.tex       ; then \
	   TEXMFLOCAL="${TEXMFLOCAL}" bibtex ${*} ;      \
         fi
	@TEXMFLOCAL="${TEXMFLOCAL}" pdflatex ${*}
	@if grep -q "bibliography" ${*}.tex       ; then \
	   TEXMFLOCAL="${TEXMFLOCAL}" bibtex ${*} ;      \
         fi
	@TEXMFLOCAL="${TEXMFLOCAL}" pdflatex ${*}
	make clean

clean    :
	@rm -f $(wildcard *.aux *.bbl *.blg *.loa *.lof *.log *.lol *.lot *.nav *.out *.snm *.toc)

spotless : clean
	@rm -f $(wildcard *.pdf)
