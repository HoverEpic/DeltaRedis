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
package com.gmail.tracebachi.DeltaRedis.Spigot;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.regex.Pattern;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class DeltaRedisMessageEvent extends Event
{
    public static final Pattern DELTA_PATTERN = Pattern.compile("/\\\\");
    public static final Pattern NEWLINE_PATTERN = Pattern.compile("\\\\n");

    private static final HandlerList handlers = new HandlerList();

    private final String sendingServer;
    private final String channel;
    private final String message;

    public DeltaRedisMessageEvent(String sendingServer, String channel, String message)
    {
        this.sendingServer = sendingServer;
        this.channel = channel;
        this.message = message;
    }

    /**
     * @return Name of the server that sent the message.
     */
    public String getSendingServer()
    {
        return sendingServer;
    }

    /**
     * @return Name of the channel that the message is targeted at.
     */
    public String getChannel()
    {
        return channel;
    }

    /**
     * @return The message/data received.
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @return Comma separated string of the sendingServer, channel, and message.
     */
    @Override
    public String toString()
    {
        return "{" + sendingServer + " , " + channel + " , " + message + "}";
    }

    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
