package world.bentobox.bentobox.commands.island;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.events.island.IslandEvent.Reason;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.island.NewIsland;

public class IslandResetCommand extends CompositeCommand {

    private Map<UUID, Long> cooldown;

    public IslandResetCommand(CompositeCommand islandCommand) {
        super(islandCommand, "reset", "restart");
    }

    @Override
    public void setup() {
        cooldown = new HashMap<>();
        setPermission("island.create");
        setOnlyPlayer(true);
        setDescription("commands.island.reset.description");
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Check cooldown
        if (getSettings().getResetWait() > 0 && onRestartWaitTime(user) > 0 && !user.isOp()) {
            user.sendMessage("general.errors.you-must-wait", TextVariables.NUMBER, String.valueOf(onRestartWaitTime(user)));
            return false;
        }
        if (!getIslands().hasIsland(getWorld(), user.getUniqueId())) {
            user.sendMessage("general.errors.no-island");
            return false;
        }
        if (!getIslands().isOwner(getWorld(), user.getUniqueId())) {
            user.sendMessage("general.errors.not-leader");
            return false;
        }
        if (getIslands().inTeam(getWorld(), user.getUniqueId())) {
            user.sendMessage("commands.island.reset.must-remove-members");
            return false;
        }
        if (getIWM().getResetLimit(getWorld()) >= 0 ) {
            int resetsLeft = getIWM().getResetLimit(getWorld()) - getPlayers().getResets(getWorld(), user.getUniqueId());
            if (resetsLeft <= 0) {
                user.sendMessage("commands.island.reset.none-left");
                return false;
            } else {
                // Notify how many resets are left
                user.sendMessage("commands.island.reset.resets-left", TextVariables.NUMBER, String.valueOf(resetsLeft));
            }
        }
        // Request confirmation
        if (getSettings().isResetConfirmation()) {
            this.askConfirmation(user, () -> resetIsland(user));
            return true;
        } else {
            return resetIsland(user);
        }

    }

    private boolean resetIsland(User user) {
        // Reset the island
        Player player = user.getPlayer();
        player.setGameMode(GameMode.SPECTATOR);
        // Get the player's old island
        Island oldIsland = getIslands().getIsland(getWorld(), player.getUniqueId());
        // Remove them from this island (it still exists and will be deleted later)
        getIslands().removePlayer(getWorld(), player.getUniqueId());
        // Remove money inventory etc.
        if (getIWM().isOnLeaveResetEnderChest(getWorld())) {
            user.getPlayer().getEnderChest().clear();
        }
        if (getIWM().isOnLeaveResetInventory(getWorld())) {
            user.getPlayer().getInventory().clear();
        }
        if (getSettings().isUseEconomy() && getIWM().isOnLeaveResetMoney(getWorld())) {
            // TODO: needs Vault
        }
        // Add a reset
        getPlayers().addReset(getWorld(), user.getUniqueId());
        // Create new island and then delete the old one
        try {
            NewIsland.builder()
            .player(user)
            .reason(Reason.RESET)
            .oldIsland(oldIsland)
            .build();
        } catch (IOException e) {
            getPlugin().logError("Could not create island for player. " + e.getMessage());
            user.sendMessage("commands.island.create.unable-create-island");
            return false;
        }
        setCooldown(user);
        return true;
    }

    private int onRestartWaitTime(User user) {
        if (!cooldown.containsKey(user.getUniqueId())) {
            return 0;
        }
        return (int) (System.currentTimeMillis() - cooldown.get(user.getUniqueId()) / 1000);
    }

    private void setCooldown(User user) {
        cooldown.put(user.getUniqueId(), System.currentTimeMillis() + (getIWM().getResetLimit(getWorld()) * 1000L));
    }
}
