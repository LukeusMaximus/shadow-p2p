TEXMFLOCAL="/usr/share/texmf"

all :
	@pdflatex thesis.tex
	@bibtex thesis.aux
	@pdflatex thesis.tex
	@pdflatex thesis.tex
	make clean

clean    :
	@rm -f $(wildcard *.aux *.bbl *.blg *.loa *.lof *.log *.lol *.lot *.nav *.out *.snm *.toc)

spotless : clean
	@rm -f $(wildcard *.pdf)
