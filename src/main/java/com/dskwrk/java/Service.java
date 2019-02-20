package com.dskwrk.java;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

public class Service {

    @Getter
    private static final Service instance = new Service();

    private SpotifyApi spotifyApi = null;

    public int ytIndex = 0;
    public String folderName;

    // Settings
    public String getClientId() {
        var pref = Preferences.userNodeForPackage(Service.class);
        return pref.get("clientId", "a713a42860b44f7faa4598cb2b173f0e");
    }

    public String getClientSecret() {
        var pref = Preferences.userNodeForPackage(Service.class);
        return pref.get("clientSecret", "f1ccd482dd944cecb8b061044937eedd");
    }

    public void setClient(String id, String secret) {
        var pref = Preferences.userNodeForPackage(Service.class);
        pref.put("clientId", id);
        pref.put("clientSecret", secret);
    }

    public String getDownloadPath() {
        var pref = Preferences.userNodeForPackage(Service.class);
        return pref.get("downloadPath", "~/Downloads/");
    }

    public void setDownloadPath(String val) {
        var pref = Preferences.userNodeForPackage(Service.class);
        pref.put("downloadPath", val);
    }


    public String getYoutubeDLPath() {
        var pref = Preferences.userNodeForPackage(Service.class);
        return pref.get("youtubeDLPath", "youtube-dl");
    }

    public void setYoutubeDLPath(String val) {
        var pref = Preferences.userNodeForPackage(Service.class);
        pref.put("youtubeDLPath", val);
    }

    private void clientCredentials() throws IOException, SpotifyWebApiException {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(getClientId())
                .setClientSecret(getClientSecret())
                .build();
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        final ClientCredentials clientCredentials = clientCredentialsRequest.execute();
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        System.out.println("Expires in: " + clientCredentials.getExpiresIn());
    }

    public void createM3U(List<Track> tracks) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(this.getDownloadPath() + folderName + File.separator + "playlist.m3u");
        out.println("#EXTM3U");
        for (Track t : tracks) {
            if (t.getStatus() == 1) {
                out.println("#EXTINF:" + t.getRunTime() + "," + t.getArtist() + " - " + t.getName());
                out.println(t.getFileName());
            }
        }
        out.close();
    }

    public void duration(Track track) throws IOException, UnsupportedAudioFileException {
        var file = new File(track.getFileName());
        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
        if (fileFormat instanceof TAudioFileFormat) {
            Map<?, ?> properties = fileFormat.properties();
            String key = "duration";
            Long microseconds = (Long) properties.get(key);
            int total = (int) (microseconds / 1000000);
            track.setRunTime(total);
        } else {
            throw new UnsupportedAudioFileException();
        }
    }


    public boolean downloadMP3(Track track) throws IOException, InterruptedException {
        if (track.getYtLinks() == null || track.getYtLinks().size() == 0 || track.getYtLinks().size() < ytIndex)
            return false;
        String ffmpegDest = "[ffmpeg] Destination:";
        Runtime rt = Runtime.getRuntime();
        String[] commands = {getYoutubeDLPath(), "-o", this.getDownloadPath() + folderName + File.separator + "%(title)s.%(ext)s", "--extract-audio", "--audio-format", "mp3", "https://www.youtube.com" + track.getYtLinks().get(this.ytIndex)};
        Process p = rt.exec(commands);
        BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
        p.waitFor();
        String line;
        String filename = null;
        while ((line = output.readLine()) != null) {
            if (line.contains(ffmpegDest)) {
                filename = line.replace(ffmpegDest, "").trim();
            }
        }
        track.setFileName(filename);
        return track.getFileName() != null;
    }

    private Track createTrack(String artist, String name, String extra) throws IOException {
        var track = new Track(artist, name);
        Document doc = Jsoup.connect("https://www.youtube.com/results?search_query=" + URLEncoder.encode(artist + " " + name + " " + extra, "UTF-8")).get();
        Elements links = doc.select("a[href].yt-uix-tile-link");
        for (var link : links) {
            if (link.attr("href").startsWith("/watch")) track.getYtLinks().add(link.attr("href"));
        }
        return track;
    }

    public List<Track> spotifyListFromPlaylistID(String id, String concat, TextField textField, ProgressBar progressBar) throws IOException, SpotifyWebApiException {
        String spotifyId = null;
        if (id.contains(":")) {
            spotifyId = id.split(":")[id.split(":").length - 1];
        } else {
            spotifyId = id;
        }
        progressBar.setProgress(0);
        if (spotifyApi == null) clientCredentials(); // FIXME: should be better
        var tracks = new ArrayList<Track>();
        var playlist = spotifyApi.getPlaylist(spotifyId).build().execute();
        textField.setText(playlist.getName());
        var counter = 0;
        for (var playlistTrack : playlist.getTracks().getItems()) {
            var track = createTrack(playlistTrack.getTrack().getArtists()[0].getName(), playlistTrack.getTrack().getName(), concat);
            tracks.add(track);
            counter++;
            var i = (1d / (double) playlist.getTracks().getItems().length);
            var j = i * counter;
            progressBar.setProgress(j);
//            System.out.println(counter + " of " + playlist.getTracks().getItems().length + ": " + track.getName() + " - " + track.getArtist() + " (" + track.getRunTime() + ") -> " + track.getYtLinks().get(0));
        }
        return tracks;
    }
}
