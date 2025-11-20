#!/bin/bash

# Script de test pour l'API de génération de labyrinthe

RENDER_URL="https://pacmaz-s1-j.onrender.com"
API_PATH="/api/labyrinthe" 

# Vérifie si jq (outil de formatage JSON) est installé
if command -v jq &> /dev/null
then
    JQ_CMD="jq ."
else
    JQ_CMD="cat"
fi

echo "==================================================================="
echo "API TEST: ${RENDER_URL}${API_PATH}"
echo "==================================================================="

echo ""
echo "--- TEST DE SUCCÈS : Paramètres par défaut (28x31) ---"
echo "Requête : GET ${RENDER_URL}${API_PATH}"
curl -s -X GET "${RENDER_URL}${API_PATH}" | $JQ_CMD
echo ""


echo ""
echo "--- TEST DE SUCCÈS : Demande personnalisée (30x30) ---"
QUERY="?width=30&height=30"
echo "Requête : GET ${RENDER_URL}${API_PATH}${QUERY}"
curl -s -X GET "${RENDER_URL}${API_PATH}${QUERY}" | $JQ_CMD
echo ""


echo ""
echo "--- TEST D'ÉCHEC : Largeur impaire ---"
# Teste la contrainte de symétrie (width=31) qui doit renvoyer 400 Bad Request
QUERY="?width=31&height=30"
echo "Requête : GET ${RENDER_URL}${API_PATH}${QUERY}"
echo "Attendu : Statut HTTP 400 Bad Request"
curl -s -i -X GET "${RENDER_URL}${API_PATH}${QUERY}"
echo ""