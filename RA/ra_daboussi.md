Jeudi 09/10 matin :

Mon travail de recherche initial s'est concentré sur l'analyse des spécifications requises pour la génération d'un labyrinthe de type Pac-Man. 

Après l'étude de plusieurs algorithmes de base (DFS, Prim, Kruskal, Wilson et Sidewinder), il est apparu qu'une approche standard ne respecterait pas les contraintes d'un labyrinthe Pac-Man (symétrie, imperfections, boucles etc...). 
La méthodologie que j'ai pu tirer et que nous allons discuter avec mes collègues serait donc une approche hybride qui consiste à d'abord définir un gabarit fixe contenant les éléments non-négociables (contours, "Ghost House" et un tunnel pour le wraparound) afin de garantir une structure bien symétrique dès le départ et proche de la structure Pac-Man.

Ensuite, appliquer un algorithme de génération, tel que Kruskal par exemple, sur les zones à générer de manière à opérer sur des paires de murs symétriques à chaque fois garantissant la symétrie du labyrinthe. 

Et enfin appliquer une phase d'ouverture contrôlée avec les imperfections nécessaires en supprimant un nombre défini de paires de murs supplémentaires afin de se rapprocher d'une structure de labyrinthes Pac-Man.

Jeudi 09/10 après-midi :

Nous avons eu un appel de groupe où on a échangé sur les potentiels algos sur lesquels on devait partir, on a traité l'algorithme Sidewinder avancé qui était aussi une belle option mais pas vraiment adapté à une structure pac-man de part la logique derrière.

On a aussi parlé de l'algorithme évoqué dans le cours qui se base sur une approche Tetris, il nous a paru assez compliqué à configurer, on pense que ça peut être un meilleur choix que l'algorithme de Kruskal afin d'obtenir un labyrinthe très ressemblant à Pac-Man.

Pour l'instant on part sur l'approche évoqué le matin en attendant de voir si on peut modifier cette structure par l'approche Tetris.


Vendredi 10/10:
Beaucoup de réflexions sur un passage à un algo basé sur des structures fixes type 'Tétris' qu'on pose aléatoirement de manière symétrique afin d'avoir un visuel de labyrinthe se rapprochant plus du pac-man.
C'est encore en chantier, j'arrive pas à voir comment bien aborder cette démarche. 
En attendant, j'ai mis le labyrinthe généré par l'algorithme effectué la veille.

Dimanche 12/10 :
La veille nous avons vu en groupe l'avancement de l'algorithme basé sur des formes Tetris. Beaucoup de questionnement sur cette approche ont été soulevés, notamment sur la complexité du vrai algo Tetris qu'on souhaite obtenir et la logique de placement des pièces qui était pas du tout respectée et beaucoup trop complexe à développer. 
De ce fait nous avons décidé de repartir sur une approche plus classique comme on l'avait bien fait, et à partir de ça essayer de bien optimiser la logique de génération de sorte à se rapprocher dans les semaines à venir d'un labyrinthe Pac-Man.

De plus, pour des raisons de performance, d'écosystème etc, nous avons décidé de passer sur du Java plutot que de rester sur Python.

Lundi 13/10 :
Je me suis chargé de configurer l'environnement Maven et mettre en place le pom.xml pour automatiser la compilation et la gestion des dépendances du projet.
J'ai aussi mis en place le README de sorte à fournir toutes les informations nécessaires de compilation, d'execution et les commandes Maven utiles pour le développement.
Etant passé sur du Java, j'ai traduit l'algorithme de génération de labyrinthe basé sur l'algo de Kruskal du python en Java. Bien evidemment je me suis aidé de ChatGPT pour éviter de devoir tout réécrire dès le début, ce qui n'a pas trop d'interêt.
C'est pour cela qu'une refactorisation est prévue pour améliorer toute la structure du code, qui pour l'instant n'est pas "très propre".

Mercredi 15/10 :
Je me suis chargé de refactoriser MazeGenerator.java pour établir une architecture propre et plus lisible.
Le projet est maintenant organisé en packages distincts : Main.java qui est le programme principal, model/ qui contient les classes de données qui décrivent la structure du labyrinthe, à savoir Maze, CellState et Point. Enfin, generator/ qui isole la logique de l'algorithme de génération avec MazeGenerator et WallPair. 