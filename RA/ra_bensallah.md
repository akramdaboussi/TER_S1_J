3 octobre :

Création du git, compréhension des problemes algorithmique, quelle structure utiliser,
comment modéliser les cases du labyrinthe. Départ initial vers un arbre indiquant les connexité
avec fils en sud et ouest, comme indiqué dans le cours. (Idée amenée a changer)


9 octobre  :

Analyse algo de génération Kruskal proposé par Akram, implémentation de base. L'algorithme
est intéressant mais semble à mon sens assez simpliste et ne ressemblant pas assez a un labyirnthe
pac man. Discussion avec Tarik et Akram sur des idées d'amélioration. 

10 octobre :

Enormément de discussion pour comparer les idées, notamment l'algorithme de Kruskal face a une approche théorique se basant
sur les formes tétris, nous les posons pour l'instant aléatoirement dans le labyrinthe. Nous décidons de partir sur cette approche pour l'instant
en attandant de voir si elle est viable. L'objectif par la suite serait de trouver des heuristiques permettant de savoir placer les pieces
aux bons endroits.

12 octobre : 

Passage de python à Java, ce dernier semble plus adapté pour un projet de ce type, de plus le langage nous parle plus.
Je trouve de mon coté quelques heuristiques intéressantes pour le placement des pieces, notamment quant à la variété des pieces, et la longueur des couloirs.
Akram nous indique que l'algorithme actuel, avec les pieces Tetris, est assez compliqué à implémenter, et qu'il faudrait peut être revenir à une approche plus simple.
De ce fait, nous décidons de revenir à l'algorithme de Kruskal, mais en essayant de l'améliorer pour qu'il ressemble plus à un labyrinthe de pacman.

17 octobre : 

Finalisation implémentation génération de labyrinthe, quelques améliorations encore possibles, principalement au niveau des culs-de-sacs + mise en place du JSON pour récupération API.

7 novembre : 
Gestion conversion json avec docker avec tests pour la compilation.

14 novembre : 

Suppression culs-de-sac + Ajout MangoDB. Le jeu commence a prendre forme par l'ajout de la gestion des agents ainsi que la logique du jeu.

21 novembre : 

Reflexion sur la modélisation du jeu.

28 novembre : 

Ajout game launcher, jeu jouable pour la premiere fois.

5 décembre : 

Implémentation du premier algo glouton avec un seul fantome, d'abord avec distance de Manhattan. On ajoute également la methode d'enregistrement d'une partie pour replay les mouvements de pacman.

12 décembre : 

Ajout du reste des fantomes avec l'algo glouton, avec véritables targets, partie réellement jouable.

19 décembre : 

Ajout A* pour les fantomes + selection des IA avant le début de partie. Présentation finale.