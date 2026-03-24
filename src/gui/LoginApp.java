package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import model.User;
import model.Trainer;
import model.Trainee;
import service.AuthService;
import service.AuthException;

/**
 * M4 — JavaFX Login Screen
 * Entry point for SkillBridge. Authenticates via AuthService,
 * then routes to the correct dashboard based on user role.
 */
public class LoginApp extends Application {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final String DEEP_NAVY    = "#0D1B2A";
    private static final String CARD_BG      = "#152232";
    private static final String ACCENT_TEAL  = "#00C9A7";
    private static final String ACCENT_WARM  = "#F4A261";
    private static final String TEXT_PRIMARY  = "#E8EDF2";
    private static final String TEXT_MUTED    = "#7A8FA6";
    private static final String BORDER_COLOR  = "#1E3448";
    private static final String ERROR_RED     = "#E05C5C";

    private final AuthService authService = new AuthService();

    // UI refs we need across methods
    private TextField     emailField;
    private PasswordField passwordField;
    private Label         statusLabel;
    private Button        loginButton;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SkillBridge — Login");
        primaryStage.setResizable(false);

        Scene scene = new Scene(buildRoot(), 900, 600);
        String stylesheet = inlineStyles();
        if (!stylesheet.isEmpty()) {
            scene.getStylesheets().add(stylesheet);
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ── Root layout: left decorative panel + right form ──────────────────────

    private HBox buildRoot() {
        HBox root = new HBox();
        root.getChildren().addAll(buildBrandPanel(), buildFormPanel());
        return root;
    }

    // ── Left decorative brand panel ───────────────────────────────────────────

    private StackPane buildBrandPanel() {
        StackPane panel = new StackPane();
        panel.setPrefWidth(380);
        panel.setPrefHeight(600);
        panel.setStyle("-fx-background-color: " + DEEP_NAVY + ";");

        // Decorative gradient rectangle
        Rectangle gradRect = new Rectangle(380, 600);
        LinearGradient grad = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#00C9A7", 0.25)),
            new Stop(1, Color.web("#0D1B2A", 0.0))
        );
        gradRect.setFill(grad);

        // Brand text stack
        VBox brand = new VBox(12);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(0, 0, 0, 48));

        Text logo = new Text("SkillBridge");
        logo.setFont(Font.font("Georgia", FontWeight.BOLD, 42));
        logo.setFill(Color.web(ACCENT_TEAL));

        DropShadow glow = new DropShadow(18, Color.web(ACCENT_TEAL, 0.6));
        logo.setEffect(glow);

        Text tagline = new Text("Digital Skill Portfolios\nfor Rural Youth");
        tagline.setFont(Font.font("Georgia", 16));
        tagline.setFill(Color.web(TEXT_PRIMARY, 0.85));
        tagline.setLineSpacing(4);

        Separator sep = new Separator();
        sep.setPrefWidth(60);
        sep.setStyle("-fx-background-color: " + ACCENT_WARM + "; -fx-pref-height: 2px;");

        Text sub = new Text("Replace paper certificates\nwith verifiable digital records.");
        sub.setFont(Font.font("Verdana", 13));
        sub.setFill(Color.web(TEXT_MUTED));
        sub.setLineSpacing(4);

        brand.getChildren().addAll(logo, tagline, sep, sub);

        // Bottom team label
        Text team = new Text("Team ByteForge · T-170");
        team.setFont(Font.font("Verdana", 11));
        team.setFill(Color.web(TEXT_MUTED, 0.6));
        StackPane.setAlignment(team, Pos.BOTTOM_LEFT);
        StackPane.setMargin(team, new Insets(0, 0, 24, 48));

        panel.getChildren().addAll(gradRect, brand, team);
        return panel;
    }

    // ── Right login form panel ────────────────────────────────────────────────

    private VBox buildFormPanel() {
        VBox panel = new VBox(24);
        panel.setPrefWidth(520);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(60, 72, 60, 72));
        panel.setStyle("-fx-background-color: " + CARD_BG + ";");

        // Heading
        Text heading = new Text("Welcome back");
        heading.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        heading.setFill(Color.web(TEXT_PRIMARY));

        Text subHeading = new Text("Sign in to your account");
        subHeading.setFont(Font.font("Verdana", 13));
        subHeading.setFill(Color.web(TEXT_MUTED));

        VBox headingBox = new VBox(4, heading, subHeading);
        headingBox.setAlignment(Pos.CENTER_LEFT);

        // Email field
        VBox emailBox = buildLabeledField("Email address");
        emailField = new TextField();
        emailField.setPromptText("you@skillbridge.com");
        styleInput(emailField);
        emailField.setOnAction(e -> passwordField.requestFocus());
        emailBox.getChildren().add(emailField);

        // Password field
        VBox passwordBox = buildLabeledField("Password");
        passwordField = new PasswordField();
        passwordField.setPromptText("••••••••");
        styleInput(passwordField);
        passwordField.setOnAction(e -> handleLogin());
        passwordBox.getChildren().add(passwordField);

        // Status label (errors / loading)
        statusLabel = new Label();
        statusLabel.setFont(Font.font("Verdana", 12));
        statusLabel.setTextFill(Color.web(ERROR_RED));
        statusLabel.setVisible(false);
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        // Login button
        loginButton = new Button("Sign In");
        loginButton.getStyleClass().add("login-btn");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> handleLogin());

        // Demo hint
        Text hint = new Text("Demo: trainer@skillbridge.com / trainer123\n"
                           + "      riya@skillbridge.com / trainee123");
        hint.setFont(Font.font("Verdana", 11));
        hint.setFill(Color.web(TEXT_MUTED, 0.7));
        hint.setLineSpacing(3);

        panel.getChildren().addAll(
            headingBox,
            emailBox,
            passwordBox,
            statusLabel,
            loginButton,
            hint
        );
        return panel;
    }

    // ── Login handler — runs DB call off the FX thread ────────────────────────

    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        // Basic client-side validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }

        setLoading(true);

        // Run AuthService.login() on a background thread so the UI stays responsive
        Task<User> loginTask = new Task<User>() {
            @Override
            protected User call() throws Exception {
                return authService.login(email, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            setLoading(false);
            User user = loginTask.getValue();
            routeToDashboard(user);
        });

        loginTask.setOnFailed(e -> {
            setLoading(false);
            Throwable ex = loginTask.getException();
            if (ex instanceof AuthException) {
                showError(ex.getMessage());
            } else {
                showError("Could not connect to database. Check your connection.");
            }
        });

        new Thread(loginTask).start();
    }

    // ── Route to the correct dashboard based on role ──────────────────────────

    private void routeToDashboard(User user) {
        Stage current = (Stage) loginButton.getScene().getWindow();

        if (user instanceof Trainer) {
            Trainer trainer = (Trainer) user;
            new TrainerDashboard(trainer).show();
        } else if (user instanceof Trainee) {
            Trainee trainee = (Trainee) user;
            new TraineeDashboard(trainee).show();
        }

        current.close();
    }

    // ── Helper: build a label + field wrapper ─────────────────────────────────

    private VBox buildLabeledField(String labelText) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        label.setTextFill(Color.web(TEXT_MUTED));
        VBox box = new VBox(6, label);
        return box;
    }

    // ── Helper: apply consistent style to text inputs ─────────────────────────

    private void styleInput(Control input) {
        input.setStyle(
            "-fx-background-color: " + DEEP_NAVY + ";" +
            "-fx-text-fill: "        + TEXT_PRIMARY + ";" +
            "-fx-prompt-text-fill: " + TEXT_MUTED + ";" +
            "-fx-border-color: "     + BORDER_COLOR + ";" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 10 14;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: Verdana;"
        );
        input.setPrefHeight(42);
        input.setMaxWidth(Double.MAX_VALUE);
    }

    private void showError(String msg) {
        statusLabel.setText("⚠  " + msg);
        statusLabel.setVisible(true);
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        loginButton.setText(loading ? "Signing in…" : "Sign In");
        statusLabel.setVisible(false);
    }

    // ── Inline CSS stylesheet ─────────────────────────────────────────────────

    private String inlineStyles() {
        // JavaFX CSS loaded as a data URI isn't supported directly,
        // so we apply styles programmatically above.
        // This method is a hook for future external stylesheet loading.
        return getClass().getResource("/styles/app.css") != null
            ? getClass().getResource("/styles/app.css").toExternalForm()
            : "";
    }

    // ── Button styling via scene CSS string ──────────────────────────────────

    @Override
    public void init() {
        // No-op — button styles set via setStyle in buildFormPanel
    }

    public static void main(String[] args) {
        launch(args);
    }
}
