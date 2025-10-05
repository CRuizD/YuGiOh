package org.example.model;

import java.util.Objects;

/**
 * ¡Representa una carta de Yu-Gi-Oh! Con sus atributos principales.
 * Esta clase almacena la información básica de una carta Monster obtenida desde la API.
 */
public class Card {
    // Nombre de la carta
    private String name;

    // Puntos de ataque (ATK)
    private int atk;

    // Puntos de defensa (DEF)
    private int def;

    // URL de la imagen de la carta
    private String imageUrl;

    // Tipo de carta (ej: "Normal Monster", "Effect Monster")
    private String type;

    /**
     * Constructor para crear una nueva carta.
     * @param name     Nombre de la carta
     * @param atk      Puntos de ataque (ATK)
     * @param def      Puntos de defensa (DEF)
     * @param imageUrl URL de la imagen de la carta
     * @param type     Tipo de carta (ej: "Normal Monster", "Effect Monster")
     */
    public Card(String name, int atk, int def, String imageUrl, String type) {
        this.name = name != null ? name : "Carta Desconocida";
        this.atk = Math.max(atk, 0); // Asegurar que no sea negativo
        this.def = Math.max(def, 0); // Asegurar que no sea negativo
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.type = type != null ? type : "Unknown Type";
    }

    /**
     * @return Nombre de la carta
     */
    public String getName() {
        return name;
    }

    /**
     * @return Puntos de ataque (ATK)
     */
    public int getAtk() {
        return atk;
    }

    /**
     * @return Puntos de defensa (DEF)
     */
    public int getDef() {
        return def;
    }

    /**
     * @return URL de la imagen de la carta
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * @return Tipo de carta
     */
    public String getType() {
        return type;
    }

    /**
     * Verifica si la carta tiene una URL de imagen válida.
     * @return true si la carta tiene imagen, false en caso contrario
     */
    public boolean hasImage() {
        return imageUrl != null &&
                !imageUrl.isEmpty() &&
                !imageUrl.equals("null") &&
                !imageUrl.equals("undefined") &&
                imageUrl.startsWith("http");
    }

    /**
     * Verifica si la carta es de tipo Monster
     * @return true si es una carta Monster, false en caso contrario
     */
    public boolean isMonster() {
        if (type == null) return false;

        String lowerType = type.toLowerCase();

        // Lista más específica de tipos Monster válidos
        return lowerType.contains("monster") &&
                !lowerType.contains("spell") &&
                !lowerType.contains("trap") &&
                !lowerType.contains("token") &&
                !lowerType.contains("skill") &&
                !lowerType.contains("magic") &&
                !lowerType.contains("normal spell") &&
                !lowerType.contains("continuous spell") &&
                !lowerType.contains("equip spell") &&
                !lowerType.contains("quick-play spell") &&
                !lowerType.contains("field spell") &&
                !lowerType.contains("ritual spell") &&
                !lowerType.contains("normal trap") &&
                !lowerType.contains("continuous trap") &&
                !lowerType.contains("counter trap");
    }

    /**
     * Verifica si la carta es válida para usar en el duelo.
     * @return true si la carta es un Monster válido con stats no negativos
     */
    public boolean isValidMonster() {
        return isMonster() &&
                atk >= 0 &&
                def >= 0 &&
                name != null &&
                !name.isEmpty() &&
                !name.equals("null") &&
                !name.equals("undefined");
    }

    /**
     * Obtiene el poder de la carta según el modo (ataque/defensa).
     * @param isAttackMode true para ataque, false para defensa
     * @return Puntos de ATK o DEF según el modo
     */
    public int getPower(boolean isAttackMode) {
        return isAttackMode ? atk : def;
    }

    /**
     * Obtiene una representación corta de los stats de la carta.
     * @return String en formato "ATK/DEF"
     */
    public String getStatsShort() {
        return atk + "/" + def;
    }

    /**
     * Obtiene el nivel de poder de la carta basado en ATK.
     * @return String descriptivo del nivel de poder
     */
    public String getPowerLevel() {
        if (atk >= 2500) return "MUY ALTO";
        if (atk >= 2000) return "ALTO";
        if (atk >= 1500) return "MEDIO";
        if (atk >= 1000) return "BAJO";
        return "MUY BAJO";
    }

    /**
     * Verifica si esta carta es más poderosa que otra carta en modo ataque.
     * @param other Otra carta a comparar
     * @return true si esta carta tiene mayor ATK
     */
    public boolean isStrongerThan(Card other) {
        return this.atk > other.atk;
    }

    /**
     * Verifica si esta carta es más defensiva que otra carta.
     * @param other Otra carta a comparar
     * @return true si esta carta tiene mayor DEF
     */
    public boolean isMoreDefensiveThan(Card other) {
        return this.def > other.def;
    }

    /**
     * Obtiene el poder total combinado (ATK + DEF).
     * @return Suma de ATK y DEF
     */
    public int getTotalPower() {
        return atk + def;
    }

    /**
     * Determina si la carta es de alto nivel basado en sus stats.
     * @return true si tiene ATK >= 2000
     */
    public boolean isHighLevel() {
        return atk >= 2000;
    }

    /**
     * Determina si la carta es de bajo nivel basado en sus stats.
     * @return true si tiene ATK <= 1000
     */
    public boolean isLowLevel() {
        return atk <= 1000;
    }

    /**
     * Obtiene una categoría de la carta basada en sus stats.
     * @return Categoría descriptiva
     */
    public String getCategory() {
        if (atk >= 2500 && def >= 2000) return "BOSS";
        if (atk >= 2000) return "ATAQUE FUERTE";
        if (def >= 2000) return "DEFENSA FUERTE";
        if (atk >= 1500 && def >= 1500) return "BALANCEADA";
        if (atk >= def) return "OFENSIVA";
        return "DEFENSIVA";
    }

    /**
     * Verifica si esta carta es igual a otra carta.
     * Dos cartas se consideran iguales si tienen el mismo nombre y stats.
     * @param obj Objeto a comparar
     * @return true si son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return atk == card.atk &&
                def == card.def &&
                Objects.equals(name, card.name) &&
                Objects.equals(type, card.type);
    }

    /**
     * Genera un código hash para la carta basado en sus atributos.
     * @return Código hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, atk, def, type);
    }

    /**
     * Representación en String de la carta.
     * @return String con formato "Nombre (ATK: X, DEF: Y, Tipo: Z)"
     */
    @Override
    public String toString() {
        return String.format("%s (ATK: %d, DEF: %d, Tipo: %s)", name, atk, def, type);
    }

    /**
     * Representación detallada de la carta para debugging.
     * @return String con todos los detalles de la carta
     */
    public String toDetailedString() {
        return String.format(
                "Card{name='%s', atk=%d, def=%d, type='%s', imageUrl='%s', hasImage=%s, isValid=%s}",
                name, atk, def, type, imageUrl, hasImage(), isValidMonster()
        );
    }

    /**
     * Representación compacta para mostrar en UI.
     * @return String compacto con nombre y stats
     */
    public String toCompactString() {
        return String.format("%s [%d/%d]", name, atk, def);
    }

    /**
     * Obtiene información de la carta en formato HTML para tooltips o mensajes.
     * @return String con formato HTML
     */
    public String toHTMLString() {
        return String.format(
                "<html><b>%s</b><br>ATK: %d | DEF: %d<br><i>%s</i></html>",
                name, atk, def, type
        );
    }
}