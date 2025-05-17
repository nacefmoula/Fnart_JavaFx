package tn.esprit.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import tn.esprit.models.User;
import tn.esprit.enumerations.Role;

import javax.net.ssl.*;
import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStreamReader;

public class GoogleAuthService {
    private static final String APPLICATION_NAME = "Fnart";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "src/main/resources/credentials.json";
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email"
    );
    private static final String CLIENT_ID = "1091630808490-ptqes5p84aa9elpoclith2uvv6j61a8a.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost:8888/oauth2callback";
    private static final int SERVER_PORT = 8888;

    private final UserService userService;
    private final NetHttpTransport transport;
    private final JacksonFactory jsonFactory;
    private GoogleAuthorizationCodeFlow flow;

    // Instead of hardcoding the port
    private int serverPort;
    private String redirectUri;



    public GoogleAuthService() {
        setupTrustAllCerts();

        this.userService = new UserService();
        this.transport = new NetHttpTransport();
        this.jsonFactory = JacksonFactory.getDefaultInstance();

        // Find an available port
        this.serverPort = findAvailablePort();
        this.redirectUri = "http://localhost:" + serverPort + "/oauth2callback";

        try {
            // Load client secrets
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    jsonFactory,
                    new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH))
            );

            // Build flow
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    transport,
                    jsonFactory,
                    clientSecrets,
                    SCOPES
            )
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTrustAllCerts() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Helper method to find an available port
    private int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            return 8889; // Fallback to a default (not 8888)
        }
    }


    public CompletableFuture<User> authenticateWithGoogle() {
        CompletableFuture<User> future = new CompletableFuture<>();

        try {
            // Create authorization URL using the dynamic redirectUri
            String authorizationUrl = flow.newAuthorizationUrl()
                    .setRedirectUri(redirectUri)
                    .setState(generateRandomState())
                    .build();

            // Open default browser with authorization URL
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authorizationUrl));
                System.out.println("Opening browser for Google authentication: " + authorizationUrl);
            } else {
                System.out.println("Please open this URL in your browser: " + authorizationUrl);
            }

            // Start a local server to capture the OAuth callback using dynamic port
            startLocalServer(future);

        } catch (Exception e) {
            e.printStackTrace();
            future.completeExceptionally(e);
        }

        return future;
    }

    private void startLocalServer(CompletableFuture<User> future) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
                System.out.println("Local server started on port " + serverPort + ". Waiting for OAuth callback...");
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String line;
                String code = null;
                String requestLine = null;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    // Store the first line of the HTTP request
                    if (requestLine == null && line.startsWith("GET")) {
                        requestLine = line;
                        System.out.println("Debug - Request line: " + line);
                    }
                }

                // Parse the code from the request line
                if (requestLine != null && requestLine.contains("code=")) {
                    int codeStart = requestLine.indexOf("code=");
                    if (codeStart != -1) {
                        code = requestLine.substring(codeStart + 5);
                        if (code.contains(" ")) {
                            code = code.substring(0, code.indexOf(" "));
                        }
                        if (code.contains("&")) {
                            code = code.substring(0, code.indexOf("&"));
                        }
                        // URL decode the authorization code
                        code = java.net.URLDecoder.decode(code, "UTF-8");
                        System.out.println("Debug - Extracted code: " + code);
                    }
                }

                if (code != null) {
                    // Send a simple success page back to the browser
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/html");
                    out.println();
                    out.println("<html><body><h1>Authentication successful!</h1><p>You can close this window now.</p></body></html>");

                    // Process the authorization code
                    try {
                        String finalCode = code;
                        System.out.println("Debug - Using code for token exchange: " + finalCode);
                        GoogleTokenResponse tokenResponse = flow.newTokenRequest(finalCode)
                                .setRedirectUri(redirectUri)  // Use the dynamic redirectUri
                                .setGrantType("authorization_code")
                                .execute();

                        String idTokenString = tokenResponse.getIdToken();
                        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                                .setAudience(Collections.singletonList(CLIENT_ID))
                                .build();

                        GoogleIdToken idToken = verifier.verify(idTokenString);
                        if (idToken != null) {
                            Payload payload = idToken.getPayload();
                            String email = payload.getEmail();
                            String name = (String) payload.get("name");
                            String pictureUrl = (String) payload.get("picture");

                            User user = processGoogleUser(email, name, pictureUrl);
                            future.complete(user);
                        } else {
                            future.completeExceptionally(new Exception("Invalid ID token"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        future.completeExceptionally(e);
                    }
                } else {
                    // No code found in the request
                    out.println("HTTP/1.1 400 Bad Request");
                    out.println("Content-Type: text/html");
                    out.println();
                    out.println("<html><body><h1>Authentication failed</h1><p>No authorization code found in the request.</p></body></html>");
                    future.completeExceptionally(new Exception("No authorization code found in the request"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        }).start();
    }

    private User processGoogleUser(String email, String name, String pictureUrl) {
        try {
            // Check if user exists in database
            User existingUser = userService.findByEmail(email);
            if (existingUser != null) {
                return existingUser;
            }

            // Create new user if doesn't exist
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setNom(name);
            newUser.setRole(Role.ARTIST); // Default role for Google sign-in
            newUser.setImage(pictureUrl);
            newUser.setIsActive(true);

            // Generate a random password for the user
            String randomPassword = generateRandomPassword();
            newUser.setPassword(randomPassword);

            // Save the new user
            userService.create(newUser);
            return newUser;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error processing Google user: " + e.getMessage());
        }
    }

    private String generateRandomPassword() {
        // Generate a random password for the user
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private String generateRandomState() {
        // Generate a random state string for CSRF protection
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder state = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 16; i++) {
            state.append(chars.charAt(random.nextInt(chars.length())));
        }
        return state.toString();
    }
}