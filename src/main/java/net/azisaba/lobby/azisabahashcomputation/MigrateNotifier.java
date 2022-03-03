package net.azisaba.lobby.azisabahashcomputation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class MigrateNotifier {
    private static URL url;
    private static final Plugin plugin;

    static {
        plugin = Bukkit.getPluginManager().getPlugin("AzisabaHashComputation");
        assert plugin != null;
        File file = new File(plugin.getDataFolder(), "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        try {
            url = new URL(Objects.requireNonNull(config.getString("hookurl")));
        } catch (MalformedURLException e) {
            url = null;
            e.printStackTrace();
        }
    }

    public static void postWebHook(String id1, String id2) {
        JsonObject msg = new JsonObject();
        msg.add("username", new JsonPrimitive("MigrateNotifier"));
        JsonArray embs = new JsonArray();
        JsonObject emb = new JsonObject();
        emb.add("title", new JsonPrimitive("照合が完了しました！"));
        JsonArray fields = new JsonArray();
        JsonObject f1 = new JsonObject();
        f1.add("name", new JsonPrimitive("旧アカウント"));
        f1.add("value", new JsonPrimitive(id1));
        f1.add("inline", new JsonPrimitive(true));
        JsonObject f2 = new JsonObject();
        f2.add("name", new JsonPrimitive("新アカウント"));
        f2.add("value", new JsonPrimitive(id2));
        f1.add("inline", new JsonPrimitive(true));
        fields.add(f1);
        fields.add(f2);
        emb.add("fields", fields);
        embs.add(emb);
        msg.add("embeds", embs);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("User-Agent", "AHC/1.0");
                    con.addRequestProperty("Content-Type", "application/JSON; charset=utf-8");
                    con.setRequestProperty("Content-Length", String.valueOf(msg.toString().length()));
                    con.setDoOutput(true);
                    OutputStream ost = con.getOutputStream();
                    ost.write(msg.toString().getBytes(StandardCharsets.UTF_8));
                    ost.flush();
                    ost.close();
                    con.disconnect();
                    con.getResponseCode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
