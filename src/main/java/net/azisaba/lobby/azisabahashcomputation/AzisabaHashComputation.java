package net.azisaba.lobby.azisabahashcomputation;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class AzisabaHashComputation extends JavaPlugin implements Listener {
    private String key;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.key = getConfig().getString("key");
        getCommand("createcode").setExecutor(this);
        getCommand("migration").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤー専用です．");
            return true;
        }

        if (command.getName().equalsIgnoreCase("createcode")) {
            String res = computeHash(sender.getName());
            if (res == null) {
                sender.sendMessage(ChatColor.RED + "発行時に不明なエラーが発生しました．\n" +
                        "このスクリーンショットをDiscord「5hak3 - しゃけ#5241」に送ってください．");
                return false;
            }
            sender.sendMessage(ChatColor.AQUA + sender.getName() + "専用の引き継ぎ用コードを発行します．このコードは絶対に他人と共有しないでください．");
            sender.sendMessage(ChatColor.RED + res);
            sender.sendMessage(ChatColor.AQUA + "ヒント: Minecraftのフォルダにある logs/latest.log を確認してみましょう．\n" +
                    "念の為，スクリーンショットも撮っておきましょう．");
            getLogger().info(sender.getName() + " created the hash code.");
            return true;
        }
        if (command.getName().equalsIgnoreCase("migration")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "MCIDと引き継ぎコードを入力してください．");
                return false;
            }
            String res = computeHash(args[0]);
            if (res == null) {
                sender.sendMessage(ChatColor.RED + "発行時に不明なエラーが発生しました．\n" +
                        "このスクリーンショットをDiscord「5hak3 - しゃけ#5241」に送ってください．");
                return false;
            }
            if (res.equals(args[1])) {
                sender.sendMessage(ChatColor.AQUA + "照合が完了しました！\n" +
                        "以下の文を含めたスクリーンショットを運営に提示して引き継ぎを行ってもらってください．");
                sender.sendMessage(ChatColor.RED + args[0] + " と " + sender.getName() + " は同一人物です．");
                getLogger().info(args[0] + " and " + sender.getName() + " are the same person.");
                MigrateNotifier.postWebHook(args[0], sender.getName());
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "照合に失敗しました．MCIDと引き継ぎコードを再確認してください．");
                return false;
            }
        }
        return false;
    }

    private String computeHash(String mcid) {
        if (mcid == null) {
            return null;
        }

        String org = key + "MCID:" + mcid;
        String ciphered = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(org.getBytes());
            byte[] ciphBytes = md.digest();
            StringBuilder sb = new StringBuilder(2 * ciphBytes.length);
            for (byte b: ciphBytes)
                sb.append(String.format("%02x", b&0xff) );
            ciphered = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return ciphered;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        TextComponent component = new TextComponent("アジ鯖公式Discordに参加する！");
        component.setColor(net.md_5.bungee.api.ChatColor.BLUE);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://discord.gg/azisaba"));
        component.setUnderlined(true);
        getServer().getScheduler().runTaskLater(this,
                () -> {
                    event.getPlayer().sendMessage(ChatColor.AQUA + "Mojangアカウントは3/10に使えなくなります！Microsoftアカウントにまだ移行していない人は移行してください！");
                    event.getPlayer().sendMessage(ChatColor.RED + "Microsoftアカウントに移行できない人は新しいアカウントを買う必要があります！詳しくは公式Discordを確認してください！");
                    event.getPlayer().sendMessage(component);
                },
                20*6);
    }
}
