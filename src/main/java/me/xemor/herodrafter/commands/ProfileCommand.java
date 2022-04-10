package me.xemor.herodrafter.commands;

import me.xemor.herodrafter.DataManager;
import me.xemor.herodrafter.Hero;
import me.xemor.herodrafter.Player;
import me.xemor.herodrafter.HeroDrafter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProfileCommand implements Command {

    @Override
    public void executeCommand(SlashCommandEvent e) {
        switch (e.getSubcommandName()) {
            case "view" -> viewProfile(e);
            case "adminview" -> viewAdminProfile(e);
            case "preferences" -> changePreferences(e);
            case "leaderboard" -> viewLeaderboard(e, Player.Rating::getPublicRating);
            case "adminleaderboard" -> executeIfPrivileged(e, (SlashCommandEvent event) -> viewLeaderboard(event, Player.Rating::getMean));
            case "confidenceleaderboard" -> executeIfPrivileged(e, (SlashCommandEvent event) -> viewLeaderboard(event, Player.Rating::getStandardDeviation));
            case "create" -> createProfile(e);
            case "add" -> addHero(e);
            case "force-add" -> executeIfPrivileged(e, this::forciblyAddHero);
            case "remove" -> removeHero(e);
            case "force-remove" -> executeIfPrivileged(e, this::forciblyRemoveHero);
            case "abandoned" -> executeIfPrivileged(e, this::abandoned);
        }
    }

    public void viewAdminProfile(SlashCommandEvent e) {
        OptionMapping option = e.getOption("user");
        User user = option == null ? e.getUser() : option.getAsUser();
        InteractionHook hook = e.getHook();
        DataManager dataManager = HeroDrafter.getDataManager();
        Optional<Player> optionalPlayer = dataManager.getPlayer(user.getIdLong());
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(user.getName() + "'s profile");
            embedBuilder.setColor(dataManager.getConfig().getColor());
            embedBuilder.setDescription("Player Statistics");
            embedBuilder.addField("Mean Rating", String.valueOf(Math.round(player.getElo())), true);
            embedBuilder.addField("Rating Standard Deviation", String.valueOf(Math.round(player.getStandardDeviation())), true);
            List<String> heroes = player.getHeroes();
            heroes.sort(String::compareTo);
            String heroList = heroes.stream().reduce((string1, string2) -> string1 + "\n" + string2).get();
            embedBuilder.addField("Heroes", heroList, true);
            embedBuilder.addField("Preferences", player.getPreferences().stream().reduce((string1, string2) -> string1 + "\n" + string2).get(), true);
            hook.sendMessageEmbeds(embedBuilder.build()).queue();
        } else {
            hook.sendMessage(dataManager.getConfig().getNeedProfileMessage().replace("%user_name%", user.getName())).queue();
        }
    }

    public void viewProfile(SlashCommandEvent e) {
        OptionMapping option = e.getOption("user");
        User user = option == null ? e.getUser() : option.getAsUser();
        InteractionHook hook = e.getHook();
        DataManager dataManager = HeroDrafter.getDataManager();
        Optional<Player> optionalPlayer = dataManager.getPlayer(user.getIdLong());
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(user.getName() + "'s profile");
            embedBuilder.setColor(dataManager.getConfig().getColor());
            embedBuilder.setDescription("Player Statistics");
            embedBuilder.addField("Overall Elo", String.valueOf(Math.round(player.getRating().getPublicRating())), true);
            List<String> heroes = player.getHeroes();
            heroes.sort(String::compareTo);
            String heroList = heroes.stream().reduce((string1, string2) -> string1 + "\n" + string2).get();
            embedBuilder.addField("Heroes", heroList, true);
            embedBuilder.addField("Preferences", player.getPreferences().stream().reduce((string1, string2) -> string1 + "\n" + string2).get(), true);
            hook.sendMessageEmbeds(embedBuilder.build()).queue();
        } else {
            hook.sendMessage(dataManager.getConfig().getNeedProfileMessage().replace("%user_name%", user.getName())).queue();
        }
    }

    public void viewLeaderboard(SlashCommandEvent e, Function<Player.Rating, Double> elo) {
        InteractionHook hook = e.getHook();
        DataManager dataManager = HeroDrafter.getDataManager();
        List<Player> players = new ArrayList<>(dataManager.getPlayers());
        if (players.size() == 0) {
            hook.sendMessage(HeroDrafter.getDataManager().getConfig().getNeedProfileMessage()).queue();
            return;
        }
        players.sort(Collections.reverseOrder(Comparator.comparingDouble((Player one) -> elo.apply(one.getRating()))));
        List<RestAction<User>> retriever = new ArrayList<>();
        for (Player player : players) {
            if (player.getId() == -1) continue;
            retriever.add(e.getJDA().retrieveUserById(player.getId()));
        }
        RestAction.allOf(retriever).queue((List<User> users) -> {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(dataManager.getConfig().getColor());
            embedBuilder.setTitle("Leaderboard");
            embedBuilder.setDescription("Find out who the ~~luckiest~~ best player is!");
            StringBuilder eloList = new StringBuilder();
            StringBuilder playerList = new StringBuilder();
            for (Player player : players) {
                Optional<User> leaderboardUser = users.stream().filter((user -> user.getIdLong() == player.getId())).findAny();
                if (leaderboardUser.isEmpty()) continue;
                eloList.append(Math.round(elo.apply(player.getRating()))).append("\n");
                playerList.append(leaderboardUser.get().getName()).append("\n");
            }
            embedBuilder.addField("Ranking", playerList.toString(), true);
            embedBuilder.addField("Elo", eloList.toString(), true);
            hook.sendMessageEmbeds(embedBuilder.build()).queue();
        });
    }

    public void changePreferences(SlashCommandEvent e) {
        User user = e.getUser();
        InteractionHook hook = e.getHook();
        String preferences = e.getOption("role-preferences").getAsString();
        String[] roles = preferences.split(" ");
        DataManager dataManager = HeroDrafter.getDataManager();
        Optional<Player> optionalPlayer = dataManager.getPlayer(user.getIdLong());
        boolean matches = dataManager.getRoles().containsAll(Arrays.asList(roles));
        if (matches && roles.length == dataManager.getRoles().size()) {
            if (optionalPlayer.isPresent()) {
                Player player = optionalPlayer.get();
                player.setPreference(new ArrayList<>(Arrays.asList(roles)));
                hook.sendMessage(dataManager.getConfig().getNewPreferencesMessage().replace("%user_name%", user.getName()).replace("%preferences%", preferences)).queue();
                dataManager.savePlayers();
            } else {
                hook.sendMessage(dataManager.getConfig().getNeedProfileMessage().replace("%user_name%", user.getName())).queue();
            }
        } else {
            hook.sendMessage(dataManager.getConfig().getInvalidPreferencesMessage()).queue();
        }
    }

    public void addHero(SlashCommandEvent e) {
        addHeroToUser(e.getUser(), e);
    }

    public void forciblyAddHero(SlashCommandEvent e) {
        addHeroToUser(e.getOption("user").getAsUser(), e);
    }

    public void addHeroToUser(User user, SlashCommandEvent e) {
        InteractionHook hook = e.getHook();
        Optional<Player> optionalPlayer = HeroDrafter.getDataManager().getPlayer(user.getIdLong());
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            String heroes = e.getOption("heroes").getAsString();
            String[] heroNames = heroes.split(" ");
            if (heroes.contains("All")) {
                player.getHeroes().clear();
                player.getHeroes().addAll(HeroDrafter.getDataManager().getHeroes().stream().map(Hero::getName).collect(Collectors.toList()));
                hook.sendMessage(HeroDrafter.getDataManager().getConfig().getSuccessfullyAddedAllHeroesMessage()).queue();
            }
            else {
                for (String heroName : heroNames) {
                    if (HeroDrafter.getDataManager().getHero(heroName).isPresent()) {
                        if (!player.getHeroes().contains(heroName)) {
                            player.getHeroes().add(heroName);
                        }
                        hook.sendMessage(HeroDrafter.getDataManager().getConfig().getSuccessfullyAddedHeroMessage().replace("%hero_name%", heroName)).queue();
                    }
                    else {
                        hook.sendMessage(HeroDrafter.getDataManager().getConfig().getInvalidHeroName().replace("%hero_name%", heroName)).queue();
                    }
                }
            }
            HeroDrafter.getDataManager().savePlayers();
        } else {
            hook.sendMessage(HeroDrafter.getDataManager().getConfig().getNeedProfileMessage().replace("%user_name%", user.getName())).queue();
        }
    }

    public void removeHero(SlashCommandEvent e) {
        removeHeroFromUser(e.getUser(), e);
    }

    public void forciblyRemoveHero(SlashCommandEvent e) {
        removeHeroFromUser(e.getOption("user").getAsUser(), e);
    }

    public void removeHeroFromUser(User user, SlashCommandEvent e) {
        InteractionHook hook = e.getHook();
        Optional<Player> optionalPlayer = HeroDrafter.getDataManager().getPlayer(user.getIdLong());
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            String heroes = e.getOption("heroes").getAsString();
            String[] heroNames = heroes.split(" ");
            for (String heroName : heroNames) {
                Optional<Hero> optionalHero = HeroDrafter.getDataManager().getHero(heroName);
                if (optionalHero.isPresent()) {
                    if (canRemoveHero(optionalHero.get(), player) && player.getHeroes().size() >= HeroDrafter.getDataManager().getConfig().getDefaultPlayer().getHeroes().size()) {
                        player.getHeroes().remove(heroName);
                        HeroDrafter.getDataManager().savePlayers();
                        hook.sendMessage(HeroDrafter.getDataManager().getConfig().getSuccessfullyRemovedHeroMessage().replace("%hero_name%", heroName)).queue();
                    }
                    else {
                        hook.sendMessage(HeroDrafter.getDataManager().getConfig().getMissingRolesMessage()).queue();
                    }
                }
                else {
                    hook.sendMessage(HeroDrafter.getDataManager().getConfig().getInvalidHeroName().replace("%hero_name%", heroName)).queue();
                }
            }
        } else {
            hook.sendMessage(HeroDrafter.getDataManager().getConfig().getNeedProfileMessage().replace("%user_name%", user.getName())).queue();
        }
    }

    public void abandoned(SlashCommandEvent e) {
        User user = e.getOption("user").getAsUser();
        Optional<Player> optionalPlayer = HeroDrafter.getDataManager().getPlayer(user.getIdLong());
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            Player.Rating rating = player.getRating();
            rating.setStandardDeviation(Math.min(500, rating.getStandardDeviation() + 25));
            player.setRating(rating);
            HeroDrafter.getDataManager().savePlayers();
            e.getHook().sendMessage(String.format("%s has been penalized! They have %.0f elo now!", user.getName(), rating.getPublicRating())).queue();
        }
    }

    private boolean canRemoveHero(Hero hero, Player player) {
        DataManager dataManager = HeroDrafter.getDataManager();
        Set<String> roleSet = player.getHeroes().stream().filter((heroName) -> !heroName.equals(hero.getName()))
                .map(dataManager::getHero)
                .map(Optional::get)
                .map(Hero::getRoles)
                .flatMap(List::stream).collect(Collectors.toSet());
        return roleSet.containsAll(hero.getRoles());
    }

    public void createProfile(SlashCommandEvent e) {
        User user = e.getUser();
        InteractionHook hook = e.getHook();
        DataManager dataManager = HeroDrafter.getDataManager();
        Optional<Player> optionalPlayer = dataManager.getPlayer(user.getIdLong());
        if (optionalPlayer.isEmpty()) {
            Player player = new Player(dataManager.getConfig().getDefaultPlayer());
            player.setId(user.getIdLong());
            dataManager.setPlayer(player);
            hook.sendMessage(dataManager.getConfig().getProfileCreated().replace("%user_name%", user.getName())).queue();
            dataManager.savePlayers();
        } else {
            hook.sendMessage(dataManager.getConfig().getAlreadyHasProfile().replace("%user_name%", user.getName())).queue();
        }
    }
}
