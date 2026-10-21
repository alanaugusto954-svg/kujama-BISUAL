package com.kujama09.visuals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Lee la canción que suena en Spotify (solo Windows). Cada pocos segundos, en un hilo aparte, consulta con
 * PowerShell el título de la ventana de la app de escritorio de Spotify, que tiene el formato
 * "Artista - Canción" mientras suena algo. Todo es local: no se conecta a internet ni usa tu cuenta.
 * Limitación: así no se puede saber el progreso ni la portada del álbum.
 */
public final class SpotifyReader {
    private static final String COMMAND =
            "[Console]::OutputEncoding=[System.Text.Encoding]::UTF8;"
                    + "Get-Process -Name Spotify -ErrorAction SilentlyContinue"
                    + " | Where-Object { $_.MainWindowTitle }"
                    + " | Select-Object -First 1 -ExpandProperty MainWindowTitle";

    private static volatile String title = "";
    private static volatile String artist = "";
    private static volatile boolean playing;
    private static boolean started;

    private SpotifyReader() {
    }

    public static synchronized void start() {
        if (started || !System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            return;
        }
        started = true;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "kujama09-spotify");
            t.setDaemon(true);
            return t;
        });
        executor.scheduleWithFixedDelay(SpotifyReader::poll, 2, 3, TimeUnit.SECONDS);
    }

    public static boolean isPlaying() {
        return playing;
    }

    public static String title() {
        return title;
    }

    public static String artist() {
        return artist;
    }

    private static void poll() {
        if (!VisualsConfig.get().music) {
            playing = false;
            return;
        }
        Process process = null;
        try {
            process = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", COMMAND)
                    .redirectErrorStream(true)
                    .start();
            String line;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                line = reader.readLine();
            }
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
            parse(line == null ? "" : line.trim());
        } catch (Exception e) {
            playing = false;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private static void parse(String windowTitle) {
        int sep = windowTitle.indexOf(" - ");
        if (windowTitle.isEmpty() || sep <= 0 || windowTitle.startsWith("Spotify")) {
            playing = false; // pausado, publicidad o Spotify cerrado
            return;
        }
        artist = windowTitle.substring(0, sep).trim();
        title = windowTitle.substring(sep + 3).trim();
        playing = !title.isEmpty();
    }
}
