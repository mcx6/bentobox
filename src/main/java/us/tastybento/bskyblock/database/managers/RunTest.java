package us.tastybento.bskyblock.database.managers;

import java.util.UUID;

import org.bukkit.Location;

import us.tastybento.bskyblock.BSkyBlock;
import us.tastybento.bskyblock.database.BSBDatabase;
import us.tastybento.bskyblock.database.DatabaseConnecter;
import us.tastybento.bskyblock.database.flatfile.FlatFileDatabaseConnecter;
import us.tastybento.bskyblock.database.objects.Island;

public class RunTest {

    public RunTest(BSkyBlock plugin) {
        try {

            DatabaseConnecter connecter = new FlatFileDatabaseConnecter(plugin, null);

            /*
                    new DatabaseConnectionSettingsImpl(
                            "127.0.0.1", 3306, "exampleDatabase","user", "pass"));
             */
            Island test = new Island();
            test.setName("testname");
            test.setOwner(UUID.randomUUID());
            test.addMember(UUID.randomUUID());
            test.addToBanList(UUID.randomUUID());
            test.setCenter(new Location(plugin.getServer().getWorld("world"), 1, 2, 3, 1, 1));
            test.setLocked(true);
            /*
            HashMap<Integer, Location> homes = new HashMap<Integer, Location>();
            homes.put(1, new Location(plugin.getServer().getWorld("world"), 1, 2, 3, 1, 1));
            homes.put(2, new Location(plugin.getServer().getWorld("world"), 3, 3, 3, 3, 3));
            test.setHomeLocations(homes);
            List<ItemStack> items = new ArrayList<ItemStack>();
            items.add(new ItemStack(Material.ACTIVATOR_RAIL, 2));
            items.add(new ItemStack(Material.FEATHER,5));
            test.setInventory(items);
             */
            BSBDatabase database = BSBDatabase.getDatabase();
            AbstractDatabaseHandler<Island> handler = (AbstractDatabaseHandler<Island>) database.getHandler(plugin, Island.class);

            handler.saveObject(test);

            plugin.getLogger().info("DEBUG: ALL WRITTEN! Now reading...");

            test = handler.loadObject(test.getUniqueId());

            plugin.getLogger().info("DEBUG: name = " + test.getName());
            plugin.getLogger().info("DEBUG: owner = " + test.getOwner());
            /*
            homes = test.getHomeLocations();
            plugin.getLogger().info("DEBUG: homes = " + homes);
            items = test.getInventory();
            plugin.getLogger().info("DEBUG: items = " + items);
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



