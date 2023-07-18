package fr.vf.lgdiscord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class LGDiscord extends JavaPlugin
{
    private JDA jda;
    private PlayerMap playerMap;

    public JDA getJDA() {
        return jda;
    }

    public Guild getDiscord()
    {
        return Objects.requireNonNull(getJDA()
                    .getGuildById(getConfig().getLong("server")));
    }

    public VoiceChannel getChannel()
    {
        return getDiscord().getVoiceChannelById(getConfig().getLong("channel"));
    }

    public PlayerMap getPlayerMap() {
        return playerMap;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        LGDiscordListener listener = new LGDiscordListener(this);

        Objects.requireNonNull(getCommand("linkdiscord")).setExecutor(new LinkDiscordCommand(this));
        getServer().getPluginManager().registerEvents(listener, this);

        playerMap = new PlayerMap(this);

        jda = JDABuilder.createDefault(getConfig().getString("token"))
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.watching("Minecraft LG"))
                .setEnabledIntents(
                        GatewayIntent.SCHEDULED_EVENTS,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_VOICE_STATES
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(listener)
                .build();
    }

    @Override
    public void onDisable()
    {
        playerMap.save();
    }
}
