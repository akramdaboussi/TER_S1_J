const API_BASE = ''; // si front & API sur même domaine Render ;
const gridEl = document.getElementById('grid');
const form = document.getElementById('controls');

const STATE = {
    SOL: 0, MUR: 1, MUR_PERMANENT: 2, GHOST_HOUSE: 3, TUNNEL: 4
};
const CLASS_BY_STATE = {
    [STATE.SOL]: 'solid',
    [STATE.MUR]: 'wall',
    [STATE.MUR_PERMANENT]: 'perm',
    [STATE.GHOST_HOUSE]: 'wall',   // option: autre couleur si vous voulez
    [STATE.TUNNEL]: 'tunnel'
};

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const width = +document.getElementById('width').value;
    const height = +document.getElementById('height').value;
    const imperfection = +document.getElementById('imperfection').value;

    await loadMaze({ width, height, imperfection });
});

async function loadMaze({ width=30, height=30, imperfection=0.1 } = {}) {
    gridEl.textContent = 'chargement...';
    try {
        const url = `${API_BASE}/api/labyrinthe?width=${width}&height=${height}&imperfection=${imperfection}`;
        const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        renderGrid(data);
    } catch (err) {
        gridEl.textContent = `Erreur: ${err.message}`;
    }
}

function renderGrid({ width, height, grid }) {
    // CSS Grid : colonnes dynamiques
    gridEl.style.gridTemplateColumns = `repeat(${width}, var(--cell))`;
    gridEl.innerHTML = '';

    // Construire les cellules
    // grid est supposé être une matrice [height][width] d'entiers
    for (let y = 0; y < height; y++) {
        const row = grid[y];
        for (let x = 0; x < width; x++) {
            const v = row[x];
            const div = document.createElement('div');
            div.className = `cell ${CLASS_BY_STATE[v] ?? 'wall'}`;
            div.title = `(${x},${y}) = ${v}`;
            gridEl.appendChild(div);
        }
    }
}

// premier rendu
loadMaze();
