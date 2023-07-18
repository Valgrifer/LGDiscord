package fr.vf.lgdiscord;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerMap
{
    private final LGDiscord main;
    private final File file;
    private final YamlConfiguration yaml;


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public PlayerMap(LGDiscord main)
    {
        this.main = main;

        file = new File(main.getDataFolder().getAbsolutePath() + File.separator + "playerMap.yml");

        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        yaml = YamlConfiguration.loadConfiguration(file);
    }

    public void save()
    {
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(OfflinePlayer player, User user)
    {
        yaml.set(player.getUniqueId().toString(), user.getIdLong());
    }

    public void remove(OfflinePlayer player)
    {
        yaml.set(player.getUniqueId().toString(), null);
    }

    public User getUser(OfflinePlayer player)
    {
        return main.getJDA().getUserById(yaml.getLong(player.getUniqueId().toString()));
    }

    public Member getMember(OfflinePlayer player)
    {
        return main.getDiscord().getMemberById(yaml.getLong(player.getUniqueId().toString()));
    }

    public OfflinePlayer get(Member member)
    {
        return get(member.getUser());
    }

    public OfflinePlayer get(User user)
    {
        String uuidKey = yaml.getKeys(false).stream().filter(uuid -> yaml.getLong(uuid) == user.getIdLong()).findFirst().orElse(null);

        if(uuidKey == null)
            return null;

        UUID uuid = UUID.fromString(uuidKey);

        return main.getServer().getPlayer(uuid);
    }
}
