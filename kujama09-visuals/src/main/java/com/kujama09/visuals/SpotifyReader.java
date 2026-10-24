package com.kujama09.visuals;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
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
 * La portada del álbum se pide aparte, solo cuando cambia la canción, al control de medios de Windows (el mismo
 * que muestra la portada en el panel de volumen). Sin progreso de la canción.
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
    private static volatile int coverVersion;
    private static volatile boolean coverOk;
    private static String lastKey = "";
    private static int coverRetries;
    private static boolean started;

    /** Archivo temporal donde se guarda la portada (PNG 64x64). */
    public static final Path COVER_FILE = Path.of(System.getProperty("java.io.tmpdir"), "kujama09_cover.png");

    private static final String COVER_SCRIPT = String.join("\n",
            "$ErrorActionPreference='Stop'",
            "Add-Type -AssemblyName System.Runtime.WindowsRuntime",
            "Add-Type -AssemblyName System.Drawing",
            "$null=[Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime]",
            "$null=[Windows.Storage.Streams.IRandomAccessStreamWithContentType,Windows.Storage.Streams,ContentType=WindowsRuntime]",
            "$asTask=([System.WindowsRuntimeSystemExtensions].GetMethods()|Where-Object{$_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1'})[0]",
            "function Await($op,$type){$t=$asTask.MakeGenericMethod($type).Invoke($null,@($op));$t.Wait(-1)|Out-Null;$t.Result}",
            "$mgr=Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])",
            "foreach($s in $mgr.GetSessions()){",
            "if($s.SourceAppUserModelId -like '*Spotify*'){",
            "$p=Await ($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])",
            "if($p.Thumbnail){",
            "$st=Await ($p.Thumbnail.OpenReadAsync()) ([Windows.Storage.Streams.IRandomAccessStreamWithContentType])",
            "$net=[System.IO.WindowsRuntimeStreamExtensions]::AsStreamForRead($st)",
            "$img=[System.Drawing.Image]::FromStream($net)",
            "$bmp=New-Object System.Drawing.Bitmap 64,64",
            "$g=[System.Drawing.Graphics]::FromImage($bmp)",
            "$g.InterpolationMode='HighQualityBicubic'",
            "$g.DrawImage($img,0,0,64,64)",
            "$bmp.Save($args[0],[System.Drawing.Imaging.ImageFormat]::Png)",
            "exit 0}}}",
            "exit 2");

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

    /** Sube cada vez que hay una portada nueva lista en {@link #COVER_FILE}. */
    public static int coverVersion() {
        return coverVersion;
    }

    public static boolean hasCover() {
        return coverOk;
    }

    private static void fetchCover() {
        coverOk = false;
        Process process = null;
        try {
            Files.deleteIfExists(COVER_FILE);
            Path script = Files.createTempFile("kujama09_cover", ".ps1");
            Files.writeString(script, COVER_SCRIPT, StandardCharsets.UTF_8);
            process = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy",
                    "Bypass", "-File", script.toString(), COVER_FILE.toString())
                    .redirectErrorStream(true)
                    .start();
            process.getInputStream().readAllBytes();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
            Files.deleteIfExists(script);
            if (Files.isRegularFile(COVER_FILE) && Files.size(COVER_FILE) > 0) {
                coverOk = true;
                coverVersion++;
            }
        } catch (Exception e) {
            coverOk = false;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
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
        if (playing && VisualsConfig.get().musicCover) {
            String key = artist + "|" + title;
            if (!key.equals(lastKey)) {
                lastKey = key;
                coverRetries = 1;
                coverOk = false;
                fetchCover();
            } else if (coverRetries > 0) {
                // Windows a veces tarda en actualizar la portada: un segundo intento en la siguiente consulta.
                coverRetries--;
                fetchCover();
            }
        }
    }
}
