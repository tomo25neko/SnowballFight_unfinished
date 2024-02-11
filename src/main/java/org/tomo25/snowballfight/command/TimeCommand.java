package org.tomo25.snowballfight.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tomo25.snowballfight.SnowballFightManager;

public class TimeCommand implements CommandExecutor {

    private final SnowballFightManager snowballFightManager;

    public TimeCommand(SnowballFightManager snowballFightManager) {
        this.snowballFightManager = snowballFightManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみが実行できます。");
            return true;
        }

        Player player = (Player) sender;

        // 引数の数が正しくない場合や数字でない場合はエラーメッセージを表示して終了
        if (args.length != 1 || !isNumeric(args[0])) {
            player.sendMessage(ChatColor.RED + "正しい使い方: /snowballfight time <数字>");
            return true;
        }

        // 数字を取得
        int newTime = Integer.parseInt(args[0]);

        // 新しい時間を設定
        snowballFightManager.setTime(newTime);

        player.sendMessage(ChatColor.GREEN + "ゲームの時間を " + newTime + " に設定しました。");

        return true;
    }

    // 文字列が数字であるかどうかを判定するメソッド
    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
