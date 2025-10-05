package org.example.listeners;

/**
 * Interface para recibir eventos del duelo y actualizar la interfaz de usuario.
 * Patrón Observer para desacoplar la lógica del juego de la interfaz gráfica.
 */
public interface BattleListener {

    /**
     * Se llama cuando se completa un turno de batalla.
     * Incluye información detallada sobre la batalla y el resultado.
     * @param playerCard Nombre de la carta del jugador
     * @param aiCard Nombre de la carta de la máquina
     * @param winner Ganador del turno ("Jugador", "Máquina" o "Empate")
     * @param battleLog Descripción detallada de la batalla con stats y reglas aplicadas
     */
    void onTurn(String playerCard, String aiCard, String winner, String battleLog);

    /**
     * Se llama cuando cambia la puntuación del duelo.
     * Esto ocurre después de cada ronda completada.
     * @param playerScore Nueva puntuación del jugador (0-2)
     * @param aiScore Nueva puntuación de la máquina (0-2)
     */
    void onScoreChanged(int playerScore, int aiScore);

    /**
     * Se llama cuando termina el duelo.
     * El duelo termina cuando un jugador alcanza 2 victorias o se completan 3 rondas.
     * @param winner Ganador del duelo ("Jugador" o "Máquina")
     */
    void onDuelEnded(String winner);

    /**
     * Se llama cuando ocurre un error en el duelo.
     * Los errores pueden ser de conexión, datos inválidos, o problemas de lógica.
     * @param errorMessage Descripción del error para mostrar al usuario
     */
    void onError(String errorMessage);

    /**
     * Se llama cuando todas las cartas han sido cargadas y el duelo puede comenzar.
     * Esto ocurre después de obtener exitosamente 6 cartas Monster de la API.
     */
    void onCardsLoaded();

    /**
     * Se llama cuando comienza un nuevo duelo.
     * Incluye información sobre quién comienza primero.
     * @param starter Jugador que inicia el duelo ("Jugador" o "Máquina")
     * @param roundNumber Número de ronda inicial (siempre 1)
     */
    void onDuelStarted(String starter, int roundNumber);

    /**
     * Se llama cuando un jugador selecciona una carta.
     * Útil para actualizar la UI con la selección actual.
     * @param playerName Nombre del jugador ("Jugador" o "Máquina")
     * @param cardName Nombre de la carta seleccionada
     * @param cardIndex Índice de la carta seleccionada (0-2)
     */
    void onCardSelected(String playerName, String cardName, int cardIndex);

    /**
     * Se llama cuando se inicia una nueva ronda.
     * Indica el número de ronda y el marcador actual.
     * @param roundNumber Número de la nueva ronda (1-3)
     * @param playerScore Puntuación actual del jugador
     * @param aiScore Puntuación actual de la máquina
     */
    void onRoundStarted(int roundNumber, int playerScore, int aiScore);

    /**
     * Se llama cuando es el turno de un jugador.
     * Útil para habilitar/deshabilitar controles en la UI.
     * @param playerName Jugador cuyo turno es ("Jugador" o "Máquina")
     * @param isPlayerTurn true si es turno del jugador humano
     */
    void onTurnStarted(String playerName, boolean isPlayerTurn);

    /**
     * Se llama cuando se cargan cartas individualmente.
     * Proporciona feedback progresivo durante la carga.
     * @param cardsLoaded Número de cartas cargadas hasta el momento
     * @param totalCards Total de cartas a cargar (siempre 6)
     * @param cardName Nombre de la última carta cargada (opcional)
     */
    void onCardLoaded(int cardsLoaded, int totalCards, String cardName);

    /**
     * Se llama cuando se actualiza el estado del duelo.
     * Útil para mostrar mensajes informativos al usuario.
     * @param statusMessage Mensaje descriptivo del estado actual
     * @param isError true si es un estado de error
     */
    void onStatusUpdate(String statusMessage, boolean isError);

    /**
     * Se llama cuando se reinicia el duelo.
     * Indica que todos los estados se han resetado y comienza un nuevo juego.
     */
    void onDuelReset();

    /**
     * Se llama cuando se procesa un modo de batalla.
     * Indica si un jugador está en modo ataque o defensa.
     *
     * @param playerName Nombre del jugador ("Jugador" o "Máquina")
     * @param isAttackMode true si está en modo ataque, false si en defensa
     */
    void onBattleModeSet(String playerName, boolean isAttackMode);
}