\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{poster}[2012/05/04 UoB CS LaTeX class, page@cs.bris.ac.uk]

\RequirePackage{pgfkeys}
\RequirePackage{pgfopts}

\pgfkeys{/poster/.cd,
  % options
      author/.store in=\poster@author,
  supervisor/.store in=\poster@supervisor,
      degree/.store in=\poster@degree,
       title/.store in=\poster@title,
    subtitle/.store in=\poster@subtitle,
        year/.store in=\poster@year,
  % defaults
      author = {},
  supervisor = {},
      degree = {MEng},
       title = {},
    subtitle = {},
        year = {\the\year}
}

\ProcessPgfOptions{/poster}
\LoadClass[final]{beamer}

% see http://tug.ctan.org/tex-archive/macros/latex/contrib/beamerposter/
\RequirePackage[orientation=portrait,size=a1,scale=1.5]{beamerposter}
% see http://www.ctan.org/tex-archive/macros/latex/contrib/url
\RequirePackage{url}
% see http://www.ctan.org/tex-archive/macros/latex/contrib/hyperref
\RequirePackage{hyperref}
% see http://www.ctan.org/tex-archive/macros/latex/required/amslatex/
\RequirePackage{amsmath}
\RequirePackage{amssymb}

\makeatletter

% =============================================================================

% define the official UoB red color as an RGB triple
\definecolor{uobred}{RGB}{191,47,56}

% configure hyperref
\hypersetup{colorlinks=true,bookmarks=false,urlcolor=red}

% configure the beamer package in a way that beamerposter can cope with
\usetheme[footheight=0.7cm]{boxes} 
\usecolortheme{rose}

% configure the poster header
\setbeamertemplate{headline}{  
  \leavevmode
  \begin{beamercolorbox}[wd=\paperwidth]{headline}
  \begin{columns}[c]
  \begin{column}{.90\paperwidth}
  \begin{center}
  \vspace{4ex}
  \raggedright
  {\Huge  \poster@title}                                         \\ [4ex]
  {\Large \poster@author, supervised by \poster@supervisor}      \\ [4ex]
  {\Large University of Bristol, Department of Computer Science}
  \vspace{4ex}
  \end{center}
  \end{column}
  \end{columns}
  \end{beamercolorbox}
}

% configure the poster footer
\setbeamertemplate{footline}{
  \leavevmode
  \begin{beamercolorbox}[wd=\paperwidth]{footline}
  \begin{columns}[c]
  \begin{column}{.45\paperwidth}
  \begin{center}
  \includegraphics[scale=0.8]{logos/logo_uob_color}
  \end{center}
  \end{column}
  \begin{column}{.45\paperwidth}
  \begin{center}
  \includegraphics[scale=0.6,trim=3.0cm 8.0cm 3.0cm 8.0cm,clip=true]{logos/logo_cs_qr}
  \end{center}
  \end{column}
  \end{columns}
  \end{beamercolorbox}
}

% configure beamer to match requirements of a poster rather than slides
\setbeamercolor{headline}{bg=uobred,fg=white}
\setbeamercolor{footline}{bg=white,fg=black}

\setbeamersize{text margin left=0.0cm}
\setbeamersize{text margin right=0.0cm}
\setbeamersize{sidebar width left=0cm}
\setbeamersize{sidebar width right=0cm}

\setbeamertemplate{navigation symbols}{}
\setbeamertemplate{bibliography item}[text]

% =============================================================================

\makeatother
