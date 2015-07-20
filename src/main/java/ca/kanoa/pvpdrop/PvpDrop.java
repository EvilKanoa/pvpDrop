package ca.kanoa.pvpdrop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PvpDrop extends JavaPlugin implements Listener{

    public final HashMap<String, ItemStack[]> inv = new HashMap<>();
    FileConfiguration config;

    public void onEnable(){
        this.getLogger().info("pvpDrop has been enabled!");
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        config = this.getConfig();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(cmd.getName().equalsIgnoreCase("pvpreload")){
            if(sender.hasPermission("pvp.reload")){
                if(args.length >= 1){
                    sender.sendMessage("Too many arguments!");
                    return true;
                }
                else if(args.length == 0){
                    sender.sendMessage("Reloading config.yml file...");
                    this.reloadConfig();
                    config = this.getConfig();
                    sender.sendMessage("config.yml has been reloaded!");
                    return true;
                }
            }
            else{
                sender.sendMessage("You don't have permission!");
                return false;
            }
        }
        return false;
    }

    public void onDisable(){
        this.getLogger().info("pvpDrop has been disabled!");
        inv.clear();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event){
        if(inv.containsKey(event.getPlayer().getName())){
            if(config.getBoolean("enabled") && config.getBoolean("no_player_drops") && event.getPlayer().hasPermission("pvp.player drops")){
                ItemStack[] newInv = inv.get(event.getPlayer().getName());
                event.getPlayer().getInventory().setContents(newInv);
                event.getPlayer().sendMessage(config.getString("respawn_message"));
                inv.remove(event.getPlayer().getName());
            }
            else if(config.getBoolean("enabled") && config.getBoolean("no_gear_drops") && event.getPlayer().hasPermission("pvp.gear_drops")){
                ItemStack[] newInv = inv.get(event.getPlayer().getName());
                event.getPlayer().getInventory().setContents(newInv);
                event.getPlayer().sendMessage(config.getString("respawn_message_gear"));
                inv.remove(event.getPlayer().getName());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        if(config.getBoolean("enabled")){
            Player player = event.getEntity().getPlayer();

            //Player inventory drops
            if(config.getBoolean("no_player_drops")){
                if(player.hasPermission("pvp.player_drops")){
                    inv.put(player.getName(), player.getInventory().getContents());
                    player.getInventory().clear();
                    event.getDrops().clear();
                }
            }
            //END

            //Gear Drops
            else if(config.getBoolean("no_gear_drops")){
                if(player.hasPermission("pvp.gear_drops")){
                    List<ItemStack> inventory = new ArrayList<>();
                    inventory.add(player.getInventory().getHelmet());
                    inventory.add(player.getInventory().getChestplate());
                    inventory.add(player.getInventory().getLeggings());
                    inventory.add(player.getInventory().getBoots());
                    inventory.add(player.getItemInHand());

                    ItemStack[] newStack = inventory.toArray(new ItemStack[inventory.size()]);
                    inv.put(player.getName(), newStack);
                    event.getDrops().removeAll(inventory);
                }

            }
            //END

            if(event.getEntity().getKiller() != null){
                //Custom EXP
                int xp;
                xp = event.getDroppedExp();
                if(!config.getBoolean("default_exp_drops")){
                    xp = 0;
                }
                if(config.getBoolean("extra_exp_enabled")){
                    xp = xp + config.getInt("extra_exp");
                }
                //Apply EXP drops
                event.setDroppedExp(xp);
                //END

                //Custom Drops
                if(config.getBoolean("custom_drops_enabled")){
                    List<Integer> items = config.getIntegerList("custom_drops");
                    List<Integer> amounts = config.getIntegerList("custom_amounts");
                    ItemStack item;
                    for(int x = 0; x < items.size(); x++){
                        if(amounts.size() < x + 1){
                            item = new ItemStack(Material.getMaterial(items.get(x)), 1);
                            event.getEntity().getLocation().getWorld().dropItemNaturally(event.getEntity().getLocation(), item);
                        }
                        else{
                            item = new ItemStack(Material.getMaterial(items.get(x)), amounts.get(x));
                            event.getEntity().getLocation().getWorld().dropItemNaturally(event.getEntity().getLocation(), item);
                        }
                    }
                }
                //END
            }
        }
    }
}