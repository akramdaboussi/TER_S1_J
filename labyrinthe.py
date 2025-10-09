import random
import time
import argparse
from enum import Enum

# --- Configuration ---
WIDTH = 28
HEIGHT = 31 
# Un pourcentage plus élevé donne un labyrinthe plus ouvert
IMPERFECTION_PERCENTAGE = 0.2  # 20% # configurable en paramètre

# --- Définition des états des cellules --- 
class CellState(Enum):
    MUR = 1
    SOL = 2
    MUR_PERMANENT = 3
    GHOST_HOUSE = 4
    TUNNEL = 5

# --- Classe principale du Labyrinthe --- 
class Maze: 
    def __init__(self, width, height):
        if width % 2 != 0:
            raise ValueError("La largeur doit être paire pour une symétrie parfaite.")
        self.width = width
        self.height = height
        # Au départ, tout est un mur potentiel
        self.grid = [[CellState.MUR for _ in range(width)] for _ in range(height)]

    def _apply_template(self):
        """Applique le gabarit fixe : contours, ghost house, etc."""
        # Murs permanents sur les contours
        for y in range(self.height):
            for x in range(self.width):
                if y == 0 or y == self.height - 1 or x == 0 or x == self.width - 1:
                    self.grid[y][x] = CellState.MUR_PERMANENT

        # Ghost House
        gh_x_start = self.width // 2 - 4
        gh_y_start = self.height // 2 - 2
        for y in range(gh_y_start, gh_y_start + 5):
            for x in range(gh_x_start, gh_x_start + 8):
                self.grid[y][x] = CellState.GHOST_HOUSE
                if y == gh_y_start or y == gh_y_start + 4 or x == gh_x_start or x == gh_x_start + 7:
                    self.grid[y][x] = CellState.MUR_PERMANENT

        self.grid[gh_y_start][self.width // 2 - 1] = CellState.SOL
        self.grid[gh_y_start][self.width // 2] = CellState.SOL
        
        # Tunnels
        tunnel_y = self.height // 2
        if tunnel_y % 2 != 0: tunnel_y +=1 # Le tunnel doit être sur une ligne paire pour notre modèle
        self.grid[tunnel_y][0] = CellState.TUNNEL
        self.grid[tunnel_y][self.width - 1] = CellState.TUNNEL
        self.grid[tunnel_y][1] = CellState.SOL
        self.grid[tunnel_y][self.width - 2] = CellState.SOL


    def generate(self, imperfection=IMPERFECTION_PERCENTAGE):
        """ Effectue la génération complète du labyrinthe.

        Parametètres:
        - imperfection: fraction des paires restantes à ouvrir (symétriquement)
        """
        self._apply_template()

        sets = {}
        set_counter = 0
        
        # 1. Initialiser les "salles" (coordonnées paires)
        for y in range(2, self.height - 2, 2):
            for x in range(2, self.width - 2, 2):
                if self.grid[y][x] == CellState.MUR:
                    sets[(x, y)] = set_counter
                    set_counter += 1
        
        # 2. Créer la liste des paires de murs symétriques
        wall_pairs = []
        for y in range(2, self.height - 2, 2):
            for x in range(2, self.width - 2, 2):
                if x < self.width / 2: # Moitié gauche seulement
                    # Mur vers la droite (mur en position impaire car salle paire)
                    if x + 2 < self.width:
                        wall = (x + 1, y)
                        sym_wall = (self.width - 1 - (x + 1), y)
                        cells = ((x, y), (x + 2, y))
                        sym_cells = ((self.width - 1 - x, y), (self.width - 1 - (x + 2), y))
                        wall_pairs.append({'wall': wall, 'sym_wall': sym_wall, 'cells': cells, 'sym_cells': sym_cells})
                    
                    # Mur vers le bas
                    if y + 2 < self.height:
                        wall = (x, y + 1)
                        sym_wall = (self.width - 1 - x, y + 1)
                        cells = ((x, y), (x, y + 2))
                        sym_cells = ((self.width - 1 - x, y), (self.width - 1 - x, y + 2))
                        wall_pairs.append({'wall': wall, 'sym_wall': sym_wall, 'cells': cells, 'sym_cells': sym_cells})

        random.shuffle(wall_pairs)
        remaining_wall_pairs = []

        # 3. Appliquer Kruskal symétrique
        for pair_data in wall_pairs:
            cell1, cell2 = pair_data['cells']
            set1 = sets.get(cell1)
            set2 = sets.get(cell2)

            if set1 is not None and set2 is not None and set1 != set2:
                # Creuser le passage
                wx, wy = pair_data['wall']
                self.grid[wy][wx] = CellState.SOL
                self.grid[cell1[1]][cell1[0]] = CellState.SOL
                self.grid[cell2[1]][cell2[0]] = CellState.SOL

                # Creuser le passage symétrique
                swx, swy = pair_data['sym_wall']
                self.grid[swy][swx] = CellState.SOL
                scell1, scell2 = pair_data['sym_cells']
                self.grid[scell1[1]][scell1[0]] = CellState.SOL
                self.grid[scell2[1]][scell2[0]] = CellState.SOL
                
                # Fusionner les ensembles
                target_set = sets[cell2]
                for k, v in sets.items():
                    if v == target_set:
                        sets[k] = set1
                
                sym_set1 = sets.get(scell1)
                sym_set2 = sets.get(scell2)
                if sym_set1 is not None and sym_set2 is not None:
                    target_sym_set = sets[scell2]
                    for k, v in sets.items():
                        if v == target_sym_set:
                            sets[k] = sym_set1
            else:
                remaining_wall_pairs.append(pair_data)
                
        # 4. Ajouter des imperfections (paramétrables)
        num_to_remove = int(len(remaining_wall_pairs) * imperfection)
        walls_to_break = random.sample(remaining_wall_pairs, min(num_to_remove, len(remaining_wall_pairs)))

        for pair_data in walls_to_break:
            wx, wy = pair_data['wall']
            if self.grid[wy][wx] == CellState.MUR:
                self.grid[wy][wx] = CellState.SOL

            swx, swy = pair_data['sym_wall']
            if self.grid[swy][swx] == CellState.MUR:
                self.grid[swy][swx] = CellState.SOL
    

    def __str__(self):
        """Crée une représentation textuelle du labyrinthe."""
        char_map = {
            CellState.MUR: "█",
            CellState.SOL: " ",
            CellState.MUR_PERMANENT: "█",
            CellState.GHOST_HOUSE: " ",
            CellState.TUNNEL: " "
        }
        output = ""
        for row in self.grid:
            for cell in row:
                output += char_map.get(cell, "?") * 2
            output += "\n"
        return output

# --- Exécution du script ---
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Générateur de labyrinthe Pac-Man avec options d'aléa")
    parser.add_argument('--seed', type=int, default=None, help='Seed pour la RNG (entier).')
    parser.add_argument('--imperfection', type=float, default=IMPERFECTION_PERCENTAGE, help='Fraction d\'imperfections (0..1).')
    args = parser.parse_args()

    seed = args.seed if args.seed is not None else int(time.time() * 1000)
    random.seed(seed)
    print(f"Génération du labyrinthe Pac-Man... (seed={seed})")
    maze = Maze(WIDTH, HEIGHT)
    maze.generate(imperfection=args.imperfection)
    print("Labyrinthe généré :\n")
    print(maze)