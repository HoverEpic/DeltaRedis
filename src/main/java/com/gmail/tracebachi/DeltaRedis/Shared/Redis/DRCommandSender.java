/*
 * This file is part of DeltaRedis.
 *
 * DeltaRedis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DeltaRedis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeltaRedis.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.DeltaRedis.Shared.Redis;

import com.gmail.tracebachi.DeltaRedis.Shared.Cache.CachedPlayer;
import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisInterface;
import com.gmail.tracebachi.DeltaRedis.Shared.Servers;
import com.gmail.tracebachi.DeltaRedis.Shared.Shutdownable;
import com.google.common.base.Preconditions;
import com.lambdaworks.redis.api.StatefulRedisConnection;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class DRCommandSender implements Shutdownable
{
    private final String serverName;
    private final String serverType;

    private StatefulRedisConnection<String, String> connection;
    private DeltaRedisInterface plugin;
    private boolean isBungeeCordOnline;
    private Set<String> cachedServers;
    private Set<String> cachedPlayers;

    public DRCommandSender(StatefulRedisConnection<String, String> connection, DeltaRedisInterface plugin, String serverType)
    {
        this.connection = connection;
        this.plugin = plugin;
        this.serverType = serverType;
        this.serverName = plugin.getServerName();
    }

    /**
     * Adds server to Redis, making it visible to other servers/
     */
    public synchronized void setup()
    {
        plugin.debug("DRCommandSender.setup()");
        connection.sync().sadd(serverType + ":servers", serverName);
    }

    @Override
    public synchronized void shutdown()
    {
        plugin.debug("DRCommandSender.shutdown()");

        connection.sync().srem(serverType + ":servers", serverName);
        connection.close();
        connection = null;
        plugin = null;
    }

    /**
     * @return An unmodifiable set of servers that are part of the
     * same BungeeCord. This method will retrieve the servers from Redis.
     */
    public synchronized Set<String> getServers()
    {
        plugin.debug("DRCommandSender.getServers()");

        Set<String> result = connection.sync().smembers(serverType + ":servers");

        isBungeeCordOnline = result.remove(Servers.BUNGEECORD);
        cachedServers = Collections.unmodifiableSet(result);

        return cachedServers;
    }

    /**
     * @return An unmodifiable set of servers that are part of the
     * same BungeeCord. This method will retrieve the servers from the last
     * call to {@link DRCommandSender#getServers()}.
     */
    public Set<String> getCachedServers()
    {
        return cachedServers;
    }

    /**
     * @return True if the BungeeCord instance was last known to be online.
     * False if it was not.
     */
    public boolean isBungeeCordOnline()
    {
        return isBungeeCordOnline;
    }

    /**
     * @return An unmodifiable set of players (names) that are part of the
     * same BungeeCord. This method will retrieve the players from Redis.
     */
    public synchronized Set<String> getPlayers()
    {
        plugin.debug("DRCommandSender.getPlayers()");

        Set<String> result = connection.sync().smembers(serverType + ":players");

        cachedPlayers = Collections.unmodifiableSet(result);

        return cachedPlayers;
    }

    /**
     * @return An unmodifiable set of players (names) that are part of the
     * same BungeeCord. This method will retrieve the players from the last
     * call to {@link DRCommandSender#getPlayers()}.
     */
    public Set<String> getCachedPlayers()
    {
        return cachedPlayers;
    }

    /**
     * Publishes a string message using Redis PubSub. The destination can
     * also be one of the special values {@link Servers#BUNGEECORD}
     * or {@link Servers#SPIGOT}.
     *
     * @param dest    Server name that message should go to.
     * @param channel Custom channel name for the message.
     * @param message Message to send.
     * @return The number of servers that received the message.
     */
    public synchronized long publish(String dest, String channel, String message)
    {
        plugin.debug("DRCommandSender.publish(" + dest + ", " + channel + ", " + message + ")");

        return connection.sync().publish(
            serverType + ':' + dest,
            serverName + "/\\" + channel + "/\\" + message);
    }

    /**
     * Returns a CachedPlayer from Redis.
     *
     * @param playerName Name of the player to find.
     * @return CachedPlayer if found and null if not.
     */
    public synchronized CachedPlayer getPlayer(String playerName)
    {
        Preconditions.checkNotNull(playerName, "PlayerName cannot be null.");

        plugin.debug("DRCommandSender.getPlayer(" + playerName + ")");

        Map<String, String> result = connection.sync().hgetall(
            serverType + ":players:" + playerName.toLowerCase());

        if(result == null) { return null; }

        String server = result.get("server");
        String ip = result.get("ip");

        if(server == null || ip == null) { return null; }

        return new CachedPlayer(server, ip);
    }
}
