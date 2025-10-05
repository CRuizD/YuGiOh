package org.example.ui;

import org.example.model.Card;
import org.example.listeners.BattleListener;
import org.example.api.YgoApiClient;
import org.example.duel.Duel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ¡Interfaz gráfica principal del simulador de duelos Yu-Gi-Oh!
 * Maneja la visualización de cartas, puntuaciones e interacción del usuario.
 * Implementa BattleListener para recibir eventos del duelo.
 */
public class DuelFrame extends JFrame implements BattleListener {
    // Paneles principales
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JLabel logoLabel;
    private JPanel battlePanel;

    // Panel del jugador
    private JPanel playerPanel;
    private JPanel playerCardsPanel;
    private JLabel playerScoreLabel;

    // Cartas del jugador
    private JPanel cartaJugador1;
    private JLabel nombreCarta1;
    private JLabel imagenCarta1;
    private JLabel statsCarta1;

    private JPanel cartaJugador2;
    private JLabel nombreCarta2;
    private JLabel imagenCarta2;
    private JLabel statsCarta2;

    private JPanel cartaJugador3;
    private JLabel nombreCarta3;
    private JLabel imagenCarta3;
    private JLabel statsCarta3;

    // Panel de la máquina
    private JPanel aiPanel;
    private JPanel aiCardsPanel;
    private JLabel aiScoreLabel;

    // Cartas de la máquina
    private JPanel cartaMaquina1;
    private JLabel nombreCartaM1;
    private JLabel imagenCartaM1;
    private JLabel statsCartaM1;

    private JPanel cartaMaquina2;
    private JLabel nombreCartaM2;
    private JLabel imagenCartaM2;
    private JLabel statsCartaM2;

    private JPanel cartaMaquina3;
    private JLabel nombreCartaM3;
    private JLabel imagenCartaM3;
    private JLabel statsCartaM3;

    // Panel de controles
    private JPanel controlPanel;
    private JPanel Panel;
    private JPanel Panel1;
    private JButton battleButton;
    private JButton restartButton;
    private JLabel statusLabel;
    private JLabel roundLabel;

    // Log de batalla
    private JTextArea battleLogArea;
    private JScrollPane battleLogScrollPane;

    private Duel duel;
    private YgoApiClient apiClient;
    private ExecutorService executor;

    private List<Card> playerCards;
    private List<Card> aiCards;
    private List<Card> availableCards;
    private int cardsLoaded;
    private List<JPanel> playerCardPanels;
    private List<JLabel[]> playerCardComponents;
    private List<JLabel[]> aiCardComponents;

    private boolean componentsInitialized = false;
    private int currentRound = 1;
    private int playerWins = 0;
    private int aiWins = 0;

    /**
     * Constructor principal que inicializa la ventana y componentes básicos.
     */
    public DuelFrame() {
        initializeLists();
        setupBasicFrame();
    }

    /**
     * Inicializa las estructuras de datos y servicios.
     */
    private void initializeLists() {
        playerCards = new ArrayList<>();
        aiCards = new ArrayList<>();
        availableCards = new ArrayList<>();
        playerCardPanels = new ArrayList<>();
        playerCardComponents = new ArrayList<>();
        aiCardComponents = new ArrayList<>();
        cardsLoaded = 0;

        duel = new Duel();
        duel.setBattleListener(this);
        apiClient = new YgoApiClient();
        executor = Executors.newFixedThreadPool(5);
    }

    /**
     * Configuración básica de la ventana.
     */
    private void setupBasicFrame() {
        setTitle("Yu-Gi-Oh! Duel Simulator - Mejor de 3 Rondas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    /**
     * Método llamado después de que el GUI Builder inyecta los componentes.
     */
    public void initializeComponents() {
        if (!componentsInitialized) {
            initializeCardLists();
            setupFrame();
            setupBattleLog();
            setupListeners();
            loadInitialCards();
            componentsInitialized = true;
        }
    }

    /**
     * Configura el frame principal después de la inyección de componentes.
     */
    private void setupFrame() {
        if (mainPanel != null) {
            setContentPane(mainPanel);
        }

        battleButton.setEnabled(false);
        restartButton.setEnabled(true);
        updateStatus("Inicializando aplicación...");
        updateRoundInfo();
    }

    /**
     * Organiza los componentes de cartas en listas para fácil acceso.
     */
    private void initializeCardLists() {
        playerCardPanels.clear();
        playerCardComponents.clear();
        aiCardComponents.clear();

        if (cartaJugador1 != null) playerCardPanels.add(cartaJugador1);
        if (cartaJugador2 != null) playerCardPanels.add(cartaJugador2);
        if (cartaJugador3 != null) playerCardPanels.add(cartaJugador3);

        if (nombreCarta1 != null && imagenCarta1 != null && statsCarta1 != null) {
            playerCardComponents.add(new JLabel[]{nombreCarta1, imagenCarta1, statsCarta1});
        }
        if (nombreCarta2 != null && imagenCarta2 != null && statsCarta2 != null) {
            playerCardComponents.add(new JLabel[]{nombreCarta2, imagenCarta2, statsCarta2});
        }
        if (nombreCarta3 != null && imagenCarta3 != null && statsCarta3 != null) {
            playerCardComponents.add(new JLabel[]{nombreCarta3, imagenCarta3, statsCarta3});
        }

        if (nombreCartaM1 != null && imagenCartaM1 != null && statsCartaM1 != null) {
            aiCardComponents.add(new JLabel[]{nombreCartaM1, imagenCartaM1, statsCartaM1});
        }
        if (nombreCartaM2 != null && imagenCartaM2 != null && statsCartaM2 != null) {
            aiCardComponents.add(new JLabel[]{nombreCartaM2, imagenCartaM2, statsCartaM2});
        }
        if (nombreCartaM3 != null && imagenCartaM3 != null && statsCartaM3 != null) {
            aiCardComponents.add(new JLabel[]{nombreCartaM3, imagenCartaM3, statsCartaM3});
        }

        if (playerScoreLabel != null) playerScoreLabel.setText("Victorias: 0");
        if (aiScoreLabel != null) aiScoreLabel.setText("Victorias: 0");
    }

    /**
     * Configura el área de log de batalla.
     */
    private void setupBattleLog() {
        battleLogArea = new JTextArea(8, 50);
        battleLogArea.setEditable(false);
        battleLogArea.setLineWrap(true);
        battleLogArea.setWrapStyleWord(true);
        battleLogArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        battleLogArea.setBackground(new Color(250, 250, 250));
        battleLogArea.setForeground(Color.DARK_GRAY);
        battleLogArea.setMargin(new Insets(10, 10, 10, 10));

        battleLogScrollPane = new JScrollPane(battleLogArea);
        battleLogScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        battleLogScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        battleLogScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(100, 100, 100)),
                        "LOG DE BATALLA - Historial del Duelo"
                ),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        battleLogScrollPane.setPreferredSize(new Dimension(600, 200));

        JPanel logContainerPanel = new JPanel(new BorderLayout());
        logContainerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        logContainerPanel.add(battleLogScrollPane, BorderLayout.CENTER);

        controlPanel.removeAll();
        controlPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        buttonPanel.add(battleButton);
        buttonPanel.add(restartButton);

        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        controlPanel.add(logContainerPanel, BorderLayout.CENTER);

        controlPanel.revalidate();
        controlPanel.repaint();

        addToBattleLog("=== YU-GI-OH! DUEL SIMULATOR ===");
        addToBattleLog("Sistema inicializado - Esperando cartas...");
        addToBattleLog("─────────────────────────────────────────");
    }

    /**
     * Configura todos los listeners de la interfaz.
     */
    private void setupListeners() {
        if (battleButton != null) {
            battleButton.addActionListener(e -> startNewDuel());
        }
        if (restartButton != null) {
            restartButton.addActionListener(e -> restartDuel());
        }
        setupCardListeners();
    }

    /**
     * Configura listeners para las cartas del jugador.
     */
    private void setupCardListeners() {
        if (playerCardPanels == null || playerCardPanels.isEmpty()) {
            return;
        }

        for (int i = 0; i < playerCardPanels.size(); i++) {
            final JPanel cardPanel = playerCardPanels.get(i);
            if (cardPanel == null) continue;

            final int cardIndex = i;

            cardPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (duel.isWaitingForPlayerSelection() && cardPanel.isVisible()) {
                        handlePlayerCardSelection(cardIndex, cardPanel);
                    } else if (duel.isGameStarted() && duel.isPlayerTurn() && duel.hasPlayerSelected()) {
                        updateStatus("Ya seleccionaste una carta para esta ronda");
                    } else if (duel.isGameStarted() && !duel.isPlayerTurn()) {
                        updateStatus("Es turno de la máquina");
                    } else if (!duel.isGameStarted()) {
                        updateStatus("El duelo no ha comenzado");
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (duel.isWaitingForPlayerSelection() && cardPanel.isVisible()) {
                        cardPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
                        cardPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (cardPanel.isVisible() && !isCardSelected(cardPanel)) {
                        cardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                    }
                }
            });
        }
    }

    /**
     * Carga las cartas iniciales desde la API.
     */
    private void loadInitialCards() {
        updateStatus("Conectando con la API YGOProDeck...");
        addToBattleLog("Cargando cartas desde la API...");
        loadCardPool();
    }

    /**
     * Carga un pool de cartas y luego las asigna a jugador y máquina.
     */
    private void loadCardPool() {
        for (int i = 0; i < 6; i++) {
            final int cardIndex = i;
            executor.execute(() -> loadCardToPool(cardIndex));
        }
    }

    /**
     * Carga una carta al pool disponible.
     */
    private void loadCardToPool(int cardIndex) {
        try {
            Card card = apiClient.getRandomMonsterCard();
            SwingUtilities.invokeLater(() -> {
                availableCards.add(card);
                cardsLoaded++;

                addToBattleLog("Carta " + cardsLoaded + "/6: " + card.getName() +
                        " (ATK: " + card.getAtk() + ", DEF: " + card.getDef() + ")");

                updateStatus("Cartas cargadas: " + cardsLoaded + "/6");

                if (cardsLoaded == 6) {
                    addToBattleLog("");
                    assignCardsToPlayers();
                }
            });
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                String errorMsg = "Error cargando carta " + (cardsLoaded + 1) + ": " + e.getMessage();
                onError(errorMsg);
                addToBattleLog("ERROR " + errorMsg);
            });
        }
    }

    /**
     * Asigna las cartas del pool a jugador y máquina.
     */
    private void assignCardsToPlayers() {
        playerCards.clear();
        aiCards.clear();

        if (availableCards.size() < 6) {
            onError("No hay suficientes cartas disponibles: " + availableCards.size() + "/6");
            return;
        }

        addToBattleLog("--- ASIGNANDO CARTAS ---");

        for (int i = 0; i < 3; i++) {
            Card originalCard = availableCards.get(i);
            Card playerCard = new Card(
                    originalCard.getName(),
                    originalCard.getAtk(),
                    originalCard.getDef(),
                    originalCard.getImageUrl(),
                    originalCard.getType()
            );
            playerCards.add(playerCard);

            if (i < playerCardComponents.size()) {
                updateCardUI(playerCard, playerCardComponents.get(i), playerCardPanels.get(i), true);
            }
            addToBattleLog("Jugador recibe: " + playerCard.getName());
        }

        for (int i = 3; i < 6; i++) {
            Card originalCard = availableCards.get(i);
            Card aiCard = new Card(
                    originalCard.getName(),
                    originalCard.getAtk(),
                    originalCard.getDef(),
                    originalCard.getImageUrl(),
                    originalCard.getType()
            );
            aiCards.add(aiCard);

            int aiIndex = i - 3;
            if (aiIndex < aiCardComponents.size()) {
                updateCardUI(aiCard, aiCardComponents.get(aiIndex), getAIPanel(aiIndex), false);
            }
            addToBattleLog("Máquina recibe: " + aiCard.getName());
        }

        duel.setPlayerCards(new ArrayList<>(playerCards));
        duel.setAiCards(new ArrayList<>(aiCards));

        updateStatus("¡Cartas listas! Haz clic en 'INICIAR BATALLA'");
        addToBattleLog("--- CARTAS LISTAS ---");
        addToBattleLog("¡Haz clic en 'INICIAR BATALLA' para comenzar!");

        if (battleButton != null) {
            battleButton.setEnabled(true);
        }
    }

    /**
     * Actualiza la UI con los datos de una carta.
     */
    private void updateCardUI(Card card, JLabel[] components, JPanel cardPanel, boolean isPlayerCard) {
        try {
            if (components == null || components.length < 3) return;
            if (card == null) return;

            String displayName = card.getName();
            if (displayName.length() > 15) {
                displayName = displayName.substring(0, 12) + "...";
            }
            components[0].setText("<html><div style='text-align: center; font-weight: bold;'>" + displayName + "</div></html>");
            components[0].setFont(new Font("Arial", Font.BOLD, 11));
            components[0].setHorizontalAlignment(SwingConstants.CENTER);

            String statsText = String.format("<html><div style='text-align: center;'>ATK: <b>%d</b><br>DEF: <b>%d</b></div></html>",
                    card.getAtk(), card.getDef());
            components[2].setText(statsText);
            components[2].setFont(new Font("Arial", Font.PLAIN, 10));
            components[2].setHorizontalAlignment(SwingConstants.CENTER);

            components[1].setHorizontalAlignment(SwingConstants.CENTER);
            components[1].setVerticalAlignment(SwingConstants.CENTER);
            components[1].setText("Cargando...");
            components[1].setFont(new Font("Arial", Font.ITALIC, 9));

            if (cardPanel != null) {
                cardPanel.setVisible(true);
            }

            loadCardImage(card, components[1]);

        } catch (Exception e) {
            System.err.printf("Error actualizando UI de carta %s: %s%n",
                    card != null ? card.getName() : "null", e.getMessage());
        }
    }

    /**
     * Carga la imagen de una carta desde su URL.
     */
    private void loadCardImage(Card card, JLabel imageLabel) {
        executor.execute(() -> {
            try {
                if (card.hasImage()) {
                    URL imageUrl = new URL(card.getImageUrl());
                    ImageIcon originalIcon = new ImageIcon(imageUrl);

                    if (originalIcon.getIconWidth() > 0) {
                        Image scaledImage = originalIcon.getImage().getScaledInstance(120, 150, Image.SCALE_SMOOTH);
                        ImageIcon scaledIcon = new ImageIcon(scaledImage);

                        SwingUtilities.invokeLater(() -> {
                            imageLabel.setIcon(scaledIcon);
                            imageLabel.setText("");
                        });
                        return;
                    }
                }
                throw new Exception("Imagen no disponible");
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    ImageIcon placeholder = createPlaceholderIcon(card);
                    imageLabel.setIcon(placeholder);
                    imageLabel.setText("");
                });
            }
        });
    }

    /**
     * Crea un icono de placeholder para cuando no hay imagen disponible.
     */
    private ImageIcon createPlaceholderIcon(Card card) {
        BufferedImage image = new BufferedImage(120, 150, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        Color bgColor = getColorByAttack(card.getAtk());
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, 120, 150);

        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, 119, 149);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));

        String nameText = card.getName().length() > 12 ?
                card.getName().substring(0, 9) + "..." : card.getName();
        String atkText = "ATK: " + card.getAtk();
        String defText = "DEF: " + card.getDef();

        FontMetrics fm = g2d.getFontMetrics();

        int nameX = (120 - fm.stringWidth(nameText)) / 2;
        int atkX = (120 - fm.stringWidth(atkText)) / 2;
        int defX = (120 - fm.stringWidth(defText)) / 2;

        g2d.drawString(nameText, nameX, 40);
        g2d.drawString(atkText, atkX, 80);
        g2d.drawString(defText, defX, 100);

        g2d.dispose();
        return new ImageIcon(image);
    }

    /**
     * Determina color de fondo basado en ATK de la carta.
     */
    private Color getColorByAttack(int attack) {
        if (attack >= 2500) return new Color(255, 150, 150);
        if (attack >= 2000) return new Color(255, 200, 150);
        if (attack >= 1500) return new Color(255, 255, 150);
        if (attack >= 1000) return new Color(150, 255, 150);
        return new Color(150, 200, 255);
    }

    /**
     * Inicia un nuevo duelo.
     */
    private void startNewDuel() {
        System.out.println("=== INICIANDO NUEVO DUELO DESDE BOTÓN ===");
        currentRound = 1;
        playerWins = 0;
        aiWins = 0;

        if (battleLogArea != null) {
            battleLogArea.setText("");
        }

        duel.startDuel();
        battleButton.setEnabled(false);
        restartButton.setEnabled(false);

        debugState("Después de startDuel()");

        updateTurnIndicator();
        enableCardInteractivity(duel.isPlayerTurn());
        resetCardBorders();

        addToBattleLog("=== NUEVO DUELO INICIADO ===");
        addToBattleLog("Marcador: Jugador 0 - 0 Máquina");
    }

    /**
     * Maneja la selección de carta del jugador.
     */
    private void handlePlayerCardSelection(int cardIndex, JPanel cardPanel) {
        if (!duel.isWaitingForPlayerSelection()) {
            System.out.println("No está esperando selección del jugador");
            return;
        }

        Card selectedCard = playerCards.get(cardIndex);
        addToBattleLog("Jugador selecciona: " + selectedCard.getName());

        // Jugador selecciona carta
        duel.playerSelectsCard(cardIndex);
        highlightCard(cardPanel);
        updateStatus("Carta seleccionada. Esperando turno de la máquina...");
        enableCardInteractivity(false);
    }

    /**
     * Reinicia el duelo completamente.
     */
    private void restartDuel() {
        playerCards.clear();
        aiCards.clear();
        availableCards.clear();
        cardsLoaded = 0;
        currentRound = 1;
        playerWins = 0;
        aiWins = 0;

        hideAllCards();
        battleButton.setEnabled(false);
        restartButton.setEnabled(true);
        updateStatus("Cargando nuevas cartas...");
        updateRoundInfo();
        resetScores();

        if (battleLogArea != null) {
            battleLogArea.setText("");
        }
        addToBattleLog("=== REINICIANDO DUELO ===");
        addToBattleLog("Cargando nuevas cartas...");

        duel = new Duel();
        duel.setBattleListener(this);
        loadInitialCards();
    }

    /**
     * Habilita/deshabilita la interactividad de las cartas del jugador.
     */
    private void enableCardInteractivity(boolean enabled) {
        if (playerCardPanels == null) return;

        for (JPanel cardPanel : playerCardPanels) {
            if (cardPanel != null && cardPanel.isVisible()) {
                cardPanel.setCursor(enabled ?
                        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) :
                        Cursor.getDefaultCursor());
            }
        }
    }

    /**
     * Resalta visualmente una carta seleccionada.
     */
    private void highlightCard(JPanel cardPanel) {
        if (playerCardPanels == null || cardPanel == null) return;

        for (JPanel panel : playerCardPanels) {
            if (panel != null && panel.isVisible()) {
                panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }
        }
        cardPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
    }

    /**
     * Reinicia los bordes de todas las cartas.
     */
    private void resetCardBorders() {
        for (JPanel panel : playerCardPanels) {
            if (panel != null && panel.isVisible()) {
                panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }
        }
    }

    /**
     * Verifica si una carta está seleccionada.
     */
    private boolean isCardSelected(JPanel cardPanel) {
        return cardPanel != null &&
                cardPanel.getBorder() instanceof LineBorder &&
                ((LineBorder) cardPanel.getBorder()).getLineColor().equals(Color.GREEN);
    }

    /**
     * Actualiza las puntuaciones visualmente.
     */
    private void updateScores() {
        if (playerScoreLabel != null) {
            playerScoreLabel.setText("Puntuación: " + playerWins);
        }
        if (aiScoreLabel != null) {
            aiScoreLabel.setText("Puntuación: " + aiWins);
        }

        if (playerScoreLabel != null && aiScoreLabel != null) {
            if (playerWins > aiWins) {
                playerScoreLabel.setForeground(new Color(41, 128, 185));
                aiScoreLabel.setForeground(Color.BLACK);
            } else if (aiWins > playerWins) {
                playerScoreLabel.setForeground(Color.BLACK);
                aiScoreLabel.setForeground(new Color(231, 76, 60));
            } else {
                playerScoreLabel.setForeground(Color.BLACK);
                aiScoreLabel.setForeground(Color.BLACK);
            }
        }
    }

    /**
     * Actualiza el mensaje de estado.
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText("Estado: " + message);
        }
    }

    /**
     * Actualiza la información de la ronda.
     */
    private void updateRoundInfo() {
        if (roundLabel != null) {
            roundLabel.setText("Ronda: " + currentRound + " | Jugador: " + playerWins + " - Máquina: " + aiWins);
        }
    }

    /**
     * Actualiza el indicador visual del turno.
     */
    private void updateTurnIndicator() {
        if (duel.isGameStarted()) {
            if (duel.isWaitingForPlayerSelection()) {
                statusLabel.setForeground(new Color(248, 251, 255));
                statusLabel.setText("Estado: ¡TU TURNO! Selecciona una carta");
                if (roundLabel != null) {
                    roundLabel.setForeground(new Color(248, 251, 255));
                }
            } else if (duel.isPlayerTurn() && duel.hasPlayerSelected()) {
                statusLabel.setForeground(Color.WHITE);
                statusLabel.setText("Estado: Esperando turno de la máquina...");
                if (roundLabel != null) {
                    roundLabel.setForeground(Color.WHITE);
                }
            } else if (!duel.isPlayerTurn()) {
                statusLabel.setForeground(Color.WHITE);
                statusLabel.setText("Estado: Turno de la máquina...");
                if (roundLabel != null) {
                    roundLabel.setForeground(Color.WHITE);
                }
            }
        } else {
            statusLabel.setForeground(Color.WHITE);
            if (roundLabel != null) {
                roundLabel.setForeground(Color.WHITE);
            }
        }
    }

    /**
     * Agrega un mensaje al log de batalla.
     */
    private void addToBattleLog(String message) {
        if (battleLogArea != null) {
            SwingUtilities.invokeLater(() -> {
                battleLogArea.append(message + "\n");
                battleLogArea.setCaretPosition(battleLogArea.getDocument().getLength());
            });
        }
    }

    /**
     * Obtiene el panel de carta de la máquina según índice.
     */
    private JPanel getAIPanel(int index) {
        switch (index) {
            case 0: return cartaMaquina1;
            case 1: return cartaMaquina2;
            case 2: return cartaMaquina3;
            default: return null;
        }
    }

    /**
     * Oculta todas las cartas inicialmente.
     */
    private void hideAllCards() {
        for (JPanel panel : playerCardPanels) {
            if (panel != null) panel.setVisible(false);
        }

        if (cartaMaquina1 != null) cartaMaquina1.setVisible(false);
        if (cartaMaquina2 != null) cartaMaquina2.setVisible(false);
        if (cartaMaquina3 != null) cartaMaquina3.setVisible(false);
    }

    /**
     * Resetea las puntuaciones a cero.
     */
    private void resetScores() {
        if (playerScoreLabel != null) {
            playerScoreLabel.setText("Victorias: 0");
            playerScoreLabel.setForeground(Color.BLACK);
        }
        if (aiScoreLabel != null) {
            aiScoreLabel.setText("Victorias: 0");
            aiScoreLabel.setForeground(Color.BLACK);
        }
    }

    private void debugState(String context) {
        System.out.println("=== DEBUG: " + context + " ===");
        System.out.println("Duelo iniciado: " + duel.isGameStarted());
        System.out.println("Turno jugador: " + duel.isPlayerTurn());
        System.out.println("Esperando jugador: " + duel.isWaitingForPlayerSelection());
        System.out.println("Jugador seleccionó: " + duel.hasPlayerSelected());
        System.out.println("Máquina seleccionó: " + duel.hasAiSelected());
        System.out.println("Ronda actual: " + duel.getCurrentRound());
        System.out.println("Marcador: " + duel.getPlayerScore() + " - " + duel.getAiScore());
        System.out.println("=============================");
    }

    /**
     * Muestra diálogo con resultado del turno.
     */
    private void showBattleDialog(String playerCard, String aiCard, String winner, String log) {
        String message = String.format(
                "<html><div style='text-align: center; width: 300px;'>" +
                        "<h2 style='color: #2C3E50;'>Ronda %d - ¡Batalla!</h2>" +
                        "<p><b style='color: #2980B9;'>Jugador:</b> %s</p>" +
                        "<p><b style='color: #E74C3C;'>Máquina:</b> %s</p>" +
                        "<hr>" +
                        "<h3 style='color: %s;'>%s</h3>" +
                        "<p style='font-size: 12px;'>%s</p>" +
                        "<p style='font-size: 11px; color: #7F8C8D;'>Marcador: Jugador %d - %d Máquina</p>" +
                        "</div></html>",
                currentRound,
                playerCard, aiCard,
                winner.equals("Jugador") ? "#2980B9" : winner.equals("Máquina") ? "#E74C3C" : "#7F8C8D",
                winner.equals("Empate") ? "¡Empate!" : "¡" + winner + " gana la ronda!",
                log.replace("\n", "<br>"),
                playerWins, aiWins
        );

        JOptionPane.showMessageDialog(this, message, "Resultado de la Ronda " + currentRound,
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra el diálogo de victoria final
     */
    private void showVictoryDialog(String winner, String message, Color color) {
        String htmlMessage = String.format(
                "<html><div style='text-align: center; width: 350px;'>" +
                        "<h1 style='color: %s; font-size: 24px; margin-bottom: 10px;'>%s</h1>" +
                        "<h2 style='color: #2C3E50; font-size: 18px;'>Resultado Final del Duelo</h2>" +
                        "<p style='font-size: 16px; font-weight: bold;'>Jugador: %d - Máquina: %d</p>" +
                        "<p style='font-size: 14px; color: #7F8C8D;'>Rondas jugadas: %d</p>" +
                        "<hr>" +
                        "<p style='font-size: 12px;'>Haz clic en 'NUEVO DUELO' para jugar otra vez</p>" +
                        "</div></html>",
                color.getRGB(),
                message,
                playerWins,
                aiWins,
                currentRound
        );

        JOptionPane.showMessageDialog(this, htmlMessage, "¡Duelo Terminado!",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * @param playerCard Nombre de la carta del jugador
     * @param aiCard Nombre de la carta de la máquina
     * @param winner Ganador del turno ("Jugador", "Máquina" o "Empate")
     * @param battleLog Descripción detallada de la batalla con stats y reglas aplicadas
     */
    @Override
    public void onTurn(String playerCard, String aiCard, String winner, String battleLog) {
        SwingUtilities.invokeLater(() -> {
            // Agregar al log de batalla
            addToBattleLog("\n--- Ronda " + currentRound + " ---");
            addToBattleLog("Jugador usa: " + playerCard);
            addToBattleLog("Máquina usa: " + aiCard);
            addToBattleLog("Resultado: " + winner);

            // Parsear el battleLog
            String[] logLines = battleLog.split("\n");
            for (String line : logLines) {
                if (!line.trim().isEmpty()) {
                    addToBattleLog("> " + line);
                }
            }

            // Actualizar contador de victorias
            if ("Jugador".equals(winner)) {
                playerWins++;
                addToBattleLog("¡Punto para el JUGADOR!");
            } else if ("Máquina".equals(winner)) {
                aiWins++;
                addToBattleLog("¡Punto para la MÁQUINA!");
            } else {
                addToBattleLog("¡EMPATE! Nadie gana punto.");
            }

            // Actualizar puntuaciones
            updateScores();
            updateRoundInfo();
            addToBattleLog("Marcador actual: Jugador " + playerWins + " - " + aiWins + " Máquina");

            // Mostrar diálogo de resultado
            showBattleDialog(playerCard, aiCard, winner, battleLog);

            // Solo resetear selecciones visuales
            resetCardBorders();

            updateTurnIndicator();
        });
    }

    /**
     * @param playerScore Nueva puntuación del jugador (0-2)
     * @param aiScore Nueva puntuación de la máquina (0-2)
     */
    @Override
    public void onScoreChanged(int playerScore, int aiScore) {
        // Este método ya no se usa para victorias de rondas
        // Se maneja internamente con updateScores()
    }

    /**
     * @param winner Ganador del duelo ("Jugador" o "Máquina")
     */
    @Override
    public void onDuelEnded(String winner) {
        SwingUtilities.invokeLater(() -> {
            addToBattleLog("\n=== DUELO TERMINADO ===");
            addToBattleLog("Resultado final: Jugador " + playerWins + " - " + aiWins + " Máquina");

            String victoryMessage;
            Color color;

            if ("Jugador".equals(winner)) {
                victoryMessage = "¡FELICIDADES! ¡HAS GANADO EL DUELO!";
                color = new Color(0, 100, 0);
                addToBattleLog("¡VICTORIA DEL JUGADOR!");
            } else if ("Máquina".equals(winner)) {
                victoryMessage = "¡LA MÁQUINA GANA EL DUELO!";
                color = new Color(200, 0, 0);
                addToBattleLog("¡VICTORIA DE LA MÁQUINA!");
            } else {
                victoryMessage = "¡EMPATE!";
                color = new Color(100, 100, 100);
                addToBattleLog("⚖¡EMPATE!");
            }

            addToBattleLog("Haz clic en 'NUEVO DUELO' para jugar otra vez.");

            // Mostrar diálogo de victoria
            showVictoryDialog(winner, victoryMessage, color);

            // Habilitar botones
            restartButton.setEnabled(true);
            enableCardInteractivity(false);
            updateStatus("Duelo terminado. " + victoryMessage);
        });
    }

    /**
     * @param errorMessage Descripción del error para mostrar al usuario
     */
    @Override
    public void onError(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            updateStatus("Error: " + errorMessage);
            addToBattleLog("ERROR: " + errorMessage);
        });
    }

    @Override
    public void onCardsLoaded() {
        SwingUtilities.invokeLater(() -> {
            if (battleButton != null) battleButton.setEnabled(true);
            updateStatus("¡Cartas listas! Haz clic en 'INICIAR BATALLA'");
            addToBattleLog("¡Todas las cartas cargadas correctamente!");
        });
    }

    /**
     * @param starter Jugador que inicia el duelo ("Jugador" o "Máquina")
     * @param roundNumber Número de ronda inicial (siempre 1)
     */
    @Override
    public void onDuelStarted(String starter, int roundNumber) {
        SwingUtilities.invokeLater(() -> {
            addToBattleLog("¡DUELO INICIADO! Comienza: " + starter);
            addToBattleLog("Ronda " + roundNumber + " - Preparados para la batalla");
            updateStatus("Duelo iniciado. " + starter + " comienza");
            updateTurnIndicator();
        });
    }

    /**
     * @param playerName Nombre del jugador ("Jugador" o "Máquina")
     * @param cardName Nombre de la carta seleccionada
     * @param cardIndex Índice de la carta seleccionada (0-2)
     */
    @Override
    public void onCardSelected(String playerName, String cardName, int cardIndex) {
        SwingUtilities.invokeLater(() -> {
            if ("Jugador".equals(playerName)) {
                addToBattleLog(playerName + " selecciona carta: " + cardName);
                updateStatus(playerName + " seleccionó: " + cardName);
            }
        });
    }

    /**
     * @param roundNumber Número de la nueva ronda (1-3)
     * @param playerScore Puntuación actual del jugador
     * @param aiScore Puntuación actual de la máquina
     */
    @Override
    public void onRoundStarted(int roundNumber, int playerScore, int aiScore) {
        SwingUtilities.invokeLater(() -> {
            currentRound = roundNumber;
            addToBattleLog("\nCOMIENZA RONDA " + roundNumber);
            addToBattleLog("Marcador: Jugador " + playerScore + " - " + aiScore + " Máquina");
            updateRoundInfo();
            resetCardBorders();
            updateTurnIndicator();
        });
    }

    /**
     * @param playerName Jugador cuyo turno es ("Jugador" o "Máquina")
     * @param isPlayerTurn true si es turno del jugador humano
     */
    @Override
    public void onTurnStarted(String playerName, boolean isPlayerTurn) {
        SwingUtilities.invokeLater(() -> {
            addToBattleLog("Turno del " + playerName);

            if (isPlayerTurn) {
                updateStatus("¡Tu turno! Selecciona una carta");
                enableCardInteractivity(true);
                resetCardBorders();
                addToBattleLog("¡Selecciona una carta para atacar/defender!");
            } else {
                updateStatus("Turno de la máquina - Pensando...");
                enableCardInteractivity(false);
                addToBattleLog("La máquina está seleccionando su carta...");
            }
            updateTurnIndicator();
        });
    }

    /**
     * @param cardsLoaded Número de cartas cargadas hasta el momento
     * @param totalCards Total de cartas a cargar (siempre 6)
     * @param cardName Nombre de la última carta cargada (opcional)
     */
    @Override
    public void onCardLoaded(int cardsLoaded, int totalCards, String cardName) {
        SwingUtilities.invokeLater(() -> {
            // Este método ya está cubierto por loadCardToPool
            if (cardName != null && !cardName.isEmpty()) {
                System.out.println("Carta cargada: " + cardName + " (" + cardsLoaded + "/" + totalCards + ")");
            }
        });
    }

    /**
     * @param statusMessage Mensaje descriptivo del estado actual
     * @param isError true si es un estado de error
     */
    @Override
    public void onStatusUpdate(String statusMessage, boolean isError) {
        SwingUtilities.invokeLater(() -> {
            updateStatus(statusMessage);
            if (isError) {
                addToBattleLog(statusMessage);
            } else {
                addToBattleLog(statusMessage);
            }
        });
    }

    @Override
    public void onDuelReset() {
        SwingUtilities.invokeLater(() -> {
            addToBattleLog("=== DUELO REINICIADO ===");
            addToBattleLog("Preparando nuevo duelo...");
            updateStatus("Duelo reiniciado - Cargando cartas...");

            // Resetear estado visual
            playerWins = 0;
            aiWins = 0;
            currentRound = 1;
            updateScores();
            updateRoundInfo();
            resetCardBorders();
            enableCardInteractivity(false);
            updateTurnIndicator();
        });
    }

    /**
     * @param playerName Nombre del jugador ("Jugador" o "Máquina")
     * @param isAttackMode true si está en modo ataque, false si en defensa
     */
    @Override
    public void onBattleModeSet(String playerName, boolean isAttackMode) {
        SwingUtilities.invokeLater(() -> {
            String mode = isAttackMode ? "ATAQUE" : "DEFENSA";
            addToBattleLog(playerName + " se prepara en modo: " + mode);

            // Mostrar visualmente el modo de batalla
            if ("Jugador".equals(playerName)) {
                updateStatus("Modo: " + mode + " - Selecciona una carta");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.printf("No se pudo cargar el Look and Feel del sistema: %s%n", e.getMessage());
            }

            DuelFrame frame = new DuelFrame();
            frame.initializeComponents();
            frame.setVisible(true);
        });
    }
}