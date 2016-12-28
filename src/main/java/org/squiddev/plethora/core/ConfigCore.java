package org.squiddev.plethora.core;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import org.squiddev.configgen.*;
import org.squiddev.plethora.core.ConfigCoreForgeLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

@Config(languagePrefix = "gui.config.plethora.")
public final class ConfigCore {
	public static Configuration configuration;

	public static ConfigCategory baseCosts;

	public static void init(File file) {
		ConfigCoreForgeLoader.init(file);
		configuration = ConfigCoreForgeLoader.getConfiguration();

		baseCosts = configuration.getCategory("basecosts");
	}

	public static void sync() {
		ConfigCoreForgeLoader.doSync();
	}

	/**
	 * Some methods have a particular cost: they
	 * consume a set amount of fuel from their owner.
	 * This level regenerates over time.
	 *
	 * *Note:* These values only apply to the default handler.
	 * Other mods may add custom handlers.
	 */
	public static class CostSystem {
		/**
		 * The fuel level all systems start at
		 */
		@DefaultDouble(100)
		@Range(min = 0)
		@RequiresRestart(mc = false, world = true)
		public static double initial;

		/**
		 * The amount of fuel regened each tick
		 */
		@DefaultDouble(10)
		@Range(min = 0)
		@RequiresRestart(mc = false, world = true)
		public static double regen;

		/**
		 * The maximum fuel level an item can have
		 */
		@DefaultDouble(100)
		@Range(min = 0)
		@RequiresRestart(mc = false, world = true)
		public static double limit;

		/**
		 * Allow costs to go into the negative.
		 * Methods will fail when there is negative energy.
		 * This allows you to use costs higher than the allocated
		 * buffer and so have a more traditional rate-limiting system.
		 */
		@DefaultBoolean(false)
		@RequiresRestart(mc = false, world = true)
		public static boolean allowNegative;
	}

	/**
	 * Blacklist various providers
	 */
	public static class Blacklist {
		/**
		 * List of provider classes, packages or methods which are blacklisted.
		 * This will blacklist all converters, methods and transfer and meta providers
		 * matching a pattern.
		 *
		 * This only applies to classes registered through annotations and does not blacklist
		 * method builders.
		 *
		 * Valid forms:
		 * - "foo.bar." - All classes in package (note trailing period).
		 * - "foo.bar.Provider" - This class, all its members and nested classes
		 * - "foo.bar.Provider#method" - A particular method with a name
		 */
		@RequiresRestart
		public static ArrayList<String> blacklistProviders;

		/**
		 * List of tile entity classes or packages which will not be wrapped
		 * as peripherals. For example use "net.minecraft." to disable wrapping
		 * any vanilla peripheral. This does not blacklist subclasses.
		 */
		@RequiresRestart
		public static ArrayList<String> blacklistTileEntities;

		/**
		 * List of mods to block.
		 * IMPORTANT: This does not block wrapping a mod's peripherals, just disables
		 * custom mod specific integration.
		 */
		@RequiresRestart
		public static HashSet<String> blacklistMods;

		/**
		 * List of modules to blacklist.
		 */
		@RequiresRestart
		public static HashSet<String> blacklistModules;
	}

	/**
	 * Various options for debugging and testing this mod
	 */
	public static class Testing {
		/**
		 * Enable strict loading mode: crash when an error is encountered
		 * when injecting methods
		 */
		@RequiresRestart
		@DefaultBoolean(false)
		public static boolean strict;

		/**
		 * Show debug messages
		 */
		@DefaultBoolean(false)
		public static boolean debug;

		/**
		 * Verify generated bytecode for built methods.
		 * Only needed if you're developing new method builders.
		 */
		@DefaultBoolean(false)
		public static boolean bytecodeVerify;

		/**
		 * Issue an an error if a method isn't documented.
		 * If strict is turned on this will throw an exception.
		 */
		@DefaultBoolean(false)
		public static boolean likeDocs;
	}

	/**
	 * The base costs for all methods.
	 */
	public static class BaseCosts {
	}
}
