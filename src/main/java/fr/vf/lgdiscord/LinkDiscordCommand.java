package fr.vf.lgdiscord;

import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.PaginationMapPreset;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class LinkDiscordCommand implements CommandExecutor
{
    private final LGDiscord main;
    private final LGInventoryHolder inventoryHolder;
    private final PaginationMapPreset<OfflinePlayer> playerPreset;
    private final PaginationMapPreset<Member> memberPreset;

    public LinkDiscordCommand(LGDiscord main)
    {
        this.main = main;

        inventoryHolder = new LGInventoryHolder(6, AQUA + "LinkDiscord");

        playerPreset = new PaginationMapPreset<OfflinePlayer>(inventoryHolder) {
            @Override
            protected Slot makeInfoButtonIcon() {
                return new Slot(ItemBuilder.make(Material.BOOK))
                {
                    @Override
                    protected ItemBuilder getItem(LGInventoryHolder h) {
                        return getDefaultItem()
                                .setDisplayName(GRAY + "Page " + GOLD + (getPageIndex() + 1))
                                .addLore(AQUA + BOLD + "Click Gauche " + GRAY + "pour sélectionner le joueur à link")
                                .addLore(AQUA + BOLD + "Click Droit " + GRAY + "pour supprimer le link du joueur");
                    }
                };
            }

            @Override
            protected ItemBuilder mapList(OfflinePlayer player) {
                ItemBuilder item = ItemBuilder.make(Material.PLAYER_HEAD)
                        .setCustomId(player.getUniqueId().toString())
                        .setDisplayName(RESET + player.getName())
                        .setSkull(player);

                Member member = main.getPlayerMap().getMember(player);
                if(member != null)
                    item.setLore(GRAY + "Discord: ",
                            WHITE + member.getEffectiveName());
                else
                    item.setType(Material.ZOMBIE_HEAD);

                return item;
            }

            @Override
            protected void itemAction(LGInventoryHolder holder, InventoryClickEvent event, OfflinePlayer player) {
                switch (event.getClick())
                {
                    case LEFT:
                        holder.getCache().set("player", player);
                        memberPreset.setObjectList(main.getChannel().getMembers());
                        holder.loadPreset("member");
                        break;
                    case RIGHT:
                        main.getPlayerMap().remove(player);
                        reset();
                }
            }

            @Override
            protected void preset() {}
        };

        memberPreset = new PaginationMapPreset<Member>(inventoryHolder) {
            @Override
            protected Slot makeInfoButtonIcon() {
                return new Slot(ItemBuilder.make(Material.BOOK))
                {
                    @Override
                    protected ItemBuilder getItem(LGInventoryHolder h) {
                        return getDefaultItem()
                                .setDisplayName(GRAY + "Page " + GOLD + (getPageIndex() + 1))
                                .setLore(AQUA + BOLD + "Click " + GRAY + "pour sélectionner le discord du joueur");
                    }
                };
            }

            @Override
            protected ItemBuilder mapList(Member member) {
                ItemBuilder item = ItemBuilder.make(Material.SKELETON_SKULL)
                        .setCustomId(member.getId())
                        .setDisplayName(RESET + member.getEffectiveName());

                OfflinePlayer player = main.getPlayerMap().get(member);
                if(player != null)
                    item.setType(Material.PLAYER_HEAD)
                            .setSkull(player);

                return item;
            }

            @Override
            protected void itemAction(LGInventoryHolder holder, InventoryClickEvent event, Member member) {
                OfflinePlayer player = holder.getCache().get("player");

                main.getPlayerMap().set(player, member.getUser());

                reset();
            }

            @Override
            protected void preset() {}
        };

        inventoryHolder.savePreset("player", playerPreset);
        inventoryHolder.savePreset("member", memberPreset);
    }

    private void reset()
    {
        inventoryHolder.getCache().remove("player");
        playerPreset.setObjectList(main.getServer().getOnlinePlayers().stream().map(player -> (OfflinePlayer) player).collect(Collectors.toCollection(ArrayList::new)));
        inventoryHolder.loadPreset("player");
    }
    private void reset(Player p)
    {
        reset();
        p.openInventory(inventoryHolder.getInventory());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Commande utilisable uniquement par un joueur");
            return true;
        }
        if(!sender.isOp()) {
            sender.sendMessage("Vous devez être Admin pour cette command");
            return true;
        }

        reset((Player) sender);

        return true;
    }
}
