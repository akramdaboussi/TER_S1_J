import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

# Configuration du style
sns.set_theme(style="whitegrid")
plt.rcParams['figure.figsize'] = [14, 8]

def nettoyer_colonnes(df):
    """Enlève les espaces parasites dans les noms de colonnes"""
    df.columns = df.columns.str.strip()
    return df

def ajouter_tri_difficulte(df):
    """Ajoute une colonne 'Difficulty' pour trier de 0 à 4 fantômes A*"""
    # On compte le nombre de "A*" dans le string de config (ex: "G-A*-G-A*" = 2)
    df['Difficulty'] = df['GHOST_CONFIG'].apply(lambda x: str(x).count("A*"))
    
    # On trie d'abord par Difficulté (nb A*), puis par Nom pour que ce soit propre
    df = df.sort_values(by=['Difficulty', 'GHOST_CONFIG'])
    return df

def generer_courbes():
    print(">>> DÉBUT GÉNÉRATION COURBES <<<")

    # ==========================================
    # GRAPHIQUE IA vs IA (Taux de Victoire)
    # ==========================================
    fichier_ia = "benchmark_ia_results.csv"
    if os.path.exists(fichier_ia):
        print(f"\nTraitement de {fichier_ia}...")
        try:
            df = pd.read_csv(fichier_ia, sep=';')
            df = nettoyer_colonnes(df)

            if 'RESULT' in df.columns:
                # Convertir WIN/LOSE en 0/1
                df['IS_WIN'] = df['RESULT'].apply(lambda x: 1 if str(x).strip() == 'WIN' else 0)
                
                # Calculer la moyenne par Config
                stats = df.groupby('GHOST_CONFIG')['IS_WIN'].mean() * 100
                stats = stats.reset_index()

                # Trier (Du plus facile au plus dur)
                stats = ajouter_tri_difficulte(stats)

                # Dessiner la courbe
                plt.figure()
                # On trace la ligne avec des points (marker='o')
                plt.plot(stats['GHOST_CONFIG'], stats['IS_WIN'], marker='o', linestyle='-', linewidth=2, color='b')
                
                # Ajout d'une zone colorée sous la courbe 
                plt.fill_between(stats['GHOST_CONFIG'], stats['IS_WIN'], color='b', alpha=0.1)

                plt.title('Performance de l\'IA Pac-Man', fontsize=16)
                plt.ylabel('Taux de Victoire (%)', fontsize=12)
                plt.xlabel('Confguration des fantômes', fontsize=12)
                
                # Rotation des étiquettes en bas pour qu'elles soient lisibles
                plt.xticks(rotation=45, ha='right')
                plt.grid(True, linestyle='--', alpha=0.7)
                plt.tight_layout()
                
                plt.savefig('courbe_victoire_ia.png')
                print(" -> SUCCÈS : courbe_victoire_ia.png créé.")
            else:
                print("Colonne RESULT manquante.")
        except Exception as e:
            print(f"Erreur IA: {e}")


    # ==========================================
    # GRAPHIQUE REPLAY (Score Moyen)
    # ==========================================
    fichier_replay = "benchmark_replay_score.csv"
    if os.path.exists(fichier_replay):
        print(f"\nTraitement de {fichier_replay}...")
        try:
            df = pd.read_csv(fichier_replay, sep=';')
            df = nettoyer_colonnes(df)

            # Trouver la colonne Score
            col_score = 'SCORE_FINAL' if 'SCORE_FINAL' in df.columns else 'SCORE'

            # Calculer la moyenne par Config
            stats = df.groupby('GHOST_CONFIG')[col_score].mean().reset_index()

            # Trier (Du plus facile au plus dur)
            stats = ajouter_tri_difficulte(stats)

            # Dessiner la courbe
            plt.figure()
            plt.plot(stats['GHOST_CONFIG'], stats[col_score], marker='o', linestyle='-', linewidth=2, color='r')
            plt.fill_between(stats['GHOST_CONFIG'], stats[col_score], color='r', alpha=0.1)

            plt.title('Score Moyen du Joueur (Simulation)', fontsize=16)
            plt.ylabel('Score Moyen', fontsize=12)
            plt.xlabel('Configuration des fantômes', fontsize=12)
            
            plt.xticks(rotation=45, ha='right')
            plt.grid(True, linestyle='--', alpha=0.7)
            plt.tight_layout()

            plt.savefig('courbe_score_replay.png')
            print(" -> SUCCÈS : courbe_score_replay.png créé.")

        except Exception as e:
            print(f"Erreur Replay: {e}")

if __name__ == "__main__":
    generer_courbes()