package gui;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.concurrent.Task;

import model.Trainer;
import model.Trainee;
import db.UserDAO;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * M4 — Trainer Dashboard
 * Shows the trainer's roster and allows enrolling new trainees.
 * All DB calls run on background threads via Task<T>.
 */
public class TrainerDashboard {

    // ── Palette (shared with LoginApp) ────────────────────────────────────────
    private static final String DEEP_NAVY    = "#0D1B2A";
    private static final String PANEL_BG     = "#0F1E2E";
    private static final String CARD_BG      = "#152232";
    private static final String ACCENT_TEAL  = "#00C9A7";
    private static final String ACCENT_WARM  = "#F4A261";
    private static final String TEXT_PRIMARY  = "#E8EDF2";
    private static final String TEXT_MUTED    = "#7A8FA6";
    private static final String BORDER_COLOR  = "#1E3448";
    private static final String SUCCESS_GREEN = "#3DDC84";
    private static final String ERROR_RED     = "#E05C5C";

    private final Trainer trainer;
    private final UserDAO userDAO = new UserDAO();
    private final Stage  stage    = new Stage();

    // Live data backing the TableView
    private final ObservableList<Trainee> rosterData = FXCollections.observableArrayList();

    // UI refs
    private TableView<Trainee> rosterTable;
    private Label               statusLabel;

    // ─────────────────────────────────────────────────────────────────────────

    public TrainerDashboard(Trainer trainer) {
        this.trainer = trainer;
    }

    public void show() {
        // Build dashboard once and then hydrate data asynchronously.
        stage.setTitle("SkillBridge — Trainer Portal");
        stage.setMinWidth(900);
        stage.setMinHeight(620);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + DEEP_NAVY + ";");
        root.setTop(buildTopBar());
        root.setLeft(buildSidebar());
        root.setCenter(buildMainContent());

        stage.setScene(new Scene(root, 1050, 650));
        stage.show();

        loadRoster(); // kick off background load
    }

    // ── Top navigation bar ────────────────────────────────────────────────────

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(16, 28, 16, 28));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setSpacing(16);
        bar.setStyle("-fx-background-color: " + CARD_BG + ";"
                   + "-fx-border-color: " + BORDER_COLOR + ";"
                   + "-fx-border-width: 0 0 1 0;");

        Text logo = new Text("SkillBridge");
        logo.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        logo.setFill(Color.web(ACCENT_TEAL));

        Text separator = new Text("·");
        separator.setFill(Color.web(TEXT_MUTED));

        Text pageTitle = new Text("Trainer Dashboard");
        pageTitle.setFont(Font.font("Verdana", 14));
        pageTitle.setFill(Color.web(TEXT_MUTED));

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Trainer name badge
        Label badge = new Label("👤  " + trainer.getName());
        badge.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        badge.setTextFill(Color.web(TEXT_PRIMARY));
        badge.setStyle(
            "-fx-background-color: " + DEEP_NAVY + ";" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-radius: 20;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 6 14;"
        );

        Button logoutBtn = new Button("↩ Logout");
        logoutBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + ACCENT_WARM + ";" +
            "-fx-font-family: Verdana;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: " + ACCENT_WARM + ";" +
            "-fx-border-radius: 16;" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 5 12;" +
            "-fx-cursor: hand;"
        );
        logoutBtn.setOnAction(e -> handleLogout());

        bar.getChildren().addAll(logo, separator, pageTitle, spacer, logoutBtn, badge);
        return bar;
    }

    // ── Left sidebar with stats ───────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(28, 18, 28, 18));
        sidebar.setStyle("-fx-background-color: " + PANEL_BG + ";"
                       + "-fx-border-color: " + BORDER_COLOR + ";"
                       + "-fx-border-width: 0 1 0 0;");

        Text sectionLabel = new Text("OVERVIEW");
        sectionLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        sectionLabel.setFill(Color.web(TEXT_MUTED));

        VBox rosterCard  = buildStatCard("Enrolled Trainees",
                                         String.valueOf(rosterData.size()), ACCENT_TEAL);
        VBox certCard    = buildStatCard("Certified", "—", SUCCESS_GREEN);
        VBox pendingCard = buildStatCard("In Progress", "—", ACCENT_WARM);

        // Keep summary cards synchronized with table backing data.
        rosterData.addListener((javafx.collections.ListChangeListener<Trainee>) c -> {
            // Count certified
            long certified = rosterData.stream().filter(Trainee::isCertified).count();
            long inProg    = rosterData.size() - certified;
            ((Text) ((VBox) rosterCard.getChildren().get(0)).getChildren().get(1))
                .setText(String.valueOf(rosterData.size()));
            ((Text) ((VBox) certCard.getChildren().get(0)).getChildren().get(1))
                .setText(String.valueOf(certified));
            ((Text) ((VBox) pendingCard.getChildren().get(0)).getChildren().get(1))
                .setText(String.valueOf(inProg));
        });

        sidebar.getChildren().addAll(sectionLabel, rosterCard, certCard, pendingCard);
        return sidebar;
    }

    private VBox buildStatCard(String label, String value, String accentColor) {
        VBox inner = new VBox(4);
        inner.setAlignment(Pos.CENTER_LEFT);

        Text valueText = new Text(value);
        valueText.setFont(Font.font("Georgia", FontWeight.BOLD, 32));
        valueText.setFill(Color.web(accentColor));

        Text labelText = new Text(label);
        labelText.setFont(Font.font("Verdana", 11));
        labelText.setFill(Color.web(TEXT_MUTED));

        inner.getChildren().addAll(valueText, labelText);

        VBox card = new VBox(inner);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color: " + CARD_BG + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-radius: 8;"
        );
        return card;
    }

    // ── Main content area: enroll form + roster table ─────────────────────────

    private VBox buildMainContent() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 32, 28, 32));

        content.getChildren().addAll(
            buildEnrollForm(),
            buildRosterTable()
        );
        return content;
    }

    // ── Enroll new trainee form ───────────────────────────────────────────────

    private VBox buildEnrollForm() {
        VBox section = new VBox(14);

        Text title = new Text("Enroll New Trainee");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        title.setFill(Color.web(TEXT_PRIMARY));

        HBox fields = new HBox(14);
        fields.setAlignment(Pos.CENTER_LEFT);

        TextField nameField  = createFormField("Full Name", 180);
        TextField emailField = createFormField("Email Address", 220);

        Button enrollBtn = new Button("＋  Enroll");
        enrollBtn.setPrefHeight(38);
        enrollBtn.setStyle(
            "-fx-background-color: " + ACCENT_TEAL + ";" +
            "-fx-text-fill: " + DEEP_NAVY + ";" +
            "-fx-font-family: Verdana;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0 20;"
        );

        // Status label for enroll feedback
        statusLabel = new Label();
        statusLabel.setFont(Font.font("Verdana", 12));
        statusLabel.setVisible(false);

        enrollBtn.setOnAction(e -> handleEnroll(
            nameField.getText().trim(),
            emailField.getText().trim(),
            nameField, emailField
        ));

        fields.getChildren().addAll(nameField, emailField, enrollBtn);
        section.getChildren().addAll(title, fields, statusLabel);
        return section;
    }

    private TextField createFormField(String prompt, double width) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(width);
        tf.setPrefHeight(38);
        tf.setStyle(
            "-fx-background-color: " + DEEP_NAVY + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-prompt-text-fill: " + TEXT_MUTED + ";" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 0 12;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: Verdana;"
        );
        return tf;
    }

    // ── Roster TableView ──────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private VBox buildRosterTable() {
        VBox section = new VBox(12);

        Text title = new Text("Trainee Roster");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        title.setFill(Color.web(TEXT_PRIMARY));

        rosterTable = new TableView<>(rosterData);
        rosterTable.setPrefHeight(320);
        rosterTable.setStyle(
            "-fx-background-color: " + CARD_BG + ";" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-table-cell-border-color: " + BORDER_COLOR + ";"
        );
        rosterTable.setPlaceholder(new Label("No trainees enrolled yet."));
        // Stretch columns so there is no empty "phantom" column on the right.
        // Fill table width so there is no empty strip on the right (JavaFX 20+).
        rosterTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);
        rosterTable.setRowFactory(tv -> {
            TableRow<Trainee> row = new TableRow<>();
            row.setStyle("-fx-background-color: " + CARD_BG + ";");
            return row;
        });

        TableColumn<Trainee, String> nameCol  = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        nameCol.setCellFactory(col -> darkTextCell());

        TableColumn<Trainee, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);
        emailCol.setCellFactory(col -> darkTextCell());

        TableColumn<Trainee, Integer> progCol = new TableColumn<>("Progress");
        progCol.setCellValueFactory(new PropertyValueFactory<>("completionPercent"));
        progCol.setPrefWidth(120);
        // Default JavaFX ProgressBar uses a light track (looks like white blocks on dark UI).
        progCol.setCellFactory(col -> new TableCell<Trainee, Integer>() {
            @Override
            protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                styleDarkTableCell(this);
                if (empty || val == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(buildDarkProgressBar(val));
                    setText(null);
                }
            }
        });

        TableColumn<Trainee, Boolean> certCol = new TableColumn<>("Certified");
        // Boolean getter is isCertified(); explicit factory avoids PropertyValueFactory edge cases.
        certCol.setCellValueFactory(cdf ->
                new ReadOnlyObjectWrapper<>(cdf.getValue().isCertified()));
        certCol.setPrefWidth(100);
        certCol.setCellFactory(col -> new TableCell<Trainee, Boolean>() {
            @Override
            protected void updateItem(Boolean val, boolean empty) {
                super.updateItem(val, empty);
                styleDarkTableCell(this);
                if (empty || val == null) {
                    setText(null);
                } else {
                    setText(val ? "Yes" : "-");
                    setTextFill(Color.web(val ? SUCCESS_GREEN : TEXT_MUTED));
                }
            }
        });

        styleTableColumns(nameCol, emailCol, progCol, certCol);
        rosterTable.getColumns().addAll(nameCol, emailCol, progCol, certCol);

        section.getChildren().addAll(title, rosterTable);
        return section;
    }

    /** Text cells with dark background so Modena's default white table cells do not show through. */
    private TableCell<Trainee, String> darkTextCell() {
        return new TableCell<Trainee, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                styleDarkTableCell(this);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTextFill(Color.web(TEXT_PRIMARY));
                }
            }
        };
    }

    private void styleDarkTableCell(TableCell<?, ?> cell) {
        cell.setStyle(
            "-fx-background-color: " + CARD_BG + ";" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-table-cell-border-color: " + BORDER_COLOR + ";"
        );
    }

    /**
     * Thin progress indicator that matches the dashboard palette.
     * Avoids {@link ProgressBar}, whose default skin draws a large light track.
     */
    private HBox buildDarkProgressBar(int percent) {
        int p = Math.max(0, Math.min(100, percent));
        double fillWidth = 88.0 * (p / 100.0);

        StackPane track = new StackPane();
        track.setPrefSize(88, 8);
        track.setMaxSize(88, 8);
        track.setStyle("-fx-background-color: " + BORDER_COLOR + "; -fx-background-radius: 4;");

        Region fill = new Region();
        fill.setPrefSize(Math.max(2, fillWidth), 6);
        fill.setMaxHeight(6);
        fill.setStyle("-fx-background-color: " + ACCENT_TEAL + "; -fx-background-radius: 3;");
        track.getChildren().add(fill);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        StackPane.setMargin(fill, new Insets(1, 0, 1, 4));

        Label pct = new Label(p + "%");
        pct.setMinWidth(38);
        pct.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");

        HBox row = new HBox(8, track, pct);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ── Apply dark theme to all columns ──────────────────────────────────────

    @SafeVarargs
    private void styleTableColumns(TableColumn<Trainee, ?>... cols) {
        for (TableColumn<Trainee, ?> col : cols) {
            col.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                "-fx-text-fill: " + TEXT_PRIMARY + ";"
            );
            col.setSortable(false);
        }
    }

    // ── Enroll handler ────────────────────────────────────────────────────────

    private void handleEnroll(String name, String email,
                               TextField nameField, TextField emailField) {
        if (name.isEmpty() || email.isEmpty()) {
            showStatus("Please fill in both name and email.", ERROR_RED);
            return;
        }
        if (!email.contains("@")) {
            showStatus("Please enter a valid email address.", ERROR_RED);
            return;
        }

        String traineeId = "TR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        // Temporary default password for prototype; replace with secure onboarding.
        Trainee newTrainee = new Trainee(
            traineeId,
            name,
            email,
            "temp123",
            trainer.getUserId()
        );

        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                userDAO.saveTrainee(newTrainee);
                return null;
            }
        };

        saveTask.setOnSucceeded(e -> {
            trainer.enrollTrainee(newTrainee);
            rosterData.add(newTrainee);
            nameField.clear();
            emailField.clear();
            showStatus("✓  " + name + " enrolled successfully.", SUCCESS_GREEN);
        });

        saveTask.setOnFailed(e -> {
            Throwable ex = saveTask.getException();
            showStatus("DB error: " + ex.getMessage(), ERROR_RED);
        });

        new Thread(saveTask).start();
    }

    private void showStatus(String msg, String color) {
        statusLabel.setText(msg);
        statusLabel.setTextFill(Color.web(color));
        statusLabel.setVisible(true);
    }

    // ── Load roster from DB on background thread ──────────────────────────────

    private void loadRoster() {
        Task<List<Trainee>> loadTask = new Task<List<Trainee>>() {
            @Override
            protected List<Trainee> call() throws Exception {
                // Filter to trainees belonging to this trainer
                return userDAO.findAll().stream()
                    .filter(u -> u instanceof Trainee)
                    .map(u -> (Trainee) u)
                    .filter(t -> trainer.getUserId().equals(t.getTrainerId()))
                    .collect(Collectors.toList());
            }
        };

        loadTask.setOnSucceeded(e -> rosterData.setAll(loadTask.getValue()));
        loadTask.setOnFailed(e -> showStatus(
            "Could not load roster: " + loadTask.getException().getMessage(), ERROR_RED));

        new Thread(loadTask).start();
    }

    private void handleLogout() {
        LoginApp.showLoginWindow();
        stage.close();
    }
}
