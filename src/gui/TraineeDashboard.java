package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.concurrent.Task;

import model.Trainee;
import model.Skill;
import model.Skill.SkillLevel;
import service.PortfolioExporter;
import db.UserDAO;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * M4 — Trainee Dashboard
 * Shows enrolled skills, per-skill completion, overall progress bar,
 * certification status, and portfolio export.
 * Wires directly to Trainee model methods.
 */
public class TraineeDashboard {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final String DEEP_NAVY     = "#0D1B2A";
    private static final String PANEL_BG      = "#0F1E2E";
    private static final String CARD_BG       = "#152232";
    private static final String ACCENT_TEAL   = "#00C9A7";
    private static final String ACCENT_WARM   = "#F4A261";
    private static final String TEXT_PRIMARY   = "#E8EDF2";
    private static final String TEXT_MUTED     = "#7A8FA6";
    private static final String BORDER_COLOR   = "#1E3448";
    private static final String SUCCESS_GREEN  = "#3DDC84";
    private static final String ERROR_RED      = "#E05C5C";
    private static final String LEVEL_BEGINNER = "#5B9BD5";
    private static final String LEVEL_INTER    = ACCENT_WARM;
    private static final String LEVEL_ADV      = "#C084FC";

    private final Trainee trainee;
    private final Stage   stage = new Stage();
    private final UserDAO userDAO = new UserDAO();
    private final PortfolioExporter portfolioExporter = new PortfolioExporter();

    // UI refs
    private VBox    skillListBox;
    private ProgressBar overallBar;
    private Text        progressText;
    private Text        certBadge;
    private Label       statusLabel;

    // Data fetched from Trainee object populated via UserDAO
    private final List<Skill> activeSkills;

    // ─────────────────────────────────────────────────────────────────────────

    public TraineeDashboard(Trainee trainee) {
        this.trainee = trainee;
        this.activeSkills = trainee.getEnrolledSkills();
    }

    public void show() {
        stage.setTitle("SkillBridge - My Portfolio");
        stage.setMinWidth(860);
        stage.setMinHeight(620);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + DEEP_NAVY + ";");
        root.setTop(buildTopBar());
        root.setLeft(buildProfilePanel());
        root.setCenter(buildSkillsPanel());

        stage.setScene(new Scene(root, 1020, 660));
        stage.show();
    }

    // ── Top bar ───────────────────────────────────────────────────────────────

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

        Text sep = new Text("-");
        sep.setFill(Color.web(TEXT_MUTED));

        Text pageTitle = new Text("My Portfolio");
        pageTitle.setFont(Font.font("Verdana", 14));
        pageTitle.setFill(Color.web(TEXT_MUTED));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Certification badge — shown only if certified
        certBadge = new Text("CERTIFIED");
        certBadge.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        certBadge.setFill(Color.web(SUCCESS_GREEN));
        certBadge.setVisible(trainee.isCertified());

        Label nameBadge = new Label("User: " + trainee.getName());
        nameBadge.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        nameBadge.setTextFill(Color.web(TEXT_PRIMARY));
        nameBadge.setStyle(
            "-fx-background-color: " + DEEP_NAVY + ";" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-radius: 20;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 6 14;"
        );

        Button logoutBtn = new Button("Logout");
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

        bar.getChildren().addAll(logo, sep, pageTitle, spacer, logoutBtn, certBadge, nameBadge);
        return bar;
    }

    // ── Left profile panel ────────────────────────────────────────────────────

    private VBox buildProfilePanel() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(240);
        panel.setPadding(new Insets(28, 18, 28, 18));
        panel.setStyle("-fx-background-color: " + PANEL_BG + ";"
                     + "-fx-border-color: " + BORDER_COLOR + ";"
                     + "-fx-border-width: 0 1 0 0;");

        // Avatar placeholder
        StackPane avatar = new StackPane();
        avatar.setPrefSize(72, 72);
        avatar.setMaxWidth(72);
        avatar.setStyle(
            "-fx-background-color: " + ACCENT_TEAL + ";" +
            "-fx-background-radius: 36;"
        );
        Text initials = new Text(getInitials(trainee.getName()));
        initials.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
        initials.setFill(Color.web(DEEP_NAVY));
        avatar.getChildren().add(initials);

        Text nameText = new Text(trainee.getName());
        nameText.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        nameText.setFill(Color.web(TEXT_PRIMARY));
        nameText.setWrappingWidth(200);

        Text emailText = new Text(trainee.getEmail());
        emailText.setFont(Font.font("Verdana", 11));
        emailText.setFill(Color.web(TEXT_MUTED));
        emailText.setWrappingWidth(200);

        Text roleText = new Text("TRAINEE");
        roleText.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        roleText.setFill(Color.web(ACCENT_TEAL));

        Separator divider = new Separator();
        divider.setStyle("-fx-background-color: " + BORDER_COLOR + ";");

        // Progress section
        Text progLabel = new Text("OVERALL PROGRESS");
        progLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        progLabel.setFill(Color.web(TEXT_MUTED));

        overallBar = new ProgressBar(trainee.getCompletionPercent() / 100.0);
        overallBar.setPrefWidth(200);
        overallBar.setPrefHeight(10);
        overallBar.setStyle("-fx-accent: " + ACCENT_TEAL + ";");

        progressText = new Text(trainee.getCompletionPercent() + "% complete");
        progressText.setFont(Font.font("Verdana", 12));
        progressText.setFill(Color.web(ACCENT_TEAL));

        // Export portfolio button
        Button exportBtn = new Button("Download Portfolio");
        exportBtn.setMaxWidth(Double.MAX_VALUE);
        exportBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + ACCENT_WARM + ";" +
            "-fx-font-family: Verdana;" +
            "-fx-font-size: 12px;" +
            "-fx-border-color: " + ACCENT_WARM + ";" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 8 0;" +
            "-fx-cursor: hand;"
        );
        exportBtn.setOnAction(e -> handleExport());

        statusLabel = new Label();
        statusLabel.setFont(Font.font("Verdana", 11));
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(200);
        statusLabel.setVisible(false);

        panel.getChildren().addAll(
            avatar, nameText, emailText, roleText,
            divider,
            progLabel, overallBar, progressText,
            new Region(), // spacer
            exportBtn, statusLabel
        );
        return panel;
    }

    // ── Center skills panel ───────────────────────────────────────────────────

    private ScrollPane buildSkillsPanel() {
        skillListBox = new VBox(14);
        skillListBox.setPadding(new Insets(28, 32, 28, 32));

        Text heading = new Text("My Skills");
        heading.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        heading.setFill(Color.web(TEXT_PRIMARY));

        Text subHeading = new Text(
            "Mark skills complete as your trainer assesses you. " +
            "Reach 100% to earn your digital certificate."
        );
        subHeading.setFont(Font.font("Verdana", 13));
        subHeading.setFill(Color.web(TEXT_MUTED));
        subHeading.setWrappingWidth(580);
        subHeading.setLineSpacing(3);

        skillListBox.getChildren().addAll(heading, subHeading);
        
        if (activeSkills.isEmpty()) {
            Text emptyMsg = new Text("\nNo skills assigned yet. Please contact your trainer.");
            emptyMsg.setFont(Font.font("Verdana", 14));
            emptyMsg.setFill(Color.web(TEXT_MUTED));
            skillListBox.getChildren().add(emptyMsg);
        } else {
            // Render a card for each active skill
            for (Skill skill : activeSkills) {
                skillListBox.getChildren().add(buildSkillCard(skill));
            }
        }

        ScrollPane scroll = new ScrollPane(skillListBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + DEEP_NAVY + ";"
                      + "-fx-background: " + DEEP_NAVY + ";");
        return scroll;
    }

    // ── Individual skill card ─────────────────────────────────────────────────

    private HBox buildSkillCard(Skill skill) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(
            "-fx-background-color: " + CARD_BG + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + (skill.isCompleted() ? ACCENT_TEAL : BORDER_COLOR) + ";" +
            "-fx-border-radius: 8;"
        );

        // Completion checkbox
        CheckBox check = new CheckBox();
        check.setSelected(skill.isCompleted());
        check.setStyle("-fx-accent: " + ACCENT_TEAL + ";");
        check.setDisable(skill.isCompleted()); // can't un-complete

        // Skill info
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Text skillName = new Text(skill.getSkillName());
        skillName.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        skillName.setFill(Color.web(skill.isCompleted() ? TEXT_MUTED : TEXT_PRIMARY));
        if (skill.isCompleted()) {
            skillName.setStrikethrough(true);
        }

        Label levelBadge = buildLevelBadge(skill.getLevel());

        HBox meta = new HBox(8, levelBadge);
        meta.setAlignment(Pos.CENTER_LEFT);

        info.getChildren().addAll(skillName, meta);

        // Completion status text
        Text statusText = new Text(skill.isCompleted() ? "[DONE]" : "Pending");
        statusText.setFont(Font.font("Verdana", 12));
        statusText.setFill(Color.web(skill.isCompleted() ? SUCCESS_GREEN : TEXT_MUTED));

        // Handle checkbox toggle (Persistent via UserDAO)
        check.setOnAction(e -> {
            if (check.isSelected()) {
                handleSkillCompletion(skill, check, skillName, statusText, card);
            }
        });

        card.getChildren().addAll(check, info, statusText);
        return card;
    }

    // ── Level badge label ─────────────────────────────────────────────────────

    private Label buildLevelBadge(SkillLevel level) {
        String color;
        switch (level) {
            case BEGINNER:
                color = LEVEL_BEGINNER;
                break;
            case INTERMEDIATE:
                color = LEVEL_INTER;
                break;
            case ADVANCED:
                color = LEVEL_ADV;
                break;
            default:
                color = LEVEL_BEGINNER;
        }
        Label badge = new Label(level.name());
        badge.setFont(Font.font("Verdana", FontWeight.BOLD, 9));
        badge.setTextFill(Color.web(color));
        badge.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 2 8;"
        );
        return badge;
    }

    // ── Recalculate progress from skill list ──────────────────────────────────

    private void handleSkillCompletion(Skill skill, CheckBox check, Text skillName, Text statusText, HBox card) {
        Task<Void> task = new Task<Void>() {
            @Override protected Void call() throws Exception {
                userDAO.updateTraineeSkill(trainee.getUserId(), skill.getSkillId(), true, 100);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            skill.markCompleted();
            check.setDisable(true);
            skillName.setFill(Color.web(TEXT_MUTED));
            skillName.setStrikethrough(true);
            statusText.setText("[DONE]");
            statusText.setFill(Color.web(SUCCESS_GREEN));
            card.setStyle(card.getStyle().replace(BORDER_COLOR, ACCENT_TEAL));
            recalculateProgress();
            showStatus("[OK] Progress synced to database.", SUCCESS_GREEN);
        });
        task.setOnFailed(e -> {
            check.setSelected(false);
            showStatus("Sync failed: " + task.getException().getMessage(), ERROR_RED);
        });
        new Thread(task).start();
    }

    private void recalculateProgress() {
        if (activeSkills.isEmpty()) return;
        long completed = activeSkills.stream().filter(Skill::isCompleted).count();
        int  percent   = (int) Math.round((completed * 100.0) / activeSkills.size());
        trainee.updateProgress(percent);

        overallBar.setProgress(percent / 100.0);
        progressText.setText(percent + "% complete");

        if (trainee.isCertified()) {
            certBadge.setVisible(true);
            progressText.setFill(Color.web(SUCCESS_GREEN));
        }
    }

    // ── Export portfolio to text file (delegated to service layer) ────────────

    private void handleExport() {
        Task<String> exportTask = new Task<String>() {
            @Override
            protected String call() throws IOException {
                return portfolioExporter.exportToTextFile(trainee, activeSkills).toString();
            }
        };

        exportTask.setOnSucceeded(e -> showStatus(
            "✓  Exported: " + exportTask.getValue(), SUCCESS_GREEN));
        exportTask.setOnFailed(e -> showStatus(
            "Export failed: " + exportTask.getException().getMessage(), ERROR_RED));

        new Thread(exportTask).start();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private String getInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private void showStatus(String msg, String color) {
        statusLabel.setText(msg);
        statusLabel.setTextFill(Color.web(color));
        statusLabel.setVisible(true);
    }

    private void handleLogout() {
        LoginApp.showLoginWindow();
        stage.close();
    }
}
