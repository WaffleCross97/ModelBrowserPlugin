package com.debornmc.modelBrowserPlugin.command;

import com.debornmc.modelBrowserPlugin.ModelBrowserPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides tab completion for the Model Browser plugin commands.
 * <p>
 * This class implements Bukkit's TabCompleter interface to offer intelligent
 * command completion for the /modelbrowser command and its subcommands.
 * Completion suggestions are context-sensitive and consider player permissions.
 * </p>
 */
public class TabCompleter implements org.bukkit.command.TabCompleter {
    private final com.debornmc.modelBrowserPlugin.manager.ModelManager modelManager;

    /**
     * Constructs a new TabCompleter instance.
     * <p>
     * Initializes the completer by obtaining a reference to the ModelManager
     * for accessing model names and categories during completion.
     * </p>
     */
    public TabCompleter() {
        this.modelManager = ModelBrowserPlugin.getInstance().getModelManager();
    }

    /**
     * Provides tab completion suggestions for the /modelbrowser command.
     * <p>
     * This method generates completion suggestions based on:
     * - The current argument position
     * - The player's permissions
     * - Available model names
     * - Context of the subcommand being used
     * </p>
     *
     * @param sender the command sender requesting completion
     * @param command the command being completed
     * @param alias the alias used for the command
     * @param args the current command arguments
     * @return a list of completion suggestions, or null for default suggestions
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NonNull [] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList(
                    "help", "list", "search", "categories", "info"
            ));

            if (player.hasPermission("modelbrowser.admin")) {
                completions.add("reload");
            }

            if (player.hasPermission("modelbrowser.delete")) {
                completions.add("delete");
            }

            return filterCompletions(completions, args[0]);
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "list":
                    return Arrays.asList("1", "2", "3", "4", "5");

                case "search":
                    return List.of("<query>");

                case "info":
                case "delete":
                    return filterCompletions(modelManager.getAvailableModels(), args[1]);
            }
        }

        return new ArrayList<>();
    }

    /**
     * Filters completion suggestions based on user input.
     * <p>
     * This method performs case-insensitive filtering to show only
     * suggestions that start with the user's current input.
     * </p>
     *
     * @param completions the list of all possible completions
     * @param input the user's current input
     * @return a filtered list of completions matching the input
     */
    private List<String> filterCompletions(List<String> completions, String input) {
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}