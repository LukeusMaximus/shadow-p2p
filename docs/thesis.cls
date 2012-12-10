\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{thesis}[2012/05/04 UoB CS LaTeX class, page@cs.bris.ac.uk]

\RequirePackage{pgfkeys}
\RequirePackage{pgfopts}

\pgfkeys{/thesis/.cd,
  % options
      author/.store in=\thesis@author,     % string
  supervisor/.store in=\thesis@supervisor, % string
      degree/.store in=\thesis@degree,     % [BSc|MEng|MSci|MSc|PhD]
       title/.store in=\thesis@title,      % string
    subtitle/.store in=\thesis@subtitle,   % string
        year/.store in=\thesis@year,       % string
  % defaults
      author = {},
  supervisor = {},
      degree = {MEng},
       title = {},
    subtitle = {},
        year = {\the\year}
}

\ProcessPgfOptions{/thesis}
\LoadClass[10pt,a4paper,twoside]{book}

% see http://www.ctan.org/tex-archive/macros/latex/required/graphics
\RequirePackage{graphicx}
% see http://www.ctan.org/tex-archive/macros/latex/required/amslatex/
\RequirePackage{amsmath}
\RequirePackage{amssymb}

% see http://www.ctan.org/tex-archive/macros/latex/contrib/etoolbox
\RequirePackage{etoolbox}
% see http://www.ctan.org/tex-archive/macros/latex/contrib/fancyhdr
\RequirePackage{fancyhdr}
% see http://www.ctan.org/tex-archive/macros/latex/contrib/geometry
\RequirePackage{geometry}
% see http://www.ctan.org/tex-archive/macros/latex/contrib/datetime/
\RequirePackage{datetime}
% see http://www.ctan.org/tex-archive/macros/latex/contrib/algorithm2e
\RequirePackage[algochapter]{algorithm2e}
% see http://www.ctan.org/tex-archive/macros/latex/contrib/listings
\RequirePackage{listings}
% see http://www.ctan.org/tex-archive/macros/latex/contrib/url
\RequirePackage{url}
% see http://www.ctan.org/tex-archive/macros/latex/contrib/hyperref
\RequirePackage{hyperref}
% see http://www.ctan.org/tex-archive/macros/latex/contrib/lipsum
\RequirePackage{lipsum}

% see http://www.ctan.org/tex-archive/usergrps/dante/dtk
\RequirePackage{dtklogos}

\makeatletter

% =============================================================================

% configure algorithms
\DontPrintSemicolon

% configure listings
\lstset{basicstyle=\ttfamily,captionpos=b}
\renewcommand{\lstlistlistingname}{List of Listings}

% configure hyperref
\hypersetup{colorlinks=false,bookmarks=false,urlcolor=red}

% configure style of table of contents
\setcounter{tocdepth}{1}

% configure style of bibliography
\bibliographystyle{plain}

% configure style of pages
\geometry{a4paper,lmargin=2.5cm,rmargin=2.5cm,tmargin=2.5cm,bmargin=2.5cm}

\fancypagestyle{plain}{
  \fancyhf{}

  \renewcommand{\headrulewidth}{0.5pt}
  \renewcommand{\footrulewidth}{0.5pt}

  \fancyfoot[C]{\thepage}
}

\fancypagestyle{marked}{
  \fancyhf{}

  \renewcommand{\headrulewidth}{0.5pt}
  \renewcommand{\footrulewidth}{0.5pt}

  \fancyhead[LO]{\slshape \rightmark}
  \fancyhead[RE]{\slshape  \leftmark}

  \fancyfoot[C]{\thepage}
}

\pagestyle{plain}
\renewcommand{\familydefault}{\sfdefault}



% -----------------------------------------------------------------------------

% define macro to generate UoB title page

\renewcommand{\maketitle}{
  \newpage
  \thispagestyle{empty}

  \vspace*{\fill}
  \begin{center}
          \includegraphics[scale=0.3]{logos/logo_uob_color}      \\
                       \vspace{1cm}
              DEPARTMENT OF COMPUTER SCIENCE                     \\

                       \vspace{3cm}
                 {\LARGE \thesis@title}                          \\
     \ifcsempty{thesis@subtitle}{}{{\Large \thesis@subtitle \\}}

                       \vspace{1cm}
                {\Large \thesis@author}                          \\
                       \vspace{3cm}

                \rule{0.5\textwidth}{0.5pt}
                       \vspace{1cm}

  A dissertation submitted to the University of Bristol
  in accordance with the requirements of the degree of
  \mbox{\ifcsstring{thesis@degree}{BSc} {Bachelor of Science}{}%
        \ifcsstring{thesis@degree}{MEng}{Master   of Engineering}{}%
        \ifcsstring{thesis@degree}{MSci}{Master   of Science}{}%
        \ifcsstring{thesis@degree}{MSc} {Master   of Science}{}%
        \ifcsstring{thesis@degree}{PhD} {Doctor   of Philosophy}{}%
  } in the Faculty of Engineering.                                

                       \vspace{1cm}
                \rule{0.5\textwidth}{0.5pt}
                       \vspace{1cm} 

   \today, {{CS}{-}{\thesis@degree}{-}{\thesis@year}}

  \end{center}
  \vspace*{\fill}
}

% define macro to generate UoB declaration

\newcommand{\makedecl}{
  \newpage
  \thispagestyle{plain}

  \chapter*{Declaration}

  This dissertation is submitted to the University of Bristol in accordance 
  with the requirements of the degree of {\thesis@degree} in the Faculty 
  of Engineering.  It has not been submitted for any other degree or diploma 
  of any examining body.  Except where specifically acknowledged, it is all 
  the work of the Author. 

  \vspace{6cm}

  \noindent {\thesis@author}, \today
}

% define macros to mark front, main and back matter

\renewcommand{\frontmatter}{
  \cleardoublepage
  \pagestyle{plain}
  \pagenumbering{roman}
}
\renewcommand{\mainmatter}{
  \cleardoublepage
  \pagestyle{marked}
  \pagenumbering{arabic}
}
\renewcommand{\backmatter}{
  \cleardoublepage
  \pagestyle{marked}
}

% =============================================================================

\makeatother
