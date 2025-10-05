package org.example.duel;

import org.example.model.Card;
import org.example.listeners.BattleListener;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * ¡Maneja la lógica principal del duelo de Yu-Gi-Oh!
 * Implementa las reglas del juego, turnos, y resolución de batallas.
 */
public class Duel {
    // Puntuación necesaria para ganar el duelo
    private static final int WINNING_SCORE = 2;

    // Número máximo de rondas por duelo
    private static final int MAX_ROUNDS = 3;

    // Cartas asignadas a cada jugador
    private List<Card> playerCards;
    private List<Card> aiCards;

    // Puntuaciones actuales
    private int playerScore;
    private int aiScore;

    // Listener para eventos del duelo
    private BattleListener listener;

    // Generador de números aleatorios
    private Random random;

    // Control de turnos
    private boolean playerTurn;
    private boolean gameStarted;
    private boolean waitingForPlayer;
    private int currentRound;

    // Modos de batalla: true = ataque, false = defensa
    private boolean playerAttackMode;
    private boolean aiAttackMode;

    // Cartas seleccionadas en la ronda actual
    private Card playerSelectedCard;
    private Card aiSelectedCard;
    private boolean playerHasSelected;
    private boolean aiHasSelected;

    // Control de cartas usadas y disponibles
    private List<Card> availablePlayerCards;
    private List<Card> availableAiCards;
    private List<Card> usedPlayerCards;
    private List<Card> usedAiCards;

    /**
     * Constructor que inicializa el estado del duelo.
     * El turno inicial es aleatorio.
     */
    public Duel() {
        this.playerCards = new ArrayList<>();
        this.aiCards = new ArrayList<>();
        this.random = new Random();
        this.playerScore = 0;
        this.aiScore = 0;
        this.gameStarted = false;
        this.waitingForPlayer = false;
        this.currentRound = 1;
        this.playerTurn = random.nextBoolean();
        this.playerAttackMode = true;
        this.aiAttackMode = true;
        this.playerSelectedCard = null;
        this.aiSelectedCard = null;
        this.playerHasSelected = false;
        this.aiHasSelected = false;

        this.availablePlayerCards = new ArrayList<>();
        this.availableAiCards = new ArrayList<>();
        this.usedPlayerCards = new ArrayList<>();
        this.usedAiCards = new ArrayList<>();
    }

    /**
     * Establece el listener para recibir eventos del duelo.
     * @param listener Objeto que implementa BattleListener
     */
    public void setBattleListener(BattleListener listener) {
        this.listener = listener;
    }

    /**
     * Asigna las cartas del jugador.
     * @param cards Lista de 3 cartas para el jugador
     */
    public void setPlayerCards(List<Card> cards) {
        if (cards == null || cards.size() != 3) {
            notifyError("El jugador debe tener exactamente 3 cartas");
            return;
        }

        for (Card card : cards) {
            if (!card.isValidMonster()) {
                notifyError("Cartas del jugador inválidas: " + card.getName());
                return;
            }
        }

        this.playerCards.clear();
        this.playerCards.addAll(cards);
        this.availablePlayerCards.clear();
        this.availablePlayerCards.addAll(cards);
        this.usedPlayerCards.clear();

        checkIfReadyToStart();
    }

    /**
     * Asigna las cartas de la máquina.
     * @param cards Lista de 3 cartas para la máquina
     */
    public void setAiCards(List<Card> cards) {
        if (cards == null || cards.size() != 3) {
            notifyError("La máquina debe tener exactamente 3 cartas");
            return;
        }

        for (Card card : cards) {
            if (!card.isValidMonster()) {
                notifyError("Cartas de la máquina inválidas: " + card.getName());
                return;
            }
        }

        this.aiCards.clear();
        this.aiCards.addAll(cards);
        this.availableAiCards.clear();
        this.availableAiCards.addAll(cards);
        this.usedAiCards.clear();
        checkIfReadyToStart();
    }

    /**
     * Inicia el duelo si ambos jugadores tienen sus cartas listas.
     * Realiza un reset completo del estado y determina el turno inicial aleatorio.
     */
    public void startDuel() {
        // Reset completo del estado
        this.gameStarted = true;
        this.currentRound = 1;
        this.playerScore = 0;
        this.aiScore = 0;
        this.playerHasSelected = false;
        this.aiHasSelected = false;
        this.playerSelectedCard = null;
        this.aiSelectedCard = null;

        // Determinar turno aleatorio
        this.playerTurn = random.nextBoolean();
        this.waitingForPlayer = playerTurn;

        System.out.println("=== DUELO INICIADO ===");
        System.out.println("Primer turno: " + (playerTurn ? "JUGADOR" : "MÁQUINA"));
        System.out.println("Cartas jugador: " + playerCards.size());
        System.out.println("Cartas máquina: " + aiCards.size());

        if (listener != null) {
            String starter = playerTurn ? "Jugador" : "Máquina";
            listener.onDuelStarted(starter, currentRound);
            listener.onRoundStarted(currentRound, playerScore, aiScore);
            listener.onTurnStarted(starter, playerTurn);
        }

        // Si la máquina empieza, ejecutar su turno
        if (!playerTurn) {
            System.out.println("Ejecutando turno inicial de la máquina...");
            startMachineTurn();
        }
    }

    /**
     * Maneja el turno de la máquina de forma controlada con un pequeño delay.
     */
    private void startMachineTurn() {
        System.out.println("startMachineTurn() - Ronda " + currentRound);

        if (playerTurn || aiHasSelected) {
            System.out.println("ERROR: No es turno de la máquina y ya seleccionó");
            return;
        }

        // Pequeño delay para simular "pensamiento"
        Timer timer = new Timer(500, e -> {
            aiPlaysTurn();
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Procesa la selección de carta del jugador.
     * @param cardIndex Índice de la carta seleccionada (0-2)
     */
    public void playerSelectsCard(int cardIndex) {
        System.out.println("=== EL JUGADOR SELECCIONA LA CARTA ===");

        if (!gameStarted) {
            notifyError("El duelo no ha comenzado");
            return;
        }

        if (!playerTurn || !waitingForPlayer) {
            notifyError("No es tu turno");
            return;
        }

        if (playerHasSelected) {
            notifyError("Ya seleccionaste una carta para esta ronda");
            return;
        }

        // Verificar que la carta esté disponible
        if (cardIndex < 0 || cardIndex >= playerCards.size()) {
            notifyError("Índice de carta inválido");
            return;
        }

        Card selectedCard = playerCards.get(cardIndex);

        // Verificar que la carta no haya sido usada
        if (!availablePlayerCards.contains(selectedCard)) {
            notifyError("Esta carta ya fue usada en este duelo");
            return;
        }

        // Obtener carta del jugador
        playerSelectedCard = selectedCard;
        playerHasSelected = true;

        // Marcar carta como usada
        availablePlayerCards.remove(selectedCard);
        usedPlayerCards.add(selectedCard);

        System.out.println("Jugador seleccionó: " + playerSelectedCard.getName() +
                " - Cartas disponibles: " + availablePlayerCards.size());

        // El jugador elige modo aleatorio
        playerAttackMode = random.nextBoolean();

        if (listener != null) {
            listener.onCardSelected("Jugador", playerSelectedCard.getName(), cardIndex);
            listener.onBattleModeSet("Jugador", playerAttackMode);
        }

        // Cambiar estado
        this.playerTurn = false;
        this.waitingForPlayer = false;

        if (aiHasSelected) {
            System.out.println("Máquina ya seleccionó - resolviendo batalla inmediata");
            resolveBattle(playerSelectedCard, aiSelectedCard);
        } else {
            System.out.println("Ejecutando turno de máquina...");
            aiPlaysTurn();
        }
    }

    /**
     * Ejecuta el turno de la máquina.
     * Selecciona una carta aleatoria y modo de batalla aleatorio.
     */
    private void aiPlaysTurn() {
        System.out.println("=== LA MÁQUINA JUEGA POR TURNO ===");

        if (!gameStarted || playerTurn || aiHasSelected) {
            System.out.println("Máquina: No puede jugar en este estado");
            return;
        }

        // Verificar que haya cartas disponibles
        if (availableAiCards.isEmpty()) {
            notifyError("La máquina no tiene cartas disponibles");
            return;
        }

        // La máquina elige carta aleatoria inmediatamente
        int aiCardIndex = random.nextInt(availableAiCards.size());
        aiSelectedCard = availableAiCards.get(aiCardIndex);
        aiHasSelected = true;

        // Marcar carta como usada
        availableAiCards.remove(aiSelectedCard);
        usedAiCards.add(aiSelectedCard);

        // La máquina elige modo aleatorio
        aiAttackMode = random.nextBoolean();

        System.out.println("Máquina: Seleccionó - " + aiSelectedCard.getName() +
                " - Cartas disponibles: " + availableAiCards.size());

        if (listener != null) {
            listener.onCardSelected("Máquina", aiSelectedCard.getName(), -1);
            listener.onBattleModeSet("Máquina", aiAttackMode);
            listener.onStatusUpdate("Máquina seleccionó: " + aiSelectedCard.getName(), false);
        }

        // Forzar la resolución de batalla si el jugador ya seleccionó
        if (playerHasSelected) {
            System.out.println("Jugador ya seleccionó - resolviendo batalla inmediata");
            resolveBattle(playerSelectedCard, aiSelectedCard);
        } else {
            System.out.println("Esperando que jugador seleccione...");
            // Cambiar turno al jugador
            this.playerTurn = true;
            this.waitingForPlayer = true;

            if (listener != null) {
                listener.onTurnStarted("Jugador", true);
            }
        }
    }

    /**
     * Reinicia el estado de cartas para nuevo duelo.
     */
    public void resetDuel() {
        this.gameStarted = false;
        this.currentRound = 1;
        this.playerScore = 0;
        this.aiScore = 0;
        this.playerHasSelected = false;
        this.aiHasSelected = false;
        this.playerSelectedCard = null;
        this.aiSelectedCard = null;

        // Resetear cartas usadas
        this.availablePlayerCards.clear();
        this.availablePlayerCards.addAll(playerCards);
        this.availableAiCards.clear();
        this.availableAiCards.addAll(aiCards);
        this.usedPlayerCards.clear();
        this.usedAiCards.clear();

        System.out.println("Duelo reiniciado - Cartas reseteadas");
    }

    /**
     * Resuelve una batalla entre dos cartas según las reglas de Yu-Gi-Oh!
     * @param playerCard Carta del jugador
     * @param aiCard Carta de la máquina
     */
    private void resolveBattle(Card playerCard, Card aiCard) {
        if (playerCard == null || aiCard == null) {
            notifyError("No se puede resolver batalla: cartas nulas");
            return;
        }

        // Determinar poderes según el modo de batalla
        int playerPower = playerAttackMode ? playerCard.getAtk() : playerCard.getDef();
        int aiPower = aiAttackMode ? aiCard.getAtk() : aiCard.getDef();

        String playerMode = playerAttackMode ? "ATK" : "DEF";
        String aiMode = aiAttackMode ? "ATK" : "DEF";

        String winner;
        String battleLog;
        String ruleDescription;

        // Aplicar reglas según combinación de modos
        if (playerAttackMode && aiAttackMode) {
            winner = resolveAttackVsAttack(playerCard, playerPower, aiCard, aiPower);
            ruleDescription = "Ambos en ataque → Gana mayor ATK";
        } else if (playerAttackMode && !aiAttackMode) {
            winner = resolveAttackVsDefense(playerCard, playerPower, aiCard, aiPower, true);
            ruleDescription = "Ataque vs Defensa → Si ATK > DEF, gana atacante";
        } else if (!playerAttackMode && aiAttackMode) {
            winner = resolveAttackVsDefense(aiCard, aiPower, playerCard, playerPower, false);
            ruleDescription = "Defensa vs Ataque → Si ATK > DEF, gana atacante";
        } else {
            winner = resolveDefenseVsDefense();
            ruleDescription = "Ambos en defensa → Empate automático";
        }

        battleLog = buildBattleLog(playerCard, playerPower, playerMode,
                aiCard, aiPower, aiMode, winner, ruleDescription);

        // Actualizar puntuación si hay ganador
        updateScores(winner);

        // Notificar resultado
        if (listener != null) {
            listener.onTurn(playerCard.getName() + " (" + playerMode + ")",
                    aiCard.getName() + " (" + aiMode + ")", winner, battleLog);
            listener.onScoreChanged(playerScore, aiScore);
        }

        System.out.println("Batalla resuelta: " + winner + " - Marcador: " + playerScore + "-" + aiScore);

        // Verificar si el duelo ha terminado
        checkDuelEnd();

        // Si el duelo continúa, preparar siguiente ronda
        if (gameStarted && !hasWinner() && currentRound < MAX_ROUNDS) {
            prepareNextRound();
        }
    }

    /**
     * Resuelve batalla cuando ambos están en modo ataque.
     */
    private String resolveAttackVsAttack(Card playerCard, int playerPower, Card aiCard, int aiPower) {
        if (playerPower > aiPower) {
            return "Jugador";
        } else if (aiPower > playerPower) {
            return "Máquina";
        } else {
            return "Empate";
        }
    }

    /**
     * Resuelve batalla cuando uno ataca y otro defiende.
     */
    private String resolveAttackVsDefense(Card attackerCard, int attackerPower,
                                          Card defenderCard, int defenderPower,
                                          boolean isPlayerAttacking) {
        if (attackerPower > defenderPower) {
            return isPlayerAttacking ? "Jugador" : "Máquina";
        } else if (attackerPower < defenderPower) {
            return "Empate";
        } else {
            return "Empate";
        }
    }

    /**
     * Resuelve batalla cuando ambos están en defensa.
     * ¡En Yu-Gi-Oh! Real: ambos en defensa → Empate (nadie gana)
     */
    private String resolveDefenseVsDefense() {
        return "Empate";
    }

    /**
     * Construye el mensaje de log de la batalla.
     */
    private String buildBattleLog(Card playerCard, int playerPower, String playerMode,
                                  Card aiCard, int aiPower, String aiMode,
                                  String winner, String rule) {
        String resultText = winner.equals("Empate") ? "¡EMPATE!" : "¡" + winner + " GANA!";

        return String.format(
                "¡%s!%n" +
                        "%s (%s: %d) vs %s (%s: %d)%n" +
                        "Regla: %s",
                resultText,
                playerCard.getName(), playerMode, playerPower,
                aiCard.getName(), aiMode, aiPower,
                rule
        );
    }

    /**
     * Actualiza las puntuaciones según el resultado.
     */
    private void updateScores(String winner) {
        if ("Jugador".equals(winner)) {
            playerScore++;
        } else if ("Máquina".equals(winner)) {
            aiScore++;
        }
    }

    /**
     * Prepara la siguiente ronda del duelo.
     */
    private void prepareNextRound() {
        // Verificar primero si el duelo debe terminar
        if (hasWinner() || currentRound >= MAX_ROUNDS) {
            checkDuelEnd();
            return;
        }

        currentRound++;

        // Resetear selecciones para la nueva ronda
        playerHasSelected = false;
        aiHasSelected = false;
        playerSelectedCard = null;
        aiSelectedCard = null;

        // Determinar turno aleatorio para esta nueva ronda
        playerTurn = random.nextBoolean();
        waitingForPlayer = playerTurn;

        System.out.println("PREPARANDO RONDA " + currentRound +
                " - Turno: " + (playerTurn ? "JUGADOR" : "MÁQUINA"));

        if (listener != null) {
            listener.onRoundStarted(currentRound, playerScore, aiScore);

            String turnPlayer = playerTurn ? "Jugador" : "Máquina";
            listener.onTurnStarted(turnPlayer, playerTurn);

            // Si la máquina empieza, que juegue inmediatamente
            if (!playerTurn) {
                System.out.println("Máquina comienza la ronda " + currentRound);
                startMachineTurn();
            }
        }
    }

    /**
     * Verifica si el duelo ha terminado.
     */
    private void checkDuelEnd() {
        boolean playerWon = playerScore >= WINNING_SCORE;
        boolean aiWon = aiScore >= WINNING_SCORE;
        boolean maxRoundsReached = currentRound >= MAX_ROUNDS;

        if (playerWon || aiWon || maxRoundsReached) {
            gameStarted = false;
            waitingForPlayer = false;

            String winner = getWinner();

            System.out.println("DUELO TERMINADO - Ganador: " + winner);

            if (listener != null) {
                listener.onDuelEnded(winner);
            }
        }
    }

    /**
     * Determina el ganador del duelo.
     */
    private String getWinner() {
        if (playerScore > aiScore) {
            return "Jugador";
        } else if (aiScore > playerScore) {
            return "Máquina";
        } else {
            return "Empate";
        }
    }

    /**
     * Verifica si hay un ganador del duelo.
     */
    private boolean hasWinner() {
        return playerScore >= WINNING_SCORE ||
                aiScore >= WINNING_SCORE ||
                (currentRound >= MAX_ROUNDS && playerScore != aiScore);
    }

    /**
     * Verifica si el duelo puede comenzar.
     */
    private void checkIfReadyToStart() {
        if (playerCards.size() == 3 && aiCards.size() == 3 && listener != null) {
            listener.onCardsLoaded();
        }
    }

    /**
     * Verifica si ambos jugadores han seleccionado carta.
     */
    private void checkIfReadyForBattle() {
        if (playerHasSelected && aiHasSelected) {
            System.out.println("¡AMBOS SELECCIONARON! Resolviendo batalla...");
            // Ambos han seleccionado, resolver batalla
            resolveBattle(playerSelectedCard, aiSelectedCard);
        } else if (!playerTurn && !aiHasSelected) {
            System.out.println("Máquina debe jugar...");
            // Es turno de la máquina y aún no ha seleccionado
            aiPlaysTurn();
        } else if (playerTurn && !playerHasSelected) {
            System.out.println("Esperando selección del jugador...");
            // Turno del jugador, esperando que seleccione
        } else if (!playerTurn && aiHasSelected) {
            System.out.println("Máquina ya jugó, esperando jugador...");
        } else {
            System.out.println("Estado inesperado");
        }
    }

    /**
     * Selecciona una carta para la Máquina.
     */
    private Card selectAICard() {
        if (aiCards.isEmpty()) {
            notifyError("La máquina no tiene cartas disponibles");
            return null;
        }
        return aiCards.get(random.nextInt(aiCards.size()));
    }

    /**
     * Notifica un error al listener.
     */
    private void notifyError(String message) {
        if (listener != null) {
            listener.onError(message);
        }
    }

    public boolean isWaitingForPlayerSelection() {
        return gameStarted && playerTurn && waitingForPlayer && !playerHasSelected;
    }

    public boolean hasPlayerSelected() {
        return playerHasSelected;
    }

    public boolean hasAiSelected() {
        return aiHasSelected;
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isWaitingForPlayer() {
        return waitingForPlayer;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public int getAiScore() {
        return aiScore;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public List<Card> getPlayerCards() {
        return new ArrayList<>(playerCards);
    }

    public List<Card> getAiCards() {
        return new ArrayList<>(aiCards);
    }

    public List<Card> getAvailableAiCards() {
        return new ArrayList<>(availableAiCards);
    }

    public int getAvailablePlayerCardsCount() {
        return availablePlayerCards.size();
    }

    public int getAvailableAiCardsCount() {
        return availableAiCards.size();
    }
}