package com.dskwrk.java;

import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GUIController {
    @FXML
    public TableView<Track> trackTable;
    @FXML
    public TextField playlistID;
    @FXML
    public Spinner<Integer> ytIndexSpinner;
    @FXML
    public TextField ytSearchConcat;
    @FXML
    public ProgressBar progressIndicator;
    @FXML
    public TextField downloadFolder;
    @FXML
    public CheckBox createM3UCheckBox;
    @FXML
    public TextField defaultDownloadFolder;
    @FXML
    public ProgressBar downloadIndicator;
    @FXML
    public Button startProcessButton;
    @FXML
    public CheckBox skipDownloadCheckbox;
    @FXML
    public TextField youtubeDLExe;
    @FXML
    public TextField spotifyClientId;
    @FXML
    public TextField spotifyClientSecret;
    @FXML
    public Tab settingsTab;
    @Setter
    private HostServices hostServices;

    @FXML
    public void initialize() {
        // Load settings
        youtubeDLExe.setText(Service.getInstance().getYoutubeDLPath());
        defaultDownloadFolder.setText(Service.getInstance().getDownloadPath());
        spotifyClientId.setText(Service.getInstance().getClientId());
        spotifyClientSecret.setText(Service.getInstance().getClientSecret());

        // Initialize table
        TableColumn<Track, String> titleCol = new TableColumn<>("Artist");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("artist"));

        TableColumn<Track, String> nameCol = new TableColumn<>("Track Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Track, String> linkCol = new TableColumn<>("Youtube Link");
        linkCol.setCellValueFactory(new PropertyValueFactory<>("link"));

        TableColumn<Track, String> fileCol = new TableColumn<>("Filename");
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        TableColumn<Track, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("runTime"));

        trackTable.getColumns().setAll(titleCol, nameCol, linkCol, fileCol, durationCol);

        ytIndexSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10));
    }

    @FXML
    public void visitSpotify(ActionEvent actionEvent) {
        hostServices.showDocument("https://developer.spotify.com/my-applications");
    }

    @FXML
    public void visitYTDL(ActionEvent actionEvent) {
        hostServices.showDocument("https://rg3.github.io/youtube-dl/");
    }

    @FXML
    public void visitFFMPEG(ActionEvent actionEvent) {
        hostServices.showDocument("https://ffmpeg.zeranoe.com/builds/");
    }

    @FXML
    public void browseFolder(ActionEvent actionEvent) {
    }

    private void process() {
        downloadIndicator.setProgress(0d);
        Service.getInstance().ytIndex = ytIndexSpinner.getValue();
        toggleUI(true);
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                try {
                    List<Track> tracks = Service.getInstance().spotifyListFromPlaylistID(playlistID.getText(), ytSearchConcat.getText(), downloadFolder, progressIndicator);
                    trackTable.setItems(FXCollections.observableList(tracks));
                    trackTable.refresh();
                    var failed = 0;
                    if (!skipDownloadCheckbox.isSelected()) {
                        Service.getInstance().folderName = downloadFolder.getText().replace(File.separator, "");
                        var counter = 0;
                        for (var track : tracks) {
                            if (Service.getInstance().downloadMP3(track)) {
                                track.setStatus(1);
                                Service.getInstance().duration(track);
                            } else {
                                track.setStatus(-1);
                                failed++;
                            }
                            counter++;
                            var i = (1d / (double) tracks.size());
                            var j = i * counter;
                            downloadIndicator.setProgress(j);
                            trackTable.refresh();
                        }
                        if (createM3UCheckBox.isSelected()) {
                            Service.getInstance().createM3U(tracks);
                        }
                    }
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Done");
                    alert.setHeaderText(tracks.size() + " track(s) found");
                    alert.setContentText(failed + " track(s) failed to download");
                    alert.showAndWait();
                } catch (IOException | SpotifyWebApiException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Occurred");
                    alert.setHeaderText("Check your clientId, playlistId or Internet connection.");
                    alert.setContentText("Details: " + e.getMessage());
                    alert.showAndWait();
                } finally {
                    return null;
                }
            }
        };
        task.setOnSucceeded(taskFinishEvent -> {
            toggleUI(false);
        });
        new Thread(task).start();
    }

    private void toggleUI(boolean s) {
        playlistID.setDisable(s);
        ytIndexSpinner.setDisable(s);
        ytSearchConcat.setDisable(s);
        progressIndicator.setVisible(s);
        downloadIndicator.setVisible(s);
        createM3UCheckBox.setDisable(s);
        downloadFolder.setDisable(s);
        startProcessButton.setDisable(s);
        skipDownloadCheckbox.setDisable(s);
        settingsTab.setDisable(s);
    }

    public void startProcess(ActionEvent actionEvent) {
        process();
    }

    public void browseYoutubeDL(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Youtube-dl Executable");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Exe Files", "*.exe"),
                    new FileChooser.ExtensionFilter("App Files", "*.app"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"));
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                Service.getInstance().setYoutubeDLPath(selectedFile.getAbsolutePath());
                youtubeDLExe.setText(Service.getInstance().getYoutubeDLPath());
            }
        });
    }

    @FXML
    public void saveSettings(ActionEvent actionEvent) {
        Service.getInstance().setClient(spotifyClientId.getText(), spotifyClientSecret.getText());
    }

    @FXML
    public void browseDefaultFolder(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File dir = chooser.showDialog(null);
            if (dir != null) {
                Service.getInstance().setDownloadPath(dir.getAbsolutePath() + File.separator);
                defaultDownloadFolder.setText(Service.getInstance().getDownloadPath());
            }
        });
    }
}
