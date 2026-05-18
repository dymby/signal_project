package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketClientReader implements DataReader{

    private final String serverUri;
    private WebSocketClient client;

    /**
     * Constructs a WebSocketClientReader that will connect to the given URI.
     *
     * @param serverUri the WebSocket server URI, e.g. "ws://localhost:8080"
     */
    public WebSocketClientReader(String serverUri) {
        this.serverUri = serverUri;
    }

    /**
     * Connects to the WebSocket server and begins receiving data.
     * This method blocks until the connection is closed.
     *
     * @param dataStorage the storage where incoming data will be stored
     * @throws IOException if the URI is invalid or connection fails
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        try {
            client = new WebSocketClient(new URI(serverUri)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to WebSocket server: " + serverUri);
                }

                @Override
                public void onMessage(String message) {
                    try {
                        parseAndStore(message, dataStorage);
                    } catch (Exception e) {
                        System.err.println("Failed to parse message: " + message
                                + " — " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from WebSocket server."
                            + " Code: " + code + ", Reason: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            };

            client.connectBlocking();

        } catch (URISyntaxException e) {
            throw new IOException("Invalid WebSocket URI: " + serverUri, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Connection interrupted", e);
        }
    }

    /**
     * Disconnects from the WebSocket server gracefully.
     *
     * @throws IOException if disconnection fails
     */
    public void disconnect() throws IOException {
        if (client != null) {
            try {
                client.closeBlocking();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Disconnection interrupted", e);
            }
        }
    }

    /**
     * Parses a message in the format: patientId,timestamp,label,data
     * and stores it in DataStorage.
     *
     * @param message     the raw message string from the WebSocket server
     * @param dataStorage the storage to write parsed data into
     */
    public void parseAndStore(String message, DataStorage dataStorage) {
        String[] parts = message.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException(
                    "Expected 4 fields, got " + parts.length);
        }

        int patientId = Integer.parseInt(parts[0].trim());
        long timestamp = Long.parseLong(parts[1].trim());
        String label = parts[2].trim();
        String data = parts[3].trim();

        // strip % from saturation data
        data = data.replace("%", "");

        double value;
        if (data.equals("triggered")) {
            value = 1.0;
        } else if (data.equals("resolved")) {
            value = 0.0;
        } else {
            value = Double.parseDouble(data);
        }

        dataStorage.addPatientData(patientId, value, label, timestamp);
    }

    /**
     * Returns whether the client is currently connected.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return client != null && client.isOpen();
    }
}
