package au.com.addstar.triggerit;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Furnace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.common.collect.ImmutableMap;

public class BasicArgumentProvider implements ArgumentProvider
{
	public static final BasicArgumentProvider instance = new BasicArgumentProvider();
	
	@SuppressWarnings( "deprecation" )
	@Override
	public Map<String, Object> provide( Object value )
	{
		if(value instanceof Player)
		{
			Player player = (Player)value;
			Location location = player.getLocation();
			ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder(); 
			
			builder.put("x", location.getX())
				.put("y", location.getY())
				.put("z", location.getZ())
				.put("yaw", location.getYaw())
				.put("pitch", location.getPitch())
				.put("world", location.getWorld())
				.put("location", location)
				.put("name", player.getName())
				.put("health", player.getHealth())
				.put("gm", player.getGameMode().ordinal())
				.put("xp", player.getTotalExperience())
				.put("xpl", player.getLevel())
				.put("xpf", player.getExp());
				
			if(player.getScoreboard() != null)
			{
				Scoreboard board = player.getScoreboard();
				Team team = board.getPlayerTeam(player);
				if(team != null)
					builder.put("team", team.getName());
				
				for(Score score : board.getScores(player))
					builder.put("score_" + score.getObjective().getName(), score.getScore());
			}
			
			return builder.build();
		}
		else if(value instanceof Entity)
		{
			Entity entity = (Entity)value;
			Location location = entity.getLocation();
			ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder(); 
			
			builder.put("x", location.getX())
				.put("y", location.getY())
				.put("z", location.getZ())
				.put("yaw", location.getYaw())
				.put("pitch", location.getPitch())
				.put("world", location.getWorld())
				.put("location", location)
				.put("type", entity.getType())
				.put("id", entity.getEntityId());
				
			return builder.build();
		}
		else if(value instanceof Block)
		{
			Block block = (Block)value;
			Location location = block.getLocation();
			ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
			
			builder.put("x", location.getX())
			.put("y", location.getY())
			.put("z", location.getZ())
			.put("world", location.getWorld())
			.put("location", location)
			.put("type", block.getType())
			.put("data", block.getData())
			.put("biome", block.getBiome().name())
			.put("light", block.getLightLevel())
			.put("lightSky", block.getLightFromSky())
			.put("lightBlock", block.getLightFromBlocks());
			
			BlockState state = block.getState();
			
			if(state instanceof Sign)
			{
				Sign sign = (Sign)state;
				builder.put("line1", sign.getLine(0))
					.put("line2", sign.getLine(1))
					.put("line3", sign.getLine(2))
					.put("line4", sign.getLine(3));
			}
			else if(state instanceof Furnace)
			{
				Furnace furnace = (Furnace)state;
				builder.put("burn", furnace.getBurnTime())
					.put("cook", furnace.getCookTime());
			}
			else if(state instanceof BrewingStand)
			{
				BrewingStand stand = (BrewingStand)state;
				builder.put("brew", stand.getBrewingTime());
			}
			else if(state instanceof NoteBlock)
			{
				NoteBlock note = (NoteBlock)state;
				builder.put("note", note.getNote().getId());
			}
			else if(state instanceof Jukebox)
			{
				Jukebox jukebox = (Jukebox)state;
				builder.put("record", jukebox.getPlaying().name())
					.put("isplaying", jukebox.isPlaying());
			}
			else if(state instanceof Skull)
			{
				Skull skull = (Skull)state;
				builder.put("facing", skull.getRotation())
					.put("skull", skull.getSkullType())
					.put("owner", skull.getOwner());
			}
			else if(state instanceof CreatureSpawner)
			{
				CreatureSpawner spawner = (CreatureSpawner)state;
				builder.put("spawned", spawner.getSpawnedType())
					.put("delay", spawner.getDelay());
			}
			else if(state instanceof CommandBlock)
			{
				CommandBlock cmd = (CommandBlock)state;
				builder.put("command", cmd.getCommand())
					.put("name", cmd.getName());
				
			}
			
			if(state instanceof InventoryHolder)
			{
				InventoryHolder holder = (InventoryHolder)state;
				Inventory inv = holder.getInventory();
				
				boolean empty = true;
				for(int i = 0; i < inv.getSize(); ++i)
				{
					if(inv.getItem(i) != null)
					{
						empty = false;
						break;
					}
				}
				
				builder.put("isEmpty", empty)
					.put("size", inv.getSize())
					.put("name", inv.getTitle());
			}
			
			return builder.build();
		}
		else if(value instanceof World)
		{
			World world = (World)value;
			ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
			
			builder.put("name", world.getName())
				.put("time", world.getTime())
				.put("difficulty", world.getDifficulty());
		}
		return null;
	}

}
