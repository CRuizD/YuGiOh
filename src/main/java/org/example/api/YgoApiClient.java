package org.example.api;

import org.example.model.Card;
import org.json.JSONObject;
import org.json.JSONArray;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;

/**
 * ¡Cliente para consumir la API de YGOProDeck y obtener cartas de Yu-Gi-Oh! Aleatorias.
 * Maneja peticiones HTTP, parseo de JSON y fallbacks para garantizar cartas válidas.
 */
public class YgoApiClient {
    // Constantes de configuración

    // URL principal de la API para obtener cartas aleatorias
    private static final String RANDOM_CARD_URL = "https://db.ygoprodeck.com/api/v7/randomcard.php";

    // Timeout para conexiones HTTP
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    /**
     * Cartas Monster populares con imágenes garantizadas.
     * Formato: [nombre, id, atk, def] - Stats balanceados para duelo justo
     */
    private static final String[][] POPULAR_MONSTERS = {
            {"Dark Magician", "46986414", "2500", "2100"},
            {"Blue-Eyes White Dragon", "89631139", "3000", "2500"},
            {"Summoned Skull", "70781052", "2500", "1200"},
            {"Gaia The Fierce Knight", "06368038", "2300", "2100"},
            {"Celtic Guardian", "91152256", "1400", "1200"},
            {"Mystical Elf", "15025844", "800", "2000"},
            {"Battle Ox", "05053103", "1700", "1000"},
            {"Kuriboh", "40640057", "300", "200"},
            {"Time Wizard", "71625222", "500", "400"},
            {"Red-Eyes B. Dragon", "74677422", "2400", "2000"},
            {"Baby Dragon", "88819587", "1200", "700"},
            {"Hitotsu-Me Giant", "76184692", "1200", "1000"},
            {"Flame Swordsman", "45231177", "1800", "1600"},
            {"Buster Blader", "78193831", "2600", "2300"},
            {"La Jinn the Mystical Genie of the Lamp", "97590747", "1800", "1000"},
            {"Man-Eater Bug", "54652250", "450", "600"},
            {"Hane-Hane", "07089711", "450", "500"},
            {"Mammoth Graveyard", "40374923", "1200", "800"},
            {"Kojikocy", "01184620", "1500", "1200"},
            {"Ryu-Kishin", "24611934", "1000", "500"}
    };

    // Variables de instancia
    private final HttpClient httpClient;
    private final Random random;

    /**
     * Constructor que inicializa el cliente HTTP con configuración optimizada.
     */
    public YgoApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECTION_TIMEOUT)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.random = new Random();
    }

    /**
     * Obtiene una carta Monster aleatoria desde la API YGOProDeck.
     * Si no encuentra cartas Monster después de varios intentos, usa cartas predefinidas.
     * @return Card objeto con los datos de la carta Monster
     * @throws Exception Si ocurre un error en la conexión o procesamiento
     */
    public Card getRandomMonsterCard() throws Exception {
        int attempts = 0;
        final int MAX_ATTEMPTS = 5;

        while (attempts < MAX_ATTEMPTS) {
            try {
                Card card = tryGetFromAPI();
                if (card != null && card.isValidMonster()) {
                    System.out.printf("Carta obtenida de API: %s (ATK: %d, DEF: %d)%n",
                            card.getName(), card.getAtk(), card.getDef());
                    return card;
                } else if (card != null) {
                    System.out.printf("Carta descartada (no Monster): %s (Tipo: %s)%n",
                            card.getName(), card.getType());
                }

            } catch (Exception e) {
                System.err.printf("Error en intento %d/%d: %s%n",
                        attempts + 1, MAX_ATTEMPTS, e.getMessage());

                // Si es el último intento y hay error grave, lanzar excepción
                if (attempts == MAX_ATTEMPTS - 1 &&
                        (e.getMessage().contains("timeout") || e.getMessage().contains("connect"))) {
                    throw new Exception("No se pudo conectar con la API después de " + MAX_ATTEMPTS + " intentos", e);
                }
            }

            attempts++;
            if (attempts < MAX_ATTEMPTS) {
                Thread.sleep(800); // Pausa entre intentos
            }
        }

        // Fallback a cartas predefinidas
        System.out.println("Usando carta predefinida (fallback)");
        return getGuaranteedMonsterCard();
    }

    /**
     * Obtiene múltiples cartas Monster de forma eficiente.
     * @param count Número de cartas a obtener
     * @return Lista de cartas Monster
     * @throws Exception Si ocurre un error
     */
    public java.util.List<Card> getMultipleMonsterCards(int count) throws Exception {
        java.util.List<Card> cards = new java.util.ArrayList<>();
        int attempts = 0;
        final int MAX_TOTAL_ATTEMPTS = count * 3;

        while (cards.size() < count && attempts < MAX_TOTAL_ATTEMPTS) {
            try {
                Card card = getRandomMonsterCard();
                if (card != null) {
                    cards.add(card);
                    System.out.printf("Progreso: %d/%d cartas obtenidas%n", cards.size(), count);
                }
            } catch (Exception e) {
                System.err.printf("Error obteniendo carta %d: %s%n", cards.size() + 1, e.getMessage());
            }
            attempts++;
        }

        // Si no se pudieron obtener suficientes cartas de la API, completar con predefinidas
        while (cards.size() < count) {
            cards.add(getGuaranteedMonsterCard());
        }

        return cards;
    }

    /**
     * Verifica la conectividad con la API.
     * @return true si la API está disponible, false en caso contrario
     */
    public boolean testAPIConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RANDOM_CARD_URL))
                    .timeout(Duration.ofSeconds(10))
                    .HEAD()
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.printf("Test de conexión fallido: %s%n", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene estadísticas de uso de la API.
     */
    public void printAPIStats() {
        System.out.println("=== ESTADÍSTICAS YGOPRODECK API ===");
        System.out.println("URL: " + RANDOM_CARD_URL);
        System.out.println("Timeout: " + REQUEST_TIMEOUT.getSeconds() + " segundos");
        System.out.println("Cartas predefinidas: " + POPULAR_MONSTERS.length);
        System.out.println("User-Agent: YuGiOhDuelSimulator/1.0");
    }

    /**
     * Intenta obtener una carta Monster desde la API.
     * @return Card si es Monster válida, null si no es Monster o hay error
     * @throws Exception Si hay error de conexión
     */
    private Card tryGetFromAPI() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RANDOM_CARD_URL))
                .timeout(REQUEST_TIMEOUT)
                .header("User-Agent", "YuGiOhDuelSimulator/1.0 (+https://github.com/yu-gi-oh-simulator)")
                .header("Accept", "application/json")
                .header("Accept-Language", "es-ES,es;q=0.9")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String responseBody = response.body();
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                try {
                    JSONObject json = new JSONObject(responseBody);
                    return parseCardFromJSON(json);
                } catch (Exception e) {
                    throw new Exception("Error parseando JSON: " + e.getMessage(), e);
                }
            } else {
                throw new Exception("Respuesta vacía de la API");
            }
        } else {
            throw new Exception("Error HTTP " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Parsea un JSONObject a un objeto Card, verificando que sea Monster.
     * @param json JSONObject con datos de la carta
     * @return Card si es Monster válida, null en caso contrario
     */
    private Card parseCardFromJSON(JSONObject json) {
        try {
            String type = json.optString("type", "");
            String name = json.optString("name", "");

            if (!isValidMonsterType(type)) {
                return null;
            }

            // Verificar que tenga stats válidos
            int atk = parseStat(json, "atk");
            int def = parseStat(json, "def");

            if (atk < 0 || def < 0) {
                System.out.printf("Carta con stats inválidos: %s (ATK: %d, DEF: %d)%n", name, atk, def);
                return null;
            }

            String imageUrl = parseImageUrl(json);

            return new Card(name, atk, def, imageUrl, type);

        } catch (Exception e) {
            System.err.printf("Error parseando carta desde JSON: %s%n", e.getMessage());
            return null;
        }
    }

    /**
     * Parsea un stat (ATK/DEF) del JSON, manejando valores no numéricos.
     */
    private int parseStat(JSONObject json, String statName) {
        try {
            Object statValue = json.opt(statName);
            if (statValue instanceof Integer) {
                return (Integer) statValue;
            } else if (statValue instanceof String) {
                String strValue = (String) statValue;
                if (strValue.equals("?") || strValue.isEmpty()) {
                    return 0; // Valores desconocidos se tratan como 0
                }
                return Integer.parseInt(strValue);
            }
            return 0;
        } catch (Exception e) {
            System.err.printf("Error parseando %s: %s%n", statName, e.getMessage());
            return 0;
        }
    }

    /**
     * Verifica si el tipo indicado corresponde a una carta Monster válida.
     * @param type Tipo de carta a verificar
     * @return true si es Monster válida, false en caso contrario
     */
    private boolean isValidMonsterType(String type) {
        if (type == null || type.isEmpty()) return false;

        String lowerType = type.toLowerCase();

        // Tipos Monster válidos
        boolean isValidMonster = lowerType.contains("monster") &&
                !lowerType.contains("spell") &&
                !lowerType.contains("trap") &&
                !lowerType.contains("token") &&
                !lowerType.contains("skill") &&
                !lowerType.contains("magic");

        if (isValidMonster) {
            System.out.printf("Tipo Monster detectado: %s%n", type);
        }

        return isValidMonster;
    }

    /**
     * Obtiene una carta Monster predefinida con imagen y stats garantizados.
     * @return Card con datos predefinidos
     */
    private Card getGuaranteedMonsterCard() {
        int index = random.nextInt(POPULAR_MONSTERS.length);
        String[] monster = POPULAR_MONSTERS[index];
        String name = monster[0];
        String cardId = monster[1];
        int atk = Integer.parseInt(monster[2]);
        int def = Integer.parseInt(monster[3]);

        // URL de imagen garantizada usando el ID oficial
        String imageUrl = "https://images.ygoprodeck.com/images/cards/" + cardId + ".jpg";
        String type = "Normal Monster";

        System.out.printf("Carta predefinida: %s (ATK: %d, DEF: %d)%n", name, atk, def);
        return new Card(name, atk, def, imageUrl, type);
    }

    /**
     * Parsea y construye la URL de imagen de la carta desde el JSON.
     * @param json JSONObject con datos de la carta
     * @return URL de la imagen o string vacío si no está disponible
     */
    private String parseImageUrl(JSONObject json) {
        try {
            // Prioridad 1: Array card_images (formato más común)
            if (json.has("card_images")) {
                Object cardImagesObj = json.get("card_images");
                if (cardImagesObj instanceof JSONArray) {
                    JSONArray cardImages = (JSONArray) cardImagesObj;
                    if (cardImages.length() > 0) {
                        JSONObject firstImage = cardImages.getJSONObject(0);

                        // Probar diferentes campos de imagen
                        String[] imageFields = {"image_url", "image_url_cropped", "image_url_small"};
                        for (String field : imageFields) {
                            if (firstImage.has(field)) {
                                String url = firstImage.optString(field, "");
                                if (!url.isEmpty()) {
                                    String fixedUrl = fixImageUrl(url);
                                    System.out.printf("Imagen encontrada en %s: %s%n", field, fixedUrl);
                                    return fixedUrl;
                                }
                            }
                        }
                    }
                }
            }

            // Prioridad 2: Campos directos en el objeto principal
            String[] directImageFields = {"card_image", "image_url", "image_url_cropped"};
            for (String field : directImageFields) {
                if (json.has(field)) {
                    String url = json.optString(field, "");
                    if (!url.isEmpty()) {
                        String fixedUrl = fixImageUrl(url);
                        System.out.printf("Imagen encontrada en campo directo %s: %s%n", field, fixedUrl);
                        return fixedUrl;
                    }
                }
            }

            // Prioridad 3: Construir desde ID
            if (json.has("id")) {
                String cardId = json.optString("id", "");
                if (!cardId.isEmpty()) {
                    String constructedUrl = "https://images.ygoprodeck.com/images/cards/" + cardId + ".jpg";
                    System.out.printf("Imagen construida desde ID: %s%n", constructedUrl);
                    return constructedUrl;
                }
            }

        } catch (Exception e) {
            System.err.printf("Error parseando imagen: %s%n", e.getMessage());
        }

        System.out.println("No se pudo obtener imagen para la carta");
        return "";
    }

    /**
     * Corrige y normaliza URLs de imágenes.
     * @param url URL a corregir
     * @return URL normalizada
     */
    private String fixImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        if (url.startsWith("//")) {
            return "https:" + url;
        }
        if (!url.startsWith("http")) {
            // Si es solo un nombre de archivo, construir URL completa
            if (url.endsWith(".jpg") || url.endsWith(".png")) {
                return "https://images.ygoprodeck.com/images/cards/" + url;
            }
        }
        return url;
    }
}