## Jeudi 09/10:
Mon travail de recherche initial s'est concentré sur l'analyse des spécifications requises pour la génération d'un labyrinthe de type Pac-Man.
Après l'étude de plusieurs algorithmes de base (DFS, Prim, Kruskal, Wilson et Sidewinder), il est apparu qu'une approche standard ne respecterait pas les contraintes d'un labyrinthe Pac-Man (symétrie, imperfections, boucles etc...). 
La méthodologie que j'ai pu tirer et que nous allons discuter avec mes collègues serait donc une approche hybride qui consiste à d'abord définir un gabarit fixe contenant les éléments non-négociables (contours, "Ghost House" et un tunnel pour le wraparound) afin de garantir une structure bien symétrique dès le départ et proche de la structure Pac-Man.

Ensuite, appliquer un algorithme de génération, tel que Kruskal par exemple, sur les zones à générer de manière à opérer sur des paires de murs symétriques à chaque fois garantissant la symétrie du labyrinthe. 

Et enfin appliquer une phase d'ouverture contrôlée avec les imperfections nécessaires en supprimant un nombre défini de paires de murs supplémentaires afin de se rapprocher d'une structure de labyrinthes Pac-Man.

L'après-midi ous avons eu un appel de groupe où on a échangé sur les potentiels algos sur lesquels on devait partir, on a traité l'algorithme Sidewinder avancé qui était aussi une belle option mais pas vraiment adapté à une structure pac-man de part la logique derrière.

On a aussi parlé de l'algorithme évoqué dans le cours qui se base sur une approche Tetris, il nous a paru assez compliqué à configurer, on pense que ça peut être un meilleur choix que l'algorithme de Kruskal afin d'obtenir un labyrinthe très ressemblant à Pac-Man.
Pour l'instant on part sur l'approche évoqué le matin en attendant de voir si on peut modifier cette structure par l'approche Tetris.

## Vendredi 10/10:
Beaucoup de réflexions sur un passage à un algo basé sur des structures fixes type 'Tétris' qu'on pose aléatoirement de manière symétrique afin d'avoir un visuel de labyrinthe se rapprochant plus du pac-man.
C'est encore en chantier, j'arrive pas à voir comment bien aborder cette démarche. 
En attendant, j'ai mis le labyrinthe généré par l'algorithme effectué la veille.

## Dimanche 12/10 :
La veille nous avons vu en groupe l'avancement de l'algorithme basé sur des formes Tetris. Beaucoup de questionnement sur cette approche ont été soulevés, notamment sur la complexité du vrai algo Tetris qu'on souhaite obtenir et la logique de placement des pièces qui était pas du tout respectée et beaucoup trop complexe à développer. 
De ce fait nous avons décidé de repartir sur une approche plus classique comme on l'avait bien fait, et à partir de ça essayer de bien optimiser la logique de génération de sorte à se rapprocher dans les semaines à venir d'un labyrinthe Pac-Man.

De plus, pour des raisons de performance, d'écosystème etc, nous avons décidé de passer sur du Java plutot que de rester sur Python.

## Lundi 13/10 :
Je me suis chargé de configurer l'environnement Maven et mettre en place le pom.xml pour automatiser la compilation et la gestion des dépendances du projet.
J'ai aussi mis en place le README de sorte à fournir toutes les informations nécessaires de compilation, d'execution et les commandes Maven utiles pour le développement.
Etant passé sur du Java, j'ai traduit l'algorithme de génération de labyrinthe basé sur l'algo de Kruskal du python en Java. Bien evidemment je me suis aidé de ChatGPT pour éviter de devoir tout réécrire dès le début, ce qui n'a pas trop d'interêt.
C'est pour cela qu'une refactorisation est prévue pour améliorer toute la structure du code, qui pour l'instant n'est pas "très propre".

## Mercredi 15/10 :
Je me suis chargé de refactoriser MazeGenerator.java pour établir une architecture propre et plus lisible.
Le projet est maintenant organisé en packages distincts : Main.java qui est le programme principal, model/ qui contient les classes de données qui décrivent la structure du labyrinthe, à savoir Maze, CellState et Point. Enfin, generator/ qui isole la logique de l'algorithme de génération avec MazeGenerator et WallPair. 
De plus j'ai commencé à regarder comment représenter le JSON pour représenter le maze afin de se préparer à la séance du vendredi.

## Jeudi 16/10 : 
Aujourd'hui je me suis chargé d'effectuer plusieurs tâches, tout d'abord j'ai complètement réorganisé la base du code pour qu'elle soit en accord avec la convention de Maven (src/main/java et modif du pom.xml)
J'ai organisé les classes suivant une architecture MVC clarifiant mieux l'architecture du projet.

Par la suite, ma principale tâche a été de passer d'une représentation textuelle du labyrinthe généré à un format JSON qui j'imagine va nous servir par la suite. J'ai donc enlevé la méthode toString() et écrit une méthode toJsonString() afin de pouvoir convertir la grille CellState[][] en une représentation numérique int[][] afin de pouvoir par la suite formater le JSON.
Une classe de transfert de données a été créé pour structurer proprement les informations avant la conversion.
Enfin, afin de représenter graphiquement le JSON qui représente le labyrinthe généré, je me suis aidé de ChatGPT pour créer la classe MazeVisualizerPanel qui va dessiner le labyrinthe. 
Bien evidemment j'ai restructuré le Main avec les nouvelles modif pour avoir un programme bien fonctionnel.

## Vacances + Lundi 03/11 Mardi 04/11 Mercredi 05/11 
Je me suis chargé de mettre en place l'API avec Spark Java et j'ai créé le dockerfile pour le déploiement Render.

Par la suite j'ai écris des tests unitaires pour les labyrinthes et des tests d'intégration pour l'API que j'ai mis dans un dossier test/ afin de pouvoir s'assurer que seul un code fonctionnel qui valide les tests ne soit déployé dans le cloud. Du coup, pour ce qui est du CI/CD j'ai initialisé Github Action dans le maven.yml pour les tests automatisés et j'ai optimisé le dockerfile pour le déploiement.

## Jeudi 06/11
Une fois l'architecture déployée, je me suis chargé de faire en sorte que le client puisse récupérer et parser le JSON de l'API et de l'afficher graphiquement.

## Vendredi 07/11 Samedi 08/11
Aujourd'hui je me suis chargé d'améliorer le rendu graphique du labyrinthe grace à BasicStroke et d'optimiser le labyrinthe pour qu'il soit bien jouable. Je me suis assuré que le chemin vers la ghost house soit libéré et que le contour de la ghost house soit libéré aussi pour un rendu épuré et aussi qu'il y'ait une ouverture sur les deux cotés du tunnels pour permettre au pacman d'emprunter plusieurs chemins.

## Dimanche 09/11
Aujourd'hui je me suis chargé de développer une méthode plus optimisé de suppression de culs-de-sac. Avant on utilisait un pourcentage de cassage de mur pour créer des boucles au hasard et donc minimiser le nombre de culs-de-sac. Cette fois pour s'assurer qu'il n'yait plus de cul-de-sac j'ai implémenté une méthode qui s'assure que chaque sol doit avoir au moins deux sol adjacents, si ce n'est pas le cas on casse un mur adjacent aléatoirement pour casser les culs-de-sac.

## Dimanche 09/11 Lundi 10/11
Durant ces deux jours je me suis chargé de ma création et de l'intégration de MongoDB afin de pouvoir stocker nos labyrinthes dans une base de donnée dans le cloud. J'ai fait en sorte de générer des identifiants uniques pour chaque labyrinthe afin que chaque labyrinthe soit bien distingué dans la base de donnée.
De plus j'ai ajouté la fonctionnalité de saisie et d'envoi des notes afin que le client puisse effectuer des requetes HTTP POST vers le serveur afin de pouvoir mettre à jour la note du labyrinthe dans la base de donnée.

## Jeudi 20/11 Vendredi 21/11 Samedi 22/11 Lundi 24/11
Ces jours-là, je me suis chargé de refactoriser la partie de conception de jeu effectué par Younes. J'ai encapsulé toute la logique HTTP dans un GameClient et j'ai utilisé un DTO pour pouvoir partager l'état du jeu. Je me suis chargé aussi de réparer plusieurs bugs qui étaient présents tels que des bugs de pellets manquants, du placement des pellets, du positionnement, du mouvement continu du pacman etc...

## Jeudi 27/11 Vendredi 28/11
En début de semaine j'avais décidé de déléguer la logique de jeu à l'API, cependant cela m'a posé beaucoup de problèmes étant donné que le labyrinthe affiché n'était pas le meme que celui sur lequel on jouait car c'est deux requetes différentes vers deux url différents. C'est ce qui nous posait des problèmes entre la jouabilité et l'affichage, du coup on décide de basculer sur une boucle de jeu locale qui est nettement meilleure, plus fluide et c'est ce qui était demandé.
J'ai aussi ajouté un constructeur Maze afin de pouvoir bien reconstruire le labyrinthe coté cient.

## Jeudi 04/12 Vendredi 05/12
Ces deux jours, je me suis chargé d'implémenter les boucles d'enregistrement et de simulation afin de pouvoir simuler les mouvements du pacman pré-enregistré contre le premier algo d'ia fantome implémenté par younes.
Je me suis aussi chargé d'ajouter donc le mode replay/record dans le jeu, gérer la gestion des tunnels dans le jeu et implémenter le comportement de fuite des fantomes quand un pacman mange un power-pellet.

## Lundi 08/12 Mardi 09/12
Durant ces deux jours je me suis chargé de refactoriser et réparer les différents bugs dans le code. J'ai implémenté les états IN_HOUSE, EXITING et CHASE et des timers pour différer la sortie de chaque fantôme pour une meilleure jouabilité. J'ai ajouté une cellule PORTE pour faire en sorte que le pacman ne touche pas la porte de sortie des fantomes et aussi pour que les fantomes ne puisse pas y retourner si jamais ils sont dehors, à part si ils se font manger ou mangent le pacman. J'ai corrigé la logique IA dans GameLogic.

De plus, je me suis chargé d'optimiser le rendu graphique du labyrinthe pour avoir un rendu Retro très fidèle au vrai Pac-Man avec affichage dybnamique du score, du mode de jeu et du nombre de vie restants au pacman.

## Mercredi 10/12
Aujourd'hui, je me suis chargé de plusieurs points. 
D'abord j'ai créé une classe Ghost pour gérer proprement les état et positions des fantomes. 

J'ai aussi optmisié la gestion des collisions entre le pacman et le fantome de sorte à ce qu'il y'est une double vérification de la position t et t+1 du fantome dans le cas où ils se croisent sans etre sur la meme cellule au même temps. J'ai aussi fait en sorte qu'il y'est une pause dans le jeu lorsque une entité se fait manger et retour à la ghost house pour les fantomes à chaque collision pour une meilleure jouabilité.

J'ai aussi fait en sorte qu'on puisse générer des labyrinthes de dimensions différentes et forcément avec ce changement j'ai aussi changé le positionnement des entités. Maintenant c'est plus codé en dur mais plutot calculé dynamiquement en fonction des dimensions du labyrinthe afin de garantir le bon positionnement des entités.

J'ai aussi ajouté le mode de jeu direct où on peut jouer directement contre les fantomes avec le clavier et aussi garder le mode enregistrement où durant la simulation les écouteurs aux claviers sont forcément désactivés.

## Vendredi 12/12 Samedi 13/12 Dimanche 14/12 Lundi 15/12
Durant ces jours j'ai réalisé plusieurs recherches et visionné plusieurs vidéos concernant l'IA gaming et les algorithmes d'IA minimax, minimax avec alpha beta et expectimax. Etant donné qu'on avait des IA fantomes basé sur un algo glouton et des IA antomes basé sur A* qui est plus optimisé, le choix de l'IA pacman devait s'adapter à l'optimalité des fantomes en face.

## Semaine d'immersion
Durant les quelques jours restants, j'ai implémenté la possibilité de pouvoir configurer les IA des fantomes pour avoir 16 combinaisons possibles de fantomes contre lesquels on peut jouer. Pour ce qui est de l'IA Pacman, je me suis rendu compte qu'étant donné que les algos d'IA des fantomes étaient déterministes et que dans la plupart des situations ils effectuaient directement le mouvement qui minimise le score du pacman, il était donc inutile de simuler toutes les combinaisons possibles des mouvements à chaque fois donc ce que j'ai décidé de faire c'est de faire une copie de l'état du jeu actuel et d'executer les mouvements futurs des fantomes sans que ça soit sur la partie originale. Ainsi, grâce à ça j'ai pu implémnter minimax avec alpha beta où le pacman essaiera de maximiser son score contre des fantomes qui de part leur mouvement déterministe minimise déjà le score du pacman. J'ai attribué des scores à chaque événement possible (manger une pastille, se rapprocher d'un fantome etc...).
Aussi, afin d'éviter que le pacman se retrouve dans une boucle infinie où c'est sa seule solution pour maximiser son score j'ai implémenté une liste qui va mémoriser la position du pacman et dès qu'il se retrouve dans la même position plus de 12 fois cela minimise beaucoup son score de sorte à le pousser à sortir de cette boucle avec certes le risque de se faire tuer mais au moins cela va pouvoir débloquer le jeu. Pour ce qui est des cas des fantomes effrayés, étant donné qu'ils ne sont pas déterministes mais totalement aléatoire, cette fois au lieu d'effectuer une seule simulation on en effectue 5 afin que les fantomes puissent choisir la direction qui minimise le plus le score du pacman.

Enfin, pour pouvoir tester les performances de l'IA Pacman face aux différentes configurations de fantomes et les performances des IA fantomes face à des mouvements pré-enregistrés, j'ai implémenté deux benchmarks qui vont se charger de simuler 100 fois l'execution d'une partie pour chaque configuration afin d'avoir un pourcentage des victoires de l'IA Pacman face aux IA fantomes et une moyenne de score du pacman pré-enregistré face aux configurations des fantomes. Ces benchmarks vont générer des csv qui par la suite pourront etre transformés en graphique lisible grace à graphique.py.




