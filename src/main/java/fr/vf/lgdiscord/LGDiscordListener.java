package fr.vf.lgdiscord;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.events.LGGameEndEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LGDiscordListener implements Listener, EventListener
{
    private final LGDiscord main;

    public LGDiscordListener(LGDiscord main)
    {
        this.main = main;
    }

    private final Map<LGGame, List<Member>> gameMueted = new HashMap<>();

    @EventHandler
    public void onKill(LGPlayerKilledEvent event)
    {
        main.getServer().getScheduler().runTaskAsynchronously(main, () -> {
            Member member = main.getPlayerMap().getMember(event.getKilled().getPlayer());
            if(member != null) {
                gameMueted
                        .computeIfAbsent(event.getGame(), game -> new ArrayList<>())
                        .add(member);
                main.getDiscord().mute(member, true).queue();
            }
        });
    }

    @EventHandler
    public void onGameEnd(LGGameEndEvent event)
    {
        main.getServer().getScheduler().runTaskAsynchronously(main, () -> {
            new Looper(gameMueted.get(event.getGame())).run();
            gameMueted.remove(event.getGame());
        });
    }

    private class Looper
    {
        Iterator<Member> players;

        public Looper(List<Member> players)
        {
            this.players = players.iterator();
        }

        private void reLooper(Void unused)
        {
            run();
        }

        public void run()
        {
            if(!players.hasNext())
                return;

            Member player = players.next();

            main.getDiscord()
                    .mute(player, false)
                    .queue(this::reLooper);
        }
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent)
            main.getLogger().info("API is ready!");
    }
}
